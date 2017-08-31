/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.db.ProjectSuffixDB;
import fr.cnes.doi.resource.admin.SuffixProjectsResource;
import fr.cnes.doi.utils.UniqueProjectName;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import org.restlet.Application;
import org.restlet.security.Group;
import org.restlet.security.MemoryRealm;
import org.restlet.security.Realm;
import org.restlet.security.Role;
import org.restlet.security.User;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class RoleAuthorizer implements Observer {

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

    private final MemoryRealm realm;

    private RoleAuthorizer() {
        this.realm = initUsersGroups();
    }

    private MemoryRealm initUsersGroups() {
        // Create in-memory users with roles
        MemoryRealm myRealm = new MemoryRealm();

        // Add users
        User admin = new User("admin", "admin");
        User jcm = new User("malapert", "pwd", "Jean-Christophe", "Malapert", "jcmalapert@gmail.com");
        User cc = new User("caillet", "pppp", "Claire", "Caillet", "claire.caillet@cnes.fr");
        User test1 = new User("test1", "test1");
        User test2 = new User("test2", "test2");
        myRealm.getUsers().add(admin);
        myRealm.getUsers().add(jcm);
        myRealm.getUsers().add(cc);
        myRealm.getUsers().add(test1);
        myRealm.getUsers().add(test2);

        // Add Groups                         
        Group users = new Group("Users", "Users");
        users.getMemberUsers().add(jcm);
        users.getMemberUsers().add(cc);
        users.getMemberUsers().add(test1);
        users.getMemberUsers().add(test2);
        myRealm.getRootGroups().add(users);

        Group administrators = new Group("Administrator", "Administrators");
        administrators.getMemberUsers().add(jcm);
        administrators.getMemberUsers().add(admin);
        myRealm.getRootGroups().add(administrators);

        return myRealm;
    }

    private void initForMds(MemoryRealm realm, Application app) {
        Map<String, Integer> projects = UniqueProjectName.getInstance().getProjects();
        for (String project : projects.keySet()) {
            Integer projectID = projects.get(project);
            for (Group group : realm.getRootGroups()) {
                realm.map(group, Role.get(app, String.valueOf(projectID)));
            }
        }
    }

    private void initForAdmin(MemoryRealm realm, Application app) {
        List<Group> groups = realm.getRootGroups();
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

        realm.map(admin, Role.get(app, "admin"));
    }

    public boolean createReamFor(Application app) {
        boolean isCreated;
        switch (app.getName()) {
            case DoiMdsApplication.NAME:
                initForMds(this.realm, app);
                isCreated = true;
                break;
            case AdminApplication.NAME:
                initForAdmin(this.realm, app);
                isCreated = true;
                break;
            default:
                isCreated = false;
                break;
        }
        return isCreated;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof ProjectSuffixDB) {
            List<Group> groups = realm.getRootGroups();
            Group users = null;
            for (Group group : groups) {
                if ("Users".equals(group.getName())) {
                    users = group;
                    break;
                }
            }
            Role role = this.realm.findRoles(users).iterator().next();
            Application app = role.getApplication();
            this.realm.map(users, Role.get(app, String.valueOf(arg)));
        }
    }

    //TODO : Synchronizer par une notification RoleAuthorize et ajout de nouveaux projets dans la DB project
}
