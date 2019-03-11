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

import fr.cnes.doi.plugin.impl.db.persistence.service.DatabaseSingleton;
import fr.cnes.doi.plugin.impl.db.persistence.service.DOIDbDataAccessService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Application;
import org.restlet.security.Role;
import org.restlet.security.User;

import fr.cnes.doi.db.MyMemoryRealm;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.plugin.AbstractUserRolePluginHelper;

/**
 * Default implementation of the authentication plugin. This implementation
 * defines users/groups/roles.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DefaultUserRoleImpl extends AbstractUserRolePluginHelper {

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
     * Default constructor of the authentication plugin.
     */
    public DefaultUserRoleImpl() {
	super();
    }

    private final DOIDbDataAccessService das = DatabaseSingleton.getInstance().getDatabaseAccess();

    /**
     * {@inheritDoc }      
     */     
    @Override
    public void setConfiguration(final Object configuration) {
    }

    /**
     * {@inheritDoc }      
     */     
    @Override
    public List<DOIUser> getUsers() {
	final List<DOIUser> listUser = new ArrayList<>();
	try {
	    listUser.addAll(das.getAllDOIusers());
	} catch (DOIDbException e) {
	    LOG.fatal("An error occured while trying to get all DOI users", e);
	}
	return Collections.unmodifiableList(listUser);
    }

    /**
     * {@inheritDoc }      
     */     
    @Override
    public List<DOIUser> getUsersFromRole(final int roleName) {
	final List<DOIUser> listUser = new ArrayList<>();
	try {
	    listUser.addAll(das.getAllDOIUsersForProject(roleName));
	} catch (DOIDbException e) {
	    LOG.fatal("An error occured while trying to get all DOI users from project " + roleName,
		    e);
	}
	return Collections.unmodifiableList(listUser);
    }

    /**
     * {@inheritDoc }      
     */     
    @Override
    public boolean addUserToRole(String user, int role) {
	boolean isAdded = false;
	try {
	    das.addDOIProjectToUser(user, role);
	    isAdded = true;
	    LOG.info("The user {} is added to role {}.", user, role);

	    User userFromRealm = REALM.findUser(user);
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
    public boolean removeUserToRole(String user, int role) {
	boolean isRemoved = false;
	try {
	    das.removeDOIProjectFromUser(user, role);
	    isRemoved = true;
	    LOG.info("The user {} is removed to role {}.", user, role);

	    final User userFromRealm = REALM.findUser(user);
	    final Set<Role> rolesFromRealm = REALM.findRoles(userFromRealm);
	    for (Role r : rolesFromRealm) {
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
     * {@inheritDoc }      
     */     
    @Override
    public boolean setUserToAdminGroup(final String user) {
	boolean isSetted = false;
	try {
	    das.setAdmin(user);
	    isSetted = true;
	    LOG.info("The user {} is added to admin group.", user);

	    final User userFromRealm = REALM.findUser(user);
	    if (!REALM.getRootGroups().get(0).getMemberUsers().contains(userFromRealm)) {
		REALM.getRootGroups().get(0).getMemberUsers().add(userFromRealm);
	    }

	} catch (DOIDbException e) {
	    LOG.fatal("An error occured while trying to add user " + user + " to admin group", e);
	}
	return isSetted;
    }

    @Override
    public boolean unsetUserFromAdminGroup(final String user) {
	boolean isUnsetted = false;
	try {
	    das.unsetAdmin(user);
	    isUnsetted = true;
	    LOG.info("The user {} is removed to admin group.", user);

	    User userFromRealm = REALM.findUser(user);
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
	    isExist =  das.isUserExist(username);
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
    public void addDOIUser(final String username, final Boolean admin) throws DOIDbException {
	this.das.addDOIUser(username, admin);
	if (REALM.findUser(username) == null) {
	    REALM.getUsers().add(new User(username));
	}
	LOG.info("The user {} is added to database.", username);
    }

    /**
     * {@inheritDoc }      
     */     
    @Override
    public void addDOIUser(final String username, final Boolean admin, final String email) throws DOIDbException {
	this.das.addDOIUser(username, admin, email);
	if (REALM.findUser(username) == null) {
	    REALM.getUsers().add(new User(username));
	}
	LOG.info("The user {} is added to database.", username);
    }

    /**
     * {@inheritDoc }      
     */    
    @Override
    public void removeDOIUser(final String username) {
	try {
	    this.das.removeDOIUser(username);
	    final User userFromRealm = REALM.findUser(username);
	    if (userFromRealm != null) {
		REALM.getUsers().remove(userFromRealm);
	    }
	    LOG.info("The user {} is removed from database.", username);
	} catch (DOIDbException ex) {
	    LOG.fatal("Cannot remove user" + username, ex);
	}
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
}
