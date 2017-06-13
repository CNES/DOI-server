/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.resource.BaseResource;
import java.util.List;
import java.util.logging.Level;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;

/**
 * Base resource for the different resources.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class BaseMdsResource extends BaseResource {       
    
    /**
     * DOI Mds application.
     */
    protected DoiMdsApplication doiApp;

    /**
     *
     * @throws ResourceException
     */
    @Override
    protected void doInit() throws ResourceException {
        this.doiApp = (DoiMdsApplication)getApplication();        
    }
    
    /**
     * Tests if the user has only one single role.
     * @param roles roles
     * @return True when the user has only one single role otherwise False
     */
    private boolean hasSingleRole(List<Role> roles) {
        return roles.size() == 1;
    }
    
    /**
     * Tests if the user has selected a role.
     * @param suffusedWithRole selected role
     * @return True when the user has selected a role otherwise False
     */
    private boolean hasSelectedRole(String suffusedWithRole) {
        return !suffusedWithRole.isEmpty();
    }    
    
    /**
     * Returns the project with which the user is associated.
     * Two exceptions could be thrown :
     * <ul>
     * <li>CLIENT_ERROR_UNAUTHORIZED : "DOIServer : The user is not allowed to use this feature"</li>
     * <li>CLIENT_ERROR_CONFLICT : "DOIServer : Cannot know which role must be applied"</li>
     * </ul> 
     * @param selectedRole selected role
     * @return the project name associated to the user
     */
    private String getRoleName(String selectedRole) {
        getLogger().entering(getClass().getName(), "getRoleName",selectedRole);
        final String roleName;
        if(hasSelectedRole(selectedRole)) {
            if(isInRole(selectedRole)) {
                roleName = selectedRole;
            } else {
                getLogger().log(Level.WARNING, "DOIServer : The role {0} is not allowed to use this feature", selectedRole);
                getLogger().exiting(getClass().getName(), "getRoleName");
                throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED,"DOIServer : The role "+selectedRole+" is not allowed to use this feature");                                
            }
        } else {
            List<Role> roles = getClientInfo().getRoles();
            if(hasSingleRole(roles)) {
                Role role = roles.get(0);
                roleName = role.getName();                
            } else {
                getLogger().log(Level.WARNING, "DOIServer : Cannot know which role must be applied");
                getLogger().exiting(getClass().getName(), "getRoleName");                
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "DOIServer : Cannot know which role must be applied");                
            }            
        }
        getLogger().exiting(getClass().getName(), "getRoleName", roleName);
        return roleName;
    }
    
    /**
     * Checks permissions to access according to the role of the user.
     * Throw an exception CLIENT_ERROR_UNAUTHORIZED when the role of the user
     * is not included in the DOI name
     * @param doiName DOI name
     * @param selectedRole Selected role
     */
    protected void checkPermission(final String doiName, final String selectedRole) {
        getLogger().entering(getClass().getName(), "checkPermission", new Object[]{doiName, selectedRole});
        String projectName = getRoleName(selectedRole);
        String prefixCNES = this.doiApp.getDataCentrePrefix();
        if(!doiName.startsWith(prefixCNES+"/"+projectName+"/")) {
            getLogger().log(Level.WARNING, "You are not allowed to use this method : {0} with {1}", new Object[]{doiName, selectedRole});
            getLogger().exiting(getClass().getName(), "checkPermission");
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "You are not allowed to use this method");
        }
        getLogger().exiting(getClass().getName(), "checkPermission");        
    } 
    
}
