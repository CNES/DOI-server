/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import java.util.List;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.exception.ClientCrossCiteException;
import fr.cnes.doi.utils.Requirement;

/**
 * The supported styles for citation.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class StyleCitationResource extends BaseCitationResource {

    /**
     * Init.
     * @throws ResourceException 
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        final StringBuilder description = new StringBuilder();
        description.append("Selects a style");
        description.append("A \"style\" can be chosen from the list of style "
                + "names found in the CSL style repository.");
        description.append("Many styles are supported, including common styles "
                + "such as apa and harvard3.");
        setDescription(description.toString());        
    }

    /**
     * Returns the styles as JSON array for a citation.
     * @return the possibles styles for a citation
     */
    @Requirement(
            reqId = "DOI_SRV_100",
            reqName = "Listing des styles"
    )     
    @Get("json")
    public List<String> getStylesToJSON() {
        try {
            return this.app.getClient().getStyles();
        } catch (ClientCrossCiteException ex) {
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
    } 
    
    /**
     * Returns the styles as XML for a citation.
     * @return the possibles styles for a citation
     */
    @Requirement(
            reqId = "DOI_SRV_100",
            reqName = "Listing des styles"
    )     
    @Get("xml")
    public List<String> getStylesToXML() {
        try {
            return this.app.getClient().getStyles();
        } catch (ClientCrossCiteException ex) {
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
    }  
          
    
    /**
     * Describes the Get Method.
     * @param info Wadl description
     */
    @Requirement(
            reqId = "DOI_DOC_010",
            reqName = "Documentation des interfaces"
    )      
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Retrieves the list of supported styles"); 
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", listRepresentation("Style representation", MediaType.TEXT_XML, "A List of String representing the possible styles")));        
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", listRepresentation("Style representation", MediaType.APPLICATION_JSON, "A JSON array representing the possible styles")));                
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us"));
    }      
}
