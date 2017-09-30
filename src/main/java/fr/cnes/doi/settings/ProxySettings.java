/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import fr.cnes.doi.utils.spec.Requirement;
import java.util.Arrays;
import java.util.logging.Logger;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;

/**
 * Sets the proxy parameter based on DoiSettings.
 *
 * @author Jean-Christophe Malapert
 */
@Requirement(
        reqId = Requirement.DOI_CONFIG_010,
        reqName = Requirement.DOI_CONFIG_010_NAME
)
public final class ProxySettings {
    
    /**
     * Class name.
     */
    private static final String CLASS_NAME = ProxySettings.class.getName();

    /**
     * Application logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProxySettings.class.getName());

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
        init();
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

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static ProxySettings getInstance() {
        return ProxySettingsHolder.INSTANCE;
    }

    /**
     * Init the proxy setting     
     */
    public void init() {
        LOGGER.entering(CLASS_NAME, "init");
        final DoiSettings settings = DoiSettings.getInstance();
        this.proxyHost = settings.getString(Consts.SERVER_PROXY_HOST);
        this.proxyPort = settings.getString(Consts.SERVER_PROXY_PORT);

        this.proxyUser = settings.getSecret(Consts.SERVER_PROXY_LOGIN);
        this.proxyPassword = settings.getSecret(Consts.SERVER_PROXY_PWD);
        this.nonProxyHosts = settings.getString(Consts.SERVER_NONPROXY_HOSTS);

        this.proxySet = settings.getBoolean(Consts.SERVER_PROXY_USED);
        this.proxyAuthentication = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, this.proxyUser, this.proxyPassword);
        
        LOGGER.info("Proxy settings have been loaded");
        
        LOGGER.exiting(CLASS_NAME, "init");        
    }

    /**
     * Gets the withProxy value
     *
     * @return the withProxy
     */
    public boolean isWithProxy() {
        LOGGER.config(String.format("isWithProxy : %s", this.proxySet));
        return this.proxySet;
    }

    /**
     * Gets the proxyHost value
     *
     * @return the proxyHost
     */
    public String getProxyHost() {
        LOGGER.config(String.format("getProxyHost : %s", this.proxyHost));        
        return proxyHost;
    }

    /**
     * Gets the proxyPort value
     *
     * @return the proxyPort
     */
    public String getProxyPort() {
        LOGGER.config(String.format("getProxyPort : %s", this.proxyPort));                
        return proxyPort;
    }

    /**
     * Gets the proxyUser value
     *
     * @return the proxyUser
     */
    public String getProxyUser() {
        LOGGER.config(String.format("getProxyUser : %s", this.proxyUser));                
        return proxyUser;
    }

    /**
     * Gets the proxyPassword value
     *
     * @return the proxyPassword
     */
    public String getProxyPassword() {
        LOGGER.config(String.format("getProxyPassword: %s", this.proxyPassword));                
        return proxyPassword;
    }

    /**
     * Gets the proxyAuthentication value
     *
     * @return the proxyAuthentication
     */
    public ChallengeResponse getProxyAuthentication() {
        LOGGER.config(String.format("getProxyAuthentication - login:"+this.proxyAuthentication.getIdentifier()+" pwd:"+Arrays.toString(this.proxyAuthentication.getSecret())));                
        return proxyAuthentication;
    }

    /**
     * Gets the nonproxyHosts value
     *
     * @return the nonproxyHosts
     */
    public String getNonProxyHosts() {
        LOGGER.config(String.format("getNonProxyHosts : %s", this.nonProxyHosts));                        
        return nonProxyHosts;
    }

}
