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

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class InitServerForTest {
    
    private static DoiServer doiServer;
    
    public static void init() {
        InitSettingsForTest.init();
        doiServer = new DoiServer(DoiSettings.getInstance());
        try {
            doiServer.start();
            while(!doiServer.isStarted()) {
                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            Logger.getLogger(InitServerForTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void close() {
        try {
            doiServer.stop();
            while(!doiServer.isStopped()) {
                Thread.sleep(1000);
            }            
        } catch (Exception ex) {
            Logger.getLogger(InitServerForTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
