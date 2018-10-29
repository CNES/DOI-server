/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.application.DoiMdsApplication.API_MDS;
import java.io.IOException;
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
import org.xml.sax.SAXException;

import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.exception.DoiServerException;
import fr.cnes.doi.utils.spec.Requirement;
import javax.xml.bind.ValidationException;
import org.apache.logging.log4j.Level;
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
     * The cache that may contains the Datacite schema.
     */
    private DoiMdsApplication.CacheSchema cache;    
    /**
     * Init.
     *
     * @throws DoiServerException - if a problem happens
     */
    @Override
    protected void doInit() throws DoiServerException {
        super.doInit();
        LOG.traceEntry();        
        setDescription("This resource can create metadata");
        this.cache = this.getDoiApp().getCache();
        LOG.traceExit();
    }

    /**
     * Create Metadata for a given DOI. The DOI name is given in the metadata entity. This request
     * stores new version of metadata and returns a 201 status when the operation is successful.
     *
     * @param entity Metadata representation
     * @return short explanation of status code e.g. CREATED, HANDLE_ALREADY_EXISTS etc
     * @throws DoiServerException - if the response is not a success 
     * <ul>
     * <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
     * <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li>     
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * <li>{@link API_MDS#METADATA_VALIDATION}</li> 
     * <li>{@link API_MDS#NETWORK_PROBLEM}</li>
     * </ul>
     */
    @Requirement(reqId = Requirement.DOI_SRV_010, reqName = Requirement.DOI_SRV_010_NAME)
    @Requirement(reqId = Requirement.DOI_SRV_040, reqName = Requirement.DOI_SRV_040_NAME)
    @Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    @Requirement(reqId = Requirement.DOI_AUTO_020, reqName = Requirement.DOI_AUTO_020_NAME)
    @Requirement(reqId = Requirement.DOI_AUTO_030, reqName = Requirement.DOI_AUTO_030_NAME)
    @Post
    public String createMetadata(final Representation entity) throws DoiServerException {
        LOG.traceEntry("Entering in createMetadata with argument " + entity);
        checkInputs(entity);
        final String result;
        try {
            setStatus(Status.SUCCESS_CREATED);
            if (!this.cache.isStored()) {                  
                this.cache.store(SCHEMA_DATACITE);                 
            }                         
            final Resource resource = createDataCiteResourceObject(entity, this.cache.getCache());
            final String selectedRole = extractSelectedRoleFromRequestIfExists();
            checkPermission(resource.getIdentifier().getValue(), selectedRole);
            resource.setPublisher("CNES");
            result = this.getDoiApp().getClient().createMetadata(resource, this.cache.getCache());
        } catch (ClientMdsException ex) {
            LOG.error("*** code *** - " + ex.getStatus().getCode());
            if(ex.getStatus().getCode() == 1001) {
                LOG.error("*** 1 ***");
            }            
            throw LOG.throwing(Level.DEBUG, 
                    new DoiServerException(getApplication(), API_MDS.DATACITE_PROBLEM, ex.getMessage(), ex)
            );
        } catch (ValidationException ex) {
            throw LOG.throwing(Level.DEBUG, 
                    new DoiServerException(getApplication(),API_MDS.METADATA_VALIDATION,"invalid XML",ex)
            );
        } catch (JAXBException ex) {
            throw LOG.throwing(Level.DEBUG, 
                    new DoiServerException(getApplication(),API_MDS.METADATA_VALIDATION,"invalid XML",ex)
            );
        } catch (SAXException ex) {
            throw LOG.throwing(Level.DEBUG, 
                    new DoiServerException(getApplication(),API_MDS.NETWORK_PROBLEM,"DataCite schema not available",ex)
            );
        } catch (IOException ex) {
            throw LOG.throwing(Level.DEBUG, 
                    new DoiServerException(getApplication(), API_MDS.NETWORK_PROBLEM, "Unknown network problem", ex)
            );
        }
        return LOG.traceExit(result);
    }

    /**
     * Checks inputs.
     * 
     * Checks if <i>obj</i> is {@link #isObjectExist set}
     *
     * @param obj object to check
     * @throws DoiServerException - {@link API_MDS#METADATA_VALIDATION}
     */
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    private void checkInputs(final Object obj) throws DoiServerException {
        LOG.traceEntry("Parameter : " + obj);
        if (isObjectNotExist(obj)) {          
            throw LOG.throwing(Level.DEBUG, 
                    new DoiServerException(getApplication(),API_MDS.METADATA_VALIDATION, "Input is not set")
            );
        }
        LOG.traceExit();
    }

    /**
     * Creates the metadata object from its representation.
     *
     * @param entity metadata representation
     * @return the metadata object
     * @throws JAXBException - if an error was encountered while creating the JAXBContex
     * @throws SAXException - if a SAX error occurs during parsing.
     * @throws IOException - if a problem happens when retrieving the entity
     * @throws ValidationException - if metadata is not valid against the schema
     */
    @Requirement(reqId = Requirement.DOI_INTER_060, reqName = Requirement.DOI_INTER_060_NAME)
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    private Resource createDataCiteResourceObject(final Representation entity, final Schema schema) 
            throws JAXBException, SAXException, ValidationException, IOException {
        LOG.traceEntry("Parameter : " + entity);
        final JAXBContext ctx = JAXBContext.newInstance(new Class[]{Resource.class});
        final Unmarshaller unMarshal = ctx.createUnmarshaller();
        unMarshal.setSchema(schema);
        final MyValidationEventHandler validationHandler = new MyValidationEventHandler(LOG);
        unMarshal.setEventHandler(validationHandler);
        final Resource resource = (Resource) unMarshal.unmarshal(entity.getStream()); 
        if (validationHandler.isValid()) {                                   
            return LOG.traceExit(resource);
        } else {
            throw LOG.throwing(Level.DEBUG, new ValidationException(validationHandler.getErrorMsg()));
        }
    }

    /**
     * Describes the POST method.
     * 
     * This request stores new version of DOI metadata. The request body must contain a valid XML.
     * 
     * <ul>
     * <li>{@link API_MDS#CREATE_METADATA}</li>
     * <li>{@link API_MDS#METADATA_VALIDATION}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * <li>{@link API_MDS#NETWORK_PROBLEM}</li>
     * <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
     * <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li> 
     * </ul>
     *
     * @param info Wadl description for POST method
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    @Override
    protected final void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("This request stores new version of metadata. "
                + "The request body must contain a valid XML.");

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

        addResponseDocToMethod(info, createResponseDoc(API_MDS.CREATE_METADATA.getStatus(),
                API_MDS.CREATE_METADATA.getShortMessage(),                
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(API_MDS.METADATA_VALIDATION.getStatus(),
                API_MDS.METADATA_VALIDATION.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(API_MDS.DATACITE_PROBLEM.getStatus(),
                API_MDS.DATACITE_PROBLEM.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(API_MDS.NETWORK_PROBLEM.getStatus(),
                API_MDS.NETWORK_PROBLEM.getShortMessage(),
                "explainRepresentationID")
        );
        super.describePost(info);

    }

    /**
     * Metadata Validation.
     */
    @Requirement(reqId = Requirement.DOI_ARCHI_020, reqName = Requirement.DOI_ARCHI_020_NAME)
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
         *
         * @param logger logger
         */
        public MyValidationEventHandler(final Logger logger) {
            this.logger = logger;
        }

        /**
         * Handles event
         *
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
         * Returns true when metadata is valid against the schema otherwise false.
         *
         * @return true when metadata is valid against the schema otherwise false
         */
        public boolean isValid() {
            this.logger.traceEntry();
            return this.logger.traceExit(!this.isNotValid());

        }

        /**
         * Returns true when metadata is not valid against the schema otherwise false.
         *
         * @return true when metadata is not valid against the schema otherwise false
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
