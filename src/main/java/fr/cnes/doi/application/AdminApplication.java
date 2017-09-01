/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import fr.cnes.doi.resource.admin.SuffixProjectsResource;
import fr.cnes.doi.resource.admin.TokenResource;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.server.monitoring.LandingPageMonitoring;
import fr.cnes.doi.utils.Requirement;
import fr.cnes.doi.db.TokenDB;
import fr.cnes.doi.security.TokenSecurity;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Role;
import org.restlet.security.RoleAuthorizer;
import org.restlet.service.TaskService;

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
    public static final String TOKEN_NAME_URI = "/{"+ TOKEN_TEMPLATE+ "}";
     
    /**
     * Token database.
     */
    private final TokenDB tokenDB;

    /**
     * Constructor.
     */
    public AdminApplication() {
        super();
        LOGGER.entering(AdminApplication.class.getName(), "Constructor");
        
        setName(NAME);
        setStatusService(new CnesStatusService());
        getServices().add(this.createCoreService(DEFAULT_CORS_ORIGIN, DEFAULT_CORS_CREDENTIALS));        
        this.setTaskService(createTaskService());
        this.tokenDB = TokenSecurity.getInstance().getTokenDB();
        
        LOGGER.exiting(AdminApplication.class.getName(), "Constructor");
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
        LOGGER.entering(AdminApplication.class.getName(), "createTaskService");
        
        TaskService checkLandingPageTask = new TaskService(true, true);
        LOGGER.log(Level.INFO, "Sets CheckLandingPage running at each {0} {1}", new Object[]{PERIOD_SCHEDULER, PERIOD_UNIT});
        checkLandingPageTask.scheduleAtFixedRate(new LandingPageMonitoring(), 0, PERIOD_SCHEDULER, PERIOD_UNIT);        
        
        LOGGER.exiting(AdminApplication.class.getName(), "createTaskService");
        return checkLandingPageTask;
    }      

    /**
     * Assigns a route to make online the website.
     *
     * @return Router
     */
    @Requirement(
            reqId = "DOI_IHM-*",
            reqName = "IHM web - IHM de création d'un DOI - génération de métadonnées - IHM par service REST"
                    + "IHM admin",
            comment = "TO DO here in /resources"
    )    
       
    @Override
    public Restlet createInboundRoot() {
        LOGGER.entering(AdminApplication.class.getName(), "createRouter");
        
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
        authorizer.setNext(adminRouter);
        
        Router router = new Router(getContext());         
        router.attachDefault(webSiteRouter);
        router.attach("/admin", ca);                 
       
        LOGGER.exiting(AdminApplication.class.getName(), "createRouter", router);

        return router;
    } 
    
    private Router createAdminRouter() {
        Router router = new Router(getContext());        
              
        router.attach(SUFFIX_PROJECT_URI, SuffixProjectsResource.class); 
        router.attach(TOKEN_URI, TokenResource.class);
        router.attach(TOKEN_URI+TOKEN_NAME_URI, TokenResource.class); 

        return router;
    }
    
    private Router createWebSiteRouter() {                
        Router router = new Router(getContext());
        
        Directory directory = new Directory(getContext(), LocalReference.createClapReference("class/website"));
        directory.setDeeplyAccessible(true);
        router.attach(RESOURCE_URI, directory);   
        
        String pathApp = DoiSettings.getInstance().getPathApp();
        File file = new File(pathApp+File.separator+JS_DIR);
        if (file.canRead()) {
            LOGGER.info(String.format("The GUI for DOI server is ready here %s", file.getPath()));
            Directory ihm = new Directory(getContext(), "file://"+file.getPath());        
            ihm.setListingAllowed(true);
            ihm.setDeeplyAccessible(true);
            ihm.setIndexName("index");            
            router.attachDefault(ihm);             
        } else {
            LOGGER.info("The GUI for DOI server is not installed");
        }        
        
        return router;
    }    
    
   private RoleAuthorizer createRoleAuthorizer() {
        //Authorize admin
        RoleAuthorizer roleAuth = new RoleAuthorizer();
        roleAuth.setAuthorizedRoles(Arrays.asList(Role.get(this, fr.cnes.doi.security.RoleAuthorizer.ROLE_ADMIN)));
        return roleAuth;
    }    
    
    /**
     * Returns the token database.
     * @return the token database
     */
    public TokenDB getTokenDB() {
        return this.tokenDB;
    }

}
