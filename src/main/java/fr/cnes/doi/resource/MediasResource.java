/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.client.ClientException;
import java.util.Arrays;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.RequestInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Resource to handle a collection of media.
 * @author Jean-Christophe Malapert
 */
public class MediasResource extends BaseMdsResource {
    
    public static final String CREATE_MEDIA = "Create media";

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
    }   
    
    /**
     * TODO check doi name
     * @param mediaForm 
     * @return  
     */
    @Post
    public Representation createMedia(final Form mediaForm) {
        getLogger().entering(getClass().getName(), "createMedia", mediaForm.getMatrixString());
        final String result;
        try {         
            setStatus(Status.SUCCESS_OK);
            Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
            String selectedRole = headers.getFirstValue("selectedRole", "");            
            checkPermission(mediaForm.getFirstValue("doi"), selectedRole);            
            result = this.doiApp.getClient().createMedia(mediaForm);
        } catch (ClientException ex) {
            getLogger().exiting(getClass().getName(), "createMedia", ex.getMessage());                    
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        getLogger().exiting(getClass().getName(), "createMedia", result);        
        return new StringRepresentation(result);
    }    
   
    @Override
    protected final void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("POST will add/update media type/urls pairs to a DOI. Standard domain restrictions check will be performed.");
                
        final RequestInfo request = new RequestInfo();
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
