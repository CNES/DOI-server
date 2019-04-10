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
package fr.cnes.doi.application;

import fr.cnes.doi.services.DOIUsersUpdate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Directory;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Role;
import org.restlet.security.RoleAuthorizer;
import org.restlet.service.TaskService;

import fr.cnes.doi.logging.business.JsonMessage;
import fr.cnes.doi.resource.admin.AuthenticateResource;
import fr.cnes.doi.resource.admin.ConfigIhmResource;
import fr.cnes.doi.resource.admin.FooterIhmResource;
import fr.cnes.doi.resource.admin.ManageProjectsResource;
import fr.cnes.doi.resource.admin.ManageSuperUserResource;
import fr.cnes.doi.resource.admin.ManageSuperUsersResource;
import fr.cnes.doi.resource.admin.ManageUsersResource;
import fr.cnes.doi.resource.admin.RedirectingResource;
import fr.cnes.doi.resource.admin.SuffixProjectsDoisResource;
import fr.cnes.doi.resource.admin.SuffixProjectsResource;
import fr.cnes.doi.resource.admin.TokenResource;
import fr.cnes.doi.security.AllowerIP;
import fr.cnes.doi.services.LandingPageMonitoring;
import fr.cnes.doi.services.UpdateTokenDataBase;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.CoverageAnnotation;
import fr.cnes.doi.utils.spec.Requirement;
import org.restlet.routing.Template;

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
//TODO SRV 190
@Requirement(reqId = Requirement.DOI_SRV_190, reqName = Requirement.DOI_SRV_190_NAME,
        coverage = CoverageAnnotation.NONE)
@Requirement(reqId = Requirement.DOI_DISPO_020, reqName = Requirement.DOI_DISPO_020_NAME)
public final class AdminApplication extends AbstractApplication {

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
     * URI {@value #ROLE_USERS_URI} to check access rights.
     */
    public static final String ROLE_USERS_URI = "/roles/users";

    /**
     * URI {@value #ROLE_ADMIN_URI} to check admin access rights.
     */
    public static final String ROLE_ADMIN_URI = "/roles/admin";

    /**
     * URI {@value #SUFFIX_PROJECT_URI} to create a project suffix.
     */
    public static final String SUFFIX_PROJECT_URI = "/projects";

    /**
     * Project suffix template.
     */
    public static final String SUFFIX_PROJECT_NAME_TEMPLATE = "suffixProject";

    /**
     * URI {@value #SUFFIX_PROJECT_NAME} to manage a project suffix.
     */
    public static final String SUFFIX_PROJECT_NAME = "/{" + SUFFIX_PROJECT_NAME_TEMPLATE + "}";

    /**
     * URI {@value #DOIS_URI} to get dois from a specific project.
     */
    public static final String DOIS_URI = "/dois";

    /**
     * URI {@value #USERS_URI} to handle users.
     */
    public static final String USERS_URI = "/users";

    /**
     * URI {@value #SUPERUSERS_URI} to handle superusers.
     */
    public static final String SUPERUSERS_URI = "/superusers";

    /**
     * Template user.
     */
    public static final String USERS_NAME_TEMPLATE = "userName";

    /**
     * URI {@value #USERS_NAME} to handle user.
     */
    public static final String USERS_NAME = "/{" + USERS_NAME_TEMPLATE + "}";

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
     * URI {@value #IHM_URI} where the web site is located.
     */
    public static final String IHM_URI = "/ihm";

    /**
     * URI {@value #IHM_CONFIG_URI} where the configuration file is located.
     */
    private static final String IHM_CONFIG_URI = "/js/config.js";

    /**
     * URI {@value #IHM_FOOTER_URI} where the footer file is located.
     */
    private static final String IHM_FOOTER_URI = "/footer.txt";

    /**
     * Location of the resources for the status page in the classpath.
     */
    private static final String STATUS_PAGE_CLASSPATH = "class/website";

    /**
     * Location of the resources for the IHM in the classpath.
     */
    private static final String IHM_CLASSPATH = "class/ihm";
    
    /**
     * Location of the resources for the API docs in the classpath.
     */
    private static final String API_CLASSPATH = "class/docs";    

    /**
     * The period between successive executions : {@value #PERIOD_SCHEDULER}.
     */
    private static final int PERIOD_SCHEDULER = 30;

    /**
     * The period between successive executions : {@value #PERIOD_SCHEDULER_FOR_TOKEN_DB}.
     */
    private static final int PERIOD_SCHEDULER_FOR_TOKEN_DB = 1;

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
     * Constructor.
     */
    public AdminApplication() {
        super();
        init();
    }

    /**
     * Defines services and metadata.
     */
    private void init() {
        setName(NAME);
        setDescription("Provides an application for handling features related to "
                + "the administration system of the DOI server.");
        this.setTaskService(createTaskService());
        this.setTaskService(createUpdateDataBaseTaskService());
        this.setTaskService(periodicalyDeleteExpiredTokenFromDB());
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
     * A task removing expired tokens in data base each days.
     *
     * @return A task
     */
    private TaskService periodicalyDeleteExpiredTokenFromDB() {
        LOG.traceEntry();
        final TaskService checkExpiredTokenTask = new TaskService(true);
        LOG.info("Sets checkExpiredTokenTask running at each {} {}", PERIOD_SCHEDULER_FOR_TOKEN_DB,
                PERIOD_UNIT);
        checkExpiredTokenTask.scheduleAtFixedRate(
                new UpdateTokenDataBase(), 0,
                PERIOD_SCHEDULER_FOR_TOKEN_DB, PERIOD_UNIT
        );
        return LOG.traceExit(checkExpiredTokenTask);
    }

    /**
     * A task updating DOI users database from authentication service at each configurable period of
     * time.
     *
     * @return A task
     */
    private TaskService createUpdateDataBaseTaskService() {
        LOG.traceEntry();
        final TaskService updateDataBaseTask = new TaskService(true);
        LOG.info("Sets UpdateDataBaseTask running at each {} {}", PERIOD_SCHEDULER, PERIOD_UNIT);
        updateDataBaseTask.scheduleAtFixedRate(
                new DOIUsersUpdate(), 0,
                DoiSettings.getInstance().getInt(Consts.DB_UPDATE_JOB_PERIOD), TimeUnit.MINUTES
        );
        return LOG.traceExit(updateDataBaseTask);
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
        final ChallengeAuthenticator challAuth = createAuthenticatorLoginBased();
        challAuth.setOptional(true);

        //   - authentication with token
        final ChallengeAuthenticator challTokenAuth = createTokenAuthenticator();
        challTokenAuth.setOptional(false);
        //if already authenticate 
        challTokenAuth.setMultiAuthenticating(false);

        // Defines the authorization
        final RoleAuthorizer authorizer = createRoleAuthorizer();

        // Defines the admin router as private
        final String ips = DoiSettings.getInstance().getString(Consts.ADMIN_IP_ALLOWER);
        final boolean isEnabled = ips != null;
        final AllowerIP blocker = new AllowerIP(getContext(), isEnabled);

        // Defines the routers
        final Router webSiteRouter = createWebSiteRouter();
        final Router adminRouter = createAdminRouter();

        // pipeline of authentication and authorization
        challAuth.setNext(challTokenAuth);
        challTokenAuth.setNext(authorizer);
        authorizer.setNext(blocker);
        blocker.setNext(adminRouter);

        final Router router = new Router(getContext());
        router.attachDefault(webSiteRouter);

        router.attach(ADMIN_URI, challAuth);

        return LOG.traceExit(router);
    }

    /**
     * Creates a authorization based on the role. Only users attached to the role
     * {@link fr.cnes.doi.security.RoleAuthorizer#ROLE_ADMIN} are allowed
     *
     * @return the authorization that contains the access rights to the resources.
     */
    private RoleAuthorizer createRoleAuthorizer() {
        LOG.traceEntry();

        final RoleAuthorizer roleAuth = new RoleAuthorizer() {

            /**
             * Cancel verification on pre-flight OPTIONS method
             *
             * @param request request
             * @param response response
             * @return the result
             */
            @Override
            public int beforeHandle(final Request request, final Response response) {
                final Method requestMethod = request.getMethod();
                final Reference requestReference = request.getOriginalRef();
                final String lastSeg = requestReference.getLastSegment();
                final String path = requestReference.getPath();
                boolean ignoreVerification = false;

                if (requestMethod.equals(Method.OPTIONS)) {
                    // options method is allowed independently of the role
                    // just need to be authenticated
                    ignoreVerification = true;
                } else if (!requestMethod.equals(Method.DELETE) && !requestMethod.equals(Method.GET)
                        && requestReference.toString().contains(ADMIN_URI + TOKEN_URI)) {
                    // token resource is allowed independently of the role
                    // just need to be authenticated because we want to access to the 
                    //authentication page of the GUI without authentication
                    ignoreVerification = true;
                } else if (requestMethod.equals(Method.GET)) {
                    // no check of the role when getting the list of projects, superusers and dois 
                    if ("projects".equals(lastSeg) && requestReference.hasQuery()) {
                        ignoreVerification = true;
                    }
                    // ignore method GET dois from project
                    if ("dois".equals(lastSeg)) {
                        ignoreVerification = true;
                    }
                    // Checks authentication for "users" group
                    if (path.contains(ADMIN_URI + ROLE_USERS_URI)) {
                        ignoreVerification = true;
                    }
                }

                final int status;
                if (ignoreVerification) {
                    response.setStatus(Status.SUCCESS_OK);
                    status = CONTINUE;
                } else {
                    status = super.beforeHandle(request, response);
                }
                return status;
            }

        };
        roleAuth.setAuthorizedRoles(
                Arrays.asList(
                        Role.get(this, fr.cnes.doi.security.RoleAuthorizer.ROLE_ADMIN)
                )
        );

        return LOG.traceExit(roleAuth);
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

        router.attach(SUFFIX_PROJECT_URI + SUFFIX_PROJECT_NAME, ManageProjectsResource.class);
        router.attach(SUFFIX_PROJECT_URI + SUFFIX_PROJECT_NAME + DOIS_URI,
                SuffixProjectsDoisResource.class);

        router.attach(SUFFIX_PROJECT_URI + SUFFIX_PROJECT_NAME + USERS_URI,
                ManageUsersResource.class);
        router.attach(SUFFIX_PROJECT_URI + SUFFIX_PROJECT_NAME + USERS_URI + USERS_NAME,
                ManageUsersResource.class);

        router.attach(SUPERUSERS_URI, ManageSuperUsersResource.class);
        router.attach(SUPERUSERS_URI + USERS_NAME, ManageSuperUserResource.class);

        router.attach(TOKEN_URI, TokenResource.class);
        router.attach(TOKEN_URI + TOKEN_NAME_URI, TokenResource.class);

        router.attach(ROLE_USERS_URI, AuthenticateResource.class);
        router.attach(ROLE_ADMIN_URI, AuthenticateResource.class);

        return LOG.traceExit(router);
    }

    /**
     * Creates a router for the web site resources. This router contains the following resources:
     * <ul>
     * <li>{@link AdminApplication#RESOURCE_URI} to distribute the web resources for the status
     * page</li>
     * <li>the website resources attached by default when it is available</li>
     * </ul>
     * The website is located to {@link AdminApplication#IHM_URI} directory when it is
     * distributed by the DOI server.
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
        LOG.traceEntry("Parameter\n router: {}", new JsonMessage(router));

        final Directory directory = new Directory(
                getContext(),
                LocalReference.createClapReference(STATUS_PAGE_CLASSPATH)
        );
        directory.setDeeplyAccessible(true);
        router.attach(RESOURCE_URI, directory);

        LOG.traceExit();
    }

    /**
     * Adds route attacURI to the target according to redirection mode.
     *
     * @param router router
     * @param redirectorMode redirection mode
     * @param target target
     * @param attachURI attachURI
     */
    private void addServices(final Router router, final int redirectorMode, final String target,
            final String attachURI) {
        LOG.traceEntry("Parameters\n   router: {}\n   redirectorMode: {}\n   "
                + "target: {}\n  attachURI: {}",
                new JsonMessage(router), redirectorMode, target, attachURI);

        final Redirector redirector = new Redirector(getContext(), target, redirectorMode);

        final Filter authentication = new Filter() {
            /**
             * {@inheritDoc }
             */
            @Override
            protected int doHandle(final Request request, final Response response) {
                response.setLocationRef(target);
                response.setStatus(Status.REDIRECTION_PERMANENT);
                return super.doHandle(request, response);
            }
        };
        authentication.setNext(redirector);
        router.attach(attachURI, authentication);
        LOG.traceExit();
    }

    /**
     * Adds route {@value #STATUS_URI} to the services describing the DataCite status.
     *
     * @param router router
     */
    private void addServicesStatus(final Router router) {
        this.addServices(router, Redirector.MODE_SERVER_OUTBOUND, TARGET_URL, STATUS_URI);
    }

    /**
     * Adds route {@value #STATS_URI} to the services giving the DataCite stats.
     *
     * @param router router
     */
    private void addServicesStats(final Router router) {
        this.addServices(router, Redirector.MODE_CLIENT_PERMANENT, TARGET_STATS_URL, STATS_URI);
    }

    /**
     * Adds default route to the website when it exists. The website must be located in the
     * {@value #IHM_URI} directory.
     *
     * @param router router
     */
    private void addRouteForWebSite(final Router router) {
        LOG.traceEntry("Parameter : {}", new JsonMessage(router));

        final Directory ihm = new Directory(
                getContext(),
                LocalReference.createClapReference(IHM_CLASSPATH)
        );
        ihm.setListingAllowed(false);
        ihm.setDeeplyAccessible(true);
        ihm.setIndexName("authentication");
       
        router.attach(IHM_URI, RedirectingResource.class, Template.MODE_EQUALS);
        router.attach(IHM_URI + "/", RedirectingResource.class, Template.MODE_EQUALS);        
        router.attach(IHM_URI + IHM_FOOTER_URI, FooterIhmResource.class);
        router.attach(IHM_URI + IHM_CONFIG_URI, ConfigIhmResource.class);
        router.attach(IHM_URI, ihm);
        
        LOG.traceExit();
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
