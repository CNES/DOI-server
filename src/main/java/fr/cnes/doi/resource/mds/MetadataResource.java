/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.exception.ClientMdsException;
import static fr.cnes.doi.security.UtilsHeader.SELECTED_ROLE_PARAMETER;
import fr.cnes.doi.utils.Requirement;

import java.util.Arrays;
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
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadataResource extends BaseMdsResource {

    public static final String GET_METADATA = "Get a Metadata";

    public static final String DELETE_METADATA = "Delete a Metadata";    

    private String doiName;

    /**
     * Init by getting a DOI.
     * @throws ResourceException
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setDescription("This resource handles a metadata : retrieve, delete");
        this.doiName = getAttribute(DoiMdsApplication.DOI_TEMPLATE);
    }

    /**
     * Retuns the metadata for a given DOI.
     * @return the metadata for a given DOI
     * @throws ResourceException Will be thrown when an error happens                    
     */
    @Requirement(
            reqId = "DOI_SRV_060",
            reqName = "Récupération des métadonnées"
    )     
    @Get
    public Representation getMetadata() throws ResourceException {
        getLogger().entering(getClass().getName(), "getMetadata", this.doiName);        
        Representation rep;
        try {
            setStatus(Status.SUCCESS_OK);
            rep = this.doiApp.getClient().getMetadata(this.doiName);
        } catch (ClientMdsException ex) {
            getLogger().throwing(getClass().getName(), "getMetadata", ex);
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
        
        getLogger().exiting(getClass().getName(), "getMetadata");        
        return rep;
    }

    /**
     * Deletes a representation for a given DOI.
     * @return the deleted representation
     * @throws ResourceException Will be thrown when an error happens                         
     */
    @Requirement(
            reqId = "DOI_SRV_050",
            reqName = "Désactivation d'un DOI"
    )     
    @Delete
    public Representation deleteMetadata() throws ResourceException { 
        getLogger().entering(getClass().getName(), "deleteMetadata");
        
        Representation rep;
        try {
            Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
            String selectedRole = headers.getFirstValue(SELECTED_ROLE_PARAMETER, "");
            getLogger().entering(getClass().getName(), "deleteMetadata", new Object[]{this.doiName, selectedRole});
            checkPermission(this.doiName, selectedRole);
            setStatus(Status.SUCCESS_OK);
            rep = this.doiApp.getClient().deleteMetadata(this.doiName);
        } catch (ClientMdsException ex) {
            getLogger().throwing(getClass().getName(), "deleteMetadata", ex);
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
        
        getLogger().exiting(getClass().getName(), "deleteMetadata");
        return rep;
    }

    /**
     * Describes the GET method.
     * @param info Wadl description for GET method
     */
    @Requirement(
            reqId = "DOI_DOC_010",
            reqName = "Documentation des interfaces"
    )      
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific metadata");

        addRequestDocToMethod(info, createQueryParamDoc(DoiMdsApplication.DOI_TEMPLATE, ParameterStyle.TEMPLATE, "DOI name", true, "xs:string"));

        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", "metadataRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "no login", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "login problem or dataset belongs to another party", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_NOT_FOUND, "DOI does not exist in our database", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_GONE, "the requested dataset was marked inactive (using DELETE method)", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));
    }

    /**
     * Describes the DELETE method.
     * @param info Wadl description for DELETE method
     */
    @Requirement(
            reqId = "DOI_DOC_010",
            reqName = "Documentation des interfaces"
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
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "no login", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "login problem or dataset belongs to another party", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_NOT_FOUND, "DOI does not exist in our database", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));
    }
}
