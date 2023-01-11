/*
 * Copyright (C) 2017-2021 Centre National d'Etudes Spatiales (CNES).
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

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import fr.cnes.doi.application.DoiMdsApplication.API_MDS;
import fr.cnes.doi.exception.DoiServerException;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Resource to get the INIST Code from server.
 *
 * @author Capgemini
 */
@SuppressWarnings("deprecation")
public class InistResource extends BaseMdsResource {

    /**
     * Function of this resource {@value #GET_INIST_CODE}.
     */
    public static final String GET_INIST_CODE = "Get the INIST code";

    /**
     * Init.
     *
     * @throws DoiServerException - if a problem happens
     */
    @Override
    protected void doInit() throws DoiServerException {
        super.doInit();
        LOG.traceEntry();
        setDescription("This resource can retrieve the INIST code");
        LOG.traceExit();
    }

    /**
     * Retrieve the INIST code from server.
     *
     * @return short explanation of status code e.g. CREATED,
     * HANDLE_ALREADY_EXISTS etc
     * @throws DoiServerException - if the response is not a success
     */
    @Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    @Requirement(reqId = Requirement.DOI_AUTO_020, reqName = Requirement.DOI_AUTO_020_NAME)
    @Requirement(reqId = Requirement.DOI_AUTO_030, reqName = Requirement.DOI_AUTO_030_NAME)
    @Get
    public Representation getInistCode() throws DoiServerException {
    	LOG.traceEntry();
        setStatus(Status.SUCCESS_OK);
        String sInistCode = this.getDoiApp().getDataCentrePrefix();
        return new StringRepresentation(LOG.traceExit("\"" + sInistCode + "\""),
            MediaType.TEXT_PLAIN, Language.ENGLISH, CharacterSet.UTF_8);
    }


    /**
     * Describes the GET method.
     *
     * This request is for retrieve the INIST code.
     *
     * <ul>
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
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("This request retrieve the INIST CODE.");

        addResponseDocToMethod(info,
	        createResponseDoc(
                Status.SUCCESS_OK,
                "Operation successful",
                stringRepresentation()));

        super.describeGet(info);
    }
}
