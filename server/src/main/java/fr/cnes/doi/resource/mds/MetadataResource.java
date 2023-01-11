/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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

import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.application.DoiMdsApplication.API_MDS;
import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.client.ClientMDS.DATACITE_API_RESPONSE;
import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.exception.DoiServerException;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Resources to handle a metadata.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadataResource extends BaseMdsResource {

    /**
     * Function of this resource {@value #GET_METADATA}.
     */
    public static final String GET_METADATA = "Get a Metadata";

    /**
     * Function of this resource {@value #DELETE_METADATA}.
     */
    public static final String DELETE_METADATA = "Delete a Metadata";

    /**
     * DOI name, which is set on the URL template.
     */
    private volatile String doiName;

    /**
     * Init by getting a DOI.
     *
     * @throws DoiServerException - if a problem happens
     */
    @Override
    protected void doInit() throws DoiServerException {
        super.doInit();
        LOG.traceEntry();
        setDescription("This resource handles a metadata : retrieve, delete");
        this.doiName = getAttributePath(DoiMdsApplication.METADATAS_URI);
        LOG.debug("DOI name " + this.doiName);
        LOG.traceExit();
    }

    /**
     * Checks input parameters
     *
     * @param doiName DOI number
     * @throws DoiServerException - 400 Bad Request if DOI_PARAMETER is not set
     */
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    private void checkInputs(final String doiName) throws DoiServerException {
        LOG.traceEntry("Parameter\n\tdoiName : {}", doiName);
        StringBuilder errorMsg = new StringBuilder();
        if (doiName == null || doiName.isEmpty()) {
            errorMsg = errorMsg.append(DoiMdsApplication.DOI_TEMPLATE).append("value is not set.");
        } else {
            try {
                ClientMDS.checkIfAllCharsAreValid(doiName);
            } catch (IllegalArgumentException ex) {
                errorMsg = errorMsg.append(DoiMdsApplication.DOI_TEMPLATE).
                        append("no valid syntax.");
            }
        }
        if (errorMsg.length() == 0) {
            LOG.debug("The input is valid");
        } else {
            throw LOG.throwing(
                    Level.ERROR,
                    new DoiServerException(getApplication(), Status.CLIENT_ERROR_BAD_REQUEST,
                            errorMsg.toString())
            );
        }
        LOG.traceExit();
    }

    /**
     * Retuns the metadata for a given DOI. 200 status is returned when the
     * operation is successful.
     *
     * @return the metadata for a given DOI as Json or XML
     * @throws DoiServerException - if the response is not a success
     * <ul>
     * <li>{@link DATACITE_API_RESPONSE#BAD_REQUEST}</li>
     * <li>{@link DATACITE_API_RESPONSE#DOI_NOT_FOUND}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * </ul>
     */
    @Requirement(reqId = Requirement.DOI_SRV_060, reqName = Requirement.DOI_SRV_060_NAME)
    @Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
    @Get("xml")
    public Representation getMetadata() throws DoiServerException {
        LOG.traceEntry();
        checkInputs(doiName);
        final Representation resource;
        try {
            setStatus(Status.SUCCESS_OK);
            resource = this.getDoiApp().getClient().getMetadata(this.doiName);
        } catch (ClientMdsException ex) {
            if (ex.getStatus().getCode() == Status.CLIENT_ERROR_NOT_FOUND.getCode()) {
                throw LOG.throwing(
                        Level.ERROR,
                        new DoiServerException(getApplication(), DATACITE_API_RESPONSE.DOI_NOT_FOUND,
                                ex)
                );
            } else if (ex.getStatus().getCode() == Status.CLIENT_ERROR_BAD_REQUEST.getCode()) {
                throw LOG.throwing(
                        Level.ERROR,
                        new DoiServerException(getApplication(), DATACITE_API_RESPONSE.BAD_REQUEST,
                                ex)
                );
            } else {
                throw LOG.throwing(
                        Level.ERROR,
                        new DoiServerException(getApplication(), API_MDS.DATACITE_PROBLEM, ex)
                );
            }
        }
        return LOG.traceExit(resource);
    }

    /**
     * Deletes a representation for a given DOI. 200 status when the operation
     * is successful.
     *
     * @return the deleted representation
     * @throws DoiServerException - if the response is not a success
     * <ul>
     * <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
     * <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li>
     * <li>{@link DATACITE_API_RESPONSE#DOI_NOT_FOUND}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * </ul>
     */
    @Requirement(reqId = Requirement.DOI_SRV_050, reqName = Requirement.DOI_SRV_050_NAME)
    @Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    @Requirement(reqId = Requirement.DOI_AUTO_020, reqName = Requirement.DOI_AUTO_020_NAME)
    @Requirement(reqId = Requirement.DOI_AUTO_030, reqName = Requirement.DOI_AUTO_030_NAME)
    @Delete
    public Representation deleteMetadata() throws DoiServerException {
        LOG.traceEntry();
        checkInputs(this.doiName);
        final Representation rep;
        try {
            final String selectedRole = extractSelectedRoleFromRequestIfExists();
            checkPermission(this.doiName, selectedRole);
            setStatus(Status.SUCCESS_OK);
            rep = this.getDoiApp().getClient().deleteMetadata(this.doiName);
        } catch (ClientMdsException ex) {
            if (ex.getStatus().getCode() == Status.CLIENT_ERROR_NOT_FOUND.getCode()) {
                throw LOG.throwing(
                        Level.ERROR,
                        new DoiServerException(getApplication(), DATACITE_API_RESPONSE.DOI_NOT_FOUND,
                                ex)
                );
            } else {
                throw LOG.throwing(
                        Level.ERROR,
                        new DoiServerException(getApplication(), API_MDS.DATACITE_PROBLEM, ex)
                );
            }
        }
        return LOG.traceExit(rep);
    }

    /**
     * Describes the GET method. The different representations are the
     * followings:
     * <ul>
     * <li>{@link DATACITE_API_RESPONSE#SUCCESS}</li>
     * <li>{@link DATACITE_API_RESPONSE#DOI_NOT_FOUND}</li>
     * <li>{@link DATACITE_API_RESPONSE#DOI_INACTIVE}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * </ul>
     *
     * @param info Wadl description for GET method
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific metadata");

        addRequestDocToMethod(info, createQueryParamDoc(
                DoiMdsApplication.DOI_TEMPLATE, ParameterStyle.TEMPLATE,
                "DOI name", true, "xs:string")
        );

        addResponseDocToMethod(info, createResponseDoc(
                DATACITE_API_RESPONSE.SUCCESS.getStatus(),
                DATACITE_API_RESPONSE.SUCCESS.getShortMessage(),
                "metadataRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                DATACITE_API_RESPONSE.DOI_NOT_FOUND.getStatus(),
                DATACITE_API_RESPONSE.DOI_NOT_FOUND.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                DATACITE_API_RESPONSE.DOI_INACTIVE.getStatus(),
                DATACITE_API_RESPONSE.DOI_INACTIVE.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.DATACITE_PROBLEM.getStatus(),
                API_MDS.DATACITE_PROBLEM.getShortMessage(),
                "explainRepresentationID")
        );
    }

    /**
     * Describes the DELETE method. The different representations are the
     * followings:
     * <ul>
     * <li>{@link DATACITE_API_RESPONSE#SUCCESS}</li>
     * <li>{@link DATACITE_API_RESPONSE#DOI_NOT_FOUND}</li>
     * <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
     * <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * </ul>
     *
     * @param info Wadl description for DELETE method
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
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
                DATACITE_API_RESPONSE.SUCCESS.getStatus(),
                DATACITE_API_RESPONSE.SUCCESS.getShortMessage(),
                "metadataRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                DATACITE_API_RESPONSE.DOI_NOT_FOUND.getStatus(),
                DATACITE_API_RESPONSE.DOI_NOT_FOUND.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.DATACITE_PROBLEM.getStatus(),
                API_MDS.DATACITE_PROBLEM.getShortMessage(),
                "explainRepresentationID")
        );
        super.describeDelete(info);
    }
}
