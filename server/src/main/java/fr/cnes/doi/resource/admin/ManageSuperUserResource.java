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

import org.apache.logging.log4j.Logger;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.application.AdminApplication;
import static fr.cnes.doi.application.AdminApplication.USERS_NAME_TEMPLATE;
import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.resource.AbstractResource;
import org.apache.logging.log4j.Level;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;

/**
 * Provide a resource to ask if a user belong to the super user group and another one to remove a
 * user from the super user group.
 */
public class ManageSuperUserResource extends AbstractResource {

    /**
     * Logger.
     */
    private volatile Logger LOG;

    /**
     * User name.
     */
    private volatile String userName;

    /**
     * Set-up method that can be overridden in order to initialize the state of the resource.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        final AdminApplication app = (AdminApplication) getApplication();
        LOG = app.getLog();
        LOG.traceEntry();
        this.userName = getAttribute(USERS_NAME_TEMPLATE);
        LOG.debug(this.userName);
        setDescription("This resource handles super user");
        LOG.traceExit();
    }

    // TODO requirement
    /**
     * Returns null is user doesn't exist otherwise return true or false if user is admin or not.
     *
     * @return boolean.
     */
    @Get
    public boolean isUserExistAndAdmin() {
        LOG.traceEntry();
        final AbstractUserRoleDBHelper manageUsers = PluginFactory.getUserManagement();
        if (!manageUsers.isUserExist(userName)) {
            throw LOG.throwing(Level.ERROR, new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "The user " + userName + " does not exist"));
        }
        return LOG.traceExit(manageUsers.isAdmin(userName));
    }

    // TODO requirement
    /**
     * Delete the SUPERUSER from database.
     *
     */
    // @Requirement(reqId = Requirement.DOI_SRV_140, reqName =
    // Requirement.DOI_SRV_140_NAME)
    @Delete
    public void deleteSuperUser() {
        LOG.traceEntry();
        final AbstractUserRoleDBHelper manageUsers = PluginFactory.getUserManagement();
        if (manageUsers.unsetUserFromAdminGroup(userName)) {
            setStatus(Status.SUCCESS_NO_CONTENT);
        } else {
            throw LOG.throwing(new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, "Can't delete super user " + userName));
        }
        LOG.traceExit();
    }

    @Override
    protected void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Checks if the user is admin");
        addRequestDocToMethod(info, createQueryParamDoc(
                USERS_NAME_TEMPLATE, ParameterStyle.TEMPLATE,
                "user name", true, "xs:string")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_NO_CONTENT, "Operation successful with true or false",
                stringRepresentation())
        );

        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_BAD_REQUEST, "The user does not exist",
                htmlRepresentation())
        );
    }

    @Override
    protected void describeDelete(final MethodInfo info) {
        info.setName(Method.DELETE);
        info.setDocumentation("Delete a project");
        addRequestDocToMethod(info, createQueryParamDoc(
                USERS_NAME_TEMPLATE, ParameterStyle.TEMPLATE,
                "user name", true, "xs:string")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_NO_CONTENT, "Operation successful",
                stringRepresentation())
        );

        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_BAD_REQUEST, "Cannot delete super user",
                htmlRepresentation())
        );
    }

}
