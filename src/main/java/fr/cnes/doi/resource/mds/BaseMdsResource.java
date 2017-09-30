/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.List;
import java.util.logging.Level;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.util.Series;
import static fr.cnes.doi.security.UtilsHeader.SELECTED_ROLE_PARAMETER;


/**
 * Base resource for the different resources.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class BaseMdsResource extends AbstractResource {

    /**
     * The parameter that describes the DOI name {@value #DOI_PARAMETER}.
     */
    public static final String DOI_PARAMETER = "doi";

    /**
     * The parameter that describes the landing page related to the DOI {@value #URL_PARAMETER}.
     */
    public static final String URL_PARAMETER = "url";

    /**
     * DOI Mds application.
     */
    private DoiMdsApplication doiApp;

    /**
     * Init.
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        this.doiApp = (DoiMdsApplication) getApplication();
    }

    /**
     * Tests if the user has only one single role.
     *
     * @param roles roles
     * @return True when the user has only one single role otherwise False
     */
    private boolean hasSingleRole(final List<Role> roles) {
        return roles.size() == 1;
    }

    /**
     * Tests if the user has no role.
     *
     * @param roles roles
     * @return True when the user has no role otherwise False
     */
    private boolean hasNoRole(final List<Role> roles) {
        return roles.isEmpty();
    }

    /**
     * Tests if the user has selected a role.
     *
     * @param suffusedWithRole selected role
     * @return True when the user has selected a role otherwise False
     */
    private boolean hasSelectedRole(final String suffusedWithRole) {
        return !suffusedWithRole.isEmpty();
    }

    /**
     * Returns the suffix project with which the user is associated. For each
     * suffix project, a role is associated. To get the suffix projet, the role
     * must be get in the HTTP header. When several roles are associated to a
     * user, a <i>selectedRole</i> must be provided to select the role that the
     * user wants to get.
     *
     * @param selectedRole selected role
     * @return the project name associated to the user
     * @throws ResourceException - CLIENT_ERROR_FORBIDDEN if the role is not
     * allowed to use this feature - CLIENT_ERROR_UNAUTHORIZED if no role is
     * provided - CLIENT_ERROR_CONFLICT if a user is associated to more than one
     * role
     */
    private String getRoleName(final String selectedRole) throws ResourceException {
        getLogger().entering(getClass().getName(), "getRoleName", selectedRole);
        final String roleName;
        if (hasSelectedRole(selectedRole)) {
            getLogger().log(Level.FINEST, "Role selected : {0}", selectedRole);
            if (isInRole(selectedRole)) {
                getLogger().log(Level.FINEST, "User is in Role {0}", selectedRole);
                roleName = selectedRole;
            } else {
                getLogger().log(Level.FINEST, "User is not in Role {0}", selectedRole);
                getLogger().log(Level.WARNING, "DOIServer : The role {0} is not allowed to use this feature", selectedRole);
                getLogger().exiting(getClass().getName(), "getRoleName");
                final ResourceException exception = new ResourceException(
                        Status.CLIENT_ERROR_FORBIDDEN, 
                        "DOIServer : The role " + selectedRole 
                                + " is not allowed to use this feature"
                );
                getLogger().throwing(this.getClass().getName(), "getRoleName", exception);
                throw exception;
            }
        } else {
            final List<Role> roles = getClientInfo().getRoles();
            if (hasNoRole(roles)) {
                final ResourceException exception = new ResourceException(
                        Status.CLIENT_ERROR_UNAUTHORIZED, "DOIServer : No role");
                getLogger().throwing(this.getClass().getName(), "getRoleName", exception);
                throw exception;
            } else if (hasSingleRole(roles)) {
                final Role role = roles.get(0);
                roleName = role.getName();
                getLogger().log(Level.FINEST, "User has a single Role {0}", role);
            } else {
                getLogger().log(Level.WARNING, "DOIServer : Cannot know which role must be applied");
                final ResourceException exception = new ResourceException(
                        Status.CLIENT_ERROR_CONFLICT, 
                        "DOIServer : Cannot know which role must be applied");
                getLogger().throwing(this.getClass().getName(), "getRoleName", exception);
                throw exception;
            }
        }
        getLogger().exiting(getClass().getName(), "getRoleName", roleName);
        return roleName;
    }

    /**
     * Checks permissions to access according to the role of the user and the
     * DOI name. The authorization in DOI server is based on <b>role</b>. Each
     * projectSuffix is
     * <ul>
     * <li>a <b>role</b></li>
     * <li>related to a projectName</li>
     * </ul>
     * When the DOI name does not start with <i>prefixDOI/role</i>, an exception
     * is send.
     *
     * @param doiName DOI name
     * @param selectedRole Selected role
     * @throws ResourceException <ul>
     * <li>403 Forbidden if the role is not allowed to use this feature or the
     * user is not allow to create the DOI</li>
     * <li>401 Unauthorized if no role is provided</li>
     * <li>409 Conflict if a user is associated to more than one role</li>
     * </ul>
     * @see fr.cnes.doi.resource.admin.SuffixProjectsResource Creates a
     * suffixProject for a given project name
     */
    @Requirement(
            reqId = Requirement.DOI_AUTO_030,
            reqName = Requirement.DOI_AUTO_030_NAME
    )    
    protected void checkPermission(final String doiName, final String selectedRole) 
            throws ResourceException {
        getLogger().entering(getClass().getName(), "checkPermission", new Object[]{doiName, selectedRole});
        final String projectRole = getRoleName(selectedRole);
        final String prefixCNES = this.getDoiApp().getDataCentrePrefix();
        if (!doiName.startsWith(prefixCNES + "/" + projectRole + "/")) {
            getLogger().log(Level.WARNING, "You are not allowed to use this method : {0} with {1}", new Object[]{doiName, selectedRole});
            getLogger().exiting(getClass().getName(), "checkPermission");
            final ResourceException exception = new ResourceException(
                    Status.CLIENT_ERROR_FORBIDDEN, "You are not allowed to use this method");
            getLogger().throwing(this.getClass().getName(), "checkPermission", exception);
            throw exception;
        }
        getLogger().exiting(getClass().getName(), "checkPermission");
    }
    
    /**
     * Extract <i>selectedRole</i> from HTTP header.
     *
     * @return the selected role or an empty string when there is no selected
     * role
     */
    @Requirement(
        reqId = Requirement.DOI_AUTO_020,
        reqName = Requirement.DOI_AUTO_020_NAME
        )    
    public String extractSelectedRoleFromRequestIfExists() {
        final Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
        final String selectedRole = headers.getFirstValue(SELECTED_ROLE_PARAMETER, "");
        getLogger().log(Level.INFO, "Selected role : {0}", selectedRole);
        return selectedRole;
    }    

    /**
     * Returns the Mds application.
     * @return the doiApp
     */
    public DoiMdsApplication getDoiApp() {
        return doiApp;
    }

}
