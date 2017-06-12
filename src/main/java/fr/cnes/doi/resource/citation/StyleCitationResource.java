/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import java.util.List;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Get Styles for citation.
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class StyleCitationResource extends BaseCitationResource {

    /**
     * Init.
     * @throws ResourceException 
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
    }
                           
    /**
     * Returns the styles for a citation.
     * @return the possibles styles for a citation
     */
    @Get
    public List<String> getStyles() {
        return this.app.getClient().getStyles();
    }    
}
