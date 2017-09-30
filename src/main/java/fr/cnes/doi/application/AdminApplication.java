/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Role;
import org.restlet.security.RoleAuthorizer;
import org.restlet.service.TaskService;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import fr.cnes.doi.resource.admin.SuffixProjectsResource;
import fr.cnes.doi.resource.admin.TokenResource;
import fr.cnes.doi.services.LandingPageMonitoring;
import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.security.AllowerIP;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.settings.ProxySettings;
import fr.cnes.doi.utils.spec.CoverageAnnotation;
import fr.cnes.doi.utils.spec.Requirement;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;

/**
 * Application to expose the web site.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
    reqId = Requirement.DOI_SRV_130,
    reqName = Requirement.DOI_SRV_130_NAME
    )
@Requirement(
    reqId = Requirement.DOI_SRV_140,
    reqName = Requirement.DOI_SRV_140_NAME
    )
@Requirement(
    reqId = Requirement.DOI_SRV_150,
    reqName = Requirement.DOI_SRV_150_NAME
    )
@Requirement(
    reqId = Requirement.DOI_SRV_160,
    reqName = Requirement.DOI_SRV_160_NAME
    )
@Requirement(
    reqId = Requirement.DOI_SRV_170,
    reqName = Requirement.DOI_SRV_170_NAME,
    coverage = CoverageAnnotation.NONE
    )
@Requirement(
    reqId = Requirement.DOI_DISPO_020,
    reqName = Requirement.DOI_DISPO_020_NAME
    )
public class AdminApplication extends AbstractApplication {
    
    /**
     * Application name.
     */
    public static final String NAME = "Admin Application";

    /**
     * URI to access to the resources of the system administration.
     */
    public static final String ADMIN_URI = "/admin";

    /**
     * URI to access to the resources of the status page.
     */
    public static final String RESOURCE_URI = "/resources";

    /**
     * Status page.
     */
    public static final String STATUS_URI = "/status";
    
    /**
     * Stats page.
     */
    public static final String STATS_URI = "/stats";    

    /**
     * URI to create a project suffix.
     */
    public static final String SUFFIX_PROJECT_URI = "/suffixProject";

    /**
     * URI to create a token.
     */
    public static final String TOKEN_URI = "/token";

    /**
     * Token template.
     */
    public static final String TOKEN_TEMPLATE = "tokenID";

    /**
     * URI to handle to get information from a token.
     */
    public static final String TOKEN_NAME_URI = "/{" + TOKEN_TEMPLATE + "}";    
    
    /**
     * Class name.
     */
    private static final String CLASS_NAME = AdminApplication.class.getName();     

    /**
     * Default directory where the web site is located.
     */
    private static final String JS_DIR = "js";

    /**
     * Location of the resources for the status page in the classpath.
     */
    private static final String STATUS_PAGE_CLASSPATH = "class/website";

    /**
     * The period between successive executions.
     */
    private static final int PERIOD_SCHEDULER = 30;

    /**
     * The time unit of the initialDelay and period parameters.
     */
    private static final TimeUnit PERIOD_UNIT = TimeUnit.DAYS;

    /**
     * DataCite Status page.
     */
    private static final String TARGET_URL = "http://status.datacite.org";
    
    /**
     * DataCite Stats page.
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
        getLogger().entering(CLASS_NAME, "Constructor");

        setName(NAME);
        setDescription("Provides an application for handling features related to "
                + "the administration system of the DOI server.");
        this.setTaskService(createTaskService());
        this.tokenDB = TokenSecurity.getInstance().getTOKEN_DB();

        getLogger().exiting(CLASS_NAME, "Constructor");
    }

    /**
     * A task checking status of landing pages each 15 days.
     *
     * @return A task
     */
    @Requirement(
            reqId = Requirement.DOI_DISPO_020,
            reqName = Requirement.DOI_DISPO_020_NAME
    )
    private TaskService createTaskService() {
        getLogger().entering(CLASS_NAME, "createTaskService");

        final TaskService checkLandingPageTask = new TaskService(true, true);
        getLogger().log(Level.INFO, "Sets CheckLandingPage running at each {0} {1}", new Object[]{PERIOD_SCHEDULER, PERIOD_UNIT});
        checkLandingPageTask.scheduleAtFixedRate(
                new LandingPageMonitoring(), 0, 
                PERIOD_SCHEDULER, PERIOD_UNIT
        );

        getLogger().exiting(CLASS_NAME, "createTaskService");
        return checkLandingPageTask;
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
        getLogger().entering(CLASS_NAME, "createInboundRoot");

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

        getLogger().exiting(CLASS_NAME, "createInboundRoot", router);

        return router;
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
     * @see TokenResource Resource to hnadle the token resource
     * @return the router
     */
    private Router createAdminRouter() {
        getLogger().entering(CLASS_NAME, "createAdminRouter");

        final Router router = new Router(getContext());
        router.attach(SUFFIX_PROJECT_URI, SuffixProjectsResource.class);
        router.attach(TOKEN_URI, TokenResource.class);
        router.attach(TOKEN_URI + TOKEN_NAME_URI, TokenResource.class);

        getLogger().exiting(CLASS_NAME, "createAdminRouter", router);

        return router;
    }

    /**
     * Creates a router for the web site resources. This router contains the
     * following resources:
     * <ul>
     * <li>{@link AdminApplication#RESOURCE_URI} to distribute the web resources
     * for the status page</li>
     * <li>the website resources attached by default when it is available</li>
     * </ul>
     * The website is located to {@link AdminApplication#JS_DIR} directory when
     * it is distributed by the DOI server.
     * @return The router for the public web site
     */
    private Router createWebSiteRouter() {
        getLogger().entering(CLASS_NAME, "createWebSiteRouter");

        final Router router = new Router(getContext());
        addStatusPage(router);
        addServicesStatus(router);
        addServicesStats(router);
        addRouteForWebSite(router);

        getLogger().exiting(CLASS_NAME, "createWebSiteRouter", router);

        return router;
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
        getLogger().entering(CLASS_NAME, "addStatusPage");

        final Directory directory = new Directory(getContext(), LocalReference.createClapReference(STATUS_PAGE_CLASSPATH));
        directory.setDeeplyAccessible(true);
        router.attach(RESOURCE_URI, directory);

        getLogger().exiting(CLASS_NAME, "addStatusPage");

    }

    /**
     * Adds route {@value #STATUS_URI} to the services describing the DataCite
     * status.
     *
     * @param router router
     */
    private void addServicesStatus(final Router router) {
        getLogger().entering(CLASS_NAME, "addServicesStatus");

        final Redirector redirector = new Redirector(getContext(), TARGET_URL, Redirector.MODE_SERVER_OUTBOUND);

        final Filter authentication = new Filter() {
            /**
             * Adds the proxy authentication and handles the call by distributing 
             * it to the next Restlet. If no Restlet is attached, then a 
             * Status.SERVER_ERROR_INTERNAL status is returned. 
             * Returns Filter.CONTINUE by default.
             * @param request request
             * @param response response
             * @return The continuation status. Either Filter.CONTINUE or Filter.STOP.
             */
            @Override
            protected int doHandle(final Request request, final Response response) {
                final ProxySettings proxySettings = ProxySettings.getInstance();
                if (proxySettings.isWithProxy()) {
                    request.setProxyChallengeResponse(proxySettings.getProxyAuthentication());
                }
                return super.doHandle(request, response);
            }
        };
        authentication.setNext(redirector);
        router.attach(STATUS_URI, authentication);

        getLogger().exiting(CLASS_NAME, "addServicesStatus");
    }
    
    /**
     * Adds route {@value #STATS_URI} to the services giving the DataCite
     * stats.
     *
     * @param router router
     */
    private void addServicesStats(final Router router) {
        getLogger().entering(CLASS_NAME, "addServicesStats");

        final Redirector redirector = new Redirector(
                getContext(), 
                TARGET_STATS_URL, 
                Redirector.MODE_CLIENT_PERMANENT
        );

        final Filter authentication = new Filter() {
            /**
             * Adds the proxy authentication and handles the call by distributing 
             * it to the next Restlet. If no Restlet is attached, then a 
             * Status.SERVER_ERROR_INTERNAL status is returned. 
             * Returns Filter.CONTINUE by default.
             * @param request request
             * @param response response
             * @return The continuation status. Either Filter.CONTINUE or Filter.STOP.
             */            
            @Override
            protected int doHandle(final Request request, final Response response) {
                final ProxySettings proxySettings = ProxySettings.getInstance();
                if (proxySettings.isWithProxy()) {
                    request.setProxyChallengeResponse(proxySettings.getProxyAuthentication());
                }
                return super.doHandle(request, response);
            }
        };
        authentication.setNext(redirector);
        router.attach(STATS_URI, authentication);

        getLogger().exiting(CLASS_NAME, "addServicesStatus");
    } 
    
    /**
     * Adds default route to the website when it exists. The website must be
     * located in the {@value #JS_DIR} directory.
     *
     * @param router router
     */
    private void addRouteForWebSite(final Router router) {
        getLogger().entering(CLASS_NAME, "addRouteForWebSite");

        final String pathApp = this.getConfig().getPathApp();
        final File file = new File(pathApp + File.separator + JS_DIR);
        if (file.canRead()) {
            getLogger().info(String.format("The website for DOI server is ready here %s", file.getPath()));
            final Directory ihm = new Directory(getContext(), "file://" + file.getPath());
            ihm.setListingAllowed(true);
            ihm.setDeeplyAccessible(true);
            ihm.setIndexName("index");
            router.attachDefault(ihm);
        } else {
            getLogger().info("The website for DOI server is not installed");
        }

        getLogger().exiting(CLASS_NAME, "addRouteForWebSite");
    }

    /**
     * Creates a authorization based on the role. Only users attached to the
     * role {@link fr.cnes.doi.security.RoleAuthorizer#ROLE_ADMIN} are allowed
     *
     * @return the authorization that contains the access rights to the
     * resources.
     */
    private RoleAuthorizer createRoleAuthorizer() {
        getLogger().entering(CLASS_NAME, "createRoleAuthorizer");

        final RoleAuthorizer roleAuth = new RoleAuthorizer();
        roleAuth.setAuthorizedRoles(
                Arrays.asList(
                        Role.get(this, fr.cnes.doi.security.RoleAuthorizer.ROLE_ADMIN)
                )
        );

        getLogger().exiting(CLASS_NAME, "createRoleAuthorizer", roleAuth);

        return roleAuth;
    }

    /**
     * Returns the token database.
     *
     * @return the token database
     */    
    public AbstractTokenDBHelper getTokenDB() {
        return this.tokenDB;
    }   

}
