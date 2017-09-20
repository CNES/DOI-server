/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.BaseApplication;
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
import org.xml.sax.SAXException;

import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.utils.spec.CoverageAnnotation;
import fr.cnes.doi.utils.spec.Requirement;
import javax.xml.bind.ValidationException;

/**
 * Resource to handle a collection of metadata.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadatasResource extends BaseMdsResource {

    public static final String CREATE_METADATA = "Create Metadata";

    public static final String SCHEMA_DATACITE = "https://schema.datacite.org/meta/kernel-4.0/metadata.xsd";

    /**
     * Init.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setDescription("This resource can create metadata");
    }

    /**
     * Create Metadata for a given DOI. The DOI name is given in the metadata
     * entity. This request stores new version of metadata and returns a 201
     * status when the operation is successful.
     *
     * @param entity Metadata representation
     * @return short explanation of status code e.g. CREATED,
     * HANDLE_ALREADY_EXISTS etc
     * @throws ResourceException - if an error happens <ul>
     * <li>400 Bad Request - invalid XML, wrong prefix in the metadata</li>
     * <li>403 Forbidden - Not allow to execute this request </li>
     * <li>401 Unauthorized - this request needs authentication</li>
     * <li>409 Conflict if a user is associated to more than one role</li>
     * <li>500 Internal Server Error - DataCite Schema not available or problem
     * when requesting DataCite</li>
     * <li>1001 Connector error - Network problem</li>
     * </ul>
     */
    @Requirement(
            reqId = Requirement.DOI_SRV_010,
            reqName = Requirement.DOI_SRV_010_NAME
    )
    @Requirement(
            reqId = Requirement.DOI_SRV_040,
            reqName = Requirement.DOI_SRV_040_NAME
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
    public String createMetadata(final Representation entity) throws ResourceException {
        getLogger().entering(getClass().getName(), "createMetadata");

        checkInputs(entity);
        String result;
        try {
            setStatus(Status.SUCCESS_CREATED);
            org.datacite.schema.kernel_4.Resource resource = createDataCiteResourceObject(entity);
            String selectedRole = extractSelectedRoleFromRequestIfExists();
            checkPermission(resource.getIdentifier().getValue(), selectedRole);
            resource.setPublisher("CNES");
            result = this.doiApp.getClient().createMetadata(resource);
        } catch (ClientMdsException ex) {
            getLogger().throwing(getClass().getName(), "createMetadata", ex);
            ((BaseApplication) getApplication()).sendAlertWhenDataCiteFailed(ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(),  ex);
        } catch (ValidationException ex) {
            getLogger().throwing(getClass().getName(), "createMetadata", ex);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "invalid XML", ex);
        } catch (JAXBException ex) {
            getLogger().throwing(getClass().getName(), "createMetadata", ex);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "invalid XML", ex);
        } catch (SAXException ex) {
            getLogger().throwing(getClass().getName(), "createMetadata", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "DataCite schema not available", ex);
        } catch (IOException ex) {
            getLogger().throwing(getClass().getName(), "createMetadata", ex);
            throw new ResourceException(Status.CONNECTOR_ERROR_COMMUNICATION, "Network problem", ex);
        }
        getLogger().exiting(getClass().getName(), "createMetadata", result);
        return result;
    }

    /**
     * Checks inputs.
     *
     * @param obj object to check
     * @throws ResourceException - 400 Bad Request if entity is null
     */
    @Requirement(
            reqId = Requirement.DOI_INTER_070,
            reqName = Requirement.DOI_INTER_070_NAME
    )    
    private void checkInputs(final Object obj) {
        getLogger().entering(this.getClass().getName(), "checkInputs");
        if (isObjectNotExist(obj)) {
            ResourceException ex = new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Entity cannot be null");
            getLogger().throwing(this.getClass().getName(), "checkInputs", ex);
            throw ex;
        }
        getLogger().exiting(this.getClass().getName(), "checkInputs");
    }

    /**
     * Creates the metadata object from its representation.
     *
     * @param entity metadata representation
     * @return the metadata object
     * @throws JAXBException - if an error was encountered while creating the
     * JAXBContex
     * @throws SAXException - if a SAX error occurs during parsing.
     * @throws IOException - if a problem happens when retrieving the entity
     * @throws ValidationException - if metadata is not valid against the schema
     */
    @Requirement(
            reqId = Requirement.DOI_INTER_060,
            reqName = Requirement.DOI_INTER_060_NAME
    )
    @Requirement(
            reqId = Requirement.DOI_INTER_070,
            reqName = Requirement.DOI_INTER_070_NAME
    )    
    private org.datacite.schema.kernel_4.Resource createDataCiteResourceObject(final Representation entity) throws JAXBException, SAXException, ValidationException, IOException {
        JAXBContext ctx = JAXBContext.newInstance(new Class[]{org.datacite.schema.kernel_4.Resource.class});
        Unmarshaller um = ctx.createUnmarshaller();
        Schema schema = this.doiApp.getSchemaFactory().newSchema(new URL(SCHEMA_DATACITE));
        um.setSchema(schema);
        MyValidationEventHandler validationHandler = new MyValidationEventHandler(getLogger());
        um.setEventHandler(validationHandler);
        JAXBElement<Resource> jaxbResource = (JAXBElement<Resource>) um.unmarshal(entity.getStream());
        if (validationHandler.isValid()) {
            return jaxbResource.getValue();
        } else {
            throw new ValidationException(validationHandler.getErrorMsg());
        }
    }

    /**
     * Describes a POST method.
     *
     * @param info Wadl description for POST method
     */
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
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
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_BAD_REQUEST, "invalid XML, wrong prefix in the metadata", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "this request needs authentication", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "Not allow to execute the request", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "DataCite Schema not available or problem when requesting DataCite", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CONNECTOR_ERROR_COMMUNICATION, "Network problem", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_CONFLICT, "Error when an user is associated to more than one role", "explainRepresentation"));

    }

    /**
     * Metadata Validation.
     */
    @Requirement(
            reqId = Requirement.DOI_ARCHI_020,
            reqName = Requirement.DOI_ARCHI_020_NAME,
            coverage = CoverageAnnotation.PARTIAL,
            comment = "Log4J n'est pas utilisé"
    )    
    private static class MyValidationEventHandler implements ValidationEventHandler {

        private final Logger logger;
        private boolean hasError = false;
        private String errorMsg = null;

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
            this.errorMsg = sb.toString();
            this.logger.warning(this.errorMsg);
            this.hasError = true;
            return true;
        }

        /**
         * Returns true when metadata is valid against the schema otherwise
         * false.
         *
         * @return true when metadata is valid against the schema otherwise
         * false
         */
        public boolean isValid() {
            return !this.isNotValid();
        }

        /**
         * Returns true when metadata is not valid against the schema otherwise
         * false.
         *
         * @return true when metadata is not valid against the schema otherwise
         * false
         */
        public boolean isNotValid() {
            return this.hasError;
        }

        /**
         * Returns the errorMsg or null when no error message.
         *
         * @return the errorMsg or null when no error message
         */
        public String getErrorMsg() {
            return this.errorMsg;
        }
    }

}
