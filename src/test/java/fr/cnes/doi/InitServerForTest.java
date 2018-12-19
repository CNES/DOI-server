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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.restlet.engine.Engine;
import org.restlet.service.CorsService;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Class to start/stop the http and https server.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class InitServerForTest {
    
    static {
        java.util.logging.Logger rootLogger = java.util.logging.LogManager.getLogManager().getLogger("");
        java.util.logging.Handler[] handlers = rootLogger.getHandlers();
        rootLogger.removeHandler(handlers[0]);
        SLF4JBridgeHandler.install();
    }    
    
    /**
     * Color reset.
     */
    public static final String ANSI_RESET = "\u001B[0m";

    /**
     * Black color.
     */
    public static final String ANSI_BLACK = "\u001B[30m";

    /**
     * Red color.
     */
    public static final String ANSI_RED = "\u001B[31m";

    /**
     * Green color.
     */
    public static final String ANSI_GREEN = "\u001B[32m";

    /**
     * Yellow color.
     */
    public static final String ANSI_YELLOW = "\u001B[33;1m";

    /**
     * Blue color.
     */
    public static final String ANSI_BLUE = "\u001B[34;1m";

    /**
     * Purple color.
     */
    public static final String ANSI_PURPLE = "\u001B[35m";

    /**
     * Cyan color.
     */
    public static final String ANSI_CYAN = "\u001B[36m";

    /**
     * White color.
     */
    public static final String ANSI_WHITE = "\u001B[37;1m";    
    
    /**
     * the servers.
     */
    private static DoiServer doiServer;
    
    /**
     * Init the settings and starts the server with the default configuration properties {@value DoiSettings#CONFIG_PROPERTIES}.
     */
    public static void init() {
        init(DoiSettings.CONFIG_PROPERTIES);
    }
    
    /**
     * Init the settings and starts the server with a specific configuration properties.
     * @param configProperties config properties
     */
    public static void init(final String configProperties) {
        InitSettingsForTest.init(configProperties);        
        try {
            doiServer = new DoiServer(DoiSettings.getInstance());
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
    
    /**
     *
     * @param title
     */
    public static void testTitle(final String title) {
        System.out.println(ANSI_BLUE + "Testing: " + title + ANSI_RESET);
    }

    /**
     *
     * @param title
     * @param status
     */
    public static void testTitle(final String title, final ColorStatus status) {
        testTitle(title, status, null);
    }

    public static void testTitle(final String title, final ColorStatus status, final String skipMessage) {
        final String colorStatus;
        switch (status) {
            case SKIP:
                colorStatus = ANSI_YELLOW + ColorStatus.SKIP.name().toLowerCase() + ANSI_RESET;
                break;
            case FAILED:
                colorStatus = ANSI_RED + ColorStatus.FAILED.name().toLowerCase() + ANSI_RESET;
                break;
            default:
                colorStatus = ANSI_GREEN + ColorStatus.OK.name().toLowerCase() + ANSI_RESET;
        }

        String pt = String.join("", Collections.nCopies(80, "."));
        if (title.length() > pt.length()) {
            pt = "";
        } else {
            pt = pt.substring(title.length() - 1, pt.length() - 1);
        }

        String skipMessageDisplay = (skipMessage == null) ? "" : ANSI_PURPLE + "  (" + skipMessage + ")" + ANSI_RESET;
        System.out.println(
                ANSI_BLUE + "Testing: " + title + ANSI_RESET + pt + colorStatus + skipMessageDisplay);
    }
        
    
    public static void mavenInfoRun(int numberTestCases) {
        String message = "[" + ANSI_BLUE + "INFO" + ANSI_RESET + "] " + ANSI_CYAN + numberTestCases + " test cases to run" + ANSI_RESET;
        System.out.println(message);        
    }

    public static void mavenTitle(Failure failure) {
        String message = "[" + ANSI_BLUE + "INFO" + ANSI_RESET + "] " + ANSI_WHITE + "Running " + failure.
                getDescription().getClassName() + ANSI_RESET;
        System.out.println(message);
    }

    public static void mavenSkipMessage(Failure failure) {
        String message = "[" + ANSI_YELLOW + "WARNING" + ANSI_RESET + "] " + ANSI_YELLOW + "Tests" + ANSI_RESET + " run: " + failure.
                getDescription().testCount() + ", Failures: 0, Errors: 0, " + ANSI_YELLOW + "Skipped: " + failure.
                        getDescription().testCount() + ANSI_RESET + ", Time elapsed: 0 s - " + failure.
                        getMessage();
        System.out.println(message);
    }    
    
    public static void mavenResult(Result result, int total, int skip, int error) {
        System.out.println("[" + ANSI_BLUE + "INFO" + ANSI_RESET + "]");
        System.out.println("[" + ANSI_BLUE + "INFO" + ANSI_RESET + "] Results (per test case) :");
        System.out.println("[" + ANSI_BLUE + "INFO" + ANSI_RESET + "]");
        if(result.wasSuccessful()) {
            if(skip > 0) {
                System.out.println("[" + ANSI_BLUE + "WARNING" + ANSI_RESET+ "] "+ANSI_GREEN+"Tests run: "+total+", Failures: "+error+", Skipped: "+skip+ANSI_RESET);
            } else {
                System.out.println("[" + ANSI_GREEN + "INFO" + ANSI_RESET +"] "+ANSI_GREEN+"Tests run: "+total+", Failures: "+error+", Skipped: "+skip+ANSI_RESET);                
            }
        } else {
            System.out.println("[" + ANSI_RED + "ERROR" + ANSI_RESET +"] "+ANSI_RED+"Tests run: "+total+", Failures: "+error+", Skipped: "+skip+ANSI_RESET);                
        }
    }   
    
    public static enum ColorStatus {
        SKIP,
        FAILED,
        OK
    }    
    
}
