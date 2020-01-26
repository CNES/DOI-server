/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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

import fr.cnes.doi.exception.ClientMdsException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.Requirement;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.nio.charset.Charset;
import java.util.logging.Level;

/**
 * DOI server
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_DEV_010, reqName = Requirement.DOI_DEV_010_NAME)
@Requirement(reqId = Requirement.DOI_DEV_020, reqName = Requirement.DOI_DEV_020_NAME)
public final class Starter {

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
        final java.util.logging.Logger rootLogger = java.util.logging.LogManager.getLogManager()
                .getLogger("");
        final java.util.logging.Handler[] handlers = rootLogger.getHandlers();
        rootLogger.removeHandler(handlers[0]);
    }

    private static void displayHelp() {
        LOG.traceEntry();
        final DoiSettings settings = DoiSettings.getInstance();
        final StringBuilder help = new StringBuilder();
        help.append("\n------------ Help for DOI Server -----------\n");
        help.append("\n");
        help.append("Usage: java -jar ").append(settings.getString(Consts.APP_NAME)).append("-")
                .append(settings.getString(Consts.VERSION))
                .append(".jar [--secret <key>] [OPTIONS] [-s]\n");
        help.append("\n\n");
        help.append("with :\n");
        help.append("  --secret <key>               : The 16 bits secret key to crypt/decrypt\n");
        help.append("  --key-sign-secret <key>      : The key to sign the token\n");
        help.append("                                 If not provided, a default one is used\n");
        help.append("  -s|--start                   : Starts the server\n");
        help.append("  -t|--stop                    : Stops the server\n");
        help.append("  -l|--status                  : Status of the server\n");
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
        LOG.traceExit();
    }

    /**
     * Stops the server
     *
     * @param server HTTP or HTTPS server
     */
    private static void stopServer(final Thread server) {
        LOG.traceEntry();
        try {
            try {
                doiServer.stop();
            } catch (Exception ex) {
                LOG.fatal("Unable to stop the server", ex);
            } finally {
                LOG.info("Interrups the server, which is stopping");
                server.interrupt();
                server.join();
            }
        } catch (InterruptedException e) {
            LOG.fatal("Cannot interrupt the server", e);
        }
        LOG.traceExit();
    }

    /**
     * Starts the server
     *
     * @param server the server
     */
    @Requirement(reqId = Requirement.DOI_ARCHI_040, reqName = Requirement.DOI_ARCHI_040_NAME)
    private static void startServer(final DoiServer server) {
        final DoiSettings settings = DoiSettings.getInstance();
        final String progName = settings.getString(Consts.APP_NAME) + "-" + settings.getString(
                Consts.VERSION) + ".jar";
        final String stopPid = getCurrentPid(progName);
        if (stopPid == null) {
            try {
                server.start();
                infoProject();
            } catch (Exception ex) {
                LOG.info("Unable to start the server");
            }
        } else {
            LOG.info("The server is already started");
        }
    }

    /**
     * Info about project.
     */
    private static void infoProject() {
        LOG.info("-------------------------------------------------");
        LOG.info("              DOI-server project");
        LOG.info("-------------------------------------------------");
        LOG.info("Project : https://cnes.github.io/DOI-server");
        LOG.info("Source : https://github.com/cnes/DOI-server");
        LOG.info("Issues : https://github.com/CNES/DOI-server");
        LOG.info("Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES)");
        LOG.info("License : LGPLV3");

    }

    /**
     * Stops the server
     *
     */
    private static void stopServer() {
        try {
            final DoiSettings settings = DoiSettings.getInstance();
            final String progName = settings.getString(Consts.APP_NAME) + "-" + settings.getString(
                    Consts.VERSION) + ".jar";
            final String stopPid = getCurrentPid(progName);
            if (stopPid != null) {
                Runtime.getRuntime().exec("kill -9 " + stopPid);
                LOG.info("Stopping the DOI server .... OK");
            } else {
                LOG.info("The DOI server is already stopped");
            }
        } catch (IOException e) {
            LOG.info("Stopping the DOI server .... Failed");
        }
    }

    /**
     * Status of the server
     *
     */
    private static void statusServer() {
        final DoiSettings settings = DoiSettings.getInstance();
        final String progName = settings.getString(Consts.APP_NAME) + "-" + settings.getString(
                Consts.VERSION) + ".jar";
        final String stopPid = getCurrentPid(progName);
        if (stopPid != null) {
            LOG.info("The DOI server is running with the pid {}", stopPid);
        } else {
            LOG.info("The DOI server is stopped");
        }
    }

    /**
     * Launches the server.
     *
     * @param settings Configuration
     */
    private static void launchServer(final DoiSettings settings) {
        LOG.traceEntry();
        try {
            settings.validConfigurationFile();
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
        } catch (ClientMdsException| DoiRuntimeException ex) {
            LOG.info("Error when starting the server: " + ex.getMessage());
        } 
        LOG.traceExit();
    }

    /**
     * Displays version.
     */
    private static void displayVersion() {
        LOG.traceEntry();
        final DoiSettings settings = DoiSettings.getInstance();
        final String appName = settings.getString(Consts.APP_NAME);
        final String version = settings.getString(Consts.VERSION);
        final String copyright = settings.getString(Consts.COPYRIGHT);
        LOG.info("{} ({}) - Version:{}\n", appName, copyright, version);
        LOG.traceExit();
    }

    /**
     * Return the server PID from the system.
     *
     * @param serverName server name
     * @return the PID or null
     */
    private static String getCurrentPid(final String serverName) {
        LOG.traceEntry("Parameter\n\tserverName:{}", serverName);
        String stopPid = null;
        BufferedReader reader = null;
        Process pro = null;
        try {
            pro = Runtime.getRuntime().exec("ps aux");
            reader = new BufferedReader(new InputStreamReader(pro.
                    getInputStream(), Charset.defaultCharset()));
            final String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().
                    getName();
            final String myPid = processName.split("@")[0];
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(serverName) && line.contains("java")) {
                    final String[] split = line.split("\\s+");
                    final String currentPid = split[1].trim();
                    if (!currentPid.equals(myPid)) {
                        stopPid = currentPid;
                        break;
                    }
                }
            }
        } catch (IOException ex) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
            if (pro != null) {
                pro.destroy();
            }
        }
        return LOG.traceExit(stopPid);
    }

    /**
     * Main.
     *
     * @param argv command line arguments
     */
    public static void main(final String[] argv) {
        final DoiSettings settings = DoiSettings.getInstance();
        final String progName = settings.getString(Consts.APP_NAME) + "-" + settings.getString(
                Consts.VERSION) + ".jar";
        final String progNameWithJar = "java -jar " + progName;

        int c;
        String arg;

        final StringBuffer sb = new StringBuffer();
        final LongOpt[] longopts = new LongOpt[10];
        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v');
        longopts[2] = new LongOpt("secret", LongOpt.REQUIRED_ARGUMENT, sb, 0);
        longopts[3] = new LongOpt("decryptProperties", LongOpt.REQUIRED_ARGUMENT, null, 'z');
        longopts[4] = new LongOpt("cryptProperties", LongOpt.REQUIRED_ARGUMENT, null, 'y');
        longopts[5] = new LongOpt("key-sign", LongOpt.NO_ARGUMENT, null, 'k');
        longopts[6] = new LongOpt("key-sign-secret", LongOpt.REQUIRED_ARGUMENT, null, 'a');
        longopts[7] = new LongOpt("start", LongOpt.NO_ARGUMENT, null, 's');
        longopts[8] = new LongOpt("stop", LongOpt.NO_ARGUMENT, null, 't');
        longopts[8] = new LongOpt("status", LongOpt.NO_ARGUMENT, null, 'l');
        //
        final Getopt g = new Getopt(progNameWithJar, argv, "hvdstke:c:f:y:z:a:b:", longopts);
        //
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 0:
                    final String secretKey = g.getOptarg();
                    if (secretKey.length() != BITS_16) {
                        throw new IllegalArgumentException(
                                "The secret key must have 16 characters.");
                    } else {
                        settings.setSecretKey(secretKey);
                    }
                    break;
                //
                case 'a':
                    LOG.debug("a option is selected");
                    final String secretSignToken = g.getOptarg();
                    TokenSecurity.getInstance().setTokenKey(secretSignToken);
                    break;
                case 'h':
                    LOG.debug("h option is selected");
                    displayHelp();
                    break;
                //
                case 's':
                    LOG.debug("s option is selected");
                    launchServer(settings);
                    break;
                case 't':
                    LOG.debug("t option is selected");
                    stopServer();
                    break;
                case 'l':
                    LOG.debug("l option is selected");
                    statusServer();
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
                    try {
                        settings.setPropertiesFile(g.getOptarg());
                    } catch (IOException ex) {
                        LOG.fatal(ex.getMessage());
                    }
                    break;
                //
                case 'v':
                    LOG.debug("v option is selected");
                    displayVersion();
                    break;
                //
                case 'y':
                    LOG.debug("y option is selected");
                    try {
                        final byte[] encodedFile = Files.readAllBytes(Paths.get(g.getOptarg()));
                        String contentFile = new String(encodedFile, StandardCharsets.UTF_8);
                        contentFile = UtilsCryptography.encrypt(contentFile,
                                settings.getSecretKey());
                        LOG.info(contentFile);
                    } catch (IOException ex) {
                        LOG.fatal("Error: {}", ex.getMessage());
                    }
                    break;
                //
                case 'z':
                    LOG.debug("z option is selected");
                    InputStream inputStream = null;
                    BufferedReader reader = null;
                    Reader inputReader = null;

                    try {
                        inputStream = new FileInputStream(g.getOptarg());
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

    /**
     * "Static" class cannot be instantiated
     */
    private Starter() {
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
