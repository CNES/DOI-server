/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

/**
 *
 * @author malapert
 */
public class WebSiteApplication extends Application {

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        Directory directory = new Directory(getContext(), LocalReference.createClapReference("class/resources"));       
        router.attach("/resources/", directory);        
        return router;
    }
    
    
    
}
