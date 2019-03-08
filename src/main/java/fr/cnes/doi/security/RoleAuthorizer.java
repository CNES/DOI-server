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
package fr.cnes.doi.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Application;
import org.restlet.security.Group;
import org.restlet.security.Role;
import org.restlet.security.User;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.db.AbstractProjectSuffixDBHelper;
import fr.cnes.doi.db.MyMemoryRealm;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.logging.business.JsonMessage;
import fr.cnes.doi.utils.DOIUser;
import fr.cnes.doi.utils.ManageUsers;
import fr.cnes.doi.utils.UniqueProjectName;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Security class for authentication by REALM.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_AUTH_010, reqName = Requirement.DOI_AUTH_010_NAME)
public class RoleAuthorizer implements Observer {

    /**
     * Role name for the amdinistrators {@value #ROLE_ADMIN}.
     */
    public static final String ROLE_ADMIN = "admin";

    /**
     * Group name for the users {@value #GROUP_USERS}
     */
    public static final String GROUP_USERS = "Users";

    /**
     * Group name for the amdinistrators {@value #GROUP_ADMIN}.
     */
    public static final String GROUP_ADMIN = "Administrator";

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(RoleAuthorizer.class.getName());

    /**
     * Realm.
     */
    private static final MyMemoryRealm REALM = ManageUsers.getInstance().getRealm();

    /**
     * Access to unique INSTANCE of role authorizer
     *
     * @return the configuration instance.
     */
    public static RoleAuthorizer getInstance() {
        return RoleAuthorizerHolder.INSTANCE;
    }

    /**
     * Constructor.
     */
    private RoleAuthorizer() {
        initUsersGroups();
    }

    /**
     * Init users and groups in REALM.
     */
    private void initUsersGroups() {
        LOG.traceEntry();

        // Add users
        LOG.debug("Add users to REALM");
        List<DOIUser> doiUsers = ManageUsers.getInstance().getUsers();
        List<User> users = new ArrayList<>();
        List<User> admins = new ArrayList<>();
        for (DOIUser doiUser : doiUsers) {
            User user = new User(doiUser.getUsername());

            // Create list of user
            users.add(user);

            // Create list of admin
            if (doiUser.getAdmin()) {
                admins.add(user);
            }
        }
        RoleAuthorizer.REALM.setUsers(users);
        LOG.debug("List of users in realm :" + RoleAuthorizer.REALM.getUsers());

        // Add Groups       
        final Group administrators = new Group(GROUP_ADMIN, "Administrators");
        LOG.debug("Add users to Administrators group");
        administrators.getMemberUsers().addAll(admins);
        LOG.debug("List of users in Administrators group " + administrators.getMemberUsers());

        if (!RoleAuthorizer.REALM.getRootGroups().contains(administrators)) {
            LOG.debug("Add administators group to rootGroups in REALM");
            RoleAuthorizer.REALM.getRootGroups().add(administrators);
        }
        LOG.traceExit();
    }

    /**
     * Sets Realm to Mds application.
     *
     * @param app Mds application
     */
    private void initForMds(final Application app) {
        LOG.traceEntry(new JsonMessage(app));

        //TODO why twice?
//        initForAdmin(app);
        // we load projects from database
        final Map<String, Integer> projects = UniqueProjectName.getInstance().getProjects();
        LOG.debug("{} projects have already been registered", projects.size());
        for (final Map.Entry<String, Integer> entry : projects.entrySet()) {
            // for each project, create a role as the project name
            final Integer projectID = entry.getValue();
            final Role role = new Role(app, String.valueOf(projectID), "Role " + String.valueOf(
                    projectID) + " for " + app.getName());

            // get the users for a role.
            List<DOIUser> doiUsers = ManageUsers.getInstance().getUsersFromRole(projectID);
            List<User> usersFromProject = new ArrayList<>();
            for (DOIUser doiUser : doiUsers) {
                User user = REALM.findUser(doiUser.getUsername());
                if (user != null) {
                    usersFromProject.add(user);
                }
            }

            // create a role authorizer for each user related to a project
            for (final User user : usersFromProject) {
                LOG.debug("Add user " + user + " to role " + projectID + " for " + app.getName());
                RoleAuthorizer.REALM.map(user, role);
            }
        }

        app.getContext().setDefaultEnroler(RoleAuthorizer.REALM.getEnroler());
        app.getContext().setDefaultVerifier(RoleAuthorizer.REALM.getVerifier());

        LOG.traceExit();
    }

    /**
     * Finds a group by its name
     *
     * @param name group name
     * @return the group
     */
    private Group findGroupByName(final String name) {
        LOG.traceEntry("Parameter : {}", name);
        final List<Group> groups = RoleAuthorizer.REALM.getRootGroups();
        Group searchedGroup = null;
        for (final Group group : groups) {
            LOG.debug("group name : {}", group.getName());
            if (group.getName().equals(name)) {
                searchedGroup = group;
                LOG.debug("group found");
                break;
            }
        }
        if (searchedGroup == null) {
            LOG.error("Please, create a group {}", name);
            throw LOG.throwing(new DoiRuntimeException("Please, create a group " + name));
        }
        return LOG.traceExit(searchedGroup);
    }

    /**
     * Sets Realm for admin application.
     *
     * @param app Admin application
     */
    private void initForAdmin(final Application app) {
        LOG.traceEntry(new JsonMessage(app));
        final Group admin = findGroupByName(GROUP_ADMIN);

        RoleAuthorizer.REALM.map(admin, Role.get(app, ROLE_ADMIN));
        app.getContext().setDefaultEnroler(RoleAuthorizer.REALM.getEnroler());
        app.getContext().setDefaultVerifier(RoleAuthorizer.REALM.getVerifier());

        LOG.traceExit();
    }

    /**
     * Init Realm for an application.
     *
     * @param app application
     * @return True when the realm is initialized otherwise False
     */
    public boolean createRealmFor(final Application app) {
        LOG.traceEntry(new JsonMessage(app));

        boolean isCreated;
        switch (app.getName()) {
            case DoiMdsApplication.NAME:
                initForMds(app);
                isCreated = true;
                LOG.debug("Init for MDS ... done");
                break;
            case AdminApplication.NAME:
                initForAdmin(app);
                isCreated = true;
                LOG.debug("Init for admin ... done");
                break;
            default:
                LOG.debug("No Realm is initialized for this application {}", app.getName());
                isCreated = false;
                break;
        }

        return LOG.traceExit(isCreated);
    }

    /**
     * Adds/removes the <i>users</i> group to the new role name related to the application MDS
     *
     * @param obs observable
     * @param obj message
     */
    @Requirement(reqId = Requirement.DOI_SRV_130, reqName = Requirement.DOI_SRV_130_NAME)
    @Requirement(reqId = Requirement.DOI_INTER_030, reqName = Requirement.DOI_INTER_030_NAME)
    @Override
    public void update(final Observable obs,
            final Object obj) {
        LOG.traceEntry(new JsonMessage(obs));
        LOG.traceEntry(new JsonMessage(obj));

        // Loads the admin group - admin group is defined by default
        final Group adminGroup = findGroupByName(GROUP_ADMIN);

        // Loads the application MDS related to admin group
        final Application mds = loadApplicationBy(adminGroup, DoiMdsApplication.NAME);

        if (mds == null) {
            LOG.info(DoiMdsApplication.NAME + " is not defined in the REALM");
        } else {
            updateObserver(obs, obj, mds);
        }
        LOG.traceExit();
    }

    /**
     * Updates REALM for mds.
     *
     * @param obs observable
     * @param obj message
     * @param mds application
     */
    private void updateObserver(final Observable obs,
            final Object obj,
            final Application mds) {
        LOG.traceEntry(new JsonMessage(obs));
        LOG.traceEntry(new JsonMessage(obj));
        LOG.traceEntry(new JsonMessage(mds));

        if (obs instanceof AbstractProjectSuffixDBHelper) {
            LOG.debug("Observable is a ProjectSuffixDB type");

            final String[] message = (String[]) obj;

            final String operation = message[0];
            final String roleName = message[1];
            List<DOIUser> doiUsers = ManageUsers.getInstance().getUsersFromRole(Integer.parseInt(
                    roleName));
            List<User> usersFromProject = new ArrayList<User>();
            for (DOIUser doiUser : doiUsers) {
                User user = new User(doiUser.getUsername());
                usersFromProject.add(user);
            }
            final List<User> users = usersFromProject;
            switch (operation) {
                case AbstractProjectSuffixDBHelper.ADD_RECORD:
                    for (final User user : users) {
                        RoleAuthorizer.REALM.map(user, Role.get(mds, roleName));
                        LOG.debug("Adds the user {} to the new role {} related to the {}", user,
                                roleName, mds.getName());
                    }
                    break;
                case AbstractProjectSuffixDBHelper.DELETE_RECORD:
                    for (final User user : users) {
                        RoleAuthorizer.REALM.unmap(user, mds, roleName);
                        LOG.info("Remove the user {} to the role {} related to the {}", user,
                                roleName, mds.getName());
                    }
                    break;
                default:
                    LOG.error("operation {} was not expected", operation);
                    break;
            }
        }
        LOG.traceExit();
    }

    /**
     * Loads the application related to a group with a name
     *
     * @param group group linked to an application
     * @param appName application name
     * @return the application or null if the application is not defined in the REALM
     */
    private Application loadApplicationBy(final Group group,
            final String appName) {
        LOG.traceEntry("Parameters : {} and {}", group, appName);

        final Set<Role> roles = RoleAuthorizer.REALM.findRoles(group);
        final Iterator<Role> roleIter = roles.iterator();
        Application searchedApp = null;
        boolean isFound = false;
        while (roleIter.hasNext() && !isFound) {
            final Role role = roleIter.next();
            final Application app = role.getApplication();
            if (app.getName().equals(appName)) {
                searchedApp = app;
                isFound = true;
                break;
            }
        }

        return LOG.traceExit(searchedApp);
    }

    /**
     * Class to handle the instance
     *
     */
    private static class RoleAuthorizerHolder {

        /**
         * Unique Instance unique
         */
        private static final RoleAuthorizer INSTANCE = new RoleAuthorizer();
    }

}
