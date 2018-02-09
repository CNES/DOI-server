/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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

import fr.cnes.doi.plugin.AbstractUserRolePluginHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.security.User;

/**
 * Default implementation of the authentication plugin.
 * This implementation defines users/groups/roles.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DefaultUserRoleImpl extends AbstractUserRolePluginHelper {

    private static final List<User> users = new ArrayList<>();
    private final String NAME = this.getClass().getName();
    private static final String DESCRIPTION = "Provides a pre-defined list of users and groups";
    private static final String VERSION = "1.0.0";
    private static final String OWNER = "CNES";
    private static final String AUTHOR = "Jean-Christophe Malapert";
    private static final String LICENSE = "LGPLV3";
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DefaultUserRoleImpl.class.getName());       

    public DefaultUserRoleImpl() {
        super();
    }
    
    @Override
    public void init(Object configuration) {
        User admin = new User("admin", "admin");
        User jcm = new User("malapert", "pwd", "Jean-Christophe", "Malapert", "jcmalapert@gmail.com");
        User cc = new User("caillet", "pppp", "Claire", "Caillet", "claire.caillet@cnes.fr");
        User test1 = new User("test1", "test1");
        User test2 = new User("test2", "test2");
        User userWithNoRole = new User("norole", "norole");
        users.add(admin);
        users.add(jcm);
        users.add(cc);
        users.add(test1);
        users.add(test2);   
        users.add(userWithNoRole);
    }
    

    @Override
    public List<User> getUsers() {
        return users;
    }

    @Override
    public List<User> getUsersFromRole(final String roleName){
        return Arrays.asList(users.get(1), users.get(2), users.get(3), users.get(4));
    }   

    @Override
    public void setUsersToAdminGroup(final List<User> adminGroup) {
        adminGroup.add(users.get(0));
        adminGroup.add(users.get(1));
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
