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
import fr.cnes.doi.utils.Utils;
import java.util.logging.Logger;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * Provides an application to get citation from a registered DOI.
 * A citation is typically used in scientific publications.
 * The citation is formatted according to the selected language and format.
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class DoiCrossCiteApplication extends BaseApplication {
    
    /**
     * URI to get the styles, which are used to format the citation.
     */
    public static final String STYLES_URI = "/style";
    /**
     * URI to get the languages, which are used to format the citation.
     */
    public static final String LANGUAGE_URI = "/language";
    /**
     * Retrieves the citation.
     */
    public static final String FORMAT_URI = "/format";
    
    /**
     * Application logger.
     */
    private static final Logger LOGGER = Utils.getAppLogger();    
    
    /**
     * Client to query CrossCite.
     */
    private final ClientCrossCiteCitation client = new ClientCrossCiteCitation();
    
    /**
     * Constructs the application by setting the proxy authentication
     * to the ClientCrossCiteCitation proxy when the configuration is set.
     */
    public DoiCrossCiteApplication() {
        super();  
        LOGGER.entering(DoiCrossCiteApplication.class.getName(), "Constructor");
        
        if(this.proxySettings.isWithProxy()) {
            LOGGER.fine("Setting the proxy authentication");
            client.setProxyAuthentication(this.proxySettings.getProxyAuthentication());
        } else {
            LOGGER.fine("No proxy authentication to set");
        }
        
        LOGGER.exiting(DoiCrossCiteApplication.class.getName(), "Constructor");
    }
    
    /**
     * Assigns routes.
     * @return router
     */
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach(STYLES_URI, StyleCitationResource.class);
        router.attach(LANGUAGE_URI, LanguageCitationResource.class);
        router.attach(FORMAT_URI, FormatCitationResource.class);
        return router;
    }
    
    /**
     * Returns the client to query cross cite.
     * @return the client
     */
    public ClientCrossCiteCitation getClient() {
        return this.client;
    }
    
}
