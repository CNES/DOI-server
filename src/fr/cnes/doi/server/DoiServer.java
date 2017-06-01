/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.server;

import fr.cnes.doi.application.DoiCrossCiteApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.application.DoiStatusApplication;
import fr.cnes.doi.application.WebSiteApplication;
import fr.cnes.doi.utils.Utils;
import gnu.getopt.Getopt;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.security.Group;
import org.restlet.security.MemoryRealm;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.restlet.util.Series;

/**
 * DOI server
 *
 * @author Jean-Christophe Malapert
 */
public class DoiServer {

    /**
     * Configuration file.
     */
    static Properties configServer;

    public static final String VERSION = "1.0.0";

    public static final String MDS_URI = "/mds";
    public static final String CITATION_URI = "/citation";
    public static final String STATUS_URI = "/status";

    public static final String PROPERTY_LOGIN_MDS = "LOGIN_MDS";
    public static final String PROPERTY_PASSWD_MDS = "PASSWD_MDS";
    public static final String PROPERTY_DOI_PREFIX_CNES = "DOI_PREFIX_CNES";
    public static final String PROPERTY_CONTACT_ADMIN = "CONTACT_ADMIN";
    public static final String PROPERTY_LOG_FORMAT = "LOG_FORMAT";
    public static final String PROPERTY_HTTP_PORT = "HTTP_PORT";

    public static Component component;

    static {
        loadProperties();
    }

    /**
     * Loads the configuration file <i>config.properties</i> from DoiServer
     * package.
     */
    private static void loadProperties() {
        configServer = new Properties();
        InputStream in = DoiServer.class.getResourceAsStream("config.properties");
        try {
            configServer.load(in);
            in.close();
        } catch (IOException e) {
        }
    }

    private static void displayHelp() {
        System.out.println("------------ Help for DOI Server -----------");
        System.out.println();
        System.out.println("Usage: java -jar DOI.jar [OPTIONS]");
        System.out.println();
        System.out.println("with OPTIONS:");
        System.out.println("  -h                     : This output");
        System.out.println("  -c <string>            : Crypts a string in the standard output");
        System.out.println("  -e <string>            : Encrypts a string in the standard output");
        System.out.println("  -s                     : Starts the server");
        System.out.println("  -d                     : Displays the property file");
        System.out.println("  -f <path>              : Path to the configuation file");
        System.out.println("  -v                     : DOI server version");
        System.out.println();
        System.out.println();
    }

    private static void stopServer(Thread server) {
        try {
            try {
                component.stop();
            } catch (Exception ex) {
                Logger.getLogger(DoiServer.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                server.interrupt();
                server.join();
            }
        } catch (InterruptedException e) {
        }
    }

    private static void launchServer() {
        final Thread server = new Thread() {
            @Override
            public void run() {
                try {
                    component.start();
                } catch (Exception ex) {
                    Logger.getLogger(DoiServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("interrupt received, killing server…");
                stopServer(server);
            }
        });

        server.start();
    }

    private static void displayDefaultConfiguration() {
        Enumeration em = configServer.keys();
        while (em.hasMoreElements()) {
            String str = (String) em.nextElement();
            System.out.println(str + "=" + configServer.get(str));
        }
    }

    private static void loadCustomConfiguration(String arg) {
        try {
            configServer.load(new FileInputStream(arg));
        } catch (IOException ex) {
            Logger.getLogger(DoiServer.class.getName()).log(Level.SEVERE, "Cannot find {0}", ex.getMessage());
        }
    }

    private static void displayVersion() {
        System.out.println("DOI (CNES) " + VERSION + "\n");
    }

    public static void main(String[] argv) {

        try {
            configServer();
        } catch (Exception ex) {
            Logger.getLogger(DoiServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        int c;
        String arg;

        // 
        Getopt g = new Getopt("java -jar DOI.jar", argv, "hvdse:c:f:");
        //
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                    displayHelp();
                    break;
                //    
                case 's':
                    launchServer();
                    break;
                //
                case 'e':
                    arg = g.getOptarg();
                    System.out.println(Utils.decrypt(arg));
                    break;
                //    
                case 'c':
                    arg = g.getOptarg();
                    System.out.println(Utils.encrypt(arg));
                    break;
                //    
                case 'd':
                    displayDefaultConfiguration();
                    break;
                //    
                case 'f':
                    arg = g.getOptarg();
                    loadCustomConfiguration(arg);
                    break;
                //    
                case 'v':
                    displayVersion();
                    break;
                //
                case '?':
                    break; // getopt() already printed an error
                //
                default:
                    System.out.print("getopt() returned " + c + "\n");
            }
        }
        //
        for (int i = g.getOptind(); i < argv.length; i++) {
            System.out.println("Non option argv element: " + argv[i] + "\n");
        }

    }

    private static void configServer() throws Exception {

        Engine.setLogLevel(java.util.logging.Level.INFO);

        // Create a new Restlet component and add a HTTP and HTTPS server connector to it
        component = new Component();
        //component.getServers().add(startHttpsServer(component, 443));
        component.getServers().add(startHttpServer(component, Integer.valueOf(configServer.getProperty(DoiServer.PROPERTY_HTTP_PORT))));
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.HTTPS);
        component.getClients().add(Protocol.CLAP);

        Application appDoiProject = new DoiMdsApplication(configServer);

        // Attach the application to the component and start it
        component.getDefaultHost().attach(MDS_URI, appDoiProject);
        component.getDefaultHost().attach(CITATION_URI, new DoiCrossCiteApplication());
        component.getDefaultHost().attach(STATUS_URI, new DoiStatusApplication());
        component.getDefaultHost().attachDefault(new WebSiteApplication());

        MemoryRealm realm = new MemoryRealm();
        Role project1 = new Role(appDoiProject, "Project1");
        Role project2 = new Role(appDoiProject, "Project2");
        User jc = new User("jcm", "myPwd", "Jean-Christophe", "Malapert", "jcmalapert@gmail.com");
        User claire = new User("claire", "myPwd2");
        User software = new User("software", "pwd");
        Group human = new Group("human", "human users");
        human.getMemberUsers().add(jc);
        human.getMemberUsers().add(claire);
        Group soft = new Group("software", "software users");
        soft.getMemberUsers().add(software);

        appDoiProject.getContext().setDefaultEnroler(realm.getEnroler());
        appDoiProject.getContext().setDefaultVerifier(realm.getVerifier());

        realm.map(human, project1);
        realm.map(human, project2);

        component.getLogService().setResponseLogFormat(configServer.getProperty(DoiServer.PROPERTY_LOG_FORMAT));

    }

    /**
     * Creates a HTTP server
     *
     * @param component component
     * @param port HTTP port
     * @return the HTTP server
     * @throws Exception
     */
    private static Server startHttpServer(final Component component, final Integer port) throws Exception {
        return new Server(Protocol.HTTP, port, component);
    }

    /**
     * Creates a HTTPS server
     *
     * @param component component
     * @param port HTTPS port
     * @return the HTTPS server
     * @throws Exception
     */
    private static Server startHttpsServer(final Component component, final Integer port) throws Exception {
        // create embedding https jetty server
        Server server = new Server(new Context(), Protocol.HTTPS, port, component);

        Series<Parameter> parameters = server.getContext().getParameters();
        parameters.add("keystore", "jks/keystore.jks");
        parameters.add("keyStorePath", "jks/keystore.jks");
        parameters.add("keyStorePassword", "xxx");
        parameters.add("keyManagerPassword", "xxx");
        parameters.add("keyPassword", "xxx");
        parameters.add("password", "xxx");
        parameters.add("keyStoreType", KeyStore.getDefaultType());
        parameters.add("tracing", "true");
        parameters.add("truststore", "jks/keystore.jks");
        parameters.add("trustStorePath", "jks/keystore.jks");
        parameters.add("trustStorePassword", "xxx");
        parameters.add("trustPassword", "xxx");
        parameters.add("trustStoreType", KeyStore.getDefaultType());
        parameters.add("allowRenegotiate", "true");
        parameters.add("type", "1");

        return server;
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

