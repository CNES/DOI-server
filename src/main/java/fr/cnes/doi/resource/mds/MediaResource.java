/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.AbstractApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.exception.ClientMdsException;
import static fr.cnes.doi.security.UtilsHeader.SELECTED_ROLE_PARAMETER;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.Requirement;

import java.util.Arrays;
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
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/** 
 * Resource to handle to Media.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MediaResource extends BaseMdsResource {    

    /**
     * DOI parsed from the URL.
     */
    private String mediaName;        

    /**
     * Init by getting the media name.
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {   
        super.doInit();
        this.mediaName = getResourcePath().replace(DoiMdsApplication.MEDIA_URI+"/", "");
    }

    /**
     * Returns the media related to a DOI.
     * This request returns list of pairs of media type and URLs associated with
     * a given DOI when 200 status is returned (operation successful). 
     * @return the media related to a DOI
     * @throws ResourceException - if an error happens <ul>
     * <li>404 Not Found - No media attached to the DOI or DOI does not exist in our database</li>
     * <li>500 Internal Server Error - Error when requesting DataCite</li>
     * </ul>
     */  
    @Requirement(
        reqId = Requirement.DOI_SRV_090,
        reqName = Requirement.DOI_SRV_090_NAME
        ) 
    @Requirement(
        reqId = Requirement.DOI_MONIT_020,
        reqName = Requirement.DOI_MONIT_020_NAME
        )      
    @Get
    public Representation getMedias() throws ResourceException {
        getLogger().entering(getClass().getName(), "getMedias", this.mediaName);
        
        final Representation rep;
        final String medias;
        try {
            setStatus(Status.SUCCESS_OK);
            medias = this.getDoiApp().getClient().getMedia(this.mediaName);
            rep = new StringRepresentation(medias, MediaType.TEXT_URI_LIST);
        } catch (ClientMdsException ex) {
            getLogger().throwing(getClass().getName(), "getMedias", ex);    
            if(ex.getStatus().getCode() == Status.CLIENT_ERROR_NOT_FOUND.getCode()) {
                throw new ResourceException(ex.getStatus(), ex.getMessage(), ex);                
            } else {
                ((AbstractApplication)getApplication()).sendAlertWhenDataCiteFailed(ex);
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(), ex);
            }
        }
        
        getLogger().exiting(getClass().getName(), "getMedias", medias);        
        return rep;
    } 
    
    /**
     * Creates a media related to an URL for a given DOI.
     * Will add/update media type/urls pairs to a DOI. Standard domain 
     * restrictions check will be performed. 200 status is returned when the 
     * operation is successful.
     * @param mediaForm Form
     * @return short explanation of status code 
     * @throws ResourceException - if an error happens :<ul>
     * <li>400 Bad Request - 
     * {@value fr.cnes.doi.resource.mds.BaseMdsResource#DOI_PARAMETER} not 
     * provided or one or more of the specified mime-types or urls are
     * invalid (e.g. non supported mime-type, not allowed url domain, etc.)</li>
     * <li>401 Unauthorized - user unauthorized</li>     
     * <li>403 Forbidden - if the role is not allowed to use this feature or 
     * the user is not allow to create media</li>
     * <li>404 Not found : The DOI does not exist
     * <li>409 Conflict if a user is associated to more than one role</li>
     * <li>500 Internal Server Error - server internal error, try later and if 
     * problem persists please contact us</li>
     * </ul>
     */   
    @Requirement(
        reqId = Requirement.DOI_SRV_080,
        reqName = Requirement.DOI_SRV_080_NAME
        ) 
    @Requirement(
        reqId = Requirement.DOI_MONIT_020,
        reqName = Requirement.DOI_MONIT_020_NAME
        )   
    @Requirement(
            reqId = Requirement.DOI_INTER_070,
            reqName = Requirement.DOI_INTER_070_NAME
    )    
    @Requirement(
        reqId = Requirement.DOI_AUTO_020,
        reqName = Requirement.DOI_AUTO_020_NAME
        )     
    @Requirement(
        reqId = Requirement.DOI_AUTO_030,
        reqName = Requirement.DOI_AUTO_030_NAME
        )     
    @Post
    public Representation createMedia(final Form mediaForm) throws ResourceException{
        getLogger().entering(getClass().getName(), "createMedia", new Object[]{this.mediaName, mediaForm.getMatrixString()});
        
        checkInputs(this.mediaName, mediaForm);
        final String result;
        try {         
            setStatus(Status.SUCCESS_OK);
            final String selectedRole = extractSelectedRoleFromRequestIfExists();         
            checkPermission(this.mediaName, selectedRole);            
            result = this.getDoiApp().getClient().createMedia(this.mediaName, mediaForm);
        } catch (ClientMdsException ex) {
            getLogger().throwing(getClass().getName(), "createMedia", ex);  
            if(ex.getStatus().getCode() == Status.CLIENT_ERROR_BAD_REQUEST.getCode()) {
                throw new ResourceException(ex.getStatus(), ex.getMessage(), ex);                
            } else {
                ((AbstractApplication)getApplication())
                        .sendAlertWhenDataCiteFailed(ex);                          
                throw new ResourceException(
                        Status.SERVER_ERROR_INTERNAL, ex.getMessage(), ex
                );                
            }
        }
        
        getLogger().exiting(getClass().getName(), "createMedia", result);        
        return new StringRepresentation(result);
    }  
    
    
    /**
     * Checks input parameters
     * @param doi DOI number
     * @param mediaForm the parameters
     * @throws ResourceException - 400 Bad Request if DOI_PARAMETER is not set
     */ 
    @Requirement(
            reqId = Requirement.DOI_INTER_070,
            reqName = Requirement.DOI_INTER_070_NAME
    )        
    private void checkInputs(final String doi, final Form mediaForm) throws ResourceException {
        getLogger().entering(this.getClass().getName(), "checkInputs", mediaForm);
        StringBuilder errorMsg = new StringBuilder();
        if(doi == null || doi.isEmpty() || !doi.startsWith(DoiSettings.getInstance().getString(Consts.INIST_DOI))) {
            getLogger().log(Level.FINE, "{0} value is not set", DOI_PARAMETER);
            errorMsg = errorMsg.append(DOI_PARAMETER).append(" value is not set.");            
        } else {
            try {
                ClientMDS.checkIfAllCharsAreValid(doi);
            } catch (IllegalArgumentException ex) {
                errorMsg = errorMsg.append(DOI_PARAMETER).append(" no valid syntax.");
            }
        }
        if(errorMsg.length() == 0) {        
            getLogger().fine("The form is valid");                    
        } else {
            final ResourceException exception =  new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, errorMsg.toString());            
            getLogger().throwing(this.getClass().getName(), "checkInputs", exception);
            throw exception;
        }      
        getLogger().exiting(this.getClass().getName(), "checkInputs");        
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
        docInfo.setTextContent("This request returns a key-value list of media "
                + "types/urls for a given DOI name");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }    

    /**
     * Describes the GET method.
     * @param info Wadl description for a GET method
     */ 
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific media for a given DOI");

        addRequestDocToMethod(info, createQueryParamDoc(
                DoiMdsApplication.DOI_TEMPLATE, ParameterStyle.TEMPLATE, 
                "DOI name", true, "xs:string")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, "Operation successful", mediaRepresentation())
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_NOT_FOUND, "DOI does not exist in our database", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SERVER_ERROR_INTERNAL, "server internal error, try later "
                        + "and if problem persists please contact us", 
                "explainRepresentation")
        );
    } 

    /**
     * Describes POST method.
     * @param info Wadl description for describing POST method
     */ 
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    @Override
    protected final void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("POST will add/update media type/urls pairs to a DOI. Standard domain restrictions check will be performed.");
        final ParameterInfo param = new ParameterInfo();
        param.setName("{mediaType}");
        param.setStyle(ParameterStyle.PLAIN);        
        param.setRequired(false);
        param.setType("xs:string");
        param.setFixed("{url}");
        param.setRepeating(true);
        param.setDocumentation("(key/value) = (mediaType/url)");
        final RepresentationInfo rep = new RepresentationInfo(MediaType.APPLICATION_WWW_FORM);
        rep.getParameters().add(param);
        
        addRequestDocToMethod(info, 
                Arrays.asList(createQueryParamDoc(SELECTED_ROLE_PARAMETER, ParameterStyle.HEADER, "A user can select one role when he is associated to several roles", false, "xs:string")), 
                rep);        
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_BAD_REQUEST, DOI_PARAMETER+" not provided or one or more of the specified mime-types or urls are invalid (e.g. non supported mime-type, not allowed url domain, etc.)", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "if no role is provided", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "if the role is not allowed to use this feature or the user is not allow to create media", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_CONFLICT, "if a user is associated to more than one role", "explainRepresentation"));        
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));           
    }     
}
