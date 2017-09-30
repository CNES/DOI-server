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
import org.restlet.service.LogService;
import org.restlet.service.Service;
import org.restlet.util.Series;

import fr.cnes.doi.application.DoiCrossCiteApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.logging.api.DoiLogDataServer;
import fr.cnes.doi.logging.security.DoiSecurityLogFilter;
import fr.cnes.doi.security.RoleAuthorizer;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.settings.JettySettings;
import fr.cnes.doi.settings.ProxySettings;
import fr.cnes.doi.utils.Utils;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * DoiServer contains the configuration of this server and the methods to
 * start/stop it.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DoiServer extends Component {

    /**
     * URI of the Meta Data Store application.
     */
    public static final String MDS_URI = "/mds";

    /**
     * URI of the Citation application.
     */
    public static final String CITATION_URI = "/citation";

    /**
     * URI of the Datacite status.
     */
    public static final String STATUS_URI = "/status";

    /**
     * Default port HTTP server.
     */
    public static final String DEFAULT_HTTP_PORT = "8182";

    /**
     * Default port for HTTPS server.
     */
    public static final String DEFAULT_HTTPS_PORT = "8183";

    /**
     * Configuration.
     */
    private final DoiSettings settings;

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DoiServer.class.getName());

    /**
     * Creates an instance of the server with settings coming from the
     * config.properties
     *
     * @param settings settings
     */
    public DoiServer(final DoiSettings settings) {
        super();
        this.settings = settings;
        startWithProxy();
    }

    /**
     * Init log services.
     */
    private void initLogServices() {
        LOGGER.entering(getClass().getName(), "initLogServices");
        this.getLogService().setLoggerName(Utils.HTTP_LOGGER_NAME);

        final LogService logServiceApplication = new DoiLogDataServer(Utils.APP_LOGGER_NAME, true);
        this.getServices().add(logServiceApplication);

        final Service logServiceSecurity = new LogService(true) {
            /**
             * Creates a filter
             * @param context context
             * @return Filter
             * @see org.restlet.service.LogService#createInboundFilter(org.restlet.Context)
             */
            @Override
            public Filter createInboundFilter(final Context context) {
                return new DoiSecurityLogFilter("fr.cnes.doi.logging.security");
            }
        };
        this.getServices().add(logServiceSecurity);
        LOGGER.exiting(getClass().getName(), "initLogServices");
    }

    /**
     * Configures the Server in HTTP and HTTPS.
     */
    @Requirement(
            reqId = Requirement.DOI_ARCHI_010,
            reqName = Requirement.DOI_ARCHI_010_NAME
    )
    private void configureServer() {
        LOGGER.entering(getClass().getName(), "init");

        initHttpServer();
        initHttpsServer();
        initClients();
        initAttachApplication();

        LOGGER.exiting(getClass().getName(), "init");
    }

    /**
     * Inits the HTTP server.
     */
    private void initHttpServer() {
        final Server serverHttp = startHttpServer(settings.getInt(Consts.SERVER_HTTP_PORT, DEFAULT_HTTP_PORT));
        this.getServers().add(serverHttp);
        initJettyConfiguration(serverHttp);
    }

    /**
     * Inits the HTTPS server.
     */
    private void initHttpsServer() {
        final Server serverHttps = startHttpsServer(settings.getInt(Consts.SERVER_HTTPS_PORT, DEFAULT_HTTPS_PORT));
        this.getServers().add(serverHttps);
        initJettyConfiguration(serverHttps);
    }

    /**
     * Init the Jetty configuration and applies it to the server.
     *
     * @param server HTTP or HTTPS server
     */
    private void initJettyConfiguration(final Server server) {
        final JettySettings jettyProps = new JettySettings(server, settings);
        jettyProps.addParamsToServerContext();
    }

    /**
     * Inits supported protocols. Theses protocols are used by the server to
     * access to resources
     */
    private void initClients() {
        this.getClients().add(Protocol.HTTP);
        this.getClients().add(Protocol.HTTPS);
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.FILE);
    }

    /**
     * Routes the applications.
     */
    private void initAttachApplication() {
        final Application appDoiProject = new DoiMdsApplication();
        final Application appAdmin = new AdminApplication();
        this.getDefaultHost().attach(MDS_URI, appDoiProject);
        this.getDefaultHost().attach(CITATION_URI, new DoiCrossCiteApplication());
        this.getDefaultHost().attachDefault(appAdmin);
        // Set authentication
        RoleAuthorizer.getInstance().createRealmFor(appDoiProject);
        RoleAuthorizer.getInstance().createRealmFor(appAdmin);

    }

    /**
     * Starts with proxy.
     *
     */
    private void startWithProxy() {
        LOGGER.entering(getClass().getName(), "startWithProxy");
        initLogServices();
        ProxySettings.getInstance();
        EmailSettings.getInstance();
        configureServer();
        LOGGER.exiting(getClass().getName(), "startWithProxy");
    }

    /**
     * Creates a HTTP server
     *
     * @param port HTTP port
     * @return the HTTP server
     */
    private Server startHttpServer(final Integer port) {
        LOGGER.entering(getClass().getName(), "startHttpServer", port);
        final Server server = new Server(Protocol.HTTP, port, this);
        LOGGER.exiting(getClass().getName(), "startHttpServer");
        return server;
    }

    /**
     * Creates a HTTPS server
     *
     * @param port HTTPS port
     * @return the HTTPS server
     */
    private Server startHttpsServer(final Integer port) {
        LOGGER.entering(getClass().getName(), "startHttpsServer", port);
        final String pathKeyStore;
        if (settings.hasValue(Consts.SERVER_HTTPS_KEYSTORE_PATH)) {
            pathKeyStore = settings.getString(Consts.SERVER_HTTPS_KEYSTORE_PATH);
        } else {
            pathKeyStore = extractKeyStoreToPath();
        }

        final String pathKeyTrustStore;
        if (settings.hasValue(Consts.SERVER_HTTPS_TRUST_STORE_PATH)) {
            pathKeyTrustStore = settings.getString(Consts.SERVER_HTTPS_TRUST_STORE_PATH);
        } else {
            pathKeyTrustStore = extractKeyStoreToPath();
        }

        // create embedding https jetty server
        final Server server = new Server(new Context(), Protocol.HTTPS, port, this);
        final Series<Parameter> parameters = server.getContext().getParameters();
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

    /**
     * Extracts keystore for JAR and copy it in a directory in order to use it.
     *
     * @return the path of the new location of the keystore.
     */
    private String extractKeyStoreToPath() {
        String result;
        final Representation jks = new ClientResource(LocalReference.createClapReference("class/doiServerKey.jks")).get();
        try {
            final Path outputDirectory = new File("jks").toPath();
            if (Files.notExists(outputDirectory)) {
                Files.createDirectory(outputDirectory);
            }
            final File outputFile = new File(outputDirectory.getFileName() + File.separator + "doiServerKey.jks");
            Files.copy(jks.getStream(), outputFile.toPath(), REPLACE_EXISTING);
            result = outputDirectory.getFileName() + File.separator + "doiServerKey.jks";
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            result = "";
        }

        return result;
    }

}
