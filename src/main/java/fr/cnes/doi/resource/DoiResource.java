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
 * DOI resource to retrieve a given DOI.
 * @author Jean-Christophe Malapert
 */
public class DoiResource extends BaseResource {
    private String doiName;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setDescription("The resource can retrieve a DOI");                        
        this.doiName = getAttribute(DoiMdsApplication.DOI_TEMPLATE); 
    }
    
    /**
     * Returns a DOI.
     * @return 
     */
    @Get
    public Representation getDoi() {
        try {
            ClientMDS client = new ClientMDS(ClientMDS.Context.DEV);
            String doi = client.getDoi(this.doiName);
            if(doi != null && !doi.isEmpty()) {
                setStatus(Status.SUCCESS_OK);
            } else {
                setStatus(Status.SUCCESS_NO_CONTENT);
            }
            return new StringRepresentation(doi, MediaType.TEXT_PLAIN);          
        } catch (ClientException ex) {
            throw new ResourceException(ex);
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
    
    private RepresentationInfo doiRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);        
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("DOI Representation");
        docInfo.setTextContent("This request returns an URL associated with a given DOI.");
        repInfo.setDocumentation(docInfo);        
        return repInfo;
    }
    
    private ResponseInfo getSuccessfullResponse() {
        ResponseInfo responseInfo = new ResponseInfo();        
        final List<RepresentationInfo> repsInfo = new ArrayList<>();        
        repsInfo.add(doiRepresentation());        
        responseInfo.getStatuses().add(Status.SUCCESS_OK);
        responseInfo.setDocumentation("Operation successful");
        responseInfo.setRepresentations(repsInfo); 
        return responseInfo;
    }
    
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific DOI");
      
        final RequestInfo request = new RequestInfo();
        request.getParameters().add(addQueryParameter());
        info.setRequest(request);
        
        info.getResponses().add(getSuccessfullResponse());
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.SUCCESS_NO_CONTENT);
        responseInfo.setDocumentation("DOI is known to MDS, but is not minted (or not resolvable e.g. due to handle's latency)");
        RepresentationInfo rep = new RepresentationInfo();
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
