/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.db.AbstractProjectSuffixDBHelper;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.utils.UniqueProjectName;
import fr.cnes.doi.utils.Utils;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Application;
import org.restlet.security.Group;
import org.restlet.security.MemoryRealm;
import org.restlet.security.Role;
import org.restlet.security.User;

/**
 * Security class for authentication by REALM.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_AUTH_010,
        reqName = Requirement.DOI_AUTH_010_NAME
)
public class RoleAuthorizer implements Observer {

    /**
     * Class name.
     */
    private static final String CLASS_NAME = RoleAuthorizer.class.getName();

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
    public static final Logger LOGGER = Utils.getAppLogger();
    
   /**
     * Plugin for user management.
     */
    private static final AbstractUserRoleDBHelper USER_ROLE_PLUGIN = PluginFactory.getUserManagement();
    
    /**
     * Realm.
     */
    private static final MemoryRealm REALM = USER_ROLE_PLUGIN.getRealm();
    

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
        LOGGER.entering(CLASS_NAME, "initUsersGroups");

        USER_ROLE_PLUGIN.init(null);

        // Add users
        LOGGER.info("Add users to REALM");
        RoleAuthorizer.REALM.setUsers(USER_ROLE_PLUGIN.getUsers());

        // Add Groups       
        final Group administrators = new Group(GROUP_ADMIN, "Administrators");
        LOGGER.info("Add users to Administrators group");
        USER_ROLE_PLUGIN.setUsersToAdminGroup(administrators.getMemberUsers());
        RoleAuthorizer.REALM.getRootGroups().add(administrators);

        LOGGER.exiting(CLASS_NAME, "initUsersGroups");
    }

    /**
     * Sets Realm to Mds application.
     * @param app Mds application
     */
    private void initForMds(final Application app) {
        LOGGER.entering(CLASS_NAME, "initForMds", new Object[]{REALM, app.getName()});

        initForAdmin(app);

        final Map<String, Integer> projects = UniqueProjectName.getInstance().getProjects();
        LOGGER.log(Level.INFO, "{0} projects have already been registered", projects.size());
        for (final Map.Entry<String, Integer> entry : projects.entrySet()) {
            final Integer projectID = entry.getValue();
            final Role role = new Role(app, String.valueOf(projectID));           
            final List<User> users = USER_ROLE_PLUGIN.getUsersFromRole(String.valueOf(projectID));
            for (final User user : users) {
                LOGGER.log(Level.INFO, "Add user {0} to role {1} for {2}", new Object[]{user, projectID, app.getName()});
                RoleAuthorizer.REALM.map(user, role);
            }
        }

        app.getContext().setDefaultEnroler(RoleAuthorizer.REALM.getEnroler());
        app.getContext().setDefaultVerifier(RoleAuthorizer.REALM.getVerifier());

        LOGGER.exiting(CLASS_NAME, "initForMds");
    }

    /**
     * Finds a group by its name
     * @param name group name
     * @return the group
     */
    private Group findGroupByName(final String name) {
        LOGGER.entering(CLASS_NAME, "findGroupByName", name);
        final List<Group> groups = RoleAuthorizer.REALM.getRootGroups();
        Group searchedGroup = null;
        for (final Group group : groups) {
            LOGGER.finest(String.format("group name : %s", group.getName()));
            if (group.getName().equals(name)) {
                searchedGroup = group;
                LOGGER.finest("group found");
                break;
            }
        }
        if (searchedGroup == null) {
            LOGGER.info(String.format("Please, create a group %s", name));
            final DoiRuntimeException runtimeEx = new DoiRuntimeException(String.format("Please, create a group %s", name));
            LOGGER.throwing(GROUP_USERS, name, runtimeEx);
            throw runtimeEx;
        }
        LOGGER.exiting(CLASS_NAME, "findGroupByName", searchedGroup);
        return searchedGroup;
    }

    /**
     * Sets Realm for admin application.
     * @param app Admin application
     */
    private void initForAdmin(final Application app) {
        LOGGER.entering(CLASS_NAME, "initForAdmin", new Object[]{REALM, app.getName()});

        final Group admin = findGroupByName(GROUP_ADMIN);

        RoleAuthorizer.REALM.map(admin, Role.get(app, ROLE_ADMIN));
        app.getContext().setDefaultEnroler(RoleAuthorizer.REALM.getEnroler());
        app.getContext().setDefaultVerifier(RoleAuthorizer.REALM.getVerifier());

        LOGGER.exiting(CLASS_NAME, "initForAdmin");
    }

    /**
     * Init Realm for an application.
     *
     * @param app application
     * @return True when the realm is initialized otherwise False
     */
    public boolean createRealmFor(final Application app) {
        LOGGER.entering(CLASS_NAME, "createReamFor", app.getName());

        boolean isCreated;
        switch (app.getName()) {
            case DoiMdsApplication.NAME:
                initForMds(app);
                isCreated = true;
                LOGGER.info("Init for MDS ... done");
                break;
            case AdminApplication.NAME:
                initForAdmin(app);
                isCreated = true;
                LOGGER.info("Init for admin ... done");
                break;
            default:
                LOGGER.log(Level.WARNING, "No Realm is initialized for this application {0}", app.getName());
                isCreated = false;
                break;
        }

        LOGGER.exiting(CLASS_NAME, "createReamFor", isCreated);

        return isCreated;
    }

    /**
     * Adds/removes the <i>users</i> group to the new role name related to the
     * application MDS
     *
     * @param obs observable
     * @param obj message
     */
    @Requirement(
            reqId = Requirement.DOI_SRV_130,
            reqName = Requirement.DOI_SRV_130_NAME
    )
    @Requirement(
            reqId = Requirement.DOI_INTER_030,
            reqName = Requirement.DOI_INTER_030_NAME
    )
    @Override
    public void update(final Observable obs, final Object obj) {
        LOGGER.entering(CLASS_NAME, "update", new Object[]{obs, obj});

        // Loads the admin group - admin group is defined by default
        final Group adminGroup = findGroupByName(GROUP_ADMIN);

        // Loads the application MDS related to admin group
        final Application mds = loadApplicationBy(adminGroup, DoiMdsApplication.NAME);

        if (mds == null) {
            LOGGER.warning(DoiMdsApplication.NAME + " is not defined in the REALM");
        } else {
            updateObserver(obs, obj, mds);
        }

        LOGGER.exiting(CLASS_NAME, "update");
    }

    /**
     * Updates REALM for mds.
     *
     * @param obs observable
     * @param obj message
     * @param mds application
     */
    private void updateObserver(final Observable obs, final Object obj, final Application mds) {
        LOGGER.entering(CLASS_NAME, "updateObserver", new Object[]{obs, obj, mds.getName()});

        if (obs instanceof AbstractProjectSuffixDBHelper) {
            LOGGER.finest("Observable is a ProjectSuffixDB type");

            final String[] message = (String[]) obj;

            final String operation = message[0];
            final String roleName = message[1];
            final List<User> users = USER_ROLE_PLUGIN.getUsersFromRole(roleName);
            switch (operation) {
                case AbstractProjectSuffixDBHelper.ADD_RECORD:
                    for (final User user : users) {
                        RoleAuthorizer.REALM.map(user, Role.get(mds, roleName));
                        LOGGER.log(Level.INFO, "Adds the user {0} to the new role {1} related to the {2}", new Object[]{user, roleName, mds.getName()});
                    }
                    break;
                case AbstractProjectSuffixDBHelper.DELETE_RECORD:
                    for (final User user : users) {
                        RoleAuthorizer.REALM.unmap(user, mds, roleName);
                        LOGGER.log(Level.INFO, "Remove the user {0} to the role {1} related to the {2}", new Object[]{user, roleName, mds.getName()});
                    }
                    break;
                default:
                    LOGGER.log(Level.WARNING, "operation {0} was not expected", operation);
                    break;
            }
        }
        LOGGER.exiting(CLASS_NAME, "update");
    }

    /**
     * Loads the application related to a group with a name
     *
     * @param group group linked to an application
     * @param appName application name
     * @return the application or null if the application is not defined in the
     * REALM
     */
    private Application loadApplicationBy(final Group group, final String appName) {
        LOGGER.entering(CLASS_NAME, "loadApplicationBy", new Object[]{group.getName(), appName});

        final Set<Role> roles = RoleAuthorizer.REALM.findRoles(group);
        final Iterator<Role> roleIter = roles.iterator();
        Application searchedApp = null;
        while (roleIter.hasNext()) {
            final Role role = roleIter.next();
            final Application app = role.getApplication();
            if (app.getName().equals(appName)) {
                searchedApp = app;
            }
        }

        LOGGER.exiting(CLASS_NAME, "loadApplicationBy", searchedApp);

        return searchedApp;
    }

}
