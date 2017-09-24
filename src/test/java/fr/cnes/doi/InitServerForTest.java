/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi;

import fr.cnes.doi.server.DoiServer;
import fr.cnes.doi.settings.DoiSettings;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.engine.Engine;

/**
 * Class to start/stop the http and https server.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class InitServerForTest {
    
    /**
     * the servers.
     */
    private static DoiServer doiServer;
    
    /**
     * Init the settings and starts the server.
     */
    public static void init() {
        InitSettingsForTest.init();
        doiServer = new DoiServer(DoiSettings.getInstance());
        try {
            doiServer.start();
            while(!doiServer.isStarted()) {
                Thread.sleep(1000);
            }
            Engine.getLogger(InitServerForTest.class).info("The test server is started");
        } catch (Exception ex) {
            Engine.getLogger(InitServerForTest.class).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Stops the server.
     */
    public static void close() {
        try {
            doiServer.stop();
            while(!doiServer.isStopped()) {
                Thread.sleep(1000);
            }      
            Engine.getLogger(InitServerForTest.class).info("The test server is stopped");
        } catch (Exception ex) {
            Engine.getLogger(InitServerForTest.class).log(Level.SEVERE, null, ex);
        }
    }
    
}
