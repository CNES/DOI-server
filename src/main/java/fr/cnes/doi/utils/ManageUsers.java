package fr.cnes.doi.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.persistence.model.DOIUser;
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
    
    public List<String> getAllUsersFromProject(int projectId){
    	LOGGER.entering(CLASS_NAME, "getAllUsersFromProject", projectId);
    	List<String> users = new ArrayList<String>();
    	List<DOIUser> doiUsers = userDB.getUsersFromRole(projectId);
    	for(DOIUser doiUser : doiUsers) {
    		users.add(doiUser.getUsername());
    	}
    	return users;
    }
    
    public boolean addUserToProject(String username, int projectId) {
    	LOGGER.entering(CLASS_NAME, "addUserToProject", new Object[] {username, projectId});
    	if(!userDB.isUserExist(username)) {
    		return false;
    	}
    	return userDB.addUserToRole(username, projectId);
    }
    
    public boolean deleteUserFromProject(int projectId, String username) {
    	LOGGER.entering(CLASS_NAME, "deleteUserFromProject", new Object[] {projectId, username});
    	return userDB.removeUserToRole(username, projectId);
	}
    
    public boolean isUserExist(String userName){
    	LOGGER.entering(CLASS_NAME, "isUserExist", userName);
    	return userDB.isUserExist(userName);
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
