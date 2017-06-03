/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.application.DoiCrossCiteApplication;
import fr.cnes.doi.client.ClientCrossCiteCitation;
import java.util.List;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 *
 * @author malapert
 */
public class LanguageCitationResource extends ServerResource {
    
    private DoiCrossCiteApplication app;

    @Override
    protected void doInit() throws ResourceException {
        this.app = (DoiCrossCiteApplication) getApplication();
    }                        
    
    @Get
    public List<String> getLanguages() {
        return this.app.getClient().getLanguages();
    }
}
