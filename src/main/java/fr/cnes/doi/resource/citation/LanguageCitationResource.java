/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import java.util.List;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * The supported languages for citation.
 * @author Jean-christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class LanguageCitationResource extends BaseCitationResource {
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit(); 
        final StringBuilder description = new StringBuilder();
        description.append("Selects a Language and Country.");
        description.append("The language is used to format the citation.");
        setDescription(description.toString());
    }                        
    
    /**
     * Returns the languages to format the citation.
     * @return the languages
     */
    @Get
    public List<String> getLanguages() {
        return this.app.getClient().getLanguages();
    }
    
    /**
     * Describes the Get Method.
     * @param info Wadl description
     */
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Retrieves the supported languages"); 
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", listRepresentation("Language representation", "A list of possible language, one per row")));        
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us"));
    }     
}
