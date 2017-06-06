/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.client.ClientException;
import java.util.ArrayList;
import java.util.List;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.RequestInfo;
import org.restlet.ext.wadl.ResponseInfo;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 *
 * @author malapert
 */
public class MetadataResource extends BaseResource {
    
    public static final String GET_METADATA = "Get a Metadata";
    public static final String DELETE_METADATA = "Delete a Metadata";
    
    private String doiName;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setDescription("This resource handles a metadata : retrieve, delete");
        this.doiName = getAttribute(DoiMdsApplication.DOI_TEMPLATE);       
    }    

    @Get
    public Representation getMetadata() {
        getLogger().entering(getClass().getName(), "getMetadata", this.doiName);
        try {
            setStatus(Status.SUCCESS_OK);
            Representation rep = this.doiApp.getClient().getMetadata(this.doiName);
            getLogger().exiting(getClass().getName(), "getMetadata");
            return rep;
        } catch (ClientException ex) {
            getLogger().exiting(getClass().getName(), "getMetadata", ex.getMessage());
            throw new ResourceException(ex);
        }        
    }
    
    @Delete
    public Representation deleteMetadata() {
        Representation rep;
        try {         
            Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
            String selectedRole = headers.getFirstValue("selectedRole", "");            
            getLogger().entering(getClass().getName(), "deleteMetadata", new Object[]{this.doiName, selectedRole});
            checkPermission(this.doiName, selectedRole);
            setStatus(Status.SUCCESS_OK);
            rep = this.doiApp.getClient().deleteMetadata(this.doiName);            
        } catch (ClientException ex) {
            getLogger().exiting(getClass().getName(), "deleteMetadata", ex.getMessage());            
            throw new ResourceException(ex);
        }
        getLogger().exiting(getClass().getName(), "deleteMetadata");
        return rep;         
    }
    
    private ParameterInfo templateQuery(){
        ParameterInfo param = new ParameterInfo();
        param.setName(DoiMdsApplication.DOI_TEMPLATE);
        param.setStyle(ParameterStyle.TEMPLATE);
        param.setDocumentation("DOI name");
        param.setRequired(true);
        param.setType("xs:string"); 
        return param;
    }
        
    
    private ResponseInfo getSuccessfullResponse() {
        ResponseInfo responseInfo = new ResponseInfo();        
        final List<RepresentationInfo> repsInfo = new ArrayList<>();        
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setReference("metadataRepresentation");
        repsInfo.add(repInfo);        
        responseInfo.getStatuses().add(Status.SUCCESS_OK);
        responseInfo.setDocumentation("Operation successful");
        responseInfo.setRepresentations(repsInfo);
        return responseInfo;
    }
    
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific metadata");
      
        final RequestInfo request = new RequestInfo();
        request.getParameters().add(templateQuery());
        info.setRequest(request);
        
        info.getResponses().add(getSuccessfullResponse());
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        responseInfo.setDocumentation("no login");
        RepresentationInfo rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);
        info.getResponses().add(responseInfo);       
        
        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_FORBIDDEN);
        responseInfo.setDocumentation("login problem or dataset belongs to another party");
        rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);        
        info.getResponses().add(responseInfo); 

        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_NOT_FOUND);
        responseInfo.setDocumentation("DOI does not exist in our database");
        rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);        
        info.getResponses().add(responseInfo); 
        
        
        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_GONE);
        responseInfo.setDocumentation("the requested dataset was marked inactive (using DELETE method)");
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
    
    private ParameterInfo addQueryParameter() {
        ParameterInfo param = new ParameterInfo();
        param.setName(DoiMdsApplication.DOI_TEMPLATE);
        param.setStyle(ParameterStyle.TEMPLATE);
        param.setDocumentation("DOI name");
        param.setRequired(true);
        param.setType("xs:string");   
        return param;
    }
    
    private RepresentationInfo deleteRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setReference("metadataRepresentation");
        return repInfo;
    }
    
    private ResponseInfo deleteSuccessfullResponse() {
        ResponseInfo responseInfo = new ResponseInfo();        
        final List<RepresentationInfo> repsInfo = new ArrayList<>();        
        repsInfo.add(deleteRepresentation());        
        responseInfo.getStatuses().add(Status.SUCCESS_OK);
        responseInfo.setDocumentation("Operation successful");
        responseInfo.setRepresentations(repsInfo);
        return responseInfo;
    }
    
    @Override
     protected final void describeDelete(final MethodInfo info) {
        info.setName(Method.DELETE);
        info.setDocumentation("Delete a specific metadata");
     
        final RequestInfo request = new RequestInfo();
        request.getParameters().add(addQueryParameter());
        ParameterInfo param = new ParameterInfo();
        param.setName("selectedRole");
        param.setStyle(ParameterStyle.HEADER);        
        param.setRequired(false);
        param.setType("xs:string");
        param.setDocumentation("A user can select one role when he is associated to several roles");        
        request.getParameters().add(param);
        info.setRequest(request);   
              
        info.getResponses().add(deleteSuccessfullResponse());
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        responseInfo.setDocumentation("no login");
        RepresentationInfo rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);        
        info.getResponses().add(responseInfo);       
        
        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_FORBIDDEN);
        responseInfo.setDocumentation("login problem or dataset belongs to another party");
        rep = new RepresentationInfo();
        rep.setReference("explainRepresentation");
        responseInfo.getRepresentations().add(rep);        
        info.getResponses().add(responseInfo); 

        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_NOT_FOUND);
        responseInfo.setDocumentation("DOI does not exist in our database");
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
