/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.db.ProjectSuffixDB;
import fr.cnes.doi.utils.UniqueProjectName;
import fr.cnes.doi.utils.Utils;
import java.text.MessageFormat;
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
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class RoleAuthorizer implements Observer {

    public static String ROLE_ADMIN = "admin";

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

    private static final MemoryRealm realm = new MemoryRealm();

    private RoleAuthorizer() {
        initUsersGroups();
    }

    private void initUsersGroups() {
        LOGGER.entering(this.getClass().getName(), "initUsersGroups");
        // Add users
        User admin = new User("admin", "admin");
        User jcm = new User("malapert", "pwd", "Jean-Christophe", "Malapert", "jcmalapert@gmail.com");
        User cc = new User("caillet", "pppp", "Claire", "Caillet", "claire.caillet@cnes.fr");
        User test1 = new User("test1", "test1");
        User test2 = new User("test2", "test2");
        RoleAuthorizer.realm.getUsers().add(admin);
        RoleAuthorizer.realm.getUsers().add(jcm);
        RoleAuthorizer.realm.getUsers().add(cc);
        RoleAuthorizer.realm.getUsers().add(test1);
        RoleAuthorizer.realm.getUsers().add(test2);

        // Add Groups                         
        Group users = new Group("Users", "Users");
        users.getMemberUsers().add(jcm);
        users.getMemberUsers().add(cc);
        users.getMemberUsers().add(test1);
        users.getMemberUsers().add(test2);
        RoleAuthorizer.realm.getRootGroups().add(users);

        Group administrators = new Group("Administrator", "Administrators");
        administrators.getMemberUsers().add(jcm);
        administrators.getMemberUsers().add(admin);
        RoleAuthorizer.realm.getRootGroups().add(administrators);

        LOGGER.exiting(this.getClass().getName(), "initUsersGroups");
    }

    private void initForMds(Application app) {
        LOGGER.entering(this.getClass().getName(), "initForMds", new Object[]{realm, app.getName()});

        initForAdmin(app);

        Map<String, Integer> projects = UniqueProjectName.getInstance().getProjects();
        for (String project : projects.keySet()) {
            Integer projectID = projects.get(project);
            for (Group group : RoleAuthorizer.realm.getRootGroups()) {
                RoleAuthorizer.realm.map(group, Role.get(app, String.valueOf(projectID)));
            }
        }

        app.getContext().setDefaultEnroler(RoleAuthorizer.realm.getEnroler());
        app.getContext().setDefaultVerifier(RoleAuthorizer.realm.getVerifier());

        LOGGER.exiting(this.getClass().getName(), "initForMds");
    }

    private void initForAdmin(Application app) {
        LOGGER.entering(this.getClass().getName(), "initForAdmin", new Object[]{realm, app.getName()});

        List<Group> groups = RoleAuthorizer.realm.getRootGroups();
        Group admin = null;
        for (Group group : groups) {
            if ("Administrator".equals(group.getName())) {
                admin = group;
                break;
            }
        }
        if (admin == null) {
            throw new RuntimeException("Please, create a group Administrator");
        }

        RoleAuthorizer.realm.map(admin, Role.get(app, ROLE_ADMIN));
        app.getContext().setDefaultEnroler(RoleAuthorizer.realm.getEnroler());
        app.getContext().setDefaultVerifier(RoleAuthorizer.realm.getVerifier());

        LOGGER.exiting(this.getClass().getName(), "initForAdmin");
    }

    public boolean createReamFor(Application app) {
        LOGGER.entering(this.getClass().getName(), "createReamFor", app.getName());

        boolean isCreated;
        switch (app.getName()) {
            case DoiMdsApplication.NAME:
                initForMds(app);
                isCreated = true;
                break;
            case AdminApplication.NAME:
                initForAdmin(app);
                isCreated = true;
                break;
            default:
                isCreated = false;
                break;
        }

        LOGGER.exiting(this.getClass().getName(), "createReamFor", isCreated);

        return isCreated;
    }

    @Override
    public void update(Observable o, Object arg) {
        LOGGER.entering(this.getClass().getName(), "update", new Object[]{o, arg});

        if (o instanceof ProjectSuffixDB) {
            LOGGER.fine("Observable is a ProjectSuffixDB type");

            List<Group> groups = RoleAuthorizer.realm.getRootGroups();
            Group admin = null;
            for (Group group : groups) {
                if ("Administrator".equals(group.getName())) {
                    admin = group;
                    break;
                }
            }
            
            // find the role for which the group is attached.
            Set<Role> roles = RoleAuthorizer.realm.findRoles(admin);
            Iterator<Role> roleIter = roles.iterator();
            Application mds = null;
            while(roleIter.hasNext()) {
                Role role = roleIter.next();
                Application app = role.getApplication();
                if(app.getName().equals(DoiMdsApplication.NAME)) {
                    mds = app;
                }
            }
            if(mds == null) {
                throw new RuntimeException("MdsApplication not found");
            }
                        
            Group users = null;
            for (Group group : groups) {
                LOGGER.fine(String.format("group name : %s", group.getName()));                
                if ("Users".equals(group.getName())) {
                    users = group;
                    LOGGER.fine("Users found");
                    break;
                }
            }

            RoleAuthorizer.realm.map(users, Role.get(mds, String.valueOf(arg)));
            LOGGER.log(Level.FINE, "Links user with role {0} for app {1}", new Object[]{arg, mds.getName()});
        }
    }

    //TODO : Synchronizer par une notification RoleAuthorize et ajout de nouveaux projets dans la DB project
}
