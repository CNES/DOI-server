/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.client.ClientException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.RequestInfo;
import org.restlet.ext.wadl.ResponseInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Resource to handle a collection of DOI.
 * @author Jean-Christophe Malapert
 */
public class DoisResource extends BaseMdsResource {
    
    /**
     *
     */
    public static final String LIST_ALL_DOIS = "List all DOIs";

    /**
     *
     */
    public static final String CREATE_DOI = "Create a DOI";
              
    /**
     *
     * @throws ResourceException
     */
    @Override
    protected void doInit() throws ResourceException {       
        super.doInit();
        setDescription("The resource contains the list of DOI and can create a new DOI");       
    }    
    
    /**
     * Returns the collection of DOI.
     * @return the list of DOI 
     */
    @Get
    public Representation getDois() {
        getLogger().entering(getClass().getName(), "getDois");
        try {
            setStatus(Status.SUCCESS_OK);
            String dois = this.doiApp.getClient().getDoiCollection();
            getLogger().exiting(getClass().getName(), "getDois", dois);
            return new StringRepresentation(dois, MediaType.TEXT_URI_LIST);
        } catch (ClientException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
            getLogger().exiting(getClass().getName(), "getDois", ex.getMessage());
            throw new ResourceException(ex.getStatus(), ex.getMessage());
        }        
    } 
    
    /**
     * Creates a new DOI based on the doi and url parameters
     * 
     * The DOI name is built following this syntax:  
     * <i>CNES_prefix></i>/<i>project_provided_the_DOI_server</i>/project_suffix.
     * 
     * <p>
     * The client provides :
     * <ul>
     * <li><i>doi</i> : the project suffix, which is an unique identifier within
     * the project.</li>
     * <li><i>url</i> : the URL of the landing page. 
     * </ul>
     * <b>Note 1</b> : the landing page should be accessible from www
     * <b>Note 2</b> : Metadata must be uploaded first
     * 
     * @param doiForm doi and url
     * @return short explanation of status code e.g. CREATED, HANDLE_ALREADY_EXISTS etc
     * TODO Check this method (The form or two lines as input)
     */    
    @Post("form")   
    public String createDoi(final Form doiForm) {         
        getLogger().entering(getClass().getName(), "createDoi", doiForm.getMatrixString());        
        String result;        
        Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
        String selectedRole = headers.getFirstValue("selectedRole", "");
        checkPermission(doiForm.getFirstValue("doi"), selectedRole);
        try {
            setStatus(Status.SUCCESS_CREATED);
            result = this.doiApp.getClient().createDoi(doiForm);            
        } catch (ClientException ex) {         
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
            getLogger().exiting(getClass().getName(), "createDoi", ex.getMessage());            
            throw new ResourceException(ex.getStatus(), ex.getMessage());
        }
        getLogger().exiting(getClass().getName(), "createDoi", result);
        return result;
    }
        
    private RepresentationInfo successFullRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_URI_LIST);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("DOI collection representation");
        docInfo.setTextContent("This request returns a list of all DOIs for the requesting datacentre. There is no guaranteed order.");
        repInfo.setDocumentation(docInfo);         
        return repInfo;
    }
    
    private RepresentationInfo noContentRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Empty representation");
        docInfo.setTextContent("No contain");
        repInfo.setDocumentation(docInfo);         
        return repInfo;        
    }
    
    private RepresentationInfo explainStatusRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setIdentifier("explainRepresentation");
        repInfo.setMediaType(MediaType.TEXT_PLAIN);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Explain representation");
        docInfo.setTextContent("short explanation of status code e.g. CREATED, HANDLE_ALREADY_EXISTS etc");
        repInfo.setDocumentation(docInfo);         
        return repInfo;           
    }            
    
    /**
     *
     * @param info
     */
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);        
        info.setDocumentation("Retrieves the DOI collection");       
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", successFullRepresentation()));
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_NO_CONTENT, "no DOIs founds", noContentRepresentation()));
    }    
    
    private RepresentationInfo requestRepresentation(){
        RepresentationInfo rep = new RepresentationInfo(MediaType.APPLICATION_WWW_FORM);
        rep.getParameters().add(createQueryParamDoc("doi", ParameterStyle.PLAIN, "DOI name", true, "xs:string"));
        rep.getParameters().add(createQueryParamDoc("url", ParameterStyle.PLAIN, "URL of the landing page", true, "xs:string"));
        return rep;
    }

    /**
     *
     * @param info
     */
    @Override
    protected final void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("POST will mint new DOI if specified DOI doesn't exist. "
                + "This method will attempt to update URL if you specify existing DOI. "
                + "Standard domains and quota restrictions check will be performed. "
                + "A Datacentre's doiQuotaUsed will be increased by 1. "
                + "A new record in Datasets will be created.");       
       
        addRequestDocToMethod(info, 
                    Arrays.asList(
                        createQueryParamDoc("selectedRole", ParameterStyle.HEADER, "A user can select one role when he is associated to several roles", false, "xs:string")
                    ), 
                    requestRepresentation());
                
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_CREATED, "Operation successful", explainStatusRepresentation()));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_BAD_REQUEST, "request body must be exactly two lines: DOI and URL; wrong domain, wrong prefix", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "no login", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "login problem, quota exceeded", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_PRECONDITION_FAILED, "metadata must be uploaded first", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));
             
    }     
}
