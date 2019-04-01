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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.security.TokenSecurity.TimeUnit;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;

/**
 * Handle the creation and deletion of a token generated after authentication by login/password.
 * Role authorizer skip methods in this class.
 *
 */
public class AuthenticationResource extends AbstractResource {

    /**
     * Parameter for the user name {@value #USER_NAME}.
     */
    public static final String USER_NAME = "user";

    /**
     * Parameter for the user token {@value #USER_TOKEN}.
     */
    public static final String USER_TOKEN = "token";

    /**
     * Logger.
     */
    private volatile Logger LOG;

    /**
     * Instance of settings to get token properties.
     */
    private final DoiSettings doiSetting = DoiSettings.getInstance();

    /**
     * The token database.
     */
    private volatile AbstractTokenDBHelper tokenDB;

    /**
     * {@inheritDoc }
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        final AdminApplication app = (AdminApplication) getApplication();
        LOG = app.getLog();
        LOG.traceEntry();
        setDescription("This resource handles the authentication.");
        this.tokenDB = ((AdminApplication) this.getApplication()).getTokenDB();

        LOG.traceExit();
    }

    /**
     * Delete an IHM token
     *
     * @param mediaForm the token to be deleted
     */
    @Delete
    public void deleteToken(final Form mediaForm) {
        LOG.traceEntry("Parameters\n\tmediaForm : {}", mediaForm);
        final String token = mediaForm.getFirstValue(USER_TOKEN);
        this.tokenDB.deleteToken(token);
        LOG.traceExit();
    }

    /**
     * Creates and stores an IHM token.
     *
     * The token creation is based on several actions :
     * <ul>
     * <li>{@link #checkInputs checks the input parameters}</li>
     * <li>creates the {@link fr.cnes.doi.security.TokenSecurity#generate}</li>
     * <li>stores the token in {@link fr.cnes.doi.db.AbstractTokenDBHelper token database}</li>
     * </ul>
     *
     * @param mediaForm submitted information when requesting the token creation
     * @return a String representation of the token
     */
    @Post
    public Representation authenticate(final Form mediaForm) {
        LOG.traceEntry("Parameters\n\tmediaForm : {}", mediaForm);
        checkInputs(mediaForm);
        final String userName = mediaForm.getFirstValue(USER_NAME);

        // token is valid for 12 hours
        final int amount = doiSetting.getInt(Consts.TOKEN_EXPIRATION_DELAY, "12");
        final int timeUnit = doiSetting.getInt(
                Consts.TOKEN_EXPIRATION_UNIT,
                String.valueOf(TokenSecurity.TimeUnit.HOUR.getTimeUnit())
        );
        final TimeUnit unit = TokenSecurity.TimeUnit.getTimeUnitFrom(timeUnit);
        final String token = TokenSecurity.getInstance().generate(userName, unit, amount);
        final boolean isTokenAdded = this.tokenDB.addToken(token);

        LOG.info("Token created {} during {} {}", token, amount, unit.name());
        if (isTokenAdded) {
            LOG.info("Token saved in data base.");
        } else {
            LOG.info("Token coud not be saved in data base.");
        }

        if (token == null) {
            throw LOG.throwing(new ResourceException(
                    Status.SERVER_ERROR_INTERNAL, "Cannot generate the token"));
        } else {
            setStatus(Status.SUCCESS_OK);
        }
        return LOG.traceExit(new StringRepresentation(token, MediaType.TEXT_PLAIN));
    }

    /**
     * Tests if the {@link #USER_NAME} is set.
     *
     * @param mediaForm the parameters
     * @throws ResourceException - if USER_NAME is not set
     */
    private void checkInputs(final Form mediaForm) throws ResourceException {
        LOG.traceEntry("Parameters\n\tmediaForm : {}", mediaForm);
        if (isValueNotExist(mediaForm, USER_NAME)) {
            throw LOG.throwing(Level.ERROR, new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    USER_NAME + " parameter must be set"));
        }
        LOG.debug("The form is valid");
        LOG.traceExit();
    }

    @Override
    protected void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("Authentifies an user and returns a token");
        addRequestDocToMethod(info, createQueryParamDoc(
                USER_NAME, ParameterStyle.MATRIX,
                "User ID of the operator that creates the token", true, "xs:string")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, "Operation successful",
                stringRepresentation())
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SERVER_ERROR_INTERNAL, "Cannot generate the token")
        );
    }
}
