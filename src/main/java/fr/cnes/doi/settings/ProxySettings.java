/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import java.util.Properties;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;

/**
 * Sets the proxy parameter based on DoiSettings.
 * @author Jean-Christophe Malapert
 */
public final class ProxySettings {
    
    /**
     * Proxy configuration - host system property
     */
    private static final String HTTP_PROXYHOST = "http.proxyHost";

    /**
     * Proxy configuration - port system property
     */
    private static final String HTTP_PROXYPORT = "http.proxyPort";

    /**
     * non proxy hosts
     */
    private static final String HTTP_NONPROXYHOSTS = "http.nonProxyHosts";    

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
    }    
    
    private static class ProxySettingsHolder {

        /**
         * Unique Instance unique not pre-initiliaze
         */
        private final static ProxySettings INSTANCE = new ProxySettings();
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
     *
     * @param settings
     */
    public void init(final DoiSettings settings) {

        proxyHost = settings.getString(Consts.PROXY_HOST);
        proxyPort = settings.getString(Consts.PROXY_PORT);

        proxyUser = settings.getSecret(Consts.PROXY_USER);
        proxyPassword = settings.getSecret(Consts.PROXY_PASSWORD);
        nonProxyHosts = settings.getString(Consts.NONPROXY_HOSTS);

        if ((proxyHost != null) && !proxyHost.equals("") && (proxyPort != null) && !proxyPort.equals("")) {
            proxySet = true;

            Properties properties = System.getProperties();
            properties.put(HTTP_PROXYHOST, proxyHost);
            properties.put(HTTP_PROXYPORT, proxyPort);
            properties.put(HTTP_NONPROXYHOSTS, nonProxyHosts);

            // Add the client authentication to the call
            ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;

            // User + Password sur le proxy
            proxyAuthentication = new ChallengeResponse(scheme, proxyUser, proxyPassword);
        }
    }

    /**
     * Init the proxy setting
     */
    public void reset() {
        proxySet = false;
        Properties properties = System.getProperties();
        properties.remove(HTTP_PROXYHOST);
        properties.remove(HTTP_PROXYPORT);
        properties.remove(HTTP_NONPROXYHOSTS);
        proxyAuthentication = null;
    }

    /**
     * Gets the withProxy value
     *
     * @return the withProxy
     */
    public boolean isWithProxy() {
        return proxySet;
    }

    /**
     * Sets the value of withProxy
     *
     * @param withProxy the withProxy to set
     */
    public void setWithProxy(boolean withProxy) {
        proxySet = withProxy;
    }

    /**
     * Gets the proxyHost value
     *
     * @return the proxyHost
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Gets the proxyPort value
     *
     * @return the proxyPort
     */
    public String getProxyPort() {
        return proxyPort;
    }

    /**
     * Gets the proxyUser value
     *
     * @return the proxyUser
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * Gets the proxyPassword value
     *
     * @return the proxyPassword
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Gets the proxyAuthentication value
     *
     * @return the proxyAuthentication
     */
    public ChallengeResponse getProxyAuthentication() {
        return proxyAuthentication;
    }

    /**
     * Gets the nonproxyHosts value
     *
     * @return the nonproxyHosts
     */
    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

}
