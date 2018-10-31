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
import java.io.IOException;
import java.net.CookieStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.util.ReferenceUtils;
import org.restlet.ext.jetty.HttpClientHelper;
import org.restlet.ext.jetty.internal.JettyClientCall;
import org.restlet.ext.jetty.internal.RestletSslContextFactory;

/**
 * Patch of the HttpClientHelper. HttpClientHelper makes a bridge between Restlet and HttpClient
 * from Jetty. It solves the problem when a proxy is used.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class HttpClientHelperPatch extends HttpClientHelper {

    /**
     * The wrapped Jetty HTTP client.
     */
    private volatile HttpClient httpClient;

    /**
     * Constructs the bridge by creating a client by default.
     */
    public HttpClientHelperPatch() {
        super(new Client(new Context(), new ArrayList<>()));
        if (this.httpClient == null) {
            this.httpClient = createHttpClient();
        }
        //getProtocols().add(Protocol.HTTP);
        //getProtocols().add(Protocol.HTTPS);
    }

    /**
     * Constructs the bridge by setting a Client.
     *
     * @param client
     */
    public HttpClientHelperPatch(final Client client) {
        super(client);
        if (this.httpClient == null) {
            this.httpClient = createHttpClient();
        }

    }

    /**
     * Creates a low-level HTTP client call from a high-level uniform call.
     *
     * @param request The high-level request.
     * @return A low-level HTTP client call.
     */
    @Override
    public ClientCall create(Request request) {
        ClientCall result = null;

        try {
            result = new JettyClientCall(this, request.getMethod().toString(),
                    ReferenceUtils.update(request.getResourceRef(), request)
                            .toString());
        } catch (IOException e) {
            getLogger().log(Level.WARNING,
                    "Unable to create the Jetty HTTP/HTTPS client call", e);
        }

        return result;
    }

    /**
     * Creates a Jetty HTTP client.
     *
     * @return A new HTTP client.
     */
    private HttpClient createHttpClient() {
        SslContextFactory sslContextFactory = null;

        try {
            sslContextFactory = new RestletSslContextFactory(
                    org.restlet.engine.ssl.SslUtils.getSslContextFactory(this));
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Unable to create the SSL context factory.", e);
        }
        final HttpClient httpClientJetty = new HttpClient(sslContextFactory);

        // configure Jetty options 
        configureOptions(httpClientJetty);

        final ProxySettings proxySettings = ProxySettings.getInstance();
        if (proxySettings.isWithProxy()) {
            final ProxyConfiguration proxyConfig = httpClientJetty.getProxyConfiguration();
            final HttpProxy proxy = new HttpProxy(proxySettings.getProxyHost(), Integer.parseInt(
                    proxySettings.getProxyPort()));

            // hosts not proxified
            addNoPxifiedHost(proxy, proxySettings.getNonProxyHosts());

            // proxy authentication if needed
            autenticateProxyIfNeeded(httpClientJetty, proxy, proxySettings.getProxyUser(),
                    proxySettings.getProxyPassword());

            // add the new proxy to the list of proxies already registered
            proxyConfig.getProxies().add(proxy);
        }

        return httpClientJetty;
    }

    /**
     * Basic options for Jetty.
     *
     * @param httpClientJetty Jetty client
     */
    private void configureOptions(final HttpClient httpClientJetty) {
        httpClientJetty.setAddressResolutionTimeout(getAddressResolutionTimeout());
        httpClientJetty.setBindAddress(getBindAddress());
        httpClientJetty.setConnectTimeout(getConnectTimeout());

        final CookieStore cookieStore = getCookieStore();

        if (cookieStore != null) {
            httpClientJetty.setCookieStore(cookieStore);
        }

        httpClientJetty.setDispatchIO(isDispatchIO());
        httpClientJetty.setExecutor(getExecutor());
        httpClientJetty.setFollowRedirects(isFollowRedirects());
        httpClientJetty.setIdleTimeout(getIdleTimeout());
        httpClientJetty.setMaxConnectionsPerDestination(getMaxConnectionsPerDestination());
        httpClientJetty.setMaxRedirects(getMaxRedirects());
        httpClientJetty.setMaxRequestsQueuedPerDestination(getMaxRequestsQueuedPerDestination());
        httpClientJetty.setRequestBufferSize(getRequestBufferSize());
        httpClientJetty.setResponseBufferSize(getResponseBufferSize());
        httpClientJetty.setScheduler(getScheduler());
        httpClientJetty.setStopTimeout(getStopTimeout());
        httpClientJetty.setStrictEventOrdering(isStrictEventOrdering());
        httpClientJetty.setTCPNoDelay(isTcpNoDelay());
        final String userAgentField = getUserAgentField();

        if (userAgentField != null) {
            httpClientJetty.setUserAgentField(new HttpField(HttpHeader.USER_AGENT, userAgentField));
        }
    }

    /**
     * Adds the non proxified host
     *
     * @param proxy proxy
     * @param noHosts non proxified hosts as comma sperated value
     */
    private void addNoPxifiedHost(final HttpProxy proxy, final String noHosts) {
        final List<String> nonProxies = new ArrayList<>();
        Collections.addAll(nonProxies, noHosts.split("\\s*,\\s*"));
        proxy.getExcludedAddresses().addAll(nonProxies);
    }

    /**
     * Checks if the proxy needs an authentication.
     *
     * @param login login
     * @param pwd password
     * @return True when the proxy needs an authentication otherwise False
     */
    private boolean hasAuthenticationProxy(final String login,
            final String pwd) {
        return !(login == null && pwd == null);
    }

    /**
     * Creates the proxy authentication
     *
     * @param httpClientJetty Jetty client
     * @param proxy proxy
     * @param login login
     * @param pwd password
     */
    private void autenticateProxyIfNeeded(final HttpClient httpClientJetty,
            final HttpProxy proxy,
            final String login,
            final String pwd) {
        if (hasAuthenticationProxy(login, pwd)) {
            AuthenticationStore auth = httpClientJetty.getAuthenticationStore();
            // Proxy credentials.
            auth.addAuthentication(
                    new BasicAuthentication(
                            proxy.getURI(), 
                            "ProxyRealm", 
                            login,
                            pwd
                    )
            );
        }
    }

    /**
     * Returns the wrapped Jetty HTTP client.
     *
     * @return The wrapped Jetty HTTP client.
     */
    @Override
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        final HttpClient httpClientJetty = getHttpClient();
        if (httpClientJetty != null) {
            getLogger().fine("Starting a patched Jetty HTTP/HTTPS client");
            httpClientJetty.start();
        }
    }

    /**
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        final HttpClient httpClientJetty = getHttpClient();
        if (httpClientJetty != null) {
            getLogger().fine("Stopping a patched Jetty HTTP/HTTPS client");
            httpClientJetty.stop();
        }

    }
}
