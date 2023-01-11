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
package fr.cnes.doi.resource.admin;

import fr.cnes.doi.application.AdminApplication;
import static fr.cnes.doi.application.AdminApplication.TOKEN_TEMPLATE;
import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.exception.TokenSecurityException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.security.RoleAuthorizer;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.security.TokenSecurity.TimeUnit;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.utils.spec.Requirement;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides a resource to create token and to decrypt token
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@SuppressWarnings("deprecation")
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
     * Set-up method that can be overridden in order to initialize the state of
     * the resource
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
     * <li>creates the {@link fr.cnes.doi.security.TokenSecurity#generate}</li>
     * <li>stores the token in
     * {@link fr.cnes.doi.db.AbstractTokenDBHelper token database}</li>
     * </ul>
     *
     * @param infoForm submitted information when requesting the token creation
     * @return the token
     */
    @Requirement(reqId = Requirement.DOI_SRV_150, reqName = Requirement.DOI_SRV_150_NAME)
    @Post
    public Representation createToken(final Form infoForm) {
        LOG.traceEntry("Paramater : {}", infoForm);
        final Form info = (infoForm == null) ? new Form() : infoForm;
        try {
            final String user = this.getClientInfo().getUser().getIdentifier();
            LOG.debug("Identified user : {}", user);
            final String userID;
            if (isInRole(RoleAuthorizer.ROLE_ADMIN)) {
                // The admin can generate for everybody
                LOG.debug("User {} is admin", user);
                userID = info.getFirstValue(IDENTIFIER_PARAMETER, user);
            } else {
                // The token is generated for the identified user.
                userID = user;
            }
            final String projectID = info.getFirstValue(PROJECT_ID_PARAMETER, null);
            final String defaultTimeUnit = DoiSettings.getInstance().getString(
                    Consts.TOKEN_EXPIRATION_UNIT,
                    String.valueOf(TokenSecurity.TimeUnit.HOUR.getTimeUnit())
            );
            final String defaultTimeDelay = DoiSettings.getInstance().getString(
                    Consts.TOKEN_EXPIRATION_DELAY, "1");

            final String timeParam = info.getFirstValue(UNIT_OF_TIME_PARAMETER, defaultTimeUnit);
            final int timeUnit = Integer.parseInt(timeParam);
            final TimeUnit unit = TokenSecurity.TimeUnit.getTimeUnitFrom(timeUnit);

            final int amount = Integer.parseInt(defaultTimeDelay);

            final String tokenJwt;
            if (projectID == null) {
                tokenJwt = TokenSecurity.getInstance().generate(
                        userID,
                        unit,
                        amount
                );
            } else {
                tokenJwt = TokenSecurity.getInstance().generate(
                        userID,
                        Integer.parseInt(projectID),
                        unit,
                        amount
                );
            }

            LOG.info("Token created {} for project {} during {} {}",
                    tokenJwt, projectID, amount, unit.name());

            if (this.tokenDB.addToken(tokenJwt)) {
                sendTokenToUser(user, userID, tokenJwt, amount, timeUnit);
            }

            ObjectMapper mapper = new ObjectMapper();

            return new StringRepresentation(LOG.traceExit(mapper.writeValueAsString(tokenJwt)),
    				MediaType.APPLICATION_JSON, Language.ENGLISH, CharacterSet.UTF_8);
        } catch (TokenSecurityException ex) {
            throw LOG.throwing(Level.INFO, new ResourceException(ex.getStatus(), ex.getMessage(),
                    ex));
        } catch (IOException e) {
            throw LOG.throwing(Level.INFO, new ResourceException(e));
        }
    }

    /**
     * Sends the token to the user when the administrator creates a token for
     * theuser
     *
     * @param userAdmin User administration
     * @param userID user to send the message
     * @param token created token for userID
     * @param amount time
     * @param timeUnit time unit
     */
    private void sendTokenToUser(final String userAdmin, final String userID, final String token,
            final int amount, final int timeUnit) {
        if (!userAdmin.equals(userID)) {
            try {
                final StringBuilder builderMsg = new StringBuilder();
                builderMsg.append("The administrator ").append(userAdmin)
                        .append(" has created a token for you:\n\n").append(token).append("\n\n")
                        .append("This token is valid during ").append(amount).append(" ")
                        .append(TokenSecurity.TimeUnit.getTimeUnitFrom(timeUnit).name());
                final List<DOIUser> doiUsers = PluginFactory.getUserManagement().getUsers();
                String email = "";
                for (final DOIUser doiUser : doiUsers) {
                    if (doiUser.getUsername().equals(userID)) {
                        email = doiUser.getEmail();
                        break;
                    }
                }
                if (email.isEmpty()) {
                    LOG.error("Email is not set for {}", userID);
                }
                EmailSettings.getInstance().sendMessage("Creating Token", builderMsg.toString(),
                        email);
            } catch (DOIDbException ex) {
                LOG.error(ex);
            }
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
            throw LOG.throwing(Level.INFO, new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    ex));
        }
    }

    /**
     * Deletes token.
     */
    @Requirement(reqId = Requirement.DOI_INTER_040, reqName = Requirement.DOI_INTER_040_NAME)
    @Delete
    public void deleteToken() {
        LOG.traceEntry();
        this.tokenDB.deleteToken(this.tokenParam);
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
