/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.client.ClientException;
import java.util.ArrayList;
import java.util.List;
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
import org.restlet.resource.ResourceException;

/**
 *
 * @author malapert
 * https://data.datacite.org/
 */
public class MediaResource extends BaseResource {

    private String mediaName;

    @Override
    protected void doInit() throws ResourceException {   
        super.doInit();
        this.mediaName = getAttribute(DoiMdsApplication.DOI_TEMPLATE);
    }

    @Get
    public Representation getMedias() {
        try {
            setStatus(Status.SUCCESS_OK);
            ClientMDS client = new ClientMDS(ClientMDS.Context.DEV);
            String medias = client.getMedia(this.mediaName);
            return new StringRepresentation(medias, MediaType.TEXT_URI_LIST);
        } catch (ClientException ex) {
            throw new ResourceException(ex.getStatus(), ex.getMessage());
        }
    } 
    
    private ParameterInfo addQueryParameter() {
        ParameterInfo param = new ParameterInfo();
        param.setName(DoiMdsApplication.DOI_TEMPLATE);
        param.setStyle(ParameterStyle.TEMPLATE);
        param.setDocumentation("DOI name");
        param.setRequired(true);
        param.setType("xs:string"); 
        return param;        
    }
    
    private RepresentationInfo mediaRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);        
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Media representation");
        docInfo.setTextContent("This request returns a key-value list of media types/urls for a given DOI name");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }
    
    private ResponseInfo getSuccessfullResponse() {
        ResponseInfo responseInfo = new ResponseInfo();        
        final List<RepresentationInfo> repsInfo = new ArrayList<>();        
        repsInfo.add(mediaRepresentation());        
        responseInfo.getStatuses().add(Status.SUCCESS_OK);
        responseInfo.setDocumentation("Operation successful");
        responseInfo.setRepresentations(repsInfo);          
        return responseInfo;
    }

    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific media for a given DOI");
       
        final RequestInfo request = new RequestInfo();
        request.getParameters().add(addQueryParameter());
        info.setRequest(request);
      
        info.getResponses().add(getSuccessfullResponse());
                
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        RepresentationInfo rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);
        responseInfo.setDocumentation("no login");
        info.getResponses().add(responseInfo);       
        
        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_FORBIDDEN);
        rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);        
        responseInfo.setDocumentation("login problem or dataset belongs to another party");
        info.getResponses().add(responseInfo); 

        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_NOT_FOUND);
        rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);          
        responseInfo.setDocumentation("DOI does not exist in our database");
        info.getResponses().add(responseInfo); 
        
        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.SERVER_ERROR_INTERNAL);
        rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);          
        responseInfo.setDocumentation("server internal error, try later and if problem persists please contact us");
        info.getResponses().add(responseInfo);
    }     
      
}
