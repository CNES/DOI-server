/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.AbstractApplication;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import javax.xml.bind.JAXBContext;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Resource to handle a collection of metadata.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadatasResource extends BaseMdsResource {

    /**
     * 
     */
    public static final String CREATE_METADATA = "Create Metadata";

    /**
     * Datacite Schema.
     */
    public static final String SCHEMA_DATACITE = "https://schema.datacite.org/meta/kernel-4.0/metadata.xsd";
    
    /**
     * Init.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();                
        LOG.traceEntry();
        setDescription("This resource can create metadata");
        LOG.traceExit();
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
        LOG.traceEntry("Entering in createMetadata with argument "+entity);
        checkInputs(entity);
        final String result;
        try {
            setStatus(Status.SUCCESS_CREATED);
            final Resource resource = createDataCiteResourceObject(entity);
            final String selectedRole = extractSelectedRoleFromRequestIfExists();
            checkPermission(resource.getIdentifier().getValue(), selectedRole);
            resource.setPublisher("CNES");
            result = this.getDoiApp().getClient().createMetadata(resource);
        } catch (ClientMdsException ex) {
            ((AbstractApplication) getApplication()).sendAlertWhenDataCiteFailed(ex);
            throw LOG.traceExit(new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(),  ex));
        } catch (ValidationException ex) {
            throw LOG.traceExit(new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, 
                    "invalid XML", 
                    ex)
            );
        } catch (JAXBException ex) {
            throw LOG.traceExit(new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, 
                    "invalid XML", 
                    ex)
            );
        } catch (SAXException ex) {            
            throw LOG.traceExit(new ResourceException(
                    Status.SERVER_ERROR_INTERNAL, 
                    "DataCite schema not available", 
                    ex)
            );
        } catch (IOException ex) {           
            throw LOG.traceExit(new ResourceException(
                    Status.CONNECTOR_ERROR_COMMUNICATION, 
                    "Network problem", 
                    ex)
            );
        }
        return LOG.traceExit(result);
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
    private void checkInputs(final Object obj) throws ResourceException {
        LOG.traceEntry("Parameter : "+obj);
        if (isObjectNotExist(obj)) {
            throw LOG.traceExit(new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, "Entity cannot be null")
            );
        }
        LOG.traceExit();
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
    private Resource createDataCiteResourceObject(final Representation entity) throws JAXBException, SAXException, ValidationException, IOException {
        LOG.traceEntry("Parameter : "+entity);
        final JAXBContext ctx = JAXBContext.newInstance(new Class[]{Resource.class});
        final Unmarshaller unMarshal = ctx.createUnmarshaller();
        final Schema schema = this.getDoiApp().getSchemaFactory()
                .newSchema(new URL(SCHEMA_DATACITE));
        unMarshal.setSchema(schema);
        final MyValidationEventHandler validationHandler = 
                new MyValidationEventHandler(LOG);
        unMarshal.setEventHandler(validationHandler);
        final Resource resource = (Resource) unMarshal.unmarshal(entity.getStream());
        if (validationHandler.isValid()) {
            return LOG.traceExit(resource);
        } else {
            throw LOG.traceExit(new ValidationException(validationHandler.getErrorMsg()));
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
                        createQueryParamDoc("selectedRole", 
                                ParameterStyle.HEADER, 
                                "A user can select one role when he is associated "
                                        + "to several roles", false, "xs:string"
                        )
                ),
                createQueryRepresentationDoc("metadataRepresentation", 
                        MediaType.APPLICATION_XML, 
                        "DataCite metadata", 
                        "default:Resource"
                )
        );

        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_CREATED, 
                "Operation successful", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_BAD_REQUEST, 
                "invalid XML, wrong prefix in the metadata", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_UNAUTHORIZED, 
                "this request needs authentication", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_FORBIDDEN, 
                "Not allow to execute the request", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SERVER_ERROR_INTERNAL, 
                "DataCite Schema not available or problem when requesting DataCite", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CONNECTOR_ERROR_COMMUNICATION, 
                "Network problem", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_CONFLICT, 
                "Error when an user is associated to more than one role without setting selectedRole parameter", 
                "explainRepresentation")
        );

    }

    /**
     * Metadata Validation.
     */
@Requirement(
        reqId = Requirement.DOI_ARCHI_020,
        reqName = Requirement.DOI_ARCHI_020_NAME
)   
    private static class MyValidationEventHandler implements ValidationEventHandler {

        /**
         * Logger.
         */
        private final Logger logger;
        
        /**
         * Indicates if an error was happening.
         */
        private boolean hasError = false;
        
        /**
         * Error message.
         */
        private String errorMsg = null;

        /**
         * Validation handler
         * @param logger logger
         */
        public MyValidationEventHandler(final Logger logger) {
            this.logger = logger;
        }

        /**
         * Handles event
         * @param event event
         * @return True
         */
        @Override
        public boolean handleEvent(final ValidationEvent event) {
            final StringBuilder stringBuilder = new StringBuilder("\nEVENT");
            stringBuilder.append("SEVERITY:  ").append(event.getSeverity()).append("\n");
            stringBuilder.append("MESSAGE:  ").append(event.getMessage()).append("\n");
            stringBuilder.append("LINKED EXCEPTION:  ").append(event.getLinkedException()).append("\n");
            stringBuilder.append("LOCATOR\n");
            stringBuilder.append("    LINE NUMBER:  ").append(event.getLocator().getLineNumber()).append("\n");
            stringBuilder.append("    COLUMN NUMBER:  ").append(event.getLocator().getColumnNumber()).append("\n");
            stringBuilder.append("    OFFSET:  ").append(event.getLocator().getOffset()).append("\n");
            stringBuilder.append("    OBJECT:  ").append(event.getLocator().getObject()).append("\n");
            stringBuilder.append("    NODE:  ").append(event.getLocator().getNode()).append("\n");
            stringBuilder.append("    URL  ").append(event.getLocator().getURL()).append("\n");
            this.errorMsg = stringBuilder.toString();
            this.logger.info(this.errorMsg);
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
            this.logger.traceEntry();
            return this.logger.traceExit(!this.isNotValid());
            
        }

        /**
         * Returns true when metadata is not valid against the schema otherwise
         * false.
         *
         * @return true when metadata is not valid against the schema otherwise
         * false
         */
        public boolean isNotValid() {
            this.logger.traceEntry();
            return this.logger.traceExit(this.hasError);
        }

        /**
         * Returns the errorMsg or null when no error message.
         *
         * @return the errorMsg or null when no error message
         */
        public String getErrorMsg() {
            this.logger.traceEntry();
            return this.logger.traceExit(this.errorMsg);
        }
    }

}
