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

/**
 * Application to expose the web site.
 * @author Jean-Christophe
 */
public class WebSiteApplication extends BaseApplication {

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
