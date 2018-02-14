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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;

/**
 * Sets the proxy parameter based on DoiSettings.
 *
 * @author Jean-Christophe Malapert
 */
@Requirement(reqId = Requirement.DOI_CONFIG_010,reqName = Requirement.DOI_CONFIG_010_NAME)
public final class ProxySettings {    

    /**
     * Application logger.
     */
    private static final Logger LOG = LogManager.getLogger(ProxySettings.class.getName());

    /**
     * Proxy configuration - host
     */
    private String proxyHost = null;

    /**
     * Proxy configuration - port
     */
    private String proxyPort = null;

    /**
     * Proxy configuration - user
     */
    private String proxyUser = null;

    /**
     * Proxy configuration - password
     */
    private String proxyPassword = null;

    /**
     * Proxy configuration - password
     */
    private String nonProxyHosts = null;

    /**
     * Cache Proxy ChallengeResponse
     */
    private ChallengeResponse proxyAuthentication = null;

    /**
     * Proxy configuration enable / disable
     */
    private boolean proxySet = false;

    /**
     * Private constructor
     */
    private ProxySettings() {
        LOG.traceEntry();
        init();
        LOG.traceExit();
    }


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
     * Init the proxy setting     
     */
    public void init() {
        LOG.traceEntry();
        final DoiSettings settings = DoiSettings.getInstance();
        LOG.info("----- proxy parameters ----");  
        
        this.proxyHost = settings.getString(Consts.SERVER_PROXY_HOST);
        LOG.info("proxyHost : {}",this.proxyHost);
        
        this.proxyPort = settings.getString(Consts.SERVER_PROXY_PORT);
        LOG.info("proxyPort : {}",this.proxyPort);
        
        this.proxyUser = settings.getSecret(Consts.SERVER_PROXY_LOGIN);
        LOG.info("proxyUser : {}",this.proxyUser);        
        
        this.proxyPassword = settings.getSecret(Consts.SERVER_PROXY_PWD);
        LOG.info("proxyPassword : {}",this.proxyPassword);                
        
        this.nonProxyHosts = settings.getString(Consts.SERVER_NONPROXY_HOSTS);
        LOG.info("nonProxyHosts : {}",this.nonProxyHosts);                
        
        this.proxySet = settings.getBoolean(Consts.SERVER_PROXY_USED);
        LOG.info("proxySet : {}",this.proxySet);                        
        
        this.proxyAuthentication = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, this.proxyUser, this.proxyPassword);        
        
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
     * Gets the proxyAuthentication value
     *
     * @return the proxyAuthentication
     */
    public ChallengeResponse getProxyAuthentication() {
        LOG.traceEntry();
        return LOG.traceExit(proxyAuthentication);
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
     * 
     */
    private static class ProxySettingsHolder {

        /**
         * Unique Instance unique not pre-initiliaze
         */
        private static final ProxySettings INSTANCE = new ProxySettings();
    }    

}
