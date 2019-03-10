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
package fr.cnes.doi.utils;

import fr.cnes.doi.db.persistence.model.DOIUser;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.db.MyMemoryRealm;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.plugin.PluginFactory;

/**
 * Utils class to manage users from database
 *
 */
public class ManageUsers {

    /**
     * Class name.
     */
    private static final String CLASS_NAME = ManageProjects.class.getName();

    /**
     * logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static ManageUsers getInstance() {
        return ManageUsersHolder.INSTANCE;
    }

    /**
     * Users database.
     */
    private final AbstractUserRoleDBHelper userDB;

    /**
     * Constructor
     */
    private ManageUsers() {
        LOGGER.entering(CLASS_NAME, "Constructor");
        this.userDB = PluginFactory.getUserManagement();
        this.userDB.init(new Object()); // not used
        LOGGER.exiting(CLASS_NAME, "Constructor");
    }

    public List<String> getAllUsersFromProject(final int projectId) {
        LOGGER.entering(CLASS_NAME, "getAllUsersFromProject", projectId);
        final List<String> users = new ArrayList<>();
        List<DOIUser> doiUsers = userDB.getUsersFromRole(projectId);
        for (DOIUser doiUser : doiUsers) {
            users.add(doiUser.getUsername());
        }
        return users;
    }

    public boolean addUserToProject(final String username, final int projectId) {
        LOGGER.entering(CLASS_NAME, "addUserToProject", new Object[]{username, projectId});
        final boolean isAdded;
        if (this.isUserExist(username)) {
            isAdded = userDB.addUserToRole(username, projectId);
        } else {
            isAdded = false;
        }
        return isAdded;
    }

    public boolean deleteUserFromProject(final int projectId, final String username) {
        LOGGER.entering(CLASS_NAME, "deleteUserFromProject", new Object[]{projectId, username});
        return userDB.removeUserToRole(username, projectId);
    }

    public boolean isUserExist(final String userName) {
        LOGGER.entering(CLASS_NAME, "isUserExist", userName);
        return userDB.isUserExist(userName);
    }

    public List<DOIUser> getUsers() {
        return userDB.getUsers();
    }

    public void removeDOIUser(final String username) {
        userDB.removeDOIUser(username);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void addDOIUser(final String username, final boolean b, final String email) {
        try {
            userDB.addDOIUser(username, b, email);
        } catch (DOIDbException ex) {
            LOGGER.log(Level.SEVERE, "Cannot add " + username, ex);
        }
    }

    public MyMemoryRealm getRealm() {
        return userDB.getRealm();
    }

    public List<DOIUser> getUsersFromRole(final Integer projectID) {
        return userDB.getUsersFromRole(projectID);
    }

    /**
     * Class to handle the instance
     *
     */
    private static class ManageUsersHolder {

        /**
         * Unique Instance unique
         */
        private static final ManageUsers INSTANCE = new ManageUsers();
    }
}
