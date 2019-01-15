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
package fr.cnes.doi.plugin.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.impl.DOIDbDataAccessServiceImpl;
import fr.cnes.doi.persistence.model.DOIUser;
import fr.cnes.doi.persistence.service.DOIDbDataAccessService;
import fr.cnes.doi.plugin.AbstractUserRolePluginHelper;

/**
 * Default implementation of the authentication plugin. This implementation defines
 * users/groups/roles.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DefaultUserRoleImplTEST extends AbstractUserRolePluginHelper {

    private static final String DESCRIPTION = "Provides a pre-defined list of users and groups";
    private static final String VERSION = "1.0.0";
    private static final String OWNER = "CNES";
    private static final String AUTHOR = "Jean-Christophe Malapert";
    private static final String LICENSE = "LGPLV3";
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DefaultUserRoleImplTEST.class.getName());
    private final String NAME = this.getClass().getName();

//    private static final List<User> users = new ArrayList<>();
    
    /**
     * Default constructor of the authentication plugin.
     */
    public DefaultUserRoleImplTEST() {
        super();
    }
    
    private final DOIDbDataAccessService das = new DOIDbDataAccessServiceImpl();

    @Override
    public void init(Object configuration) {
//        LOG.info("Init the database");
//        users.clear();
//        User admin = new User("admin", "admin");
//        User jcm = new User("malapert", "pwd", "Jean-Christophe", "Malapert", "jcmalapert@gmail.com");
//        User cc = new User("caillet", "pppp", "Claire", "Caillet", "claire.caillet@cnes.fr");
//        User test1 = new User("test1", "test1");
//        User test2 = new User("test2", "test2");
//        User userWithNoRole = new User("norole", "norole");
//        users.add(admin);
//        users.add(jcm);
//        users.add(cc);
//        users.add(test1);
//        users.add(test2);
//        users.add(userWithNoRole);
    }

    @Override
    public List<DOIUser> getUsers() {
    	List<DOIUser> listUser = new ArrayList<DOIUser>();
    	try {
			listUser = das.getAllDOIusers();
		} catch (DOIDbException e) {
			LOG.fatal("An error occured while trying to get all DOI users" ,e);
		}
    	return Collections.unmodifiableList(listUser);
    }

    @Override
    public List<DOIUser> getUsersFromRole(final int roleName) {
    	List<DOIUser> listUser = new ArrayList<DOIUser>();
    	try {
    		listUser = das.getAllDOIUsersForProject(roleName);
		} catch (DOIDbException e) {
			LOG.fatal("An error occured while trying to get all DOI users from project " + roleName ,e);
		}
    	return Collections.unmodifiableList(listUser);
    }

	@Override
	public boolean addUserToRole(String user, int role) {
		boolean isAdded = false;
		try {
			das.addDOIProjectToUser(user, role);
			isAdded = true;
		} catch (DOIDbException e) {
			LOG.fatal("An error occured while trying to add user " + user + " to project " + role ,e);
		}
		return isAdded;
	}

	@Override
	public boolean removeUserToRole(String user, int role) {
		boolean isRemoved = false;
		try {
			das.removeDOIProjectFromUser(user, role);
			isRemoved = true;
		} catch (DOIDbException e) {
			LOG.fatal("An error occured while trying to remove user " + user + " from project " + role ,e);
		}
		return isRemoved;
	}

	@Override
	public boolean setUserToAdminGroup(String user) {
		boolean isSetted = false;
		try {
			das.setAdmin(user);
			isSetted = true;
		} catch (DOIDbException e) {
			LOG.fatal("An error occured while trying to add user " + user + " to admin group",e);
		}
		return isSetted;
	}
	

	@Override
	public boolean unsetUserFromAdminGroup(String user) {
		boolean isUnsetted = false;
		try {
			das.unsetAdmin(user);
			isUnsetted = true;
		} catch (DOIDbException e) {
			LOG.fatal("An error occured while trying to remove user " + user + " to admin group",e);
		}
		return isUnsetted;
	}

    @Override
    public boolean isUserExist(String username) {
    	boolean isExist = false;
    	try {
    		isExist =  das.isUserExist(username);
    	} catch (DOIDbException e) {
    		LOG.fatal("An error occured while trying to know if user " + username + " exist",e);
    	}
    	return isExist;
    }
    
    @Override
    public Boolean isAdmin(String username) {
    	Boolean isExist=null;
    	try {
    		isExist =  das.isAdmin(username);
    	} catch (DOIDbException e) {
    		LOG.fatal("An error occured while trying to know if user " + username + " exist and is admin",e);
    	}
    	return isExist;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getOwner() {
        return OWNER;
    }

    @Override
    public String getLicense() {
        return LICENSE;
    }


}
