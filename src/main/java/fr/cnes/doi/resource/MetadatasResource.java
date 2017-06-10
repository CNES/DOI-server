/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.client.ClientException;
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
 * Resourece to handle a collection of metadata.
 *
 * @author Jean-Christophe Malapert
 */
public class MetadatasResource extends BaseMdsResource {

    /**
     *
     */
    public static final String CREATE_METADATA = "Create Metadata";

    /**
     * @throws ResourceException
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setDescription("This resource can create metadata");
    }

    /**
     * https://search.datacite.org/help.html
     *
     * @param entity TODO update DOI name
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

    /**
     * @param info
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
