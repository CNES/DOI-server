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
package fr.cnes.doi.settings;

import fr.cnes.doi.utils.spec.Requirement;
import fr.cnes.httpclient.HttpClientFactory.Type;
import fr.cnes.httpclient.configuration.ProxyConfiguration;
import fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Sets the proxy parameter based on DoiSettings.
 *
 * @author Jean-Christophe Malapert
 */
@Requirement(reqId = Requirement.DOI_CONFIG_010, reqName = Requirement.DOI_CONFIG_010_NAME)
public final class ProxySettings {

    /**
     * Application logger.
     */
    private static final Logger LOG = LogManager.getLogger(ProxySettings.class.getName());

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static ProxySettings getInstance() {
        LOG.traceEntry();
        return LOG.traceExit(ProxySettingsHolder.INSTANCE);
    }

    /**
     * Proxy configuration - host
     */
    private volatile String proxyHost;

    /**
     * Proxy configuration - port
     */
    private volatile String proxyPort;

    /**
     * Proxy configuration - user
     */
    private volatile String proxyUser;

    /**
     * Proxy configuration - password
     */
    private volatile String proxyPassword;

    /**
     * Proxy configuration - password
     */
    private volatile String nonProxyHosts;

    /**
     * Proxy configuration enable / disable
     */
    private volatile boolean proxySet = false;

    /**
     * Proxy type.
     */
    private volatile String proxyType;

    /**
     * Service provider name for SPNEGO protocol.
     */
    private volatile String proxySpn;

    /**
     * Jaas file where configuration for SPNego is described.
     */
    private volatile String proxyJaasFile;

    /**
     * Jaas context, which is described in Jaas file.
     */
    private volatile String proxyJaasCtx;

    /**
     * Private constructor
     */
    private ProxySettings() {
        LOG.traceEntry();
        init();
        LOG.traceExit();
    }

    /**
     * Init the proxy setting
     */
    public void init() {
        LOG.traceEntry();
        final DoiSettings settings = DoiSettings.getInstance();
        LOG.info("----- proxy parameters ----");

        this.proxyHost = settings.getString(Consts.SERVER_PROXY_HOST, "");
        LOG.info("proxyHost : {}", this.proxyHost);

        this.proxyPort = settings.getString(Consts.SERVER_PROXY_PORT, "");
        LOG.info("proxyPort : {}", this.proxyPort);

        this.proxyUser = settings.getSecret(Consts.SERVER_PROXY_LOGIN);
        LOG.info("proxyUser : {}", this.proxyUser);

        this.proxyPassword = settings.getSecret(Consts.SERVER_PROXY_PWD);
        LOG.info("proxyPassword : {}", this.proxyPassword);

        this.nonProxyHosts = settings.getString(Consts.SERVER_NONPROXY_HOSTS, "localhost");
        LOG.info("nonProxyHosts : {}", this.nonProxyHosts);

        this.proxySet = !Type.NO_PROXY.toString().equals(settings.getString(Consts.SERVER_PROXY_TYPE));
        LOG.info("proxySet : {}", this.proxySet);

        this.proxyType = settings.getString(Consts.SERVER_PROXY_TYPE, Type.NO_PROXY.name());
        LOG.info("proxyType : {}", this.proxyType);

        this.proxySpn = settings.getString(Consts.SERVER_PROXY_JAAS_SPN, "");
        LOG.info("proxy SPN : {}", this.proxySpn);

        this.proxyJaasFile = settings.getString(Consts.SERVER_PROXY_JAAS_FILE, "");
        LOG.info("proxy JAAS file : {}", this.proxyJaasFile);

        this.proxyJaasCtx = settings.getString(Consts.SERVER_PROXY_JAAS_CONTEXT, "");
        LOG.info("proxy JAAS ctx : {}", this.proxyJaasCtx);

        LOG.info("Proxy settings have been loaded");
        LOG.info("--------------------------");

        LOG.traceExit();
    }

    /**
     * Gets the withProxy value
     *
     * @return the withProxy
     */
    public boolean isWithProxy() {
        LOG.traceEntry();
        return LOG.traceExit(this.proxySet);
    }

    /**
     * Returns true when the proxy needs an authentication otherwise false.
     *
     * @return true when the proxy needs an authentication otherwise false.
     */
    public boolean isAuthenticate() {
        LOG.traceEntry();
        return LOG.traceExit(
                !this.getProxyUser().isEmpty() && !this.getProxyPassword().isEmpty() && this.
                isWithProxy());
    }

    /**
     * Gets the proxyHost value
     *
     * @return the proxyHost
     */
    public String getProxyHost() {
        LOG.traceEntry();
        return LOG.traceExit(proxyHost);
    }

    /**
     * Gets the proxyPort value
     *
     * @return the proxyPort
     */
    public String getProxyPort() {
        LOG.traceEntry();
        return LOG.traceExit(proxyPort);
    }

    /**
     * Gets the proxyUser value
     *
     * @return the proxyUser
     */
    public String getProxyUser() {
        LOG.traceEntry();
        return LOG.traceExit(proxyUser);
    }

    /**
     * Gets the proxyPassword value
     *
     * @return the proxyPassword
     */
    public String getProxyPassword() {
        LOG.traceEntry();
        return LOG.traceExit(proxyPassword);
    }

    /**
     * Gets the nonproxyHosts value
     *
     * @return the nonproxyHosts
     */
    public String getNonProxyHosts() {
        LOG.traceEntry();
        return LOG.traceExit(nonProxyHosts);
    }

    /**
     * Returns get type of proxy
     *
     * @return the proxy type
     */
    public String getProxyType() {
        LOG.traceEntry();
        return LOG.traceExit(this.proxyType);
    }

    /**
     * Configure the proxy.
     */
    public void configureProxy() {
        Type type = Type.valueOf(this.getProxyType());
        LOG.info("Starting with proxy : "+this.proxySet);
        if (this.proxySet) {
            switch (type) {
                case PROXY_BASIC:
                    LOG.info("Proxy with Basic authentication");
                    ProxyConfiguration.HTTP_PROXY.setValue(this.getProxyHost() + ":" + this.
                            getProxyPort());
                    ProxyConfiguration.NO_PROXY.setValue(this.getNonProxyHosts());
                    if (this.isAuthenticate()) {
                        ProxyConfiguration.USERNAME.setValue(this.getProxyUser());
                        ProxyConfiguration.PASSWORD.setValue(this.getProxyPassword());
                    }
                    break;
                case PROXY_SPNEGO_API:
                    LOG.info("Proxy with SPNego using API");
                    throw new IllegalArgumentException(
                            "SPNego trough API not supported, use JAAS configuration file");
                case PROXY_SPNEGO_JAAS:
                    LOG.info("Proxy with SPNego using JAAS configuration file");
                    ProxySPNegoJAASConfiguration.HTTP_PROXY.setValue(
                            this.getProxyHost() + ":" + this.getProxyPort());
                    ProxySPNegoJAASConfiguration.NO_PROXY.setValue(this.getNonProxyHosts());
                    ProxySPNegoJAASConfiguration.JAAS.setValue(this.proxyJaasFile);
                    ProxySPNegoJAASConfiguration.JAAS_CONTEXT.setValue(this.proxyJaasCtx);
                    ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.setValue(this.proxySpn);
                    break;
                default:
                    throw new IllegalArgumentException("Proxy "+type+" is not implemented");               
            }
        }
    }

    /**
     *
     */
    private static class ProxySettingsHolder {

        /**
         * Unique Instance unique not pre-initiliaze
         */
        private static final ProxySettings INSTANCE = new ProxySettings();
    }

}
