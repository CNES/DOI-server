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
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.utils.spec.Requirement;

import java.util.Arrays;
import org.datacite.schema.kernel_4.Resource;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Resources to handle a metadata.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadataResource extends BaseMdsResource {

    /**
     * 
     */
    public static final String GET_METADATA = "Get a Metadata";

    /**
     * 
     */
    public static final String DELETE_METADATA = "Delete a Metadata";    

    /**
     * DOI name, which is set on the URL template.
     */
    private String doiName;

    /**
     * Init by getting a DOI.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();        
        LOG.traceEntry();
        setDescription("This resource handles a metadata : retrieve, delete");
        this.doiName = getResourcePath().replace(DoiMdsApplication.METADATAS_URI+"/", "");
        LOG.debug("DOI name "+this.doiName);
        LOG.traceExit();
    }

    /**
     * Retuns the metadata for a given DOI. 200 status is returned when the
     * operation is successful.
     *
     * @return the metadata for a given DOI as Json or XML
     * @throws ResourceException - if an error happens<ul>
     * <li>400 Bad Request - DOI's syntax is not valid</li>     
     * <li>404 Not Found - DOI does not exist in DataCite</li>
     * <li>410 Gone - the requested dataset was marked inactive (using DELETE
     * method)</li>
     * <li>500 Internal Server Error - Error when calling DataCite</li>
     * </ul>
     */ 
    @Requirement(reqId = Requirement.DOI_SRV_060,reqName = Requirement.DOI_SRV_060_NAME)   
    @Requirement(reqId = Requirement.DOI_MONIT_020,reqName = Requirement.DOI_MONIT_020_NAME)      
    @Get("xml|json")
    public Resource getMetadata() throws ResourceException {
        LOG.traceEntry();
        checkInputs(doiName);
        final Resource resource;
        try {
            setStatus(Status.SUCCESS_OK);
            resource = this.getDoiApp().getClient().getMetadataAsObject(this.doiName);
        } catch (ClientMdsException ex) {
            if (ex.getStatus().getCode() == Status.CLIENT_ERROR_NOT_FOUND.getCode() || ex.getStatus().getCode() == Status.CLIENT_ERROR_GONE.getCode()) {
                throw LOG.throwing(new ResourceException(ex.getStatus(), ex.getMessage(), ex));
            } else {
                ((AbstractApplication)getApplication()).sendAlertWhenDataCiteFailed(ex);
                throw LOG.throwing(new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(), ex));
            }
        }
        return LOG.traceExit(resource);
    }

    /**
     * Deletes a representation for a given DOI. 200 status when the operation
     * is successful.
     *
     * @return the deleted representation
     * @throws ResourceException - if an error happens <ul>
     * <li>400 Bad request - if the DOI's syntax is not valid</li>     
     * <li>401 Unauthorized if no role is provided</li>
     * <li>403 Forbidden - if the role is not allowed to use this feature or the
     * user is not allow to create the DOI</li>
     * <li>404 Not found id the DOI is not found in DataCite</li>
     * <li>409 Conflict if a user is associated to more than one role</li>
     * <li>500 Internal Server Error - Error when calling DataCite</li>
     * </ul>
     */ 
    @Requirement(reqId = Requirement.DOI_SRV_050,reqName = Requirement.DOI_SRV_050_NAME)   
    @Requirement(reqId = Requirement.DOI_MONIT_020,reqName = Requirement.DOI_MONIT_020_NAME)  
    @Requirement(reqId = Requirement.DOI_INTER_070,reqName = Requirement.DOI_INTER_070_NAME)    
    @Requirement(reqId = Requirement.DOI_AUTO_020,reqName = Requirement.DOI_AUTO_020_NAME)     
    @Requirement(reqId = Requirement.DOI_AUTO_030,reqName = Requirement.DOI_AUTO_030_NAME)     
    @Delete
    public Representation deleteMetadata() throws ResourceException {
        LOG.traceEntry();
        checkInputs(this.doiName);
        final Representation rep;
        try {
            final String selectedRole = extractSelectedRoleFromRequestIfExists();
            checkPermission(this.doiName, selectedRole);
            setStatus(Status.SUCCESS_OK);
            rep = this.getDoiApp().getClient().deleteMetadata(this.doiName);
        } catch (ClientMdsException exception) {
            if (exception.getStatus().getCode() == Status.CLIENT_ERROR_NOT_FOUND.getCode()) {
                throw LOG.traceExit(new ResourceException(
                        exception.getStatus(), exception.getDetailMessage(), 
                        exception)
                );
            } else {
                ((AbstractApplication)getApplication())
                        .sendAlertWhenDataCiteFailed(exception);            
                throw LOG.traceExit(new ResourceException(
                        Status.SERVER_ERROR_INTERNAL, exception.getDetailMessage(), 
                        exception)
                );
            }
        }
        return LOG.traceExit(rep);
    }

    /**
     * Describes the GET method.
     *
     * @param info Wadl description for GET method
     */
    @Requirement(reqId = Requirement.DOI_DOC_010,reqName = Requirement.DOI_DOC_010_NAME)      
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific metadata");

        addRequestDocToMethod(info, createQueryParamDoc(
                DoiMdsApplication.DOI_TEMPLATE, ParameterStyle.TEMPLATE, 
                "DOI name", true, "xs:string")
        );

        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, "Operation successful", 
                "metadataRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_NOT_FOUND, 
                "DOI does not exist in DataCite", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_GONE, 
                "the requested dataset was marked inactive (using DELETE method)", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SERVER_ERROR_INTERNAL, 
                "server internal error, try later and if problem persists please "
                        + "contact us", 
                "explainRepresentation")
        );
    }

    /**
     * Describes the DELETE method.
     *
     * @param info Wadl description for DELETE method
     */
    @Requirement(reqId = Requirement.DOI_DOC_010,reqName = Requirement.DOI_DOC_010_NAME)      
    @Override
    protected final void describeDelete(final MethodInfo info) {
        info.setName(Method.DELETE);
        info.setDocumentation("Delete a specific metadata");

        addRequestDocToMethod(info,
                Arrays.asList(
                        createQueryParamDoc(DoiMdsApplication.DOI_TEMPLATE, 
                                ParameterStyle.TEMPLATE, "DOI name", true, "xs:string"
                        ),
                        createQueryParamDoc("selectedRole", ParameterStyle.HEADER, 
                                "A user can select one role when he is associated "
                                        + "to several roles", false, "xs:string"
                        )
                )
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, 
                "Operation successful", 
                "metadataRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_UNAUTHORIZED, 
                "if no role is provided", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_FORBIDDEN, 
                "if the role is not allowed to use this feature or the role is "
                        + "not allow to delete the DOI", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_NOT_FOUND, 
                "DOI does not exist in our database", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_CONFLICT, 
                "Error when a user is associated to more than one role without "
                        + "setting selectedRole parameter", 
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SERVER_ERROR_INTERNAL, 
                "server internal error, try later and if problem persists please "
                        + "contact us", 
                "explainRepresentation")
        );
    } 
    
    /**
     * Checks input parameters
     * @param doiName DOI number
     * @throws ResourceException - 400 Bad Request if DOI_PARAMETER is not set
     */ 
    @Requirement(reqId = Requirement.DOI_INTER_070,reqName = Requirement.DOI_INTER_070_NAME)    
    private void checkInputs(final String doiName) throws ResourceException {
        LOG.traceEntry("Parameter : {}",doiName);
        StringBuilder errorMsg = new StringBuilder();
        if(doiName == null || doiName.isEmpty()) {
            errorMsg = errorMsg.append(DoiMdsApplication.DOI_TEMPLATE).append(" value is not set.");  
        } else {
            try {
                ClientMDS.checkIfAllCharsAreValid(doiName);
            } catch(IllegalArgumentException ex) {
                errorMsg = errorMsg.append(DoiMdsApplication.DOI_TEMPLATE).append(" no valid syntax.");
            }
        }
        if(errorMsg.length() == 0) {        
            LOG.debug("The input is valid");                    
        } else {
            throw LOG.throwing(new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, errorMsg.toString()));            
        }          
        LOG.traceExit();
    }
}
