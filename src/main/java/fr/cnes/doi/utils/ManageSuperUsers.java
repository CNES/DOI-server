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
import java.util.logging.Logger;

import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.plugin.PluginFactory;

/**
 * Utils class to manage SuperUsers from database
 *
 */
public class ManageSuperUsers {

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
    public static ManageSuperUsers getInstance() {
        return ManageSuperUsersHolder.INSTANCE;
    }

    /**
     * SuperUsers database.
     */
    private final AbstractUserRoleDBHelper userDB;

    /**
     * Constructor
     */
    private ManageSuperUsers() {
        LOGGER.entering(CLASS_NAME, "Constructor");
        this.userDB = PluginFactory.getUserManagement();
        this.userDB.init(new Object()); // not used
        LOGGER.exiting(CLASS_NAME, "Constructor");
    }

    /**
     * Tests if the super user exists.
     * @param username user name
     * @return True when the super user exists otherwise False
     */
    public boolean isSuperUserExist(final String username) {
        LOGGER.entering(CLASS_NAME, "isSuperUserExist", username);
        return userDB.isUserExist(username);
    }
    
    /**
     * Tests if the user is the super user.
     * @param username user name
     * @return True when the super user is the super user
     */    
    public boolean isSuperUser(final String username) {
        LOGGER.entering(CLASS_NAME, "isSuperUser", username);        
        return userDB.isAdmin(username);
    }

    /**
     * Adds the user as super user.
     * @param username user name
     * @return True when the user is added as super user.
     */
    public boolean addSuperUser(final String username) {
        LOGGER.entering(CLASS_NAME, "addSuperUser", username);
        if (!userDB.isUserExist(username)) {
            return false;
        }
        return userDB.setUserToAdminGroup(username);
    }

    /**
     * Deletes a super user.
     * @param username user name
     * @return True when the super is deleted otherwise False
     */
    public boolean deleteSuperUser(final String username) {
        LOGGER.entering(CLASS_NAME, "deleteSuperUser", username);
        return userDB.unsetUserFromAdminGroup(username);
    }

    /**
     * Returns the users.
     * @return the user
     */
    public List<String> getSuperUsers() {
        LOGGER.entering(CLASS_NAME, "getSuperUsers");
        ArrayList<String> result = new ArrayList<>();

        final List<DOIUser> users = userDB.getUsers();
        for (DOIUser doiUser : users) {
            if (doiUser.isAdmin()) {
                result.add(doiUser.getUsername());
            }
        }
        return result;
    }

    /**
     * Class to handle the instance
     *
     */
    private static class ManageSuperUsersHolder {

        /**
         * Unique Instance unique
         */
        private static final ManageSuperUsers INSTANCE = new ManageSuperUsers();
    }
}
