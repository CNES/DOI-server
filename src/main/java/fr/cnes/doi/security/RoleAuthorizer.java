/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.db.UserRoleDBHelper;
import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.db.ProjectSuffixDBHelper;
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
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_AUTH_010,
        reqName = Requirement.DOI_AUTH_010_NAME
)
public class RoleAuthorizer implements Observer {

    public static String ROLE_ADMIN = "admin";
    public static String GROUP_USERS = "Users";
    public static String GROUP_ADMIN = "Administrator";

    /**
     * Logger.
     */
    public static final Logger LOGGER = Utils.getAppLogger();

    /**
     * Class to handle the instance
     *
     */
    private static class RoleAuthorizerHolder {

        /**
         * Unique Instance unique
         */
        private final static RoleAuthorizer INSTANCE = new RoleAuthorizer();
    }

    /**
     * Access to unique INSTANCE of role authorizer
     *
     * @return the configuration instance.
     */
    public static RoleAuthorizer getInstance() {
        return RoleAuthorizerHolder.INSTANCE;
    }

    private static final UserRoleDBHelper userRolePlugin = PluginFactory.getUserManagement();
    private static final MemoryRealm REALM = userRolePlugin.getRealm();

    private RoleAuthorizer() {
        initUsersGroups();
    }

    private void initUsersGroups() {
        LOGGER.entering(this.getClass().getName(), "initUsersGroups");

        userRolePlugin.init(null);

        // Add users
        LOGGER.info("Add users to REALM");
        RoleAuthorizer.REALM.setUsers(userRolePlugin.getUsers());

        // Add Groups       
        Group administrators = new Group(GROUP_ADMIN, "Administrators");
        LOGGER.info("Add users to Administrators group");
        userRolePlugin.setUsersToAdminGroup(administrators.getMemberUsers());
        RoleAuthorizer.REALM.getRootGroups().add(administrators);

        LOGGER.exiting(this.getClass().getName(), "initUsersGroups");
    }

    private void initForMds(Application app) {
        LOGGER.entering(this.getClass().getName(), "initForMds", new Object[]{REALM, app.getName()});

        initForAdmin(app);

        Map<String, Integer> projects = UniqueProjectName.getInstance().getProjects();
        LOGGER.log(Level.INFO, "{0} projects have already been registered", projects.size());
        for (String project : projects.keySet()) {
            Integer projectID = projects.get(project);
            Role role = new Role(app, String.valueOf(projectID));
            List<User> users = userRolePlugin.getUsersFromRole(String.valueOf(projectID));
            for (User user : users) {
                LOGGER.log(Level.INFO, "Add user {0} to role {1} for {2}", new Object[]{user, projectID, app.getName()});
                RoleAuthorizer.REALM.map(user, role);
            }
        }

        app.getContext().setDefaultEnroler(RoleAuthorizer.REALM.getEnroler());
        app.getContext().setDefaultVerifier(RoleAuthorizer.REALM.getVerifier());

        LOGGER.exiting(this.getClass().getName(), "initForMds");
    }

    private Group findGroupByName(final String name) {
        LOGGER.entering(this.getClass().getName(), "findGroupByName", name);
        List<Group> groups = RoleAuthorizer.REALM.getRootGroups();
        Group searchedGroup = null;
        for (Group group : groups) {
            LOGGER.finest(String.format("group name : %s", group.getName()));
            if (group.getName().equals(name)) {
                searchedGroup = group;
                LOGGER.finest("group found");
                break;
            }
        }
        if (searchedGroup == null) {
            LOGGER.info(String.format("Please, create a group %s", name));
            DoiRuntimeException runtimeEx = new DoiRuntimeException(String.format("Please, create a group %s", name));
            LOGGER.throwing(GROUP_USERS, name, runtimeEx);
            throw runtimeEx;
        }
        LOGGER.exiting(this.getClass().getName(), "findGroupByName", searchedGroup);
        return searchedGroup;
    }

    private void initForAdmin(Application app) {
        LOGGER.entering(this.getClass().getName(), "initForAdmin", new Object[]{REALM, app.getName()});

        Group admin = findGroupByName(GROUP_ADMIN);

        RoleAuthorizer.REALM.map(admin, Role.get(app, ROLE_ADMIN));
        app.getContext().setDefaultEnroler(RoleAuthorizer.REALM.getEnroler());
        app.getContext().setDefaultVerifier(RoleAuthorizer.REALM.getVerifier());

        LOGGER.exiting(this.getClass().getName(), "initForAdmin");
    }

    /**
     * Init Realm for application.
     *
     * @param app application
     * @return True when the realm is initialized otherwise False
     */
    public boolean createRealmFor(Application app) {
        LOGGER.entering(this.getClass().getName(), "createReamFor", app.getName());

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

        LOGGER.exiting(this.getClass().getName(), "createReamFor", isCreated);

        return isCreated;
    }

    /**
     * Adds/removes the <i>users</i> group to the new role name related to the
     * application MDS
     *
     * @param o observable
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
    public void update(Observable o, Object obj) {
        LOGGER.entering(this.getClass().getName(), "update", new Object[]{o, obj});
        
        // Loads the admin group - admin group is defined by default
        Group adminGroup = findGroupByName(GROUP_ADMIN);

        // Loads the application MDS related to admin group
        Application mds = loadApplicationBy(adminGroup, DoiMdsApplication.NAME);
        
        if (mds == null) {
            LOGGER.warning(DoiMdsApplication.NAME + " is not defined in the REAM");
        } else {
            updateObserver(o, obj, mds);
        }

        LOGGER.exiting(this.getClass().getName(), "update");
    }
    
    /**
     * Updates REALM for mds.
     * @param o observable
     * @param obj message
     * @param mds application
     */
    private void updateObserver(Observable o, Object obj, Application mds) {
        LOGGER.entering(this.getClass().getName(), "updateObserver", new Object[]{o, obj, mds.getName()});
        
        if (o instanceof ProjectSuffixDBHelper) {
            LOGGER.finest("Observable is a ProjectSuffixDB type");

            String[] message = (String[]) obj;

            String operation = message[0];
            String roleName = message[1];
            List<User> users = userRolePlugin.getUsersFromRole(roleName);
            switch (operation) {
                case ProjectSuffixDBHelper.ADD_RECORD:
                    for (User user : users) {
                        RoleAuthorizer.REALM.map(user, Role.get(mds, roleName));
                        LOGGER.log(Level.INFO, "Adds the user {0} to the new role {1} related to the {2}", new Object[]{user, roleName, mds.getName()});
                    }
                    break;
                case ProjectSuffixDBHelper.DELETE_RECORD:
                    for (User user : users) {
                        RoleAuthorizer.REALM.unmap(user, mds, roleName);
                        LOGGER.log(Level.INFO, "Remove the user {0} to the role {1} related to the {2}", new Object[]{user, roleName, mds.getName()});
                    }
                    break;
                default:
                    LOGGER.log(Level.WARNING, "operation {0} was not expected", operation);
                    break;
            }
        }
        LOGGER.exiting(this.getClass().getName(), "update");        
    }

    /**
     * Loads the application related to a group with a name
     *
     * @param group group linked to an application
     * @param appName application name
     * @return the application or null if the application is not defined in the REALM
     */
    private Application loadApplicationBy(final Group group, final String appName) {
        LOGGER.entering(this.getClass().getName(), "loadApplicationBy", new Object[]{group.getName(), appName});

        Set<Role> roles = RoleAuthorizer.REALM.findRoles(group);
        Iterator<Role> roleIter = roles.iterator();
        Application searchedApp = null;
        while (roleIter.hasNext()) {
            Role role = roleIter.next();
            Application app = role.getApplication();
            if (app.getName().equals(appName)) {
                searchedApp = app;
            }
        }

        LOGGER.exiting(this.getClass().getName(), "loadApplicationBy", searchedApp);

        return searchedApp;
    }

}
