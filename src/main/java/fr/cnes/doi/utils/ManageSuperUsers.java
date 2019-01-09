package fr.cnes.doi.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.persistence.model.DOIUser;
import fr.cnes.doi.plugin.PluginFactory;

/**
 * Utils class to manage SuperUsers from database
 *
 */
public class ManageSuperUsers {
	
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
    public static ManageSuperUsers getInstance() {
        return ManageSuperUsersHolder.INSTANCE;
    }
    
    /**
     * SuperUsers database.
     */
    private final AbstractUserRoleDBHelper userDB;
    
    /**
     * Constructor
     */
    private ManageSuperUsers() {
        LOGGER.entering(CLASS_NAME, "Constructor");
        this.userDB = PluginFactory.getUserManagement();
        this.userDB.init(new Object()); // not used
        LOGGER.exiting(CLASS_NAME, "Constructor");
    }
    
    public boolean isSuperUser(String username){
    	LOGGER.entering(CLASS_NAME, "isSuperUser", username);
    	/*
    	List<String> SuperUsers = new ArrayList<String>();
    	List<DOIUser> doiSuperUsers = userDB.getSuperUsersFromRole(projectId);
    	for(DOIUser doiUser : doiSuperUsers) {
    		SuperUsers.add(doiUser.getUsername());
    	}*/
    	return true;
    }
    
    public boolean addSuperUser(String username) {
    	LOGGER.entering(CLASS_NAME, "addSuperUser", username);
    	
    	DOIUser adminToChange = new DOIUser();
    	adminToChange.setUsername(username);
    	
    	return userDB.setUserToAdminGroup(adminToChange);
    }
    
    public boolean deleteSuperUser(String username) {
    	LOGGER.entering(CLASS_NAME, "deleteSuperUser", username);

    	DOIUser adminToChange = new DOIUser();
    	adminToChange.setUsername(username);
    	return userDB.unsetUserFromAdminGroup(adminToChange);
	}
    
    public List<String> getSuperUsers() {
    	LOGGER.entering(CLASS_NAME, "getSuperUsers");
        ArrayList<String> result = new ArrayList();
        
        List<DOIUser> users = userDB.getUsers();
        for(DOIUser doiUser : users) {
    		if (doiUser.getAdmin()) {
    			result.add(doiUser.getUsername());
    		}
    	}
        return result;
    }
    
    
    
    /**
     * Class to handle the instance
     *
     */
    private static class ManageSuperUsersHolder {
        
        /**
         * Unique Instance unique
         */
        private static final ManageSuperUsers INSTANCE = new ManageSuperUsers();
    }
}
