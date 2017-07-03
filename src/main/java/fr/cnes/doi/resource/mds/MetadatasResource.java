/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.client.ClientMdsException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import org.datacite.schema.kernel_4.Resource;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.xml.sax.SAXException;

/**
 * Resource to handle a collection of metadata.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadatasResource extends BaseMdsResource {

    public static final String CREATE_METADATA = "Create Metadata";
    
    /**
     * Init.
     * @throws ResourceException
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setDescription("This resource can create metadata");
    }

    /**
     * Create Metadata for a given DOI.
     * The DOI name is given in the metadata entity.
     * This request stores new version of metadata. The different status:
     * <ul>
     * <li>201 Created - operation successful</li>
     * <li>400 Bad Request - invalid XML, wrong prefix</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem, quota exceeded</li>
     * <li>500 Internal Server Error - server internal error, try later and if 
     * problem persists please contact us</li>
     * </ul>
     * @param entity Metadata representation
     * @return short explanation of status code e.g. CREATED, HANDLE_ALREADY_EXISTS etc
     * @throws ResourceException Will be thrown when an error happens                         
     */
    @Post
    public String createMetadata(final Representation entity) throws ResourceException {
        
        //TODO : replace DOI name when PRE_PROD
        getLogger().entering(getClass().getName(), "createMetadata");
        String result;
        try {
            setStatus(Status.SUCCESS_CREATED);
            JAXBContext ctx = JAXBContext.newInstance(new Class[]{org.datacite.schema.kernel_4.Resource.class});
            Unmarshaller um = ctx.createUnmarshaller();
            Schema schema = this.doiApp.getSchemaFactory().newSchema(new URL("https://schema.datacite.org/meta/kernel-4.0/metadata.xsd"));
            um.setSchema(schema);
            um.setEventHandler(new MyValidationEventHandler(getLogger()));
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
        } catch (ClientMdsException ex) {
            getLogger().exiting(getClass().getName(), "createMetadata", ex.getDetailMessage());
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
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

    /**
     * Describes a POST method.
     * @param info Wadl description for POST method
     */
    @Override
    protected final void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("This request stores new version of metadata. "
                + "The request body must contain valid XML.");

        addRequestDocToMethod(info,
                Arrays.asList(
                        createQueryParamDoc("selectedRole", ParameterStyle.HEADER, "A user can select one role when he is associated to several roles", false, "xs:string")
                ),
                createQueryRepresentationDoc("metadataRepresentation", MediaType.APPLICATION_XML, "DataCite metadata", "default:Resource")
        );

        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_CREATED, "Operation successful", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_BAD_REQUEST, "invalid XML, wrong prefix", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "no login", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "login problem, quota exceeded", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));
    }

    /**
     * Metadata Validation.
     */
    private static class MyValidationEventHandler implements ValidationEventHandler {
        
        private final Logger logger;

        public MyValidationEventHandler(final Logger logger) {
            this.logger = logger;
        }

        @Override
        public boolean handleEvent(final ValidationEvent event) {            
            StringBuilder sb = new StringBuilder("\nEVENT");
            sb = sb.append("SEVERITY:  ").append(event.getSeverity()).append("\n");
            sb = sb.append("MESSAGE:  ").append(event.getMessage()).append("\n");
            sb = sb.append("LINKED EXCEPTION:  ").append(event.getLinkedException()).append("\n");
            sb = sb.append("LOCATOR\n");            
            sb = sb.append("    LINE NUMBER:  ").append(event.getLocator().getLineNumber()).append("\n");
            sb = sb.append("    COLUMN NUMBER:  ").append(event.getLocator().getColumnNumber()).append("\n");
            sb = sb.append("    OFFSET:  ").append(event.getLocator().getOffset()).append("\n");
            sb = sb.append("    OBJECT:  ").append(event.getLocator().getObject()).append("\n");
            sb = sb.append("    NODE:  ").append(event.getLocator().getNode()).append("\n");
            sb = sb.append("    URL  ").append(event.getLocator().getURL()).append("\n");
            this.logger.warning(sb.toString());
            return true;
        }
    }
}
