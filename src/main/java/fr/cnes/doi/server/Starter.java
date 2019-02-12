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
package fr.cnes.doi.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.resource.admin.SuffixProjectsResource;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.utils.UniqueDoi;
import fr.cnes.doi.utils.spec.Requirement;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 * DOI server
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_DEV_010, reqName = Requirement.DOI_DEV_010_NAME)
@Requirement(reqId = Requirement.DOI_DEV_020, reqName = Requirement.DOI_DEV_020_NAME)
public class Starter {

    /**
     * Length of the secret key {@value #BITS_16}
     */
    public static final int BITS_16 = 16;

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(Starter.class.getName());

    /**
     * DOI Server.
     */
    private static DoiServer doiServer;

    static {
        java.util.logging.Logger rootLogger = java.util.logging.LogManager.getLogManager().
                getLogger("");
        java.util.logging.Handler[] handlers = rootLogger.getHandlers();
        rootLogger.removeHandler(handlers[0]);
        SLF4JBridgeHandler.install();
    }

    private static void displayHelp() {
        LOG.trace("Entering in displayHelp");
        DoiSettings settings = DoiSettings.getInstance();
        StringBuilder help = new StringBuilder();
        help.append("\n------------ Help for DOI Server -----------\n");
        help.append("\n");
        help.append("Usage: java -jar ").append(settings.getString(Consts.APP_NAME)).append("-")
                .append(settings.getString(Consts.VERSION)).append(
                ".jar [--secret <key>] [OPTIONS] [-s]\n");
        help.append("\n\n");
        help.append("with :\n");
        help.append("  --doi \"<INIST_prefix> <projectName> <landingPage>\" : Creates a DOI\n");
        help.append("  --secret <key>               : The 16 bits secret key to crypt/decrypt\n");
        help.append("  --key-sign-secret <key>      : The key to sign the token\n");
        help.append("                                 If not provided, a default one is used\n");
        help.append("  -s                           : Starts the server\n");
        help.append("with OPTIONS:\n");
        help.append("  -h|--help                    : This output\n");
        help.append("  -k|--key-sign                : Creates a key to sign JWT token\n");
        help.append("  -c <string>                  : Crypts a string in the standard output\n");
        help.append("  -e <string>                  : Decrypts a string in the standard output\n");
        help.append("  -d                           : Displays the configuration file\n");
        help.append("  -f <path>                    : Loads the configuation file\n");
        help.append(
                "  -y|--cryptProperties <path>  : crypts the properties file on the output standard\n");
        help.append(
                "  -z|--decryptProperties <path>: Decrypts the properties on the output standard\n");
        help.append("  -v|--version                 : DOI server version\n");
        help.append("\n");
        help.append("\n");
        LOG.info(help.toString());
        LOG.trace("Exiting from displayHelp");
    }

    /**
     * Stops the server
     *
     * @param server HTTP or HTTPS server
     */
    private static void stopServer(final Thread server) {
        LOG.trace("Entering in stopServer");
        try {
            try {
                LOG.info("Stopping the server ...");
                doiServer.stop();
                LOG.info("Server stopped");
            } catch (Exception ex) {
                LOG.fatal("Unable to stop the server", ex);
            } finally {
                LOG.info("Interrups the server, which is stopping");
                EmailSettings.getInstance().sendMessage("[DOI] Stopping Server",
                        "Ther server has been interrupted");
                server.interrupt();
                server.join();
            }
        } catch (InterruptedException e) {
            LOG.debug("Cannot interrupt the server", e);
        }
        LOG.trace("Exiting from stopServer");
    }

    /**
     * Starts the server
     *
     * @param server the server
     */
    @Requirement(reqId = Requirement.DOI_ARCHI_040, reqName = Requirement.DOI_ARCHI_040_NAME)
    private static void startServer(final DoiServer server) {
        LOG.trace("Entering in startServer");
        try {
            fr.cnes.doi.plugin.Utils.addPath(
                    DoiSettings.getInstance().getPathApp() + File.separatorChar + "plugins"
            );
            LOG.info("Starting server ...");
            server.start();
            LOG.info("Server started");
        } catch (Exception ex) {
            LOG.fatal("Unable to start the server");
        }
        LOG.trace("Exiting from startServer");
    }

    /**
     * Launches the server.
     *
     * @param settings Configuration
     */
    private static void launchServer(final DoiSettings settings) {

        LOG.trace("Entering in launchServer");
        settings.validConfigurationFile();
        LOG.info("launchServer, entering DOI server");
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
                LOG.info("interrupt received, killing server…");
                stopServer(server);
            }
        });

        server.start();
        LOG.trace("Exiting from launchServer");
    }

    /**
     * Displays version.
     */
    private static void displayVersion() {
        LOG.trace("Entering in displayVersion");
        final DoiSettings settings = DoiSettings.getInstance();
        final String appName = settings.getString(Consts.APP_NAME);
        final String version = settings.getString(Consts.VERSION);
        final String copyright = settings.getString(Consts.COPYRIGHT);
        LOG.info("{} ({}) - Version:{}\n", appName, copyright, version);
        LOG.trace("Exiting from displayVersion");
    }

    /**
     * Main.
     *
     * @param argv command line arguments
     */
    public static void main(final String[] argv) {
        final DoiSettings settings = DoiSettings.getInstance();
        final String progName = "java -jar " + settings.getString(Consts.APP_NAME) + "-"
                + settings.getString(Consts.VERSION) + ".jar";

        int c;
        String arg;

        StringBuffer sb = new StringBuffer();
        LongOpt[] longopts = new LongOpt[8];
        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v');
        longopts[2] = new LongOpt("secret", LongOpt.REQUIRED_ARGUMENT, sb, 0);
        longopts[3] = new LongOpt("decryptProperties", LongOpt.REQUIRED_ARGUMENT, null, 'z');
        longopts[4] = new LongOpt("cryptProperties", LongOpt.REQUIRED_ARGUMENT, null, 'y');
        longopts[5] = new LongOpt("key-sign", LongOpt.NO_ARGUMENT, null, 'k');
        longopts[6] = new LongOpt("key-sign-secret", LongOpt.REQUIRED_ARGUMENT, null, 'a');
        longopts[7] = new LongOpt("doi", LongOpt.REQUIRED_ARGUMENT, null, 'b');

        //
        Getopt g = new Getopt(progName, argv, "hvdske:c:f:y:z:a:b:", longopts);
        //
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 0:
                    String secretKey = g.getOptarg();
                    if (secretKey.length() != BITS_16) {
                        throw new IllegalArgumentException("The secret key must have 16 characters.");
                    } else {
                        settings.setSecretKey(secretKey);
                    }
                    break;
                //
                case 'a':
                    String secretSignToken = g.getOptarg();
                    TokenSecurity.getInstance().setTokenKey(secretSignToken);
                    break;
                case 'b':
                    String[] arguments = g.getOptarg().split(" ");
                    String prefix = arguments[0];
                    String projectName = arguments[1];
                    String landingPage = arguments[2];
                     {
                        try {
                            String doi = UniqueDoi.getInstance().createDOI(prefix, projectName,
                                    new URI(landingPage), SuffixProjectsResource.NB_DIGITS);
                            LOG.info(doi);
                        } catch (URISyntaxException ex) {
                            throw new IllegalArgumentException(ex.getMessage());
                        }
                    }
                    break;
                case 'h':
                    LOG.debug("h option is selected");
                    displayHelp();
                    break;
                //
                case 's':
                    LOG.debug("s option is selected");
                    try {
                        launchServer(settings);
                    } catch (DoiRuntimeException ex) {
                        LOG.fatal("Error when starting the server: " + ex.getMessage());
                    }
                    break;
                //
                case 'k':
                    LOG.debug("k option is selected");
                    LOG.info(TokenSecurity.createKeySignatureHS256());
                    break;
                //
                case 'e':
                    LOG.debug("e option is selected");
                    arg = g.getOptarg();
                    try {
                        LOG.info(UtilsCryptography.decrypt(arg, settings.getSecretKey()));
                    } catch (DoiRuntimeException ex) {
                        LOG.fatal("Unable to decrypt {} : {}", arg, ex.getMessage());
                    }
                    break;
                //
                case 'c':
                    LOG.debug("c option is selected");
                    arg = g.getOptarg();
                    try {
                        LOG.info(UtilsCryptography.encrypt(arg, settings.getSecretKey()));
                    } catch (DoiRuntimeException ex) {
                        LOG.fatal("Unable to encrypt {} : {}", arg, ex.getMessage());
                    }
                    break;
                //
                case 'd':
                    LOG.debug("d option is selected");
                    settings.displayConfigFile();
                    break;
                //
                case 'f':
                    LOG.debug("f option is selected");
                    arg = g.getOptarg();
                     {
                        try {
                            settings.setPropertiesFile(arg);
                        } catch (IOException ex) {
                            LOG.fatal(ex.getMessage());
                        }
                    }
                    break;
                //
                case 'v':
                    LOG.debug("v option is selected");
                    displayVersion();
                    break;
                //
                case 'y':
                    arg = g.getOptarg();
                    try {
                        byte[] encodedFile = Files.readAllBytes(Paths.get(arg));
                        String contentFile = new String(encodedFile, StandardCharsets.UTF_8);
                        contentFile = UtilsCryptography.
                                encrypt(contentFile, settings.getSecretKey());
                        LOG.info(contentFile);
                    } catch (IOException ex) {
                        LOG.fatal("Error: {}", ex.getMessage());
                    }
                    break;
                //
                case 'z':
                    arg = g.getOptarg();
                    InputStream inputStream = null;
                    BufferedReader reader = null;
                    Reader inputReader = null;

                    try {
                        inputStream = new FileInputStream(arg);
                        inputReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                        reader = new BufferedReader(inputReader);
                        String content = reader.lines().collect(Collectors.joining("\n"));
                        content = UtilsCryptography.decrypt(content, settings.getSecretKey());
                        LOG.info(content);
                    } catch (FileNotFoundException ex) {
                        LOG.fatal("Error: {}", ex.getMessage());
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();

                            } catch (IOException e) {
                                LOG.fatal("Error closing inputstream: {}", e.getMessage(), e);
                            }

                        }
                        if (inputReader != null) {
                            try {
                                inputReader.close();
                            } catch (IOException ex) {
                                LOG.fatal("Error closing inputReader: {}", ex.getMessage(), ex);
                            }
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException ex) {
                                LOG.fatal("Error closing reader: {}", ex.getMessage(), ex);
                            }

                        }
                    }
                    break;
                case '?':
                    break; // getopt() already printed an error
                //
                default:
                    LOG.debug("getopt() returned {}\n", c);
            }
        }
        //
        for (int i = g.getOptind(); i < argv.length; i++) {
            LOG.info("Non option argv element: {}\n", argv[i]);
        }

        if (argv.length == 0) {
            displayHelp();
        }

    }

}
// Generating a self-signed certificate
// --------------------------------------
// keytool -keystore serverKey.jks -alias server -genkey -keyalg RSA
// -keysize 2048 -dname "CN=www.cnes.fr,OU=Jean-Christophe Malapert,O=CNES,C=FR"
// -sigalg "SHA1withRSA"
//
// Note that you’ll be prompted for passwords for the keystore and the key
// itself. Let’s
// enter password as the example value. This certificate can then be exported as
// an inde-
// pendent certificate file server.crt, using this command (providing the same
// password):
// keytool -exportcert -keystore serverKey.jks -alias server -file serverKey.crt
//
// Generating a certificate request
// ----------------------------------
// keytool -certreq -keystore serverKey.jks -alias server -file serverKey.crt
//
// Importing a trusted certificate
// ----------------------------------
// After approval of the certificate request, the CA will provide a certificate
// file, usually in
// PEM or DER format. It needs to be imported back into the keystore to be used
// as a
// server certificate:
// keytool -import -keystore serverKey.jks -alias server -file serverKey.crt
// This command is also used for importing CA certificates into a special
// keystore that’s
// going to be used as a truststore—on the client side, for example. In this
// case the
// -trustcacerts options may also be required:
// keytool -import -keystore clientTrust.jks -trustcacerts -alias server -file
// serverKey.crt
// This trusted certificate may also be imported explicitly into your browser or
// used by a
// programmatic HTTPS client. This is useful if you’re deploying your own
// infrastruc-
// ture, or during development phases.
