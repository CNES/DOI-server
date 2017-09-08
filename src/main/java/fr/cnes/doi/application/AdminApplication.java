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
import fr.cnes.doi.utils.Requirement;
import fr.cnes.doi.db.TokenDB;
import fr.cnes.doi.security.AllowerIP;
import fr.cnes.doi.security.TokenSecurity;

/**
 * Application to expose the web site.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class AdminApplication extends BaseApplication {

    /**
     * Default directory where the web site is located.
     */
    private static final String JS_DIR = "js";

    /**
     * The period between successive executions.
     */
    private static final int PERIOD_SCHEDULER = 30;

    /**
     * The time unit of the initialDelay and period parameters.
     */
    private static final TimeUnit PERIOD_UNIT = TimeUnit.DAYS;

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
     * Token database.
     */
    private final TokenDB tokenDB;

    /**
     * Constructor.
     */
    public AdminApplication() {
        super();
        getLogger().entering(AdminApplication.class.getName(), "Constructor");

        setName(NAME);
        this.setTaskService(createTaskService());
        this.tokenDB = TokenSecurity.getInstance().getTokenDB();

        getLogger().exiting(AdminApplication.class.getName(), "Constructor");
    }

    /**
     * A task checking status of landing pages each 15 days.
     *
     * @return A task
     */
    @Requirement(
            reqId = "DOI_DISPO_020",
            reqName = "Vérification des landing pages"
    )
    private TaskService createTaskService() {
        getLogger().entering(AdminApplication.class.getName(), "createTaskService");

        TaskService checkLandingPageTask = new TaskService(true, true);
        getLogger().log(Level.INFO, "Sets CheckLandingPage running at each {0} {1}", new Object[]{PERIOD_SCHEDULER, PERIOD_UNIT});
        checkLandingPageTask.scheduleAtFixedRate(new LandingPageMonitoring(), 0, PERIOD_SCHEDULER, PERIOD_UNIT);

        getLogger().exiting(AdminApplication.class.getName(), "createTaskService");
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
        getLogger().entering(AdminApplication.class.getName(), "createInboundRoot");

        // Defines the strategy of authentication (authentication is not required)
        //   - authentication with login/pwd
        ChallengeAuthenticator ca = createAuthenticator();
        ca.setOptional(false);

        // Defines the authorization
        RoleAuthorizer authorizer = createRoleAuthorizer();

        // pipeline of authentication and authorization
        ca.setNext(authorizer);

        // Defines the routers
        Router webSiteRouter = createWebSiteRouter();
        Router adminRouter = createAdminRouter();

        // Defines the admin router as private
        AllowerIP blocker = new AllowerIP(getContext());        
        authorizer.setNext(blocker);
        blocker.setNext(adminRouter);
        
        Router router = new Router(getContext());
        router.attachDefault(webSiteRouter);
        router.attach(ADMIN_URI, ca);

        getLogger().exiting(AdminApplication.class.getName(), "createInboundRoot", router);

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
        getLogger().entering(AdminApplication.class.getName(), "createAdminRouter");

        Router router = new Router(getContext());
        router.attach(SUFFIX_PROJECT_URI, SuffixProjectsResource.class);
        router.attach(TOKEN_URI, TokenResource.class);
        router.attach(TOKEN_URI + TOKEN_NAME_URI, TokenResource.class);

        getLogger().exiting(AdminApplication.class.getName(), "createAdminRouter", router);

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
     */
    @Requirement(
            reqId = "DOI_IHM-*",
            reqName = "IHM web - IHM de création d'un DOI - génération de métadonnées - IHM par service REST"
            + "IHM admin",
            comment = "TO DO here in /resources"
    )
    private Router createWebSiteRouter() {
        getLogger().entering(AdminApplication.class.getName(), "createWebSiteRouter");

        Router router = new Router(getContext());

        Directory directory = new Directory(getContext(), LocalReference.createClapReference("class/website"));
        directory.setDeeplyAccessible(true);
        router.attach(RESOURCE_URI, directory);

        String pathApp = this.config.getPathApp();
        File file = new File(pathApp + File.separator + JS_DIR);
        if (file.canRead()) {
            getLogger().info(String.format("The website for DOI server is ready here %s", file.getPath()));
            Directory ihm = new Directory(getContext(), "file://" + file.getPath());
            ihm.setListingAllowed(true);
            ihm.setDeeplyAccessible(true);
            ihm.setIndexName("index");
            router.attachDefault(ihm);
        } else {
            getLogger().info("The website for DOI server is not installed");
        }

        getLogger().exiting(AdminApplication.class.getName(), "createWebSiteRouter", router);

        return router;
    }

    /**
     * Creates a authorization based on the role. Only users attached to the
     * role {@link fr.cnes.doi.security.RoleAuthorizer#ROLE_ADMIN} are allowed
     *
     * @return the authorization that contains the access rights to the
     * resources.
     */
    private RoleAuthorizer createRoleAuthorizer() {
        getLogger().entering(AdminApplication.class.getName(), "createRoleAuthorizer");

        RoleAuthorizer roleAuth = new RoleAuthorizer();
        roleAuth.setAuthorizedRoles(Arrays.asList(Role.get(this, fr.cnes.doi.security.RoleAuthorizer.ROLE_ADMIN)));

        getLogger().exiting(AdminApplication.class.getName(), "createRoleAuthorizer", roleAuth);

        return roleAuth;
    }

    /**
     * Returns the token database.
     *
     * @return the token database
     */
    public TokenDB getTokenDB() {
        return this.tokenDB;
    }

}
