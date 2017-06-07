/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import fr.cnes.doi.settings.ProxySettings;
import org.restlet.ext.wadl.WadlApplication;

/**
 * Base application.
 * @author Jean-Christophe Malapert
 */
public class BaseApplication extends WadlApplication {
    
    /**
     * Proxy settings.
     */
    protected ProxySettings proxySettings;
    
    /**
     * This constructor creates an instance of proxySettings.
     */
    public BaseApplication() {                
        proxySettings = ProxySettings.getInstance();       
    }
    
}
