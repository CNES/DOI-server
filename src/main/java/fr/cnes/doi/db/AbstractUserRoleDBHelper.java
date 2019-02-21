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
package fr.cnes.doi.db;

import java.util.List;
import java.util.Observable;

import org.restlet.security.MemoryRealm;

import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.utils.DOIUser;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Interface for handling users and role database. This database is used to authenticate the
 * requests.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_INTER_050, reqName = Requirement.DOI_INTER_050_NAME)
public abstract class AbstractUserRoleDBHelper extends Observable {

    /**
     * Notification message when an user is added {@value #ADD_USER_NOTIFICATION}.
     */
    public static final String ADD_USER_NOTIFICATION = "AddUserNotification";

    /**
     * Notification message when an user is deleted {@value #REMOVE_USER_NOTIFICATION}.
     */
    public static final String REMOVE_USER_NOTIFICATION = "RemoveUserNotification";

    
    /**
     * Realm.
     */
    private static final MyMemoryRealm REALM = new MyMemoryRealm();

    /**
     * Init the connection.
     *
     * @param configuration the connection configuration
     */
    public abstract void init(Object configuration);

    /**
     * Returns the realm.
     *
     * @return the realm
     */
    public MyMemoryRealm getRealm() {
        return REALM;
    }

    /**
     * Returns the allowed users for authentication.
     *
     * @return List of users to add for the authentication
     */
    public abstract List<DOIUser> getUsers();

    /**
     * Get users from a specific role.
     *
     * @param roleName role name
     * @return The users related to a specific role
     */
    public abstract List<DOIUser> getUsersFromRole(final int roleName);

    /**
     * Adds an user to a specific role.
     *
     * @param user user to add
     * @param role role
     */
    public abstract boolean addUserToRole(final String user,
            final int role);
//    {
//        REALM.map(user, role);
//    }

    /**
     * Removes an user from a specific role.
     *
     * @param user user to remove
     * @param role role
     */
    public abstract boolean removeUserToRole(final String user,
            final int role);
//    {
//        REALM.unmap(user, role);
//    }

    /**
     * Add user to Administrators group
     *
     * @param adminGroup user to add
     */
    public abstract boolean setUserToAdminGroup(final String admin);

    /**
     * Remove user to Administrators group
     *
     * @param adminGroup user to add
     */
    public abstract boolean unsetUserFromAdminGroup(final String admin);

    /**
     * Add a DOI user
     *
     * @throws DOIDbException
     */
    public abstract void addDOIUser(String username, Boolean admin) throws DOIDbException;

    /**
     * Add a DOI user
     *
     * @throws DOIDbException
     */
    public abstract void addDOIUser(String username, Boolean admin, String email) throws
            DOIDbException;

    public abstract boolean isUserExist(String username);

    // "Boolean" and not "boolean" , because the first one can be null
    public abstract Boolean isAdmin(String username);

    public abstract void removeDOIUser(String username);

}
