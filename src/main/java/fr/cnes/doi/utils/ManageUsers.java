package fr.cnes.doi.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.db.MyMemoryRealm;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.plugin.PluginFactory;

/**
 * Utils class to manage users from database
 *
 */
public class ManageUsers {

    /**
     * Class name.
     */
    private static final String CLASS_NAME = ManageProjects.class.getName();

    /**
     * logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static ManageUsers getInstance() {
        return ManageUsersHolder.INSTANCE;
    }

    /**
     * Users database.
     */
    private final AbstractUserRoleDBHelper userDB;

    /**
     * Constructor
     */
    private ManageUsers() {
        LOGGER.entering(CLASS_NAME, "Constructor");
        this.userDB = PluginFactory.getUserManagement();
        this.userDB.init(new Object()); // not used
        LOGGER.exiting(CLASS_NAME, "Constructor");
    }

    public List<String> getAllUsersFromProject(int projectId) {
        LOGGER.entering(CLASS_NAME, "getAllUsersFromProject", projectId);
        List<String> users = new ArrayList<String>();
        List<DOIUser> doiUsers = userDB.getUsersFromRole(projectId);
        for (DOIUser doiUser : doiUsers) {
            users.add(doiUser.getUsername());
        }
        return users;
    }

    public boolean addUserToProject(String username, int projectId) {
        LOGGER.entering(CLASS_NAME, "addUserToProject", new Object[]{username, projectId});
        final boolean isAdded;
        if (this.isUserExist(username)) {
            isAdded = userDB.addUserToRole(username, projectId);
        } else {
            isAdded = false;
        }
        return isAdded;
    }

    public boolean deleteUserFromProject(int projectId, String username) {
        LOGGER.entering(CLASS_NAME, "deleteUserFromProject", new Object[]{projectId, username});
        return userDB.removeUserToRole(username, projectId);
    }

    public boolean isUserExist(String userName) {
        LOGGER.entering(CLASS_NAME, "isUserExist", userName);
        return userDB.isUserExist(userName);
    }

    public List<DOIUser> getUsers() {
        return userDB.getUsers();
    }

    public void removeDOIUser(String username) {
        userDB.removeDOIUser(username);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void addDOIUser(String username, boolean b, String email) {
        try {
            userDB.addDOIUser(username, b, email);
        } catch (DOIDbException ex) {
            LOGGER.log(Level.SEVERE, "Cannot add " + username, ex);
        }
    }

    public MyMemoryRealm getRealm() {
        return userDB.getRealm();
    }

    public List<DOIUser> getUsersFromRole(Integer projectID) {
        return userDB.getUsersFromRole(projectID);
    }

    /**
     * Class to handle the instance
     *
     */
    private static class ManageUsersHolder {

        /**
         * Unique Instance unique
         */
        private static final ManageUsers INSTANCE = new ManageUsers();
    }
}
