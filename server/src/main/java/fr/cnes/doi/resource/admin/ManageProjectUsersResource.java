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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.ArrayList;

/**
 * Provide resources to get all users, bind or delete a user to/from a project.
 */
public class ManageProjectUsersResource extends AbstractResource {

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
     * Suffix of the project.
     */
    private volatile String suffixProject;

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
        this.suffixProject = getAttribute("suffixProject");
        this.userName = getAttribute("userName");
        LOG.debug(this.suffixProject);
        LOG.debug(this.userName);

        LOG.traceExit();
    }

    //TODO requirement
    /**
     * Get users.
     *
     * @return users.
     */
    @Get
    public String getUsers() {
        LOG.traceEntry();
        try {
            final int idProject = Integer.parseInt(suffixProject);
            final List<String> users = new ArrayList<>();
            final AbstractUserRoleDBHelper manageUsers = PluginFactory.getUserManagement();
            final List<DOIUser> doiUsers = manageUsers.getUsersFromRole(idProject);
            for (final DOIUser doiUser : doiUsers) {
                users.add(doiUser.getUsername());
            }
            ObjectMapper mapper = new ObjectMapper();
            
            
            return LOG.traceExit(mapper.writeValueAsString(users));
        } catch (DOIDbException ex) {
            throw LOG.throwing(new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage()));
        } catch (JsonProcessingException e) {
        	throw LOG.throwing(new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage()));
		}
    }

    //TODO requirement
    /**
     * Adds user to project
     *
     * @param mediaForm form
     */
    @Requirement(reqId = Requirement.DOI_SRV_140, reqName = Requirement.DOI_SRV_140_NAME)
    @Post
    public void addUserToProject(final Form mediaForm) {
        LOG.traceEntry("Parameters\n\tmediaForm : {}", mediaForm);
        checkInputs(mediaForm);
        final String user = mediaForm.getFirstValue(USER_NAME_PARAMETER);
        final int idProject = Integer.parseInt(suffixProject);
        final AbstractUserRoleDBHelper manageUsers = PluginFactory.getUserManagement();
        if (manageUsers.isUserExist(user)) {
            if (manageUsers.addUserToRole(user, idProject)) {
                setStatus(Status.SUCCESS_NO_CONTENT);
            } else {
                throw LOG.throwing(new ResourceException(
                        Status.SERVER_ERROR_INTERNAL, "Can't add user to project"));
            }
        } else {
            throw LOG.throwing(new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, "User " + user + " not found"));
        }
        LOG.traceExit();
    }

    //TODO requirement
    /**
     * Delete the project
     *
     */
    @Requirement(reqId = Requirement.DOI_SRV_140, reqName = Requirement.DOI_SRV_140_NAME)
    @Delete
    public void deleteProject() {
        LOG.traceEntry();
        final int idProject = Integer.parseInt(suffixProject);
        final AbstractUserRoleDBHelper manageUsers = PluginFactory.getUserManagement();
        if (manageUsers.removeUserToRole(userName, idProject)) {
            setStatus(Status.SUCCESS_NO_CONTENT);
        } else {
            throw LOG.throwing(new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Can't remove user " + userName));
        }
    }

    /**
     * Tests if the {@link #USER_NAME_PARAMETER} is set.
     *
     * @param mediaForm the parameters
     * @throws ResourceException - if USER_NAME_PARAMETER is not set
     */
    private void checkInputs(final Form mediaForm) throws ResourceException {
        LOG.traceEntry("Parameters\n\tmediaForm : {}", mediaForm);
        if (isValueNotExist(mediaForm, USER_NAME_PARAMETER)) {
            throw LOG.throwing(Level.DEBUG, new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    USER_NAME_PARAMETER + " parameter must be set"));
        }
        LOG.debug("The form is valid");
        LOG.traceExit();
    }

}
