/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import java.util.List;
import org.restlet.security.User;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public interface UserGroupMngtPlugin {
    
    public void addUsersToRealm(final List<User> realmUsers);
    
    public void addUsersToUserGroup(final List<User> usersGroup);
    
    public void addUsersToAdminGroup(final List<User> adminGroup);
    
}
