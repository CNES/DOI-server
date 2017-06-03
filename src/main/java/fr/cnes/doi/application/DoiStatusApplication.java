/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

/**
 *
 * @author malapert
 */
public class DoiStatusApplication extends Application {
    
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        String target = "http://status.datacite.org";
        Redirector redirector = new Redirector(getContext(), target, Redirector.MODE_SERVER_OUTBOUND);
        router.attachDefault(redirector);
        return router;    
    }
    
}
