/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import fr.cnes.doi.client.ClientCrossCiteCitation;
import fr.cnes.doi.resource.FormatCitationResource;
import fr.cnes.doi.resource.LanguageCitationResource;
import fr.cnes.doi.resource.StyleCitationResource;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 *
 * @author malapert
 */
public class DoiCrossCiteApplication extends Application {
    
    private final ClientCrossCiteCitation client = new ClientCrossCiteCitation();    
    
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/style", StyleCitationResource.class);
        router.attach("/language", LanguageCitationResource.class);
        router.attach("/format", FormatCitationResource.class);
        return router;
    }
    
    public ClientCrossCiteCitation getClient() {
        return this.client;
    }
    
}
