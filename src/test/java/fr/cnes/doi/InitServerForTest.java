/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi;

import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.server.DoiServer;
import fr.cnes.doi.settings.DoiSettings;
import java.util.logging.Level;
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
     * @throws fr.cnes.doi.exception.ClientMdsException When cannot get Datacite schema
     */
    public static void init() throws ClientMdsException {
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
