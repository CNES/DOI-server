/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.server;

import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.Utils;
import gnu.getopt.Getopt;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * DOI server
 *
 * @author Jean-Christophe Malapert
 */
public class Starter {

    static {
        final InputStream inputStream = Starter.class.getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (final IOException e) {
            Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
            Logger.getAnonymousLogger().severe(e.getMessage());
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Starter.class.getName());
    private static final Logger GLOBAL_LOGGER = Logger.getGlobal();

    private static DoiServer doiServer;

    private static void displayHelp() {
        GLOBAL_LOGGER.entering(Starter.class.getName(), "displayHelp");
        StringBuilder help = new StringBuilder();
        help.append("------------ Help for DOI Server -----------\n");
        help.append("\n");
        help.append("Usage: java -jar DOI.jar [OPTIONS]\n");
        help.append("\n");
        help.append("with OPTIONS:\n");
        help.append("  -h                     : This output\n");
        help.append("  -c <string>            : Crypts a string in the standard output\n");
        help.append("  -e <string>            : Encrypts a string in the standard output\n");
        help.append("  -s                     : Starts the server\n");
        help.append("  -d                     : Displays the property file\n");
        help.append("  -f <path>              : Path to the configuation file\n");
        help.append("  -v                     : DOI server version\n");
        help.append("\n");
        help.append("\n");
        LOGGER.info(help.toString());
        GLOBAL_LOGGER.exiting(Starter.class.getName(), "displayHelp");
    }

    /**
     * Stops the server
     *
     * @param server
     */
    private static void stopServer(final Thread server) {
        GLOBAL_LOGGER.entering(Starter.class.getName(), "stopServer");
        try {
            try {
                GLOBAL_LOGGER.info("Stopping the server ...");
                doiServer.stop();
                GLOBAL_LOGGER.info("Server stopped");
            } catch (Exception ex) {
                GLOBAL_LOGGER.info("Unable to stop the server");
                LOGGER.info("Unable to stop the server");
            } finally {
                GLOBAL_LOGGER.info("Interrups the server, which is stopping");
                server.interrupt();
                server.join();
            }
        } catch (InterruptedException e) {
            GLOBAL_LOGGER.finer(e.getMessage());
        }
    }

    /**
     * Starts the server
     *
     * @param server the server
     */
    private static void startServer(final DoiServer server) {
        GLOBAL_LOGGER.entering(Starter.class.getName(), "startServer");
        try {
            GLOBAL_LOGGER.info("Starting server ...");
            server.start();
            GLOBAL_LOGGER.info("Server started");
        } catch (Exception ex) {
            GLOBAL_LOGGER.info("Unable to start the server");
            LOGGER.info("Unable to start the server");
        }
    }

    /**
     * Launches the server.
     *
     * @param settings Configuration
     */
    private static void launchServer(final DoiSettings settings) {
        GLOBAL_LOGGER.entering(Starter.class.getName(), "launchServer");
        doiServer = new DoiServer(settings);
        final Thread server = new Thread() {
            @Override
            public void run() {
                startServer(doiServer);
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("interrupt received, killing server…");
                stopServer(server);
            }
        });

        server.start();
        GLOBAL_LOGGER.exiting(Starter.class.getName(), "launchServer");
    }

    /**
     * Displays version.
     */
    private static void displayVersion() {
        GLOBAL_LOGGER.entering(Starter.class.getName(), "displayVersion");
        final DoiSettings settings = DoiSettings.getInstance();
        final String appName = settings.getString(Consts.APP_NAME);
        final String version = settings.getString(Consts.VERSION);
        final String copyright = settings.getString(Consts.COPYRIGHT);
        LOGGER.log(Level.INFO, "{0} ({1}) - Version:{2}\n", new Object[]{appName, copyright, version});
        GLOBAL_LOGGER.exiting(Starter.class.getName(), "displayVersion");
    }

    public static void main(String[] argv) {
        final DoiSettings settings = DoiSettings.getInstance();

        int c;
        String arg;

        // 
        Getopt g = new Getopt("java -jar DOI.jar", argv, "hvdse:c:f:");
        //
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                    GLOBAL_LOGGER.fine("h option is selected");
                    displayHelp();
                    break;
                //    
                case 's':
                    GLOBAL_LOGGER.fine("s option is selected");
                    launchServer(settings);
                    break;
                //
                case 'e':
                    GLOBAL_LOGGER.fine("e option is selected");
                    arg = g.getOptarg();
                    System.out.println(Utils.decrypt(arg));
                    break;
                //    
                case 'c':
                    GLOBAL_LOGGER.fine("c option is selected");
                    arg = g.getOptarg();
                    System.out.println(Utils.encrypt(arg));
                    break;
                //    
                case 'd':
                    GLOBAL_LOGGER.fine("d option is selected");
                    settings.displayConfigFile();
                    break;
                //    
                case 'f':
                    GLOBAL_LOGGER.fine("f option is selected");
                    arg = g.getOptarg();
                     {
                        try {
                            settings.setPropertiesFile(arg);
                        } catch (IOException ex) {
                            LOGGER.info(ex.getMessage());
                        }
                    }
                    break;
                //    
                case 'v':
                    GLOBAL_LOGGER.fine("v option is selected");
                    displayVersion();
                    break;
                //
                case '?':
                    break; // getopt() already printed an error
                //
                default:
                    LOGGER.log(Level.INFO, "getopt() returned {0}\n", c);
            }
        }
        //
        for (int i = g.getOptind(); i < argv.length; i++) {
            LOGGER.log(Level.INFO, "Non option argv element: {0}\n", argv[i]);
        }

    }

}
// Generating a self-signed certificate
//--------------------------------------
//keytool -keystore serverKey.jks -alias server -genkey -keyalg RSA
//-keysize 2048 -dname "CN=simpson.org,OU=Simpson family,O=The Simpsons,C=US"
//-sigalg "SHA1withRSA"
//
//Note that you’ll be prompted for passwords for the keystore and the key itself. Let’s
//enter password as the example value. This certificate can then be exported as an inde-
//pendent certificate file server.crt, using this command (providing the same password):
//keytool -exportcert -keystore serverKey.jks -alias server -file serverKey.crt
//
// Generating a certificate request
//----------------------------------
//keytool -certreq -keystore serverKey.jks -alias server -file serverKey.csr
//
// Importing a trusted certificate
//----------------------------------
//After approval of the certificate request, the CA will provide a certificate file, usually in
//PEM or DER format. It needs to be imported back into the keystore to be used as a
//server certificate:
//keytool -import -keystore serverKey.jks -alias server -file serverKey.crt
//This command is also used for importing CA certificates into a special keystore that’s
//going to be used as a truststore—on the client side, for example. In this case the
//-trustcacerts options may also be required:
//keytool -import -keystore clientTrust.jks -trustcacerts -alias server -file serverKey.crt
//This trusted certificate may also be imported explicitly into your browser or used by a
//programmatic HTTPS client. This is useful if you’re deploying your own infrastruc-
//ture, or during development phases.

