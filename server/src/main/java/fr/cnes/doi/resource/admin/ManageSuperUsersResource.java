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

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.resource.AbstractResource;
import java.util.ArrayList;

/**
 * Provide a resource to get all super users and add a user to the super user group.
 */
public class ManageSuperUsersResource extends AbstractResource {

    /**
     * Parameter for the SUPERUSER name {@value #SUPERUSER_NAME_PARAMETER}. This parameter is send
     * to create a new identifier for the SUPERUSER.
     */
    public static final String SUPERUSER_NAME_PARAMETER = "superUserName";

    /**
     * Logger.
     */
    private volatile Logger LOG;

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
        setDescription("This resource handles super users");
        LOG.traceExit();
    }

    // TODO requirement
    /**
     * Rename the SUPERUSER from the SUPERUSER id sent in url.
     *
     * @param mediaForm Data sent with request containing the user name to be super user.
     */
    // @Requirement(reqId = Requirement.DOI_SRV_140, reqName =
    // Requirement.DOI_SRV_140_NAME)
    @Post
    public void createSuperUser(final Form mediaForm) {
        LOG.traceEntry("Parameters\n\tmediaForm : {}", mediaForm);
        checkInputs(mediaForm);
        final String newSuperUserName = mediaForm.getFirstValue(SUPERUSER_NAME_PARAMETER);
        final AbstractUserRoleDBHelper manageUsers = PluginFactory.getUserManagement();
        if (!manageUsers.isUserExist(newSuperUserName)) {
            throw LOG.throwing(new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Can't find user "+newSuperUserName));
        } else if(manageUsers.setUserToAdminGroup(newSuperUserName)) {
            setStatus(Status.SUCCESS_NO_CONTENT);
        } else {
            throw LOG.throwing(new ResourceException(Status.SERVER_ERROR_INTERNAL, "Can't create user "+newSuperUserName));            
        }
    }

    // TODO requirement
    /**
     * Returns the list of superusers as an array format.
     *
     * @return the list of superusers as an array format
     */
    // @Requirement(reqId = Requirement.DOI_SRV_140, reqName =
    // Requirement.DOI_SRV_140_NAME)
    @Get
    public List<String> getSuperUsersAsJson(){
        LOG.traceEntry();        
        try {
            final ArrayList<String> result = new ArrayList<>();
            final AbstractUserRoleDBHelper manageUsers = PluginFactory.getUserManagement();
            final List<DOIUser> users = manageUsers.getUsers();
            for (final DOIUser doiUser : users) {
                if (doiUser.isAdmin()) {
                    result.add(doiUser.getUsername());
                }
            }
            return LOG.traceExit(result);
        } catch (DOIDbException ex) {
            throw LOG.throwing(new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage()));
        }
    }

    /**
     * Tests if the {@link #SUPERUSER_NAME_PARAMETER} is set.
     *
     * @param mediaForm the parameters
     * @throws ResourceException - if SUPERUSER_NAME_PARAMETER is not set
     */
    private void checkInputs(final Form mediaForm) throws ResourceException {
        LOG.traceEntry("Parameters\n\tmediaForm : {}", mediaForm);
        if (isValueNotExist(mediaForm, SUPERUSER_NAME_PARAMETER)) {
            throw LOG.throwing(Level.ERROR, new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    SUPERUSER_NAME_PARAMETER + " parameter must be set"));
        }
        LOG.debug("The form is valid");
        LOG.traceExit();
    }      
}
