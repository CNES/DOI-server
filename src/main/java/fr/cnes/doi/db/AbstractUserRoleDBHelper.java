/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.db;

import fr.cnes.doi.utils.spec.Requirement;
import java.util.List;
import java.util.Observable;
import org.restlet.security.MemoryRealm;
import org.restlet.security.Role;
import org.restlet.security.User;

/**
 * Interface for handling users and role database.
 * This database is used to authenticate the requests.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_INTER_050,
        reqName = Requirement.DOI_INTER_050_NAME
)
public abstract class  AbstractUserRoleDBHelper extends Observable {
    
    /**
     * Notification message when an user is added {@value #ADD_USER_NOTIFICATION}.
     */    
    public static final String ADD_USER_NOTIFICATION = "AddUserNotification";
    
    /**
     * Notification message when an user is deleted {@value #REMOVE_USER_NOTIFICATION}.
     */    
    public static final String REMOVE_USER_NOTIFICATION = "RemoveUserNotification";   
    
    /**
     * Realm. 
     */
    private static final MemoryRealm REALM = new MemoryRealm();
    
    /**
     * Init the connection.
     * @param configuration the connection configuration
     */
    public abstract void init(Object configuration);
    
    /**
     * Returns the realm.
     * @return the realm
     */
    public MemoryRealm getRealm() {
        return REALM;
    }
    
    /**
     * Returns the allowed users for authentication.
     * @return List of users to add for the authentication
     */
    public abstract List<User> getUsers();
    
    /**
     * Adds users to a specific role.
     * @param roleName role name
     * @return The users related to a specific role
     */
    public abstract List<User> getUsersFromRole(final String roleName);
        
    /**
     * Adds an user to a specific role.
     * @param user user to add 
     * @param role role
     */
    public void addUserToRole(final User user, final Role role) {
        REALM.map(user, role);
    }
    
    /**
     * Removes an user from a specific role.
     * @param user user to remove
     * @param role role
     */
    public void removeUserToRole(final User user, final Role role){
        REALM.unmap(user, role);
    }    
    
    /**
     * Adds user to Administrators group
     * @param adminGroup list of users to add
     */
    public abstract void setUsersToAdminGroup(final List<User> adminGroup);    
    
}
