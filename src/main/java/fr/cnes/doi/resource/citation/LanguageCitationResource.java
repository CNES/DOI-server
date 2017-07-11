/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.exception.ClientCrossCiteException;

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
     * Returns the languages as JSON to format the citation.
     * @return the languages
     */
    @Get("json")
    public List<String> getLanguagesToJSON() {
        try {
            return this.app.getClient().getLanguages();
        } catch (ClientCrossCiteException ex) {
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
    }
    
    /**
     * Returns the languages as XML to format the citation.
     * @return the languages
     */    
    @Get("xml")
    public List<String> getLanguagesToXML() {
        try {
            return this.app.getClient().getLanguages();
        } catch (ClientCrossCiteException ex) {
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
    }    
    
    /**
     * Describes the Get Method.
     * @param info Wadl description
     */
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Retrieves the supported languages"); 
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", listRepresentation("Language representation", MediaType.TEXT_XML, "A List of String representing the possible languages")));        
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", listRepresentation("Language representation", MediaType.APPLICATION_JSON, "A JSON array representing the possible languages")));                
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us"));
    }     
}
