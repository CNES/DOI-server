/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.doi.utils;

import fr.cnes.doi.settings.ProxySettings;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.engine.Engine;
import org.restlet.ext.httpclient.HttpClientHelper;
import org.restlet.ext.httpclient.internal.IgnoreCookieSpecFactory;

/**
 * Patch of the HttpClientHelper for Apache Commons. HttpClientHelper makes a bridge between Restlet
 * and HttpClient from Apache
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class HttpClientHelperPatchAC extends HttpClientHelper {

    /**
     * Proxy login for proxy authentication (optional).
     */
    final String login;
    /**
     * Proxy password for proxy authentication (optional).
     */
    final String pwd;
    /**
     * Proxy host (required) when proxy is set.
     */
    final String proxyHost;
    /**
     * Proxy port (required) when proxy is set.
     */
    final String proxyPort;
    /**
     * Excluded hosts, which are not proxified. By default, localhost is set in the constructor 
     */
    final List<String> excludedHosts = new ArrayList<>();
    /**
     * True when the proxy needs authentication.
     */
    final boolean isAuthenticate;
    /**
     * True when the proxy is set.
     */
    final boolean isWithProxy;

    public HttpClientHelperPatchAC(Client client) {
        super(new Client(new Context(), new ArrayList<>()));
        this.login = ProxySettings.getInstance().getProxyUser();
        this.pwd = ProxySettings.getInstance().getProxyPassword();
        this.proxyHost = ProxySettings.getInstance().getProxyHost();
        this.proxyPort = ProxySettings.getInstance().getProxyPort();
        this.isAuthenticate = ProxySettings.getInstance().isAuthenticate();
        this.isWithProxy = ProxySettings.getInstance().isWithProxy();
        Collections.addAll(this.excludedHosts, ProxySettings.getInstance().getNonProxyHosts().split(
                "\\s*,\\s*"));
        this.excludedHosts.add("localhost");
        getLogger().setLevel(Level.OFF);
    }

    @Override
    public String getProxyHost() {
        return this.proxyHost;
    }

    @Override
    public int getProxyPort() {
        return Integer.parseInt(this.proxyPort);
    }
    
    /**
     * Excluded hosts.
     * @return excluded hosts
     */
    public List<String> getExcludedHosts() {
        return this.excludedHosts;
    }

    @Override
    protected void configure(HttpParams params) {
        ConnManagerParams.setMaxTotalConnections(params, getMaxTotalConnections());
        ConnManagerParams.setMaxConnectionsPerRoute(params,
                new ConnPerRouteBean(getMaxConnectionsPerHost()));

        // Configure other parameters
        // Comment the following parameter. If uncomment, the application will crash
        // when requesting a URL starting by http:// through a proxy
        //HttpClientParams.setAuthenticating(params, false);
        HttpClientParams.setRedirecting(params, isFollowRedirects());

        HttpClientParams.setCookiePolicy(params, CookiePolicy.IGNORE_COOKIES);
        HttpConnectionParams.setTcpNoDelay(params, getTcpNoDelay());
        HttpConnectionParams.setConnectionTimeout(params, getSocketConnectTimeoutMs());
        HttpConnectionParams.setSoTimeout(params, getSocketTimeout());

        if (isWithProxy) {
            // proxy is set, so we put the proxy host/port
            HttpHost proxy = new HttpHost(getProxyHost(), getProxyPort());
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    }

    /**
     * Configures route planner when the proxy is set.
     * We have two cases to consider:
     * <ul>
     * <li>URL for which the proxy must not be used</li>
     * <li>URL to proxify</li>
     * </ul>
     * @param httpClient http client
     */
    private void configureRouterPlanner(final DefaultHttpClient httpClient) {
        if (isWithProxy) {

            final HttpHost proxy = (HttpHost) httpClient.getParams().getParameter(
                    ConnRoutePNames.DEFAULT_PROXY);

            HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy) {

                @Override
                public HttpRoute determineRoute(HttpHost host, HttpRequest request,
                        HttpContext context) throws HttpException {
                    final HttpClientContext clientContext = HttpClientContext.adapt(context);
                    final RequestConfig config = clientContext.getRequestConfig();
                    final InetAddress local = config.getLocalAddress();
                    HttpHost proxy = config.getProxy();

                    final HttpHost target;
                    if (host.getPort() > 0
                            && (host.getSchemeName().equalsIgnoreCase("http") 
                            && host.getPort() == 80
                            || host.getSchemeName().equalsIgnoreCase("https") 
                            && host.getPort() == 443)) {
                        target = new HttpHost(host.getHostName(), -1, host.getSchemeName());
                    } else {
                        target = host;
                    }
                    final boolean secure = target.getSchemeName().equalsIgnoreCase("https");
                    if (getExcludedHosts().contains(host.getHostName())) {
                        return new HttpRoute(target, local, secure);
                    } else {
                        return new HttpRoute(target, local, proxy, secure);
                    }
                }
            };

            httpClient.setRoutePlanner(routePlanner);
        }
    }
    
    /**
     * Configures the retry handler.
     * @param httpClient http client
     */
    private void configureRetryHandler(final DefaultHttpClient httpClient) {
        if (getRetryHandler() != null) {
            try {
                HttpRequestRetryHandler retryHandler = (HttpRequestRetryHandler) Engine
                        .loadClass(getRetryHandler()).newInstance();
                httpClient.setHttpRequestRetryHandler(retryHandler);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                getLogger().log(Level.WARNING, "An error occurred during the instantiation of the "
                        + "retry handler.", e);
            }
        }        
    }
    
    /**
     * Configures authentication.
     * @param httpClient http client
     */
    private void configureAuthentication(final DefaultHttpClient httpClient) {
        if (this.isAuthenticate) {
            final CredentialsProvider credProviders = httpClient.getCredentialsProvider();
            credProviders.setCredentials(
                    new AuthScope(this.proxyHost, Integer.parseInt(this.proxyPort)),
                    new UsernamePasswordCredentials(this.login, this.pwd)
            );
        }        
    }

    @Override
    protected void configure(DefaultHttpClient httpClient) {
        configureRouterPlanner(httpClient);
        configureRetryHandler(httpClient);
        configureAuthentication(httpClient);
        CookieSpecRegistry csr = new CookieSpecRegistry();
        csr.register(CookiePolicy.IGNORE_COOKIES, new IgnoreCookieSpecFactory());        
        httpClient.setCookieSpecs(csr);
    }

    @Override
    public void start() throws Exception {
        super.start();
    }
}
