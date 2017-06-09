/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import java.util.List;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Get Language for citation.
 * @author Jean-christophe Malapert
 */
public class LanguageCitationResource extends BaseCitationResource {
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();        
    }                        
    
    @Get
    public List<String> getLanguages() {
        return this.app.getClient().getLanguages();
    }
}
