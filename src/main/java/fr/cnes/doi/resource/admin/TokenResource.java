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
package fr.cnes.doi.resource.admin;

import fr.cnes.doi.application.AdminApplication;
import static fr.cnes.doi.application.AdminApplication.TOKEN_TEMPLATE;
import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.exception.TokenSecurityException;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.security.TokenSecurity.TimeUnit;
import fr.cnes.doi.utils.spec.Requirement;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * Provides a resource to create token and to decrypt token
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_INTER_040, reqName = Requirement.DOI_INTER_040_NAME)
public class TokenResource extends AbstractResource {

    /**
     * User ID of the operator that creates the token.
     */
    public static final String IDENTIFIER_PARAMETER = "identifier";

    /**
     * Token for a specific project.
     */
    public static final String PROJECT_ID_PARAMETER = "projectID";

    /**
     * Unit of time used to define the expiration time of the token.
     */
    public static final String UNIT_OF_TIME_PARAMETER = "typeOfTime";

    /**
     * Amount of time for which the token is not expirated.
     */
    public static final String AMOUNT_OF_TIME_PARAMETER = "amountTime";

    /**
     * Token parameter catched from the URL.
     */
    private volatile String tokenParam;

    /**
     * The token database.
     */
    private volatile AbstractTokenDBHelper tokenDB;

    /**
     * Logger.
     */
    private volatile Logger LOG;

    /**
     * Set-up method that can be overridden in order to initialize the state of the resource
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        final AdminApplication app = (AdminApplication) getApplication();
        LOG = app.getLog();
        LOG.traceEntry();
        setDescription("This resource handles the token");
        this.tokenParam = getAttribute(TOKEN_TEMPLATE);
        this.tokenDB = ((AdminApplication) this.getApplication()).getTokenDB();
        LOG.debug("Token Param : {}", this.tokenParam);
        LOG.traceExit();
    }

    /**
     * Creates and stores a token.
     *
     * The token creation is based on several actions :
     * <ul>
     * <li>{@link #checkInputs checks the input parameters}</li>
     * <li>creates the {@link fr.cnes.doi.security.TokenSecurity#generate}</li>
     * <li>stores the token in {@link fr.cnes.doi.db.AbstractTokenDBHelper token database}</li>
     * </ul>
     *
     * @param info submitted information when requesting the token creation
     * @return the token
     */
    @Requirement(reqId = Requirement.DOI_SRV_150, reqName = Requirement.DOI_SRV_150_NAME)
    @Post
    public String createToken(final Form info) {
        LOG.traceEntry("Paramater : {}", info);
        checkInputs(info);
        try {
            final String userID = info.getFirstValue(IDENTIFIER_PARAMETER, null);
            final String projectID = info.getFirstValue(PROJECT_ID_PARAMETER, null);
            final String timeParam = info.getFirstValue(UNIT_OF_TIME_PARAMETER, "0");

            final int timeUnit = "0".equals(timeParam) ? 1 : Integer.parseInt(timeParam);
            final TimeUnit unit = TokenSecurity.TimeUnit.getTimeUnitFrom(timeUnit);

            final int amount = Integer.parseInt(info.getFirstValue(AMOUNT_OF_TIME_PARAMETER, "1"));

            final String tokenJwt = TokenSecurity.getInstance().generate(
                    userID,
                    Integer.parseInt(projectID),
                    unit,
                    amount
            );
            LOG.info("Token created {} for project {} during {} {}",
                    tokenJwt, projectID, amount, unit.name());

            this.tokenDB.addToken(tokenJwt);

            return LOG.traceExit(tokenJwt);
        } catch (TokenSecurityException ex) {
            throw LOG.throwing(Level.DEBUG, new ResourceException(ex.getStatus(), ex.getMessage(),
                    ex));
        }
    }

    /**
     * Checks input parameters.
     *
     * @param mediaForm the parameters
     * @throws ResourceException - if {@link #PROJECT_ID_PARAMETER} and
     * {@link #IDENTIFIER_PARAMETER} are not set
     */
    private void checkInputs(final Form mediaForm) throws ResourceException {
        LOG.traceEntry("Parameter : {}", mediaForm);
        final StringBuilder errorMsg = new StringBuilder();
        if (isValueNotExist(mediaForm, IDENTIFIER_PARAMETER)) {
            errorMsg.append(IDENTIFIER_PARAMETER).append(" value is not set.");
        }
        if (isValueNotExist(mediaForm, PROJECT_ID_PARAMETER)) {
            errorMsg.append(PROJECT_ID_PARAMETER).append(" value is not set.");
        }
        if (errorMsg.length() == 0) {
            LOG.debug("The form is valid");
        } else {
            throw LOG.throwing(Level.DEBUG, new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, errorMsg.toString()));
        }
    }

    /**
     * Returns the information from the token encoded as JSon format.
     *
     * @return the included information in the token
     */
    @Requirement(reqId = Requirement.DOI_SRV_180, reqName = Requirement.DOI_SRV_180_NAME)
    @Get
    public Representation getTokenInformation() {
        LOG.traceEntry();
        try {
            final Jws<Claims> jws = TokenSecurity.getInstance()
                    .getTokenInformation(this.tokenParam);
            return LOG.traceExit(new JsonRepresentation(jws));
        } catch (DoiRuntimeException ex) {
            throw LOG.throwing(Level.DEBUG, new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    ex));
        }
    }

    /**
     * projects representation
     *
     * @return Wadl representation for projects
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    private RepresentationInfo jsonRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.APPLICATION_JSON);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Json Representation");
        docInfo.setTextContent("The representation contains informations about the token.");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    /**
     * Describes GET method.
     *
     * @param info method info
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    @Override
    protected void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get information about a specific token");
        addRequestDocToMethod(info, createQueryParamDoc(
                TOKEN_TEMPLATE, ParameterStyle.TEMPLATE,
                "token", true, "xs:string")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, "Operation successful", jsonRepresentation())
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_BAD_REQUEST, "Wrong token")
        );
    }

    /**
     * Describes POST method.
     *
     * @param info method info
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    @Override
    protected void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("Creates a token");
        addRequestDocToMethod(info, createQueryParamDoc(
                IDENTIFIER_PARAMETER, ParameterStyle.MATRIX,
                "User ID of the operator that creates the token", true, "xs:string")
        );
        addRequestDocToMethod(info, createQueryParamDoc(
                PROJECT_ID_PARAMETER, ParameterStyle.MATRIX,
                "Token for a specific project", true, "xs:string")
        );
        addRequestDocToMethod(info, createQueryParamDoc(
                UNIT_OF_TIME_PARAMETER, ParameterStyle.MATRIX,
                "Unit of time used to define the expiration time of the token",
                false, "xs:int")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, "Operation successful",
                stringRepresentation())
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_BAD_REQUEST, "Submitted values are not valid")
        );
    }

}
