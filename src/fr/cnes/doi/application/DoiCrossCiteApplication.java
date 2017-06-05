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
import fr.cnes.doi.settings.ProxySettings;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * Application to get citation based on DOI metadata.
 * @author Jean-Christophe Malapert
 */
public class DoiCrossCiteApplication extends BaseApplication {
    
    private final ClientCrossCiteCitation client = new ClientCrossCiteCitation();
    
    public DoiCrossCiteApplication() {
        super();  
        client.setProxyAuthentication(this.proxySettings.getProxyAuthentication());
    }
    
    
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
