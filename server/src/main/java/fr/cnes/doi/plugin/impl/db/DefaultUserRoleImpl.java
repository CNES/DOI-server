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
package fr.cnes.doi.plugin.impl.db;

import fr.cnes.doi.db.MyMemoryRealm;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.plugin.AbstractUserRolePluginHelper;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_MAX_ACTIVE_CONNECTIONS;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_MAX_IDLE_CONNECTIONS;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_MIN_IDLE_CONNECTIONS;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_PWD;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_URL;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_USER;
import fr.cnes.doi.plugin.impl.db.service.DOIDbDataAccessService;
import fr.cnes.doi.plugin.impl.db.service.DatabaseSingleton;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Application;
import org.restlet.security.Role;
import org.restlet.security.User;

/**
 * Default implementation of the authentication plugin. This implementation defines
 * users/groups/roles.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class DefaultUserRoleImpl extends AbstractUserRolePluginHelper {

    /**
     * Plugin description.
     */
    private static final String DESCRIPTION = "Provides a pre-defined list of users and groups";
    /**
     * Plugin version.
     */
    private static final String VERSION = "1.0.0";
    /**
     * Plugin owner.
     */
    private static final String OWNER = "CNES";
    /**
     * Plugin author.
     */
    private static final String AUTHOR = "Jean-Christophe Malapert";
    /**
     * Plugin license.
     */
    private static final String LICENSE = "LGPLV3";
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DefaultUserRoleImpl.class.getName());
    /**
     * Class name.
     */
    private final String NAME = this.getClass().getName();

    /**
     * User/Role realm
     */
    private final MyMemoryRealm REALM = getRealm();

    /**
     * Database access.
     */
    private DOIDbDataAccessService das;

    /**
     * Configuration file.
     */
    private Map<String, String> conf;

    /**
     * Status of the plugin configuration.
     */
    private boolean configured = false;

    /**
     * Options for JDBC.
     */
    private final Map<String, Integer> options = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc }
     */
    @Override
    public void setConfiguration(final Object configuration) {
        this.conf = (Map<String, String>) configuration;
        final String dbUrl = this.conf.get(DB_URL);
        final String dbUser = this.conf.get(DB_USER);
        final String dbPwd = this.conf.get(DB_PWD);
        if (this.conf.containsKey(DB_MIN_IDLE_CONNECTIONS)) {
            this.options.put(DB_MIN_IDLE_CONNECTIONS,
                    Integer.valueOf(this.conf.get(DB_MIN_IDLE_CONNECTIONS)));
        }
        if (this.conf.containsKey(DB_MAX_IDLE_CONNECTIONS)) {
            this.options.put(DB_MAX_IDLE_CONNECTIONS,
                    Integer.valueOf(this.conf.get(DB_MAX_IDLE_CONNECTIONS)));
        }
        if (this.conf.containsKey(DB_MAX_ACTIVE_CONNECTIONS)) {
            this.options.put(DB_MAX_ACTIVE_CONNECTIONS,
                    Integer.valueOf(this.conf.get(DB_MAX_ACTIVE_CONNECTIONS)));
        }
        LOG.info("[CONF] Plugin database URL : {}", dbUrl);
        LOG.info("[CONF] Plugin database user : {}", dbUser);
        LOG.info("[CONF] Plugin database password : {}", Utils.transformPasswordToStars(dbPwd));
        LOG.info("[CONF] Plugin options : {}", options);

        this.configured = true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void initConnection() throws DoiRuntimeException {
        DatabaseSingleton.getInstance().init(
                this.conf.get(DB_URL), this.conf.get(DB_USER), this.conf.get(DB_PWD), this.options);
        this.das = DatabaseSingleton.getInstance().getDatabaseAccess();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<DOIUser> getUsers() throws DOIDbException {
        final List<DOIUser> listUser = new ArrayList<>();
        listUser.addAll(das.getAllDOIusers());
        return Collections.unmodifiableList(listUser);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<DOIUser> getUsersFromRole(final int roleName) throws DOIDbException {
        final List<DOIUser> listUser = new ArrayList<>();
        listUser.addAll(das.getAllDOIUsersForProject(roleName));
        return Collections.unmodifiableList(listUser);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean addUserToRole(final String user, final int role) {
        boolean isAdded = false;
        try {
            das.addDOIProjectToUser(user, role);
            isAdded = true;
            LOG.info("The user {} is added to role {}.", user, role);

            final User userFromRealm = REALM.findUser(user);
            final Role roleFromRealm = new Role(Application.getCurrent(), String.valueOf(role),
                    "Role " + String.valueOf(role) + " for " + Application.getCurrent().getName());
            REALM.map(userFromRealm, roleFromRealm);

        } catch (DOIDbException e) {
            LOG.fatal("An error occured while trying to add user " + user + " to project " + role,
                    e);
        }
        return isAdded;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean removeUserToRole(final String user, final int role) {
        boolean isRemoved = false;
        try {
            das.removeDOIProjectFromUser(user, role);
            isRemoved = true;
            LOG.info("The user {} is removed to role {}.", user, role);

            final User userFromRealm = REALM.findUser(user);
            final Set<Role> rolesFromRealm = REALM.findRoles(userFromRealm);
            for (final Role r : rolesFromRealm) {
                if (r.getName().equals(String.valueOf(role))) {
                    REALM.unmap(userFromRealm, r);
                }
            }

        } catch (DOIDbException e) {
            LOG.fatal("An error occured while trying to remove user " + user + " from project "
                    + role, e);
        }
        return isRemoved;
    }

    /**
     * Sets the user to the admin group.
     *
     * @param user username
     * @throws DOIDbException - if a Database error occurs
     */
    private void setUserToAdminGroupInDB(final String user) throws DOIDbException {
        das.setAdmin(user);
        LOG.info("The user {} is added to admin group.", user);
        EmailSettings.getInstance().sendMessage("[DOI] Admin group",
                "The user " + user + " has been added to the administror group.");
        final User userFromRealm = REALM.findUser(user);
        if (!REALM.getRootGroups().get(0).getMemberUsers().contains(userFromRealm)) {
            REALM.getRootGroups().get(0).getMemberUsers().add(userFromRealm);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean setUserToAdminGroup(final String user) {
        boolean isSetted = false;
        try {
            if (!das.isAdmin(user)) {
                setUserToAdminGroupInDB(user);
                isSetted = true;
            }
        } catch (DOIDbException e) {
            LOG.fatal("An error occured while trying to add user " + user + " to admin group", e);
        }
        return isSetted;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean unsetUserFromAdminGroup(final String user) {
        boolean isUnsetted = false;
        try {
            das.unsetAdmin(user);
            isUnsetted = true;
            LOG.info("The user {} is removed to admin group.", user);

            EmailSettings.getInstance().sendMessage("[DOI] Admin group",
                    "The user " + user + " has been removed from the administrator group.");

            final User userFromRealm = REALM.findUser(user);
            if (REALM.getRootGroups().get(0).getMemberUsers().contains(userFromRealm)) {
                REALM.getRootGroups().get(0).getMemberUsers().remove(userFromRealm);
            }

        } catch (DOIDbException e) {
            LOG.fatal("An error occured while trying to remove user " + user + " to admin group",
                    e);
        }
        return isUnsetted;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isUserExist(final String username) {
        boolean isExist = false;
        try {
            isExist = das.isUserExist(username);
        } catch (DOIDbException e) {
            LOG.fatal("An error occured while trying to know if user " + username + " exist", e);
        }
        return isExist;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isAdmin(final String username) {
        boolean isExist = false;
        try {
            isExist = das.isAdmin(username);
        } catch (DOIDbException e) {
            LOG.fatal("An error occured while trying to know if user " + username
                    + " exist and is admin", e);
        }
        return isExist;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean addDOIUser(final String username, final Boolean admin) {
        boolean isAdded;
        try {
            this.das.addDOIUser(username, admin);
            if (REALM.findUser(username) == null) {
                REALM.getUsers().add(new User(username));
            }
            isAdded = true;
            LOG.info("The user {} is added to database.", username);
        } catch (DOIDbException ex) {
            isAdded = false;
            LOG.fatal("Cannot add the user {}", username);
        }
        return isAdded;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean addDOIUser(final String username, final Boolean admin, final String email) {
        boolean isAdded;
        try {
            this.das.addDOIUser(username, admin, email);
            if (REALM.findUser(username) == null) {
                REALM.getUsers().add(new User(username));
            }
            isAdded = true;
            LOG.info("The user {} is added to database.", username);
        } catch (DOIDbException ex) {
            isAdded = false;
            LOG.fatal("Cannot add the user {}", username);
        }
        return isAdded;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean removeDOIUser(final String username) {
        boolean isRemoved;
        try {
            this.das.removeDOIUser(username);
            final User userFromRealm = REALM.findUser(username);
            if (userFromRealm != null) {
                REALM.getUsers().remove(userFromRealm);
            }
            isRemoved = true;
            LOG.info("The user {} is removed from database.", username);
        } catch (DOIDbException ex) {
            isRemoved = false;
            LOG.fatal("Cannot remove user" + username, ex);
        }
        return isRemoved;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getOwner() {
        return OWNER;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getLicense() {
        return LICENSE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuilder validate() {
        final StringBuilder validation = new StringBuilder();
        final String message = "Sets ";
        if (!this.conf.containsKey(DB_URL)) {
            validation.append(message).append(DB_URL).append("\n");
        }
        return validation;
    }

    /**
     * Checks if the keyword is a password.
     *
     * @param key keyword to check
     * @return True when the keyword is a password otherwise False
     */
    public static boolean isPassword(final String key) {
        return DB_PWD.equals(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        this.conf.clear();
        try {
            if(this.das != null) {
                this.das.close();
            }
        } catch (DOIDbException ex) {
        }
        this.configured = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigured() {
        return this.configured;
    }
}
