/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.util.logging.Level;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.restlet.Client;
import org.restlet.engine.Engine;
import org.restlet.ext.httpclient.HttpClientHelper;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class HttpClientHelperPatch extends HttpClientHelper {
    
    public HttpClientHelperPatch(Client client) {
        super(client);
    }
    
    protected void configure(HttpParams params) {
        ConnManagerParams.setMaxTotalConnections(params,
                getMaxTotalConnections());
        ConnManagerParams.setMaxConnectionsPerRoute(params,
                new ConnPerRouteBean(getMaxConnectionsPerHost()));

        // Configure other parameters
        //HttpClientParams.setAuthenticating(params, false);
        HttpClientParams.setRedirecting(params, isFollowRedirects());
        //HttpClientParams.setCookiePolicy(params, "ignore");
        HttpConnectionParams.setTcpNoDelay(params, getTcpNoDelay());
        HttpConnectionParams.setConnectionTimeout(params,
                getSocketConnectTimeoutMs());
        HttpConnectionParams.setSoTimeout(params, getSocketTimeout());

        String httpProxyHost = getProxyHost();
        if (httpProxyHost != null) {
            System.out.println("host="+httpProxyHost);
            System.out.println("prt="+getProxyPort());
            HttpHost proxy = new HttpHost(httpProxyHost, getProxyPort());
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    } 
    
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

        //CookieSpecRegistry csr = new CookieSpecRegistry();
        //csr.register("ignore", new IgnoreCookieSpecFactory());
        //this.httpClient.setCookieSpecs(csr);
    }    

    @Override
    public void start() throws Exception {
        super.start();
        ((DefaultHttpClient)this.getHttpClient()).getCredentialsProvider().setCredentials(
                new AuthScope(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST), Integer.valueOf(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT))),
                new UsernamePasswordCredentials(DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN), DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD))
        );        
    }
    
}
