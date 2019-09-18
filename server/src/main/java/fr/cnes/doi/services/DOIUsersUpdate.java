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
package fr.cnes.doi.services;

import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.db.IAuthenticationDBHelper;
import java.util.List;

import fr.cnes.doi.exception.AuthenticationAccessException;
import fr.cnes.doi.db.model.AuthSystemUser;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.db.model.DOIUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fr.cnes.doi.plugin.PluginFactory;

/**
 * Updates the database from the authentication system.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DOIUsersUpdate implements Runnable {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DOIUsersUpdate.class.getName());

    /**
     * Access to the authentication system.
     */
    private static final IAuthenticationDBHelper AUTHENTICATION_SERVICE = PluginFactory.
            getAuthenticationSystem();

    /**
     * Fills the DOI users database from the members of the authentication
     * system.
     *
     * @throws AuthenticationAccessException When an authentication problem
     * occurs
     * @throws DOIDbException When a SQL problem occurs
     */
    private void updateDoiServerDataBaseFromAuthSystem() throws AuthenticationAccessException,
            DOIDbException {
        LOG.traceEntry();
        final AbstractUserRoleDBHelper manageUsers = PluginFactory.getUserManagement();
        final List<AuthSystemUser> authMembers = AUTHENTICATION_SERVICE.getDOIProjectMembers();
        LOG.debug("Authentication system members: {}", authMembers);
        final List<DOIUser> dbusers = manageUsers.getUsers();
        LOG.debug("Users from database: {}", dbusers);
        LOG.debug("remove from database, users that are no longer members of doi_server project");
        for (final DOIUser dbuser : dbusers) {
            if (!isContained(dbuser, authMembers)) {
                LOG.debug("User {} is removed from database", dbuser.getUsername());
                manageUsers.removeDOIUser(dbuser.getUsername());
            }
        }
        LOG.debug("add to database users that are new members of doi_server project");
        for (final AuthSystemUser authMember : authMembers) {
            if (!isContained(authMember, dbusers)) {
                // add authMember to doi database
                LOG.debug("authSystemUser {} is added to database as simple user", authMember.
                        getUsername());
                manageUsers.addDOIUser(authMember.getUsername(), false, authMember.getEmail());
            }
        }
        LOG.traceExit();
    }

    /**
     * Tests if the authMember of the authentication mechanism is contained in
     * the users from the database.
     *
     * @param authMember authMember of the authentication mechanism
     * @param dbusers users database
     * @return True when the authMember is contained in the users from database
     * otherwise false
     */
    private boolean isContained(final AuthSystemUser authMember, final List<DOIUser> dbusers) {
        LOG.traceEntry("Parameters {},", authMember, dbusers);
        boolean isContained = false;
        for (final DOIUser dbuser : dbusers) {
            if (dbuser.getUsername().equals(authMember.getUsername())) {
                isContained = true;
                break;
            }
        }
        return LOG.traceExit(isContained);
    }

    /**
     * Tests if a user from the database is contained in the members from the
     * authentication system.
     *
     * @param dbuser the user from the database
     * @param authMembers the users from the authentication system
     * @return True when the user from the database is contained from the users
     * from the authentication system
     */
    private boolean isContained(final DOIUser dbuser, final List<AuthSystemUser> authMembers) {
        LOG.traceEntry("Parameters {},", dbuser, authMembers);
        boolean isContained = false;
        for (final AuthSystemUser authenticationUser : authMembers) {
            if (authenticationUser.getUsername().equals(dbuser.getUsername())) {
                isContained = true;
                break;
            }
        }
        return LOG.traceExit(isContained);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        LOG.info("executing task that updates database from the authentication system !");
        try {
            this.updateDoiServerDataBaseFromAuthSystem();
        } catch (AuthenticationAccessException | DOIDbException e) {
            LOG.error("error occured when calling DOIUsersUpdate job", e);
        }
    }

}
