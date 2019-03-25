/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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
 * HttpClient configuration.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class HttpClientHelper extends org.restlet.engine.connector.HttpClientHelper {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(HttpClientHelper.class.getName());

    /**
     * Http client.
     */
    private volatile HttpClient httpClient;

    /**
     * Constructor.
     *
     * @param client client
     */
    public HttpClientHelper(final Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
        this.httpClient = null;
    }

    /**
     * Returns the key store type.
     *
     * @return the key store type or null
     */
    public String getKeyStoreType() {
        return getHelpedParameters().getFirstValue(HttpClient.KEYSTORE_TYPE, null);
    }

    /**
     * Returns the key store path.
     *
     * @return the key store path or null
     */
    public String getKeyStorePath() {
        return getHelpedParameters().getFirstValue(HttpClient.KEYSTORE_PATH, null);
    }

    /**
     * Returns the key store password.
     *
     * @return the key store password or null
     */
    public String getKeyStorePwd() {
        return getHelpedParameters().getFirstValue(HttpClient.KEYSTORE_PWD, null);
    }

    /**
     * Returns the trust store type.
     *
     * @return the key store type or null
     */
    public String getTrustStoreType() {
        return getHelpedParameters().getFirstValue(HttpClient.TRUSTSTORE_TYPE, null);
    }

    /**
     * Returns the trust store path.
     *
     * @return the key store path or null
     */
    public String getTrustStorePath() {
        return getHelpedParameters().getFirstValue(HttpClient.TRUSTSTORE_PATH, null);
    }

    /**
     * Returns the trust store password.
     *
     * @return the key store password or null
     */
    public String getTrustStorePwd() {
        return getHelpedParameters().getFirstValue(HttpClient.TRUSTSTORE_PWD, null);
    }

    /**
     * Returns the maximum number of connections that will be created for any particular host.
     *
     * @return The maximum number of connections that will be created for any particular host.
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
     * Returns the time in ms beyond which idle connections are eligible for reaping. The default
     * value is 60000 ms.
     *
     * @return The time in millis beyond which idle connections are eligible for reaping.
     */
    public long getIdleTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                HttpClient.CONNECTION_TIME_TO_LIVE_MS, "60000"));
    }

    /**
     * Get Max retry.
     *
     * @return max retry
     */
    public int getRetry() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(HttpClient.MAX_RETRY, "3"));
    }

    /**
     * Returns true if the SSL is disabled otherwise false.
     *
     * @return true if the SSL is disabled otherwise false
     */
    public boolean isDisabledSSL() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(HttpClient.IS_DISABLED_SSL,
                "false"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientCall create(final Request request) {
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

    /**
     * Configures the http client.
     *
     * @param parameters parameters
     */
    private void configure(final Series<Parameter> parameters) {
        final Map<String, String> config = new HashMap<>();
        config.put(HttpClient.CONNECTION_MAX_PER_ROUTE, String.valueOf(this.
                getMaxConnectionsPerHost()));
        config.put(HttpClient.CONNECTION_MAX_TOTAL, String.valueOf(this.getMaxTotalConnections()));
        config.put(HttpClient.CONNECTION_TIME_TO_LIVE_MS, String.valueOf(this.getIdleTimeout()));
        config.put(HttpClient.MAX_RETRY, String.valueOf(this.getRetry()));
        config.computeIfAbsent(HttpClient.KEYSTORE_TYPE, v -> this.getKeyStoreType());
        config.computeIfAbsent(HttpClient.KEYSTORE_PATH, v -> this.getKeyStorePath());
        config.computeIfAbsent(HttpClient.KEYSTORE_PWD, v -> this.getKeyStorePwd());
        config.computeIfAbsent(HttpClient.TRUSTSTORE_TYPE, v -> this.getTrustStoreType());
        config.computeIfAbsent(HttpClient.TRUSTSTORE_PATH, v -> this.getTrustStorePath());
        config.computeIfAbsent(HttpClient.TRUSTSTORE_PWD, v -> this.getTrustStorePwd());

        LOG.info("Http Config : {}", config);

        final Type type = HttpClientFactory.Type.valueOf(ProxySettings.getInstance().getProxyType());
        ProxySettings.getInstance().configureProxy();
        LOG.info("Httpclient type : {}", type);
        this.httpClient = HttpClientFactory.create(type, this.isDisabledSSL(), config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void start() throws Exception {
        final Series<Parameter> parameters = getHelpedParameters();
        configure(parameters);
        LOG.info("Starting the internal HTTP client");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stop() throws Exception {
        if (this.httpClient != null) {
            this.getHttpClient().close();
            this.httpClient = null;
            LOG.info("Stopping the internal HTTP client");
        }
    }

}
