/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.client.ClientException;
import java.util.ArrayList;
import java.util.List;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.RequestInfo;
import org.restlet.ext.wadl.ResponseInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Resource to handle a collection of media.
 * @author Jean-Christophe Malapert
 */
public class MediasResource extends BaseResource {
    
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
 
    private ResponseInfo postSuccessFullResponse() {
        ResponseInfo responseInfo = new ResponseInfo();        
        final List<RepresentationInfo> repsInfo = new ArrayList<>();        
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setReference("explainRepresentation");
        repsInfo.add(repInfo);        
        responseInfo.getStatuses().add(Status.SUCCESS_OK);
        responseInfo.setDocumentation("Operation successful");
        responseInfo.setRepresentations(repsInfo);
        return responseInfo;
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
        request.getRepresentations().add(rep);
                
        param = new ParameterInfo();
        param.setName("selectedRole");
        param.setStyle(ParameterStyle.HEADER);        
        param.setRequired(false);
        param.setType("xs:string");
        param.setDocumentation("A user can select one role when he is associated to several roles");        
        request.getParameters().add(param);
        
        info.setRequest(request);        
        
        info.getResponses().add(postSuccessFullResponse());
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_BAD_REQUEST);
        responseInfo.setDocumentation("invalid XML, wrong prefix");
        rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);        
        info.getResponses().add(responseInfo);
        
        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        responseInfo.setDocumentation("no login");
        rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);         
        info.getResponses().add(responseInfo);
        
        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_FORBIDDEN);
        responseInfo.setDocumentation("login problem, quota exceeded");
        rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);          
        info.getResponses().add(responseInfo);                   
        
        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.SERVER_ERROR_INTERNAL);
        responseInfo.setDocumentation("server internal error, try later and if problem persists please contact us");
        rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);          
        info.getResponses().add(responseInfo);             
    }     
}
