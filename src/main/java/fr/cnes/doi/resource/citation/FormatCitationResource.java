/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import java.util.Arrays;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.exception.ClientCrossCiteException;

/**
 * Formats a citation.
 * CrossRef, DataCite and mEDRA support formatted citations via the 
 * text/bibliography content type. These are the output of the Citation Style 
 * Language processor, citeproc-js. The content type can take two additional 
 * parameters to customise its response format.
 * <p>
 * "\"style\" can be chosen from the list of style names found in the CSL style 
 * repository. Many styles are supported, including common styles such as apa 
 * and harvard3
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class FormatCitationResource extends BaseCitationResource {
    
    private String doiName;
    private String style;
    private String language;

    /**
     * Init by getting doi, lang and style values.
     * @throws ResourceException 
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();        
        this.doiName = getQueryValue("doi");
        this.language = getQueryValue("lang");
        this.style = getQueryValue("style");        
        final StringBuilder description = new StringBuilder();
        description.append("CrossRef, DataCite and mEDRA support formatted "
                + "citations via the text/bibliography content type. These are "
                + "the output of the Citation Style Language processor, "
                + "citeproc-js. The content type can take two additional "
                + "parameters to customise its response format.");
        description.append("\"style\" can be chosen from the list of style names"
                + " found in the CSL style repository. Many styles are supported,"
                + " including common styles such as apa and harvard3");
        setDescription(description.toString());       
    }

    /**
     * Returns the formatted citation.
     * @return the formatted citation
     */
    @Get
    public String getFormat() {
        try {
            getLogger().entering(getClass().getName(), "getFormat",new Object[]{this.doiName, this.language, this.style});
            
            final String result = this.app.getClient().getFormat(this.doiName, this.style, this.language);
            
            getLogger().exiting(getClass().getName(), "getFormats", result);
            return result;
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
        info.setDocumentation("Select Formatting Style"); 
        addRequestDocToMethod(info, Arrays.asList(
                createQueryParamDoc("doi", ParameterStyle.QUERY, "doiName", true, "xs:string"),
                createQueryParamDoc("lang", ParameterStyle.QUERY, "language", true, "xs:string"),
                createQueryParamDoc("style", ParameterStyle.QUERY, "style", true, "xs:string")                                
        ));
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", listRepresentation("Format representation", MediaType.TEXT_PLAIN, "The formatted citation")));        
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_NOT_FOUND, "DOI not found", listRepresentation("Error representation", MediaType.TEXT_HTML, "Error")));        
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_BAD_REQUEST, "Wrong input parameters", listRepresentation("Error representation", MediaType.TEXT_HTML, "Error")));                        
    }     
    
}
