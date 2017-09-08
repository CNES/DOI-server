/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import java.util.List;
import org.restlet.security.User;

/**
 * Interface for adding users to authenticate the requests.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public interface UserGroupMngtHelper {
    
    /**
     * Adds users to REALM.
     * @param realmUsers list of users to add
     */
    public void addUsersToRealm(final List<User> realmUsers);
    
    /**
     * Adds users to group.
     * @param usersGroup list of users to add
     */
    public void addUsersToUserGroup(final List<User> usersGroup);
    
    /**
     * Adds user to Administrators group
     * @param adminGroup list of users to add
     */
    public void addUsersToAdminGroup(final List<User> adminGroup);
    
}
