/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.application.DoiCrossCiteApplication;
import java.util.List;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Get Styles for citation.
 * @author Jean-Christophe Malapert
 */
public class StyleCitationResource extends ServerResource {
    private DoiCrossCiteApplication app;

    @Override
    protected void doInit() throws ResourceException {
        this.app = (DoiCrossCiteApplication) getApplication();
    }
                           
    @Get
    public List<String> getStyles() {
        return this.app.getClient().getStyles();
    }    
}
