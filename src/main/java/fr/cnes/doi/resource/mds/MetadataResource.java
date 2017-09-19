/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.BaseApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.exception.ClientMdsException;
import static fr.cnes.doi.resource.mds.BaseMdsResource.DOI_PARAMETER;
import static fr.cnes.doi.security.UtilsHeader.SELECTED_ROLE_PARAMETER;
import fr.cnes.doi.utils.spec.Requirement;

import java.util.Arrays;
import java.util.logging.Level;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Resources to handle a metadata.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadataResource extends BaseMdsResource {

    public static final String GET_METADATA = "Get a Metadata";

    public static final String DELETE_METADATA = "Delete a Metadata";

    private String doiName;

    /**
     * Init by getting a DOI.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setDescription("This resource handles a metadata : retrieve, delete");
        this.doiName = getAttribute(DoiMdsApplication.DOI_TEMPLATE);
    }

    /**
     * Retuns the metadata for a given DOI. 200 status is returned when the
     * operation is successful.
     *
     * @return the metadata for a given DOI
     * @throws ResourceException - if an error happens<ul>
     * <li>400 Bad Request - DOI's syntax is not valid</li>     
     * <li>404 Not Found - DOI does not exist in DataCite</li>
     * <li>410 Gone - the requested dataset was marked inactive (using DELETE
     * method)</li>
     * <li>500 Internal Server Error - Error when calling DataCite</li>
     * </ul>
     */ 
    @Requirement(
            reqId = Requirement.DOI_SRV_060,
            reqName = Requirement.DOI_SRV_060_NAME
    )   
    @Requirement(
            reqId = Requirement.DOI_MONIT_020,
            reqName = Requirement.DOI_MONIT_020_NAME
    )      
    @Get
    public Representation getMetadata() throws ResourceException {
        getLogger().entering(getClass().getName(), "getMetadata", this.doiName);
        checkInputs(doiName);
        Representation rep;
        try {
            setStatus(Status.SUCCESS_OK);
            rep = this.doiApp.getClient().getMetadata(this.doiName);
        } catch (ClientMdsException ex) {
            getLogger().throwing(getClass().getName(), "getMetadata", ex);
            if (ex.getStatus().getCode() == 404 || ex.getStatus().getCode() == 410) {
                throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
            } else {
                ((BaseApplication)getApplication()).sendAlertWhenDataCiteFailed(ex);
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getDetailMessage());
            }
        }

        getLogger().exiting(getClass().getName(), "getMetadata");
        return rep;
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
    @Requirement(
            reqId = Requirement.DOI_SRV_050,
            reqName = Requirement.DOI_SRV_050_NAME
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
    @Delete
    public Representation deleteMetadata() throws ResourceException {
        getLogger().entering(getClass().getName(), "deleteMetadata");
        checkInputs(this.doiName);
        Representation rep;
        try {
            String selectedRole = extractSelectedRoleFromRequestIfExists();
            getLogger().entering(getClass().getName(), "deleteMetadata", new Object[]{this.doiName, selectedRole});
            checkPermission(this.doiName, selectedRole);
            setStatus(Status.SUCCESS_OK);
            rep = this.doiApp.getClient().deleteMetadata(this.doiName);
        } catch (ClientMdsException ex) {
            getLogger().throwing(getClass().getName(), "deleteMetadata", ex);
            if (ex.getStatus().getCode() == 404) {
                throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
            } else {
                ((BaseApplication)getApplication()).sendAlertWhenDataCiteFailed(ex);            
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getDetailMessage());
            }
        }

        getLogger().exiting(getClass().getName(), "deleteMetadata");
        return rep;
    }

    /**
     * Describes the GET method.
     *
     * @param info Wadl description for GET method
     */
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific metadata");

        addRequestDocToMethod(info, createQueryParamDoc(DoiMdsApplication.DOI_TEMPLATE, ParameterStyle.TEMPLATE, "DOI name", true, "xs:string"));

        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", "metadataRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_NOT_FOUND, "DOI does not exist in DataCite", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_GONE, "the requested dataset was marked inactive (using DELETE method)", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));
    }

    /**
     * Describes the DELETE method.
     *
     * @param info Wadl description for DELETE method
     */
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    @Override
    protected final void describeDelete(final MethodInfo info) {
        info.setName(Method.DELETE);
        info.setDocumentation("Delete a specific metadata");

        addRequestDocToMethod(info,
                Arrays.asList(
                        createQueryParamDoc(DoiMdsApplication.DOI_TEMPLATE, ParameterStyle.TEMPLATE, "DOI name", true, "xs:string"),
                        createQueryParamDoc("selectedRole", ParameterStyle.HEADER, "A user can select one role when he is associated to several roles", false, "xs:string")
                )
        );
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", "metadataRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "if no role is provided", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "if the role is not allowed to use this feature or the role is not allow to delete the DOI", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_NOT_FOUND, "DOI does not exist in our database", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_CONFLICT, "Error when a user is associated to more than one role", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));
    } 
    
    /**
     * Checks input parameters
     * @param doiName DOI number
     * @throws ResourceException - 400 Bad Request if DOI_PARAMETER is not set
     */ 
    @Requirement(
            reqId = Requirement.DOI_INTER_070,
            reqName = Requirement.DOI_INTER_070_NAME
    )    
    private void checkInputs(String doiName) throws ResourceException {
        getLogger().entering(this.getClass().getName(), "checkInputs", doiName);
        StringBuilder errorMsg = new StringBuilder();
        if(doiName == null || doiName.isEmpty()) {
            getLogger().log(Level.FINE, "{0} value is not set", DoiMdsApplication.DOI_TEMPLATE);
            errorMsg = errorMsg.append(DoiMdsApplication.DOI_TEMPLATE).append(" value is not set.");  
        } else {
            try {
                ClientMDS.checkIfAllCharsAreValid(doiName);
            } catch(IllegalArgumentException ex) {
                errorMsg = errorMsg.append(DoiMdsApplication.DOI_TEMPLATE).append(" no valid syntax.");
            }
        }
        if(errorMsg.length() == 0) {        
            getLogger().fine("The input is valid");                    
        } else {
            ResourceException ex =  new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, errorMsg.toString());            
            getLogger().throwing(this.getClass().getName(), "checkInputs", ex);
            throw ex;
        }          
        getLogger().exiting(this.getClass().getName(), "checkInputs"); 
    }
}
