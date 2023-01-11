/*
 * Copyright (C) 2017-2021 Centre National d'Etudes Spatiales (CNES).
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ClientInfo;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Directory;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Role;
import org.restlet.security.RoleAuthorizer;

import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.logging.business.JsonMessage;
import fr.cnes.doi.resource.admin.AuthenticateResource;
import fr.cnes.doi.resource.admin.ManageProjectUsersResource;
import fr.cnes.doi.resource.admin.ManageProjectsResource;
import fr.cnes.doi.resource.admin.ManageSuperUserResource;
import fr.cnes.doi.resource.admin.ManageSuperUsersResource;
import fr.cnes.doi.resource.admin.ManageUsersResource;
import fr.cnes.doi.resource.admin.RedirectingResource;
import fr.cnes.doi.resource.admin.SuffixProjectsDoisResource;
import fr.cnes.doi.resource.admin.SuffixProjectsResource;
import fr.cnes.doi.resource.admin.TokenResource;
import fr.cnes.doi.security.AllowerIP;
import fr.cnes.doi.services.DOIUsersUpdate;
import fr.cnes.doi.services.LandingPageMonitoring;
import fr.cnes.doi.services.UpdateTokenDataBase;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.CoverageAnnotation;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Provides an application for handling features related to the administration
 * system of the DOI server.
 *
 * The administration application provides the following features:
 * <ul>
 * <li>An asynchronous task to check the availability of created landing pages 
 * each {@value #PERIOD_SCHEDULER_FOR_LANDINGPAGE} days</li>
 * <li>An asynchronous task to update users from auth services 
 * (period in configuration file)</li>
 * <li>An asynchronous task to check the expired tokens 
 * each {@value #PERIOD_SCHEDULER_FOR_TOKEN_DB} days</li>
 * <li>The form for creating DOI</li>
 * <li>The Datacite status page to check Datacite services availability</li>
 * <li>The Datacite stats page</li>
 * <li>A service to create random string as a part of the DOI suffix</li>
 * <li>A token service for creating token and to get token information</li>
 * </ul>
 * <p>
 * <b>Security</b><br>
 * --------------<br>
 * The authentication is done by a simple login/password. Only users having the
 * group "admin" will be allowed to log in to this application.<br>
 * The website for creating DOI is opened on the net whereas the others services
 * are filtered by IP. The allowed IPs are localhost and the IPs defined in
 * {@value fr.cnes.doi.settings.Consts#ADMIN_IP_ALLOWER} attribute from the
 * configuration file
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
 * |_|____________|____ token/{tokenID} (Get token information -
 * authorization)<br>
 *
 * @see <a href="http://status.datacite.org">Datacite status page</a>
 * @see <a href="https://stats.datacite.org/#tab-prefixes">Datacite stats
 * page</a>
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
     * URI {@value #ADMIN_URI} to access to the resources of the system
     * administration.
     */
    public static final String ADMIN_URI = "/admin";

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
     * URI {@value #IHM_URI} where the web site is located.
     */
    public static final String IHM_URI = "/ihm";
    
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(AdminApplication.class.getName());

    /**
     * Location of the resources for the IHM in the classpath.
     */
    private static final String IHM_CLASSPATH = "class/ihm";

    /**
     * The period between successive executions : {@value #PERIOD_SCHEDULER_FOR_LANDINGPAGE}.
     */
    private static final int PERIOD_SCHEDULER_FOR_LANDINGPAGE = 1;

    /**
     * The period between successive executions :
     * {@value #PERIOD_SCHEDULER_FOR_TOKEN_DB}.
     */
    private static final int PERIOD_SCHEDULER_FOR_TOKEN_DB = 1;

    /**
     * The time unit of the initialDelay and period parameters.
     */
    private static final TimeUnit PERIOD_UNIT = TimeUnit.DAYS;
    
    /**
     * ClientMDS
     */
    private final ClientMDS client;

    /**
     * Constructor.
     * @param client Client MDS
     */
    public AdminApplication(final ClientMDS client) {
        super();
        this.client = client;
        init();
    }

    /**
     * Defines services and metadata.
     */
    @Requirement(reqId = Requirement.DOI_DISPO_020, reqName = Requirement.DOI_DISPO_020_NAME)
    private void init() {
        LOG.traceEntry();
        setName(NAME);
        setDescription(
                "Provides an application for handling features related to "
                        + "the administration system of the DOI server.");

        // Create a pool executor with 3 tasks
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

        // 1 - Check landing pages
        LOG.info("Sets CheckLandingPage running at each {} {}",
                PERIOD_SCHEDULER_FOR_LANDINGPAGE, PERIOD_UNIT);
        executor.scheduleAtFixedRate(new LandingPageMonitoring(this.client), 0,
                PERIOD_SCHEDULER_FOR_LANDINGPAGE, PERIOD_UNIT);

        // 2 - Check users from auth service
        LOG.info("Sets UpdateDataBaseTask running at each {} {}",
                DoiSettings.getInstance().getInt(Consts.DB_UPDATE_JOB_PERIOD),
                TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(new DOIUsersUpdate(), 0,
                DoiSettings.getInstance().getInt(Consts.DB_UPDATE_JOB_PERIOD),
                TimeUnit.MINUTES);

        // 3 - Check expired token
        LOG.info("Sets checkExpiredTokenTask running at each {} {}",
                PERIOD_SCHEDULER_FOR_TOKEN_DB, PERIOD_UNIT);
        executor.scheduleAtFixedRate(new UpdateTokenDataBase(), 0,
                PERIOD_SCHEDULER_FOR_TOKEN_DB, PERIOD_UNIT);

        getMetadataService().addExtension("xsd", MediaType.TEXT_XML, true);
        LOG.traceExit();
    }

    /**
     * Creates a router for the AdminApplication. This router routes :
     * <ul>
     * <li>the web resources for the website with no authentication</li>
     * <li>the REST resources for the system administration with
     * authentication/authorization</li>
     * </ul>
     * The web resources are attached by default to the AdminApplication where
     * as the REST resources for the system administration are attached with the
     * {@link AdminApplication#ADMIN_URI}
     *
     * @see AdminApplication#createWebSiteRouter() the router that contains the
     * the web resources
     * @see AdminApplication#createAdminRouter() the router that contains the
     * REST resources
     * @see AdminApplication#createAuthenticator() the authentication mechanism
     * @see AdminApplication#createRoleAuthorizer() the authorization mechanism
     * @return Router
     */
    @Override
    public Restlet createInboundRoot() {
        LOG.traceEntry();
        
        final Filter logContext = new Filter() {
            /**
             * Get the IP client from proxy (if there is one) and set it in
             * the header.
             * 
             * @param request request
             * @param response response
             * @return 0
             */
            @Override
            protected int beforeHandle(final Request request, final Response response) {
                final ClientInfo clientInfo = request.getClientInfo();
                final String ipAddress = request.getHeaders().getFirstValue(
                        Consts.PROXIFIED_IP, clientInfo.getUpstreamAddress()
                );
                ThreadContext.put(Consts.LOG_IP_ADDRESS, ipAddress);
                return Filter.CONTINUE;
            }
        }; 
        
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
        
        logContext.setNext(router);        

        return LOG.traceExit(logContext);
    }

    /**
     * Creates a authorization based on the role. Only users attached to the
     * role {@link fr.cnes.doi.security.RoleAuthorizer#ROLE_ADMIN} are allowed
     *
     * @return the authorization that contains the access rights to the
     * resources.
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
     * Creates a router for REST services for the system administration. This
     * router contains the following resources :
     * <ul>
     * <li>{@link AdminApplication#SUFFIX_PROJECT_URI} resource to handle the
     * project suffix, which is used in the Digital Object Identifier</li>
     * <li>{@link AdminApplication#TOKEN_URI} resource to handle the token for
     * the authentication</li>
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
                ManageProjectUsersResource.class);
        router.attach(SUFFIX_PROJECT_URI + SUFFIX_PROJECT_NAME + USERS_URI + USERS_NAME,
                ManageProjectUsersResource.class);
        
        router.attach(USERS_URI, ManageUsersResource.class);

        router.attach(SUPERUSERS_URI, ManageSuperUsersResource.class);
        router.attach(SUPERUSERS_URI + USERS_NAME, ManageSuperUserResource.class);

        router.attach(TOKEN_URI, TokenResource.class);
        router.attach(TOKEN_URI + TOKEN_NAME_URI, TokenResource.class);

        router.attach(ROLE_USERS_URI, AuthenticateResource.class);
        router.attach(ROLE_ADMIN_URI, AuthenticateResource.class);

        return LOG.traceExit(router);
    }

    /**
     * Creates a router for the web site resources. This router contains the
     * following resources:
     * <ul>
     * <li>the website resources attached by default when it is available</li>
     * </ul>
     * The website is located to {@link AdminApplication#IHM_URI} directory when
     * it is distributed by the DOI server.
     *
     * @return The router for the public web site
     */
    private Router createWebSiteRouter() {
        LOG.traceEntry();

        final Router router = new Router(getContext());
        addRouteForWebSite(router);

        return LOG.traceExit(router);
    }

    /**
     * Adds default route to the website when it exists. The website must be
     * located in the {@value #IHM_URI} directory.
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

        // Redirecting to the default page (dashboard)
        router.attach(IHM_URI, RedirectingResource.class, Template.MODE_EQUALS);
        router.attach(IHM_URI + "/", RedirectingResource.class, Template.MODE_EQUALS);
        router.attach(IHM_URI + "/login", RedirectingResource.class);
        router.attach(IHM_URI + "/dashboard", RedirectingResource.class);
        router.attach(IHM_URI + "/creation-token", RedirectingResource.class);
        router.attach(IHM_URI + "/administration", RedirectingResource.class);
        router.attach(IHM_URI + "/citation", RedirectingResource.class);
        router.attach(IHM_URI + "/doi-mgmt", RedirectingResource.class, Template.MODE_STARTS_WITH);
        
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


    /**
     * Returns only the dois within the specified project from the search
     * result.
     *
     * @param idProject project ID
     * @return the search result
     * @throws fr.cnes.doi.exception.ClientMdsException it happens when a problem
     * happens with Datacite
     */
    public List<String> getDois(final String idProject) throws ClientMdsException {
        return this.client.getDois(idProject);
    }    
}
