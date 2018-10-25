/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import org.mockserver.verify.VerificationTimes;

/**
 * Test specifications.
 * @author Jean-Christophe Malapert
 */
public class AbstractSpec {

    private static ClientAndServer mockServer;
    
    /**
     *
     */
    public static final String ANSI_RESET = "\u001B[0m";

    /**
     *
     */
    public static final String ANSI_BLACK = "\u001B[30m";

    /**
     *
     */
    public static final String ANSI_RED = "\u001B[31m";

    /**
     *
     */
    public static final String ANSI_GREEN = "\u001B[32m";

    /**
     *
     */
    public static final String ANSI_YELLOW = "\u001B[33m";

    /**
     *
     */
    public static final String ANSI_BLUE = "\u001B[34m";

    /**
     *
     */
    public static final String ANSI_PURPLE = "\u001B[35m";

    /**
     *
     */
    public static final String ANSI_CYAN = "\u001B[36m";

    /**
     *
     */
    public static final String ANSI_WHITE = "\u001B[37m";    

    
    /**
     *
     * @param title
     */
    public static void classTitle(final String title) {
        System.out.println("\n\n"+ANSI_BLUE+"------ Testing "+title+" ------"+ANSI_RESET);
    }

    /**
     *
     * @param title
     */
    public static void testTitle(final String title) {
        System.out.println(ANSI_BLUE+"Testing: "+title+ANSI_RESET);
    }    

    /**
     *
     * @author Jean-Christophe Malapert
     */
    protected class MockupServer {

        public MockupServer(int port) {
            mockServer = startClientAndServer(port);
            while (!mockServer.isRunning()) {
                try {
                    // wait the server starts.
                    sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MockupServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public void createSpec(String verb, String path, int statusCode, String body) {
            mockServer
                    .when(
                            request()
                                    .withPath(path)
                                    .withMethod(verb)
                    )
                    .respond(
                            response()
                                    .withBody(body, StandardCharsets.UTF_8)
                                    .withStatusCode(statusCode)
                    );
        }

        public void verifySpec(String verb, String path) {
            mockServer.verify(
                    request()
                            .withMethod(verb)
                            .withPath(path), VerificationTimes.atLeast(1)
            );
        }
        
        public void verifySpec(String verb, String path, int exactly) {
            mockServer.verify(
                    request()
                            .withMethod(verb)
                            .withPath(path), VerificationTimes.exactly(exactly)
            );
        }        

        public void close() {
            try {
                mockServer.close();
            } catch (IOException ex) {
                Logger.getLogger(MockupServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
