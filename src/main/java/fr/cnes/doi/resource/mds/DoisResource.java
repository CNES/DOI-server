/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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

import fr.cnes.doi.application.AbstractApplication;
import fr.cnes.doi.client.ClientMDS;
import java.util.Arrays;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.exception.ClientMdsException;
import static fr.cnes.doi.security.UtilsHeader.SELECTED_ROLE_PARAMETER;
import static fr.cnes.doi.client.ClientMDS.POST_DOI;
import static fr.cnes.doi.client.ClientMDS.POST_URL;
import fr.cnes.doi.utils.spec.Requirement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Resource to handle a collection of DOI.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DoisResource extends BaseMdsResource {

    /**
     * 
     */
    public static final String LIST_ALL_DOIS = "List all DOIs";

    /**
     * 
     */
    public static final String CREATE_DOI = "Create a DOI";
        

    /**
     * Init.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        LOG.traceEntry();
        setDescription("The resource contains the list of DOI and can create a new DOI");
        LOG.traceExit();
    }

    /**
     * Returns the collection of DOI. This request returns a list of all DOIs
     * for the requesting datacentre. Status 200 is returned when operation is
     * successful.
     *
     * @return the list of DOI
     * @throws ResourceException 204 No Content - no DOIs founds
     */       
    @Get
    public Representation getDois() throws ResourceException {
        LOG.traceEntry();
        final Representation rep;
        try {
            setStatus(Status.SUCCESS_OK);
            final String dois = this.getDoiApp().getClient().getDoiCollection();
            if (dois != null && !dois.isEmpty()) {
                setStatus(Status.SUCCESS_OK);
            } else {
                setStatus(Status.SUCCESS_NO_CONTENT);
            }            
            rep = new StringRepresentation(dois, MediaType.TEXT_URI_LIST);
        } catch (ClientMdsException ex) {
            ((AbstractApplication) getApplication()).sendAlertWhenDataCiteFailed(ex);            
            throw LOG.throwing(new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(), ex));
        }        
        return LOG.traceExit(rep);
    }

    /**
     * Creates a new DOI based on the doi and url parameters
     *
     * The DOI name is built following this syntax:
     * <i>CNES_prefix</i>/<i>project_provided_the_DOI_server</i>/project_suffix.
     *
     * <p>
     * The client provides :
     * <ul>
     * <li><i>doi</i> : the project suffix, which is an unique identifier within
     * the project.</li>
     * <li><i>url</i> : the URL of the landing page.
     * </ul>
     * <b>Note 1</b> : the landing page should be accessible from www
     * <b>Note 2</b> : Metadata must be uploaded first
     *
     * <p>
     * Will mint new DOI if specified DOI doesn't exist. This method will
     * attempt to update URL if you specify existing DOI. Standard domains and
     * quota restrictions check will be performed. A Datacentre's doiQuotaUsed
     * will be increased by 1. A new record in Datasets will be created by
     * returning a 201 status for operation successful.
     *
     * @param doiForm doi and url
     * @return short explanation of status code e.g. CREATED,
     * HANDLE_ALREADY_EXISTS etc
     * @throws ResourceException - if an error happens <ul>
     * <li>400 Bad Request - request body must be exactly two lines: DOI and
     * URL; wrong domain, wrong prefix</li>
     * <li>401 Unauthorized - if no role is provided or forbidden role</li>
     * <li>403 Forbidden - if the role is not allowed to use this feature or the
     * user is not allow to create the DOI</li>
     * <li>409 Conflict if a user is associated to more than one role</li>
     * <li>412 Precondition failed - metadata must be uploaded first</li>
     * <li>500 Internal Server Error - Error when requesting DataCite</li>
     * </ul>
     */ 
    @Requirement(
        reqId = Requirement.DOI_SRV_020,
        reqName = Requirement.DOI_SRV_020_NAME
        )   
    @Requirement(
        reqId = Requirement.DOI_SRV_030,
        reqName = Requirement.DOI_SRV_030_NAME
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
    @Post("form")
    public String createDoi(final Form doiForm) throws ResourceException {
        LOG.traceEntry("Parameter : {}", doiForm);
        checkInputs(doiForm);
        final String result;
        final String selectedRole = extractSelectedRoleFromRequestIfExists();
        checkPermission(doiForm.getFirstValue(DOI_PARAMETER), selectedRole);
        try {
            setStatus(Status.SUCCESS_CREATED);
            result = this.getDoiApp().getClient().createDoi(doiForm);
        } catch (ClientMdsException ex) {
            if (ex.getStatus().getCode() == Status.CLIENT_ERROR_PRECONDITION_FAILED.getCode()) {
                throw LOG.throwing(new ResourceException(ex.getStatus(), ex.getMessage(), ex));
            } else {
                ((AbstractApplication) getApplication()).sendAlertWhenDataCiteFailed(ex);
                throw LOG.throwing(new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(), ex));
            }
        }

        return LOG.traceExit(result);
    }

    /**
     * Checks input parameters. Checks that {@value #DOI_PARAMETER} and
     * {@value #URL_PARAMETER} are provided in the mediaForm.
     *
     * @param mediaForm the parameters {@value #DOI_PARAMETER} and
     * {@value #URL_PARAMETER}
     * @throws ResourceException - 400 Bad Request if DOI_PARAMETER and
     * URL_PARAMETER are not set
     */  
    @Requirement(
        reqId = Requirement.DOI_INTER_070,
        reqName = Requirement.DOI_INTER_070_NAME
        )    
    private void checkInputs(final Form mediaForm) throws ResourceException {
        LOG.traceEntry("Parameter : {}",mediaForm);
        StringBuilder errorMsg = new StringBuilder();
        if (isValueNotExist(mediaForm, DOI_PARAMETER)) {
            errorMsg = errorMsg.append(DOI_PARAMETER).append(" value is not set.");
        } else {
            try {
                ClientMDS.checkIfAllCharsAreValid(mediaForm.getFirstValue(DOI_PARAMETER));
            } catch (IllegalArgumentException ex) {
                errorMsg = errorMsg.append(DOI_PARAMETER).append(" no valid syntax.");
            } 
        }
        if (isValueNotExist(mediaForm, URL_PARAMETER)) {
            errorMsg = errorMsg.append(URL_PARAMETER).append(" value is not set.");
        }
        if (errorMsg.length() == 0) {
            LOG.debug("The form is valid");
        } else {
            throw LOG.throwing(new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, errorMsg.toString()));
        }
        LOG.traceExit();
    }

    /**
     * Retuns the sucessfull representation.
     *
     * @return the Wadl Representation
     */
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    private RepresentationInfo successFullRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_URI_LIST);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("DOI collection representation");
        docInfo.setTextContent("This request returns a list of all DOIs for the "
                + "requesting datacentre. There is no guaranteed order.");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    /**
     * Returns the no content representation.
     *
     * @return the Wadl description
     */
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    private RepresentationInfo noContentRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Empty representation");
        docInfo.setTextContent("No contain");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    /**
     * Returns the exit status representation.
     *
     * @return the exit status representation
     */
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    private RepresentationInfo explainStatusRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setIdentifier("explainRepresentation");
        repInfo.setMediaType(MediaType.TEXT_PLAIN);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Explain representation");
        docInfo.setTextContent("short explanation of status code e.g. CREATED, "
                + "HANDLE_ALREADY_EXISTS etc");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    /**
     * Describes the GET method.
     *
     * @param info Wadl description
     */
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Retrieves the DOI collection");
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, "Operation successful", successFullRepresentation()));
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_NO_CONTENT, "no DOIs founds", noContentRepresentation()));
        addResponseDocToMethod(info, createResponseDoc(
                Status.SERVER_ERROR_INTERNAL, "Unexpected error"));        
    }

    /**
     * Request representation.
     *
     * @return representation
     */
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    private RepresentationInfo requestRepresentation() {
        final RepresentationInfo rep = new RepresentationInfo(MediaType.APPLICATION_WWW_FORM);
        rep.getParameters().add(createQueryParamDoc(
                POST_DOI, ParameterStyle.PLAIN, "DOI name", true, "xs:string"));
        rep.getParameters().add(createQueryParamDoc(
                POST_URL, ParameterStyle.PLAIN, "URL of the landing page", true, "xs:string"));
        return rep;
    }

    /**
     * Describes the POST method.
     *
     * @param info Wadl description
     */
    @Requirement(
        reqId = Requirement.DOI_DOC_010,
        reqName = Requirement.DOI_DOC_010_NAME
        )      
    @Override
    protected final void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("POST will mint new DOI if specified DOI doesn't exist. "
                + "This method will attempt to update URL if you specify existing DOI. "
                + "Standard domains and quota restrictions check will be performed. "
                + "A Datacentre's doiQuotaUsed will be increased by 1. "
                + "A new record in Datasets will be created.");

        addRequestDocToMethod(info,
                Arrays.asList(
                        createQueryParamDoc(SELECTED_ROLE_PARAMETER, 
                                ParameterStyle.HEADER, 
                                "A user can select one role when he is associated "
                                        + "to several roles", false, "xs:string")
                ),
                requestRepresentation());

        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_CREATED, "Operation successful", explainStatusRepresentation()));
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_BAD_REQUEST, 
                "request body must be exactly two lines: DOI and URL; wrong domain, "
                        + "wrong prefix", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_UNAUTHORIZED, 
                "this request need an authorization", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_FORBIDDEN, 
                "Not allow to execute this request", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_PRECONDITION_FAILED, 
                "metadata must be uploaded first", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SERVER_ERROR_INTERNAL, 
                "Error when requesting DataCite", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_CONFLICT, 
                "Error when an user is associated to more than one role without "
                        + "setting selectedRole parameter", 
                "explainRepresentation")
        );

    }
}
