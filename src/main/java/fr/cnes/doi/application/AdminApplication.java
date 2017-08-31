/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import fr.cnes.doi.resource.admin.SuffixProjectsResource;
import fr.cnes.doi.resource.admin.TokenResource;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.CheckLandingPage;
import fr.cnes.doi.utils.Requirement;
import fr.cnes.doi.db.TokenDB;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.utils.Utils;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.LocalReference;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MemoryRealm;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.restlet.service.CorsService;
import org.restlet.service.TaskService;

/**
 * Application to expose the web site.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class AdminApplication extends BaseApplication {
    
    /**
     * Application logger.
     */
    private static final Logger LOGGER = Utils.getAppLogger();   
    private static final String JS_DIR = "js";
    public static final String NAME = "Admin Application";
    private final TokenDB tokenDB;

    public AdminApplication() {
        super();
        setName(NAME);
        setStatusService(new CnesStatusService());
        getServices().add(createCoreService());        
        this.setTaskService(createTaskService());
        this.tokenDB = TokenSecurity.getInstance().getTokenDB();
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
        TaskService checkLandingPageTask = new TaskService(true, true);
        checkLandingPageTask.scheduleAtFixedRate(new CheckLandingPage(), 0, 30, TimeUnit.DAYS);
        return checkLandingPageTask;
    }
    
    private CorsService createCoreService() {
        LOGGER.entering(AdminApplication.class.getName(), "createCoreService");

        CorsService corsService = new CorsService();
        corsService.setAllowedOrigins(new HashSet(Arrays.asList("*")));
        corsService.setAllowedCredentials(true);

        LOGGER.exiting(AdminApplication.class.getName(), "createCoreService", corsService);
        return corsService;
    }    
    
    private ChallengeAuthenticator createAuthenticator() {
        LOGGER.entering(AdminApplication.class.getName(), "createAuthenticator");

        ChallengeAuthenticator guard = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "realm");

        // Create in-memory users with roles
        MemoryRealm realm = new MemoryRealm();
        User user = new User("jcm", "user");
        realm.getUsers().add(user);
        realm.map(user, Role.get(this, "admin"));
        
        // Attach verifier to check authentication and enroler to determine
        // roles
        guard.setVerifier(realm.getVerifier());
        guard.setEnroler(realm.getEnroler());

        LOGGER.exiting(AdminApplication.class.getName(), "createAuthenticator", guard);

        return guard;
    }     
    
//    private RoleAuthorizer createRoleAuthorizer() {
//        //Authorize owners and forbid users on roleAuth's children
//        RoleAuthorizer roleAuth = new RoleAuthorizer();
//        roleAuth.getAuthorizedRoles().add(Role.get(this, "admin"));
//        return roleAuth;
//    }
    
    
   

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

        Router router = new Router(getContext());        
        Directory directory = new Directory(getContext(), LocalReference.createClapReference("class/website"));
        directory.setDeeplyAccessible(true);
        router.attach("/resources", directory);                
        router.attach("/suffixProject", SuffixProjectsResource.class); 
        router.attach("/token", TokenResource.class);
        router.attach("/token/{tokenID}", TokenResource.class);
        
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

        LOGGER.exiting(AdminApplication.class.getName(), "createRouter", router);

        return router;
    }    
    
    public TokenDB getTokenDB() {
        return this.tokenDB;
    }

}
