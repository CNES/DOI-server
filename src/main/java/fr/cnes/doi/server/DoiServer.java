/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.server;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.LocalReference;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Filter;
import org.restlet.security.Group;
import org.restlet.security.MemoryRealm;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.restlet.service.LogService;
import org.restlet.service.Service;
import org.restlet.util.Series;

import fr.cnes.doi.application.DoiCrossCiteApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.application.DoiStatusApplication;
import fr.cnes.doi.application.WebSiteApplication;
import fr.cnes.doi.logging.api.DoiLogDataServer;
import fr.cnes.doi.logging.security.DoiSecurityLogFilter;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.settings.JettySettings;
import fr.cnes.doi.settings.ProxySettings;
import fr.cnes.doi.utils.Utils;

/**
 *
 * @author malapert
 */
public class DoiServer extends Component {

    public static final String MDS_URI = "/mds";
    public static final String CITATION_URI = "/citation";
    public static final String STATUS_URI = "/status";

    public static final String DEFAULT_HTTP_PORT = "8182";
    public static final String DEFAULT_HTTPS_PORT = "8183";

    private final DoiSettings settings;

    private static final Logger LOGGER = Logger.getLogger(DoiServer.class.getName());

    public DoiServer(final DoiSettings settings) {
        super();
        this.settings = settings;
        startWithProxy(settings);
        
    }



    private void initLogServices() {
        LOGGER.entering(getClass().getName(), "initLogServices");
        this.setLogService(new DoiLogDataServer(Utils.HTTP_LOGGER_NAME, true));

        LogService logServiceApplication = new DoiLogDataServer(Utils.HTTP_LOGGER_NAME, true);
        this.getServices().add(logServiceApplication);
        //final String securityLoggerName = settings.getString("Starter.SecurityLogName");

        Service logServiceSecurity = new LogService(true) {
            /*
             * (non-Javadoc)
             * 
             * @see org.restlet.service.LogService#createInboundFilter(org.restlet.Context)
             */
            @Override
            public Filter createInboundFilter(Context context) {
                return new DoiSecurityLogFilter("fr.cnes.doi.logging.security");
            }
        };
        this.getServices().add(logServiceSecurity);
        LOGGER.exiting(getClass().getName(), "initLogServices");
    }

    /**
     * Configures the Server
     */
    private void configureServer() {
        LOGGER.entering(getClass().getName(), "init");

        Server serverHttp = startHttpServer(settings.getInt(Consts.SERVER_HTTP_PORT, DEFAULT_HTTP_PORT));
        Server serverHttps = startHttpsServer(settings.getInt(Consts.SERVER_HTTPS_PORT));

        this.getServers().add(serverHttps);
        this.getServers().add(serverHttp);
        this.getClients().add(Protocol.HTTP);
        this.getClients().add(Protocol.HTTPS);
        this.getClients().add(Protocol.CLAP);

        // Add configuration parameters to Servers
        JettySettings jettyProps = new JettySettings(serverHttp, settings);
        jettyProps.addParamsToServerContext();
        jettyProps = new JettySettings(serverHttps, settings);
        jettyProps.addParamsToServerContext();                

        Application appDoiProject = new DoiMdsApplication();

        // Attach the application to the this and start it
        this.getDefaultHost().attach(MDS_URI, appDoiProject);
        this.getDefaultHost().attach(CITATION_URI, new DoiCrossCiteApplication());
        this.getDefaultHost().attach(STATUS_URI, new DoiStatusApplication());
        this.getDefaultHost().attachDefault(new WebSiteApplication());

        // Set authentication
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

        LOGGER.exiting(getClass().getName(), "init");
    }

    /**
     * Starts with proxy.
     *
     * @param settings
     */
    private void startWithProxy(final DoiSettings settings) {
        LOGGER.entering(getClass().getName(), "startWithProxy");
        initLogServices();
        ProxySettings.getInstance().init(settings);
        EmailSettings.getInstance().init(settings);
        configureServer();
        LOGGER.exiting(getClass().getName(), "startWithProxy");
    }

    /**
     * Creates a HTTP server
     *
     * @param this this
     * @param port HTTP port
     * @return the HTTP server
     * @throws Exception
     */
    private Server startHttpServer(final Integer port) {
        LOGGER.entering(getClass().getName(), "startHttpServer", port);
        Server server = new Server(Protocol.HTTP, port, this);
        LOGGER.exiting(getClass().getName(), "startHttpServer");
        return server;
    }

    /**
     * Creates a HTTPS server
     *
     * @param this this
     * @param port HTTPS port
     * @return the HTTPS server
     * @throws Exception
     */
    private Server startHttpsServer(final Integer port) {
        LOGGER.entering(getClass().getName(), "startHttpsServer", port);
        String pathKeyStore;
        if(settings.hasValue(Consts.SERVER_HTTPS_KEYSTORE_PATH)) {
            pathKeyStore = settings.getString(Consts.SERVER_HTTPS_KEYSTORE_PATH);
        } else {
            pathKeyStore = extractKeyStoreToPath();
        }
        
        String pathKeyTrustStore;
        if(settings.hasValue(Consts.SERVER_HTTPS_TRUST_STORE_PATH)) {
            pathKeyTrustStore = settings.getString(Consts.SERVER_HTTPS_TRUST_STORE_PATH);
        } else {
            pathKeyTrustStore = extractKeyStoreToPath();
        }        
        
        // create embedding https jetty server
        Server server = new Server(new Context(), Protocol.HTTPS, port, this);
        Series<Parameter> parameters = server.getContext().getParameters();
        parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory");
        // Specifies the path for the keystore used by the server
        parameters.add("keyStorePath", pathKeyStore);
        // Specifies the password for the keystore containing several keys
        parameters.add("keyStorePassword", settings.getSecret(Consts.SERVER_HTTPS_KEYSTORE_PASSWD));
        // Specifies the type of the keystore
        parameters.add("keyStoreType", KeyStore.getDefaultType());
        // Specifies the password of the specific key used
        parameters.add("keyPassword", settings.getSecret(Consts.SERVER_HTTPS_SECRET_KEY));
        // Specifies the path to the truststore
        parameters.add("trustStorePath", pathKeyTrustStore);
        // Specifies the password of the truststore
        parameters.add("trustStorePassword", settings.getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
        // Specifies the type of the truststore
        parameters.add("trustStoreType", KeyStore.getDefaultType());
        LOGGER.exiting(getClass().getName(), "startHttpsServer", server);
        return server;
    }

    private String extractKeyStoreToPath() {
        String result;
        Representation jks = new ClientResource(LocalReference.createClapReference("class/doiServerKey.jks")).get();
        try {
            Path outputDirectory = new File("jks").toPath();
            if(Files.notExists(outputDirectory)) {
                Files.createDirectory(outputDirectory);
            }
            File outputFile = new File(outputDirectory.getFileName() + File.separator + "doiServerKey.jks");
            Files.copy(jks.getStream(), outputFile.toPath(), REPLACE_EXISTING);
            result = outputDirectory.getFileName() + File.separator + "doiServerKey.jks";
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            result = "";
        }

        return result;
    }

}
