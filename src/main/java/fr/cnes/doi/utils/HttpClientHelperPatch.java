/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.restlet.Client;
import org.restlet.engine.Engine;
import org.restlet.ext.httpclient.HttpClientHelper;
import org.restlet.ext.httpclient.internal.HttpIdleConnectionReaper;
import org.restlet.ext.httpclient.internal.IgnoreCookieSpecFactory;

/**
 * Patch of the HttpClientHelper.
 * HttpClientHelper makes a bridge between Restlet and HttpClient from Apache
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class HttpClientHelperPatch extends HttpClientHelper {
    
    /** the idle connection reaper. */
    private volatile HttpIdleConnectionReaper idleConnectionReaper;
    
    public HttpClientHelperPatch(Client client) {
        super(client);
        HttpClient clientHttp = this.getHttpClient();
        if(clientHttp == null) {
            clientHttp = new DefaultHttpClient();
        }
    }
    
    @Override
    protected void configure(HttpParams params) {
        ConnManagerParams.setMaxTotalConnections(params,
                getMaxTotalConnections());
        ConnManagerParams.setMaxConnectionsPerRoute(params,
                new ConnPerRouteBean(getMaxConnectionsPerHost()));

        // Configure other parameters
        
        // Comment the following parameter. If uncomment, the application will crash
        // when requesting a URL starting by http:// through a proxy
        //HttpClientParams.setAuthenticating(params, false);
        HttpClientParams.setRedirecting(params, isFollowRedirects());        
        HttpClientParams.setCookiePolicy(params, CookiePolicy.IGNORE_COOKIES);
        HttpConnectionParams.setTcpNoDelay(params, getTcpNoDelay());
        HttpConnectionParams.setConnectionTimeout(params,
                getSocketConnectTimeoutMs());
        HttpConnectionParams.setSoTimeout(params, getSocketTimeout());

        String httpProxyHost = getProxyHost();
        if (httpProxyHost != null) {
            HttpHost proxy = new HttpHost(httpProxyHost, getProxyPort());
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    } 
    
    @Override
    protected void configure(DefaultHttpClient httpClient) {
        if (getRetryHandler() != null) {
            try {
                HttpRequestRetryHandler retryHandler = (HttpRequestRetryHandler) Engine
                        .loadClass(getRetryHandler()).newInstance();
                ((DefaultHttpClient)this.getHttpClient()).setHttpRequestRetryHandler(retryHandler);
            } catch (Exception e) {
                getLogger()
                        .log(Level.WARNING,
                                "An error occurred during the instantiation of the retry handler.",
                                e);
            }
        }

        CookieSpecRegistry csr = new CookieSpecRegistry();
        csr.register(CookiePolicy.IGNORE_COOKIES, new IgnoreCookieSpecFactory());        
        ((DefaultHttpClient)this.getHttpClient()).setCookieSpecs(csr);
    }  
          
    public void setProxyAuthentication(final String host, final String port, final String login, final String pwd) {
        ((DefaultHttpClient)this.getHttpClient()).getCredentialsProvider().setCredentials(
                new AuthScope(host, Integer.valueOf(port)),
                new UsernamePasswordCredentials(login, pwd)
        );         
    }    
    
    @Override
    public void start() throws Exception {
        // Define configuration parameters
        HttpParams params = new BasicHttpParams();
        configure(params);

        // Set-up the scheme registry
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        configure(schemeRegistry);

        // Create the connection manager
        ClientConnectionManager connectionManager = createClientConnectionManager(
                params, schemeRegistry);

        // Create and configure the HTTP client
        configure((DefaultHttpClient) this.getHttpClient());

        if (this.idleConnectionReaper != null) {
            // If a previous reaper is present, stop it
            this.idleConnectionReaper.stop();
        }

        this.idleConnectionReaper = new HttpIdleConnectionReaper(getHttpClient(),
                getIdleCheckInterval(), getIdleTimeout());

        getLogger().info("Starting the Apache HTTP client");
    }    
    
    @Override
    public void stop() throws Exception {
        if (this.idleConnectionReaper != null) {
            this.idleConnectionReaper.stop();
        }
        if (getHttpClient() != null) {
            getHttpClient().getConnectionManager().closeExpiredConnections();
            getHttpClient().getConnectionManager().closeIdleConnections(
                    getStopIdleTimeout(), TimeUnit.MILLISECONDS);
            getHttpClient().getConnectionManager().shutdown();
            getLogger().info("Stopping the HTTP client");
        }
    }    
}
