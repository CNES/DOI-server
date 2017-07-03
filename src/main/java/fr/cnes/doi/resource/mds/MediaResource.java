/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.client.ClientMdsException;
import java.util.Arrays;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/** 
 * Resource to handle to Media.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MediaResource extends BaseMdsResource {    

    private String mediaName;

    /**
     * Init by getting the media name.
     * @throws ResourceException
     */
    @Override
    protected void doInit() throws ResourceException {   
        super.doInit();
        this.mediaName = getAttribute(DoiMdsApplication.DOI_TEMPLATE);
    }

    /**
     * Returns the media related to a DOI.
     * This request returns list of pairs of media type and URLs associated with
     * a given DOI. The difference status:
     * <ul>
     * <li>200 OK - operation successful</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 login problem or dataset belongs to another party</li>
     * <li>404 Not Found - No media attached to the DOI or DOI does not exist in our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if problem persists please contact us</li>
     * </ul>
     * @return the media related to a DOI
     * @throws ResourceException Will be thrown when an error happens          
     */
    @Get
    public Representation getMedias() {
        getLogger().entering(getClass().getName(), "getMedias", this.mediaName);
        
        final Representation rep;
        String medias;
        try {
            setStatus(Status.SUCCESS_OK);
            medias = this.doiApp.getClient().getMedia(this.mediaName);
            rep = new StringRepresentation(medias, MediaType.TEXT_URI_LIST);
        } catch (ClientMdsException ex) {
            getLogger().exiting(getClass().getName(), "getMedias", ex.getDetailMessage());            
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
        
        getLogger().exiting(getClass().getName(), "getMedias", medias);        
        return rep;
    } 
    
    /**
     * Creates a media related to an URL for a given DOI.
     * Will add/update media type/urls pairs to a DOI. Standard domain 
     * restrictions check will be performed. The different status:
     * <ul>
     * <li>200 OK - operation successful</li>
     * <li>400 Bad Request - one or more of the specified mime-types or urls are
     * invalid (e.g. non supported mime-type, not allowed url domain, etc.)</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem</li>
     * <li>500 Internal Server Error - server internal error, try later and if 
     * problem persists please contact us</li>
     * </ul>
     * @param mediaForm Form
     * @return a media related to an URL for a given DOI 
     * @throws ResourceException Will be thrown when an error happens               
     */
    @Post
    public Representation createMedia(final Form mediaForm) throws ResourceException{
        getLogger().entering(getClass().getName(), "createMedia", new Object[]{this.mediaName, mediaForm.getMatrixString()});
        final String result;
        try {         
            setStatus(Status.SUCCESS_OK);
            Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
            String selectedRole = headers.getFirstValue("selectedRole", "");            
            checkPermission(mediaForm.getFirstValue("doi"), selectedRole);            
            result = this.doiApp.getClient().createMedia(this.mediaName, mediaForm);
        } catch (ClientMdsException ex) {
            getLogger().exiting(getClass().getName(), "createMedia", ex.getDetailMessage());                    
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
        getLogger().exiting(getClass().getName(), "createMedia", result);        
        return new StringRepresentation(result);
    }     
   
    /**
     * Media representation.
     * @return Wadl description for a Media representation
     */
    private RepresentationInfo mediaRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);        
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Media representation");
        docInfo.setTextContent("This request returns a key-value list of media types/urls for a given DOI name");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }    

    /**
     * Describes the GET method.
     * @param info Wadl description for a GET method
     */
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific media for a given DOI");

        addRequestDocToMethod(info, createQueryParamDoc(DoiMdsApplication.DOI_TEMPLATE, ParameterStyle.TEMPLATE, "DOI name", true, "xs:string"));
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", mediaRepresentation()));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "no login", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "login problem or dataset belongs to another party", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_NOT_FOUND, "DOI does not exist in our database", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));
    } 

    /**
     * Describes POST method.
     * @param info Wadl description for describing POST method
     */
    @Override
    protected final void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("POST will add/update media type/urls pairs to a DOI. Standard domain restrictions check will be performed.");
                        ParameterInfo param = new ParameterInfo();
        param.setName("{mediaType}");
        param.setStyle(ParameterStyle.PLAIN);        
        param.setRequired(false);
        param.setType("xs:string");
        param.setFixed("{url}");
        param.setRepeating(true);
        param.setDocumentation("(key/value) = (mediaType/url)");
        RepresentationInfo rep = new RepresentationInfo(MediaType.APPLICATION_WWW_FORM);
        rep.getParameters().add(param);
        
        addRequestDocToMethod(info, 
                Arrays.asList(createQueryParamDoc("selectedRole", ParameterStyle.HEADER, "A user can select one role when he is associated to several roles", false, "xs:string")), 
                rep);        
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_BAD_REQUEST, "invalid XML, wrong prefix", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "no login", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "login problem, quota exceeded", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));           
    }     
      
}
