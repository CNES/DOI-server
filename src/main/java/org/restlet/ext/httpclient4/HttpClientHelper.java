/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.restlet.ext.httpclient4;

import fr.cnes.doi.settings.ProxySettings;
import fr.cnes.httpclient.HttpClient;
import fr.cnes.httpclient.HttpClientFactory;
import fr.cnes.httpclient.HttpClientFactory.Type;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.util.ReferenceUtils;
import org.restlet.util.Series;

/**
 *
 * @author malapert
 */
public class HttpClientHelper extends org.restlet.engine.connector.HttpClientHelper {
    
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(HttpClientHelper.class.getName());    

    private volatile HttpClient httpClient;

    public HttpClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
        this.httpClient = null;
    }        
    
    
    /**
     * Returns the maximum number of connections that will be created for any
     * particular host.
     * 
     * @return The maximum number of connections that will be created for any
     *         particular host.
     */
    public int getMaxConnectionsPerHost() {        
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                HttpClient.CONNECTION_MAX_PER_ROUTE, "10"));
    }

    /**
     * Returns the maximum number of active connections.
     * 
     * @return The maximum number of active connections.
     */
    public int getMaxTotalConnections() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                HttpClient.CONNECTION_MAX_TOTAL, "20"));
    }    
    
    /**
     * Returns the time in ms beyond which idle connections are eligible for
     * reaping. 
     * The default value is 60000 ms.
     * 
     * @return The time in millis beyond which idle connections are eligible for
     *         reaping.
     */
    public long getIdleTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                HttpClient.CONNECTION_TIME_TO_LIVE_MS, "60000"));
    }
    
    /**
     * Get Max retry.
     * @return max retry
     */
    public int getRetry() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(HttpClient.MAX_RETRY, "3"));
    }
    
    
    public boolean isDisabledSSL() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(HttpClient.IS_DISABLED_SSL, "false"));
    }

    @Override
    public ClientCall create(Request request) {
        ClientCall result = null;

        try {
            result = new HttpMethodCall(this, request.getMethod().toString(),
                    ReferenceUtils.update(request.getResourceRef(), request)
                            .toString(), request.isEntityAvailable());
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING,
                    "Unable to create the HTTP client call", ioe);
        }

        return result;
    }

    /**
     * Returns the wrapped Apache HTTP Client.
     *
     * @return The wrapped Apache HTTP Client.
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
    }
    

    private void configure(final Series<Parameter> parameters) {
        final Map<String, String> config = new HashMap<>();
        config.put(HttpClient.CONNECTION_MAX_PER_ROUTE, String.valueOf(this.getMaxConnectionsPerHost()));
        config.put(HttpClient.CONNECTION_MAX_TOTAL, String.valueOf(this.getMaxTotalConnections()));
        config.put(HttpClient.CONNECTION_TIME_TO_LIVE_MS, String.valueOf(this.getIdleTimeout()));
        config.put(HttpClient.MAX_RETRY, String.valueOf(this.getRetry()));  
        LOG.info("Http Config : {}", config);        
        
        Type type = HttpClientFactory.Type.valueOf(ProxySettings.getInstance().getProxyType());
        ProxySettings.getInstance().configureProxy();
        LOG.info("Httpclient type : {}", type);
        this.httpClient = HttpClientFactory.create(type, this.isDisabledSSL(), config);
    }    

    @Override
    public synchronized void start() throws Exception {                
        final Series<Parameter> parameters =  getHelpedParameters();
        configure(parameters);
        super.start();
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        if (this.httpClient != null) {
            this.getHttpClient().close();
            this.httpClient = null;
        }
    }

}
