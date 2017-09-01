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
import fr.cnes.doi.utils.Requirement;
import java.util.logging.Level;

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

    /**
     * The digital object identifier to format.
     */
    private String doiName;
    
    /**
     * The style of formatting.
     */
    private String style;
    
    /**
     * The language to format.
     */
    private String language;

    /**
     * Init by getting doi, lang and style values.
     * @throws ResourceException 
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();        
        
        this.doiName = getQueryValue(DOI_PARAMETER);
        this.language = getQueryValue(LANG_PARAMETER);
        this.style = getQueryValue(STYLE_PARAMETER);        
        
        getLogger().log(Level.FINE, "DOI Parameter : {0}", this.doiName);        
        getLogger().log(Level.FINE, "LANGUAGE Parameter : {0}", this.language);
        getLogger().log(Level.FINE, "STYLE Parameter : {0}", this.language);        
        
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
    @Requirement(
            reqId = "DOI_SRV_120",
            reqName = "Formatage d'une citation"
    )     
    @Get
    public String getFormat() {
        try {
            getLogger().entering(getClass().getName(), "getFormat",new Object[]{this.doiName, this.language, this.style});
            
            checkInputs();
            final String result = this.app.getClient().getFormat(this.doiName, this.style, this.language);
            
            getLogger().exiting(getClass().getName(), "getFormats", result);
            return result;
        } catch (ClientCrossCiteException ex) {
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
    }
    
    /**
     * Checks input parameters
     * @ResourceException if DOI_PARAMETER and LANG_PARAMETER and STYLE_PARAMETER are not set
     */
    private void checkInputs() {
        StringBuilder errorMsg = new StringBuilder();
        if (this.doiName == null || this.doiName.isEmpty()) {
            getLogger().log(Level.FINE, "{0} value is not set", DOI_PARAMETER);
            errorMsg = errorMsg.append(DOI_PARAMETER).append(" value is not set.");
        } 
        if (this.language == null || this.language.isEmpty()) {
            getLogger().log(Level.FINE, "{0} value is not set", LANG_PARAMETER);
            errorMsg = errorMsg.append(LANG_PARAMETER).append(" value is not set.");            
        }    
        if (this.style == null || this.style.isEmpty()) {
            getLogger().log(Level.FINE, "{0} value is not set", STYLE_PARAMETER);
            errorMsg = errorMsg.append(STYLE_PARAMETER).append(" value is not set.");            
        }        
        if(errorMsg.length() == 0) {        
            getLogger().fine("The parameters are valid");                    
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, errorMsg.toString());            
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
        info.setDocumentation("Select Formatting Style"); 
        addRequestDocToMethod(info, Arrays.asList(
                createQueryParamDoc(DOI_PARAMETER, ParameterStyle.QUERY, "doi to format for the citation", true, "xs:string"),
                createQueryParamDoc(LANG_PARAMETER, ParameterStyle.QUERY, "language for the citation formating", true, "xs:string"),
                createQueryParamDoc(STYLE_PARAMETER, ParameterStyle.QUERY, "style fot the citation formating", true, "xs:string")                                
        ));
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", listRepresentation("Format representation", MediaType.TEXT_PLAIN, "The formatted citation")));        
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_NOT_FOUND, "DOI not found", listRepresentation("Error representation", MediaType.TEXT_HTML, "Error")));        
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_BAD_REQUEST, "Wrong input parameters", listRepresentation("Error representation", MediaType.TEXT_HTML, "Error")));                        
    }     
    
}
