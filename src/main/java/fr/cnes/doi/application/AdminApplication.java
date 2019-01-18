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
package fr.cnes.doi.application;

import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.logging.business.JsonMessage;
import fr.cnes.doi.resource.admin.SuffixProjectsResource;
import fr.cnes.doi.resource.admin.TokenResource;
import fr.cnes.doi.security.AllowerIP;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.services.LandingPageMonitoring;
import fr.cnes.doi.utils.spec.CoverageAnnotation;
import fr.cnes.doi.utils.spec.Requirement;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Status;
import org.restlet.resource.Directory;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Role;
import org.restlet.security.RoleAuthorizer;
import org.restlet.service.MetadataService;
import org.restlet.service.TaskService;

/**
 * Provides an application for handling features related to the administration system of the DOI
 * server.
 *
 * The administration application provides the following features:
 * <ul>
 * <li>An asynchronous task to check the availability of created landing pages each
 * {@value #PERIOD_SCHEDULER} days</li>
 * <li>The form for creating DOI</li>
 * <li>The Datacite status page to check Datacite services availability</li>
 * <li>The Datacite stats page</li>
 * <li>A service to create random string as a part of the DOI suffix</li>
 * <li>A token service for creating token and to get token information</li>
 * </ul>
 * <p>
 * <b>Security</b><br>
 * --------------<br>
 * The authentication is done by a simple login/password. Only users having the group "admin" will
 * be allowed to log in to this application.<br>
 * The website for creating DOI is opened on the net whereas the others services are filtered by IP.
 * The allowed IPs are localhost and the IPs defined in
 * {@value fr.cnes.doi.settings.Consts#ADMIN_IP_ALLOWER} attribute from the configuration file
 *
 * <p>
 * <b>Routing</b><br>
 * --------------<br>
 * <br>
 * root (DOI creation web form - no authorization)<br>
 * |<br>
 * |__ resources (status page - no authorization)<br>
 * |__ status (Datacite status page - authorization)<br>
 * |__ stats (Datacite stats page - authorization)<br>
 * |_ ____________<br>
 * |_|************|____ suffixProject (Get a random suffix - authorization)<br>
 * |_|IP_filtering|____ token (Create a token - authorization)<br>
 * |_|____________|____ token/{tokenID} (Get token information - authorization)<br>
 *
 * @see <a href="http://status.datacite.org">Datacite status page</a>
 * @see <a href="https://stats.datacite.org/#tab-prefixes">Datacite stats page</a>
 * @see SuffixProjectsResource Creating a project suffix for DOI
 * @see TokenResource Creating a token and getting information about a token
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_SRV_130, reqName = Requirement.DOI_SRV_130_NAME)
@Requirement(reqId = Requirement.DOI_SRV_140, reqName = Requirement.DOI_SRV_140_NAME)
@Requirement(reqId = Requirement.DOI_SRV_150, reqName = Requirement.DOI_SRV_150_NAME)
@Requirement(reqId = Requirement.DOI_SRV_180, reqName = Requirement.DOI_SRV_180_NAME)
@Requirement(reqId = Requirement.DOI_SRV_190, reqName = Requirement.DOI_SRV_190_NAME,
        coverage = CoverageAnnotation.NONE)
@Requirement(reqId = Requirement.DOI_DISPO_020, reqName = Requirement.DOI_DISPO_020_NAME)
public class AdminApplication extends AbstractApplication {

    /**
     * Application name.
     */
    public static final String NAME = "Admin Application";

    /**
     * URI {@value #ADMIN_URI} to access to the resources of the system administration.
     */
    public static final String ADMIN_URI = "/admin";

    /**
     * URI {@value #RESOURCE_URI} to access to the resources of the status page.
     */
    public static final String RESOURCE_URI = "/resources";

    /**
     * URI {@value #STATUS_URI} to access to the status page.
     */
    public static final String STATUS_URI = "/status";

    /**
     * URI {@value #STATS_URI} to access to Stats page.
     */
    public static final String STATS_URI = "/stats";

    /**
     * URI {@value #SUFFIX_PROJECT_URI} to create a project suffix.
     */
    public static final String SUFFIX_PROJECT_URI = "/suffixProject";

    /**
     * URI {@value #TOKEN_URI} to create a token.
     */
    public static final String TOKEN_URI = "/token";

    /**
     * Token template.
     */
    public static final String TOKEN_TEMPLATE = "tokenID";

    /**
     * URI {@value #TOKEN_NAME_URI} to handle to get information from a token.
     */
    public static final String TOKEN_NAME_URI = "/{" + TOKEN_TEMPLATE + "}";

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(AdminApplication.class.getName());

    /**
     * Default directory {@value #JS_DIR} where the web site is located.
     */
    private static final String JS_DIR = "js";

    /**
     * Location of the resources for the status page in the classpath.
     */
    private static final String STATUS_PAGE_CLASSPATH = "class/website";

    /**
     * The period between successive executions : {@value #PERIOD_SCHEDULER}.
     */
    private static final int PERIOD_SCHEDULER = 30;

    /**
     * The time unit of the initialDelay and period parameters.
     */
    private static final TimeUnit PERIOD_UNIT = TimeUnit.DAYS;

    /**
     * DataCite Status page {@value #TARGET_URL}.
     */
    private static final String TARGET_URL = "http://status.datacite.org";

    /**
     * DataCite Stats page {@value #TARGET_STATS_URL}.
     */
    private static final String TARGET_STATS_URL = "https://stats.datacite.org/#tab-prefixes";

    /**
     * Token database.
     */
    private final AbstractTokenDBHelper tokenDB;

    /**
     * Constructor.
     */
    public AdminApplication() {
        super();
        setName(NAME);
        setDescription("Provides an application for handling features related to "
                + "the administration system of the DOI server.");
        this.setTaskService(createTaskService());
        this.tokenDB = TokenSecurity.getInstance().getTOKEN_DB();  
        getMetadataService().addExtension("xsd", MediaType.TEXT_XML, true);
    }

    /**
     * A task checking status of landing pages each 30 days.
     *
     * @return A task
     */
    @Requirement(reqId = Requirement.DOI_DISPO_020, reqName = Requirement.DOI_DISPO_020_NAME)
    private TaskService createTaskService() {
        LOG.traceEntry();
        final TaskService checkLandingPageTask = new TaskService(true);
        LOG.info("Sets CheckLandingPage running at each {} {}", PERIOD_SCHEDULER, PERIOD_UNIT);
        checkLandingPageTask.scheduleAtFixedRate(
                new LandingPageMonitoring(), 0,
                PERIOD_SCHEDULER, PERIOD_UNIT
        );
        return LOG.traceExit(checkLandingPageTask);
    }

    /**
     * Creates a router for the AdminApplication. This router routes :
     * <ul>
     * <li>the web resources for the website with no authentication</li>
     * <li>the REST resources for the system administration with authentication/authorization</li>
     * </ul>
     * The web resources are attached by default to the AdminApplication where as the REST resources
     * for the system administration are attached with the {@link AdminApplication#ADMIN_URI}
     *
     * @see AdminApplication#createWebSiteRouter() the router that contains the the web resources
     * @see AdminApplication#createAdminRouter() the router that contains the REST resources
     * @see AdminApplication#createAuthenticator() the authentication mechanism
     * @see AdminApplication#createRoleAuthorizer() the authorization mechanism
     * @return Router
     */
    @Override
    public Restlet createInboundRoot() {
        LOG.traceEntry();
        // Defines the strategy of authentication (authentication is not required)
        //   - authentication with login/pwd
        final ChallengeAuthenticator challAuth = createAuthenticator();
        challAuth.setOptional(false);

        // Defines the authorization
        final RoleAuthorizer authorizer = createRoleAuthorizer();

        // pipeline of authentication and authorization
        challAuth.setNext(authorizer);

        // Defines the routers
        final Router webSiteRouter = createWebSiteRouter();
        final Router adminRouter = createAdminRouter();

        // Defines the admin router as private
        final AllowerIP blocker = new AllowerIP(getContext());
        authorizer.setNext(blocker);
        blocker.setNext(adminRouter);

        final Router router = new Router(getContext());
        router.attachDefault(webSiteRouter);
        router.attach(ADMIN_URI, challAuth);

        return LOG.traceExit(router);
    }

    /**
     * Creates a router for REST services for the system administration. This router contains the
     * following resources :
     * <ul>
     * <li>{@link AdminApplication#SUFFIX_PROJECT_URI} resource to handle the project suffix, which
     * is used in the Digital Object Identifier</li>
     * <li>{@link AdminApplication#TOKEN_URI} resource to handle the token for the
     * authentication</li>
     * </ul>
     *
     * @see SuffixProjectsResource Resource to handle the the project suffix
     * @see TokenResource Resource to handle the token resource
     * @return the router
     */
    private Router createAdminRouter() {
        LOG.traceEntry();

        final Router router = new Router(getContext());
        router.attach(SUFFIX_PROJECT_URI, SuffixProjectsResource.class);
        router.attach(TOKEN_URI, TokenResource.class);
        router.attach(TOKEN_URI + TOKEN_NAME_URI, TokenResource.class);

        return LOG.traceExit(router);
    }

    /**
     * Creates a router for the web site resources. This router contains the following resources:
     * <ul>
     * <li>{@link AdminApplication#RESOURCE_URI} to distribute the web resources for the status
     * page</li>
     * <li>the website resources attached by default when it is available</li>
     * </ul>
     * The website is located to {@link AdminApplication#JS_DIR} directory when it is distributed by
     * the DOI server.
     *
     * @return The router for the public web site
     */
    private Router createWebSiteRouter() {
        LOG.traceEntry();

        final Router router = new Router(getContext());
        addStatusPage(router);
        addServicesStatus(router);
        addServicesStats(router);
        addRouteForWebSite(router);

        return LOG.traceExit(router);
    }

    /**
     * Adds route {@value #RESOURCE_URI} to the status page.
     *
     * The resources of the status page are located in the classpath
     * {@value #STATUS_PAGE_CLASSPATH}.
     *
     * @param router router
     */
    private void addStatusPage(final Router router) {
        LOG.traceEntry("Parameter : {}", new JsonMessage(router));

        final Directory directory = new Directory(
                getContext(),
                LocalReference.createClapReference(STATUS_PAGE_CLASSPATH)
        );
        directory.setDeeplyAccessible(true);
        router.attach(RESOURCE_URI, directory);

        LOG.traceExit();

    }

    /**
     * Adds route {@value #STATUS_URI} to the services describing the DataCite status.
     *
     * @param router router
     */
    private void addServicesStatus(final Router router) {
        LOG.traceEntry("Parameter : {}", new JsonMessage(router));

        final Redirector redirector = new Redirector(getContext(), TARGET_URL,
                Redirector.MODE_SERVER_OUTBOUND);

        final Filter authentication = new Filter() {
            /**
             * Adds the proxy authentication and handles the call by distributing it to the next
             * Restlet. If no Restlet is attached, then a Status.SERVER_ERROR_INTERNAL status is
             * returned. Returns Filter.CONTINUE by default.
             *
             * @param request request
             * @param response response
             * @return The continuation status. Either Filter.CONTINUE or Filter.STOP.
             */
            @Override
            protected int doHandle(final Request request, final Response response) {
                response.setLocationRef(TARGET_URL);
                response.setStatus(Status.REDIRECTION_PERMANENT);                
                return super.doHandle(request, response);
            }
        };
        authentication.setNext(redirector);
        router.attach(STATUS_URI, authentication);

        LOG.traceExit();
    }

    /**
     * Adds route {@value #STATS_URI} to the services giving the DataCite stats.
     *
     * @param router router
     */
    private void addServicesStats(final Router router) {
        LOG.traceEntry("Parameter : {}", new JsonMessage(router));

        final Redirector redirector = new Redirector(
                getContext(),
                TARGET_STATS_URL,
                Redirector.MODE_CLIENT_PERMANENT
        );

        final Filter authentication = new Filter() {
            /**
             * Adds the proxy authentication and handles the call by distributing it to the next
             * Restlet. If no Restlet is attached, then a Status.SERVER_ERROR_INTERNAL status is
             * returned. Returns Filter.CONTINUE by default.
             *
             * @param request request
             * @param response response
             * @return The continuation status. Either Filter.CONTINUE or Filter.STOP.
             */
            @Override
            protected int doHandle(final Request request, final Response response) {                   
                response.setLocationRef(TARGET_STATS_URL);
                response.setStatus(Status.REDIRECTION_PERMANENT);                
                return super.doHandle(request, response);
            }
        };
        authentication.setNext(redirector);
        router.attach(STATS_URI, authentication);

        LOG.traceExit();
    }

    /**
     * Adds default route to the website when it exists. The website must be located in the
     * {@value #JS_DIR} directory.
     *
     * @param router router
     */
    private void addRouteForWebSite(final Router router) {
        LOG.traceEntry("Parameter : {}", new JsonMessage(router));

        final String pathApp = this.getConfig().getPathApp();
        final File file = new File(pathApp + File.separator + JS_DIR);
        if (file.canRead()) {
            LOG.info("The website for DOI server is ready here {}", file.getPath());
            final Directory ihm = new Directory(getContext(), "file://" + file.getPath());
            ihm.setListingAllowed(true);
            ihm.setDeeplyAccessible(true);
            ihm.setIndexName("index");
            router.attachDefault(ihm);
        } else {
            LOG.warn("The website for DOI server is not installed");
        }
        LOG.traceExit();
    }

    /**
     * Creates a authorization based on the role. Only users attached to the role
     * {@link fr.cnes.doi.security.RoleAuthorizer#ROLE_ADMIN} are allowed
     *
     * @return the authorization that contains the access rights to the resources.
     */
    private RoleAuthorizer createRoleAuthorizer() {
        LOG.traceEntry();

        final RoleAuthorizer roleAuth = new RoleAuthorizer();
        roleAuth.setAuthorizedRoles(
                Arrays.asList(
                        Role.get(this, fr.cnes.doi.security.RoleAuthorizer.ROLE_ADMIN)
                )
        );

        return LOG.traceExit(roleAuth);
    }

    /**
     * Returns the token database.
     *
     * @return the token database
     */
    public AbstractTokenDBHelper getTokenDB() {
        return this.tokenDB;
    }

    /**
     * Returns the logger.
     *
     * @return the logger
     */
    @Override
    public Logger getLog() {
        return LOG;
    }

}
