/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import fr.cnes.doi.utils.CheckLandingPage;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.service.TaskService;

/**
 * Application to expose the web site.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class WebSiteApplication extends BaseApplication {
    
    public WebSiteApplication() {
        super();        
        this.setTaskService(createTaskService());
    }
    
    private TaskService createTaskService() {
        TaskService checkLandingPageTask = new TaskService(true, true);
        checkLandingPageTask.scheduleAtFixedRate(new CheckLandingPage(), 0, 1, TimeUnit.DAYS);                
        return checkLandingPageTask;
    }

    /**
     * Assigns a route to make online the website.
     * @return Router
     */
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        Directory directory = new Directory(getContext(), LocalReference.createClapReference("class/website"));       
        router.attach("/resources/", directory);        
        return router;
    }
            
}
