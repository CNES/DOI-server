/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.client.ClientException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import org.datacite.schema.kernel_4.Resource;
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
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.xml.sax.SAXException;

/**
 * Resourece to handle a collection of metadata.
 * @author Jean-Christophe Malapert
 */
public class MetadatasResource extends BaseResource {
    
    public static final String CREATE_METADATA = "Create Metadata";

    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setDescription("This resource can create metadata");
    }
        
    
    /**
     * https://search.datacite.org/help.html
     * @param entity 
     * TODO update DOI name
     * @return 
     */
    @Post
    public String createMetadata(final Representation entity) {   
        getLogger().entering(getClass().getName(), "createMetadata");
        String result;
        try {
            setStatus(Status.SUCCESS_CREATED);
            JAXBContext ctx = JAXBContext.newInstance(new Class[]{org.datacite.schema.kernel_4.Resource.class});
            Unmarshaller um = ctx.createUnmarshaller();
            Schema schema = this.doiApp.getSchemaFactory().newSchema(new URL("https://schema.datacite.org/meta/kernel-4.0/metadata.xsd"));                    
            um.setSchema(schema);
            JAXBElement<Resource> jaxbResource = (JAXBElement<Resource>) um.unmarshal(entity.getStream());
            org.datacite.schema.kernel_4.Resource resource = jaxbResource.getValue();              
            Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
            String selectedRole = headers.getFirstValue("selectedRole", "");            
            checkPermission(resource.getIdentifier().getValue(), selectedRole);
            //Creator creator = new Resource.Creators.Creator();
            //creator.setCreatorName(projectDM.getAlternateName());
            //resource.getCreators().getCreator().add(creator);
            resource.setPublisher("Centre National d'Etudes Spatiales (CNES)");            
            result = this.doiApp.getClient().createMetadata(resource);
        } catch (ClientException ex) {
            getLogger().exiting(getClass().getName(), "createMetadata", ex.getMessage());
            throw new ResourceException(ex.getStatus(), ex.getMessage());
        } catch (JAXBException | IOException ex) {
            getLogger().exiting(getClass().getName(), "createMetadata", ex.getMessage());
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "invalid XML");
        } catch (SAXException ex) {
            getLogger().exiting(getClass().getName(), "createMetadata", ex.getMessage());
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "DataCite schema not available");
        }
        getLogger().exiting(getClass().getName(), "createMetadata", result);
        return result;
    }           
    
    private ResponseInfo postSuccessfullResponse() {
        ResponseInfo responseInfo = new ResponseInfo();        
        final List<RepresentationInfo> repsInfo = new ArrayList<>();        
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setReference("explainRepresentation");
        repsInfo.add(repInfo);        
        responseInfo.getStatuses().add(Status.SUCCESS_CREATED);
        responseInfo.setDocumentation("Operation successful");
        responseInfo.setRepresentations(repsInfo);
        return responseInfo;
    }
    
    private RequestInfo postMetadataQuery() {
        RequestInfo info = new RequestInfo();
        RepresentationInfo rep = new RepresentationInfo();
        rep.setIdentifier("metadataRepresentation");
        rep.setMediaType(MediaType.APPLICATION_XML);
        rep.setDocumentation("DataCite metadata");
        rep.setXmlElement("default:Resource");
        info.getRepresentations().add(rep);

        ParameterInfo param = new ParameterInfo();
        param.setName("selectedRole");
        param.setStyle(ParameterStyle.HEADER);        
        param.setRequired(false);
        param.setType("xs:string");
        param.setDocumentation("A user can select one role when he is associated to several roles");        
        info.getParameters().add(param);
                
        return info;
                     
    }
    
    @Override
    protected final void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("This request stores new version of metadata. "
                + "The request body must contain valid XML.");
        
        info.setRequest(postMetadataQuery());
        info.getResponses().add(postSuccessfullResponse());
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_BAD_REQUEST);
        responseInfo.setDocumentation("invalid XML, wrong prefix");
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
