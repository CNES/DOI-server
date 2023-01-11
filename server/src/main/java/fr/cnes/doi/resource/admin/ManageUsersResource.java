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
package fr.cnes.doi.resource.admin;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.resource.AbstractResource;
import java.util.ArrayList;

/**
 * Provide resources to get all users.
 */
public class ManageUsersResource extends AbstractResource {

    /**
     * Parameter for the user name {@value #USER_NAME_PARAMETER}. This parameter
     * is send to associate an user to a project.
     */
    public static final String USER_NAME_PARAMETER = "user";

    /**
     * Logger.
     */
    private volatile Logger LOG;


    /**
     * User name.
     */
    private volatile String userName;

    /**
     * Set-up method that can be overridden in order to initialize the state of
     * the resource.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        final AdminApplication app = (AdminApplication) getApplication();
        LOG = app.getLog();
        LOG.traceEntry();
        setDescription("This resource handles association between users and projects");
        this.userName = getAttribute("userName");
        LOG.debug(this.userName);

        LOG.traceExit();
    }

    /**
     * Get users.
     *
     * @return users.
     */
    @Get
    public List<String> getUsers() {
        LOG.traceEntry();
        try {
            final List<String> users = new ArrayList<>();
            final AbstractUserRoleDBHelper manageUsers = PluginFactory.getUserManagement();
            final List<DOIUser> doiUsers = manageUsers.getUsers();
            for (final DOIUser doiUser : doiUsers) {
                users.add(doiUser.getUsername());
            }
            return LOG.traceExit(users);
        } catch (DOIDbException ex) {
            throw LOG.throwing(new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage()));
        }
    }
}
