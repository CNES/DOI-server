/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.List;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.util.Series;
import static fr.cnes.doi.security.UtilsHeader.SELECTED_ROLE_PARAMETER;
import org.apache.logging.log4j.Logger;


/**
 * Base resource for the different resources.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class BaseMdsResource extends AbstractResource {
    
    /**
     * Logger.
     */
    protected Logger LOG;

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
        super.doInit();
        this.doiApp = (DoiMdsApplication) getApplication();
        LOG = this.doiApp.getLog();
        LOG.traceEntry();        
        LOG.traceExit();
    }

    /**
     * Tests if the user has only one single role.
     *
     * @param roles roles
     * @return True when the user has only one single role otherwise False
     */
    private boolean hasSingleRole(final List<Role> roles) {
        LOG.traceEntry("Parameter : {}",roles);
        return LOG.traceExit(roles.size() == 1);
    }

    /**
     * Tests if the user has no role.
     *
     * @param roles roles
     * @return True when the user has no role otherwise False
     */
    private boolean hasNoRole(final List<Role> roles) {
        LOG.traceEntry("Parameter : {}",roles);        
        return LOG.traceExit(roles.isEmpty());
    }

    /**
     * Tests if the user has selected a role.
     *
     * @param suffusedWithRole selected role
     * @return True when the user has selected a role otherwise False
     */
    private boolean hasSelectedRole(final String suffusedWithRole) {
        LOG.traceEntry("Parameter : {}",suffusedWithRole);
        return LOG.traceExit(!suffusedWithRole.isEmpty());
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
        LOG.traceEntry("Parameter : {}",selectedRole);
        final String roleName;
        if (hasSelectedRole(selectedRole)) {
            LOG.debug("Role selected : "+ selectedRole);
            if (isInRole(selectedRole)) {
                LOG.debug("User is in Role : "+ selectedRole);
                roleName = selectedRole;
            } else {
                LOG.debug("User is not in Role :"+ selectedRole);
                LOG.info("DOIServer : The role {} is not allowed to use this feature", selectedRole);
                throw LOG.throwing(new ResourceException(
                        Status.CLIENT_ERROR_FORBIDDEN, 
                        "DOIServer : The role " + selectedRole 
                                + " is not allowed to use this feature"
                ));
            }
        } else {
            final List<Role> roles = getClientInfo().getRoles();
            if (hasNoRole(roles)) {
                throw LOG.throwing(new ResourceException(
                        Status.CLIENT_ERROR_UNAUTHORIZED, "DOIServer : No role"));
            } else if (hasSingleRole(roles)) {
                final Role role = roles.get(0);
                roleName = role.getName();
                LOG.debug("User has a single Role "+ role);
            } else {
                LOG.info("DOIServer : Cannot know which role must be applied");
                throw LOG.throwing(new ResourceException(
                        Status.CLIENT_ERROR_CONFLICT, 
                        "DOIServer : Cannot know which role must be applied"));
            }
        }
        return LOG.traceExit(roleName);
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
    @Requirement(reqId = Requirement.DOI_AUTO_030,reqName = Requirement.DOI_AUTO_030_NAME)    
    protected void checkPermission(final String doiName, final String selectedRole) 
            throws ResourceException {
        LOG.traceEntry("Parameters : {} and {}", doiName, selectedRole);
        final String projectRole = getRoleName(selectedRole);
        final String prefixCNES = this.getDoiApp().getDataCentrePrefix();
        if (!doiName.startsWith(prefixCNES + "/" + projectRole + "/")) {
            LOG.debug("You are not allowed to use this method : {} with {}", doiName, selectedRole);
            throw LOG.throwing(new ResourceException(
                    Status.CLIENT_ERROR_FORBIDDEN, "You are not allowed to use this method"));
        }
        LOG.traceExit();
    }
    
    /**
     * Extract <i>selectedRole</i> from HTTP header.
     *
     * @return the selected role or an empty string when there is no selected
     * role
     */
    @Requirement(reqId = Requirement.DOI_AUTO_020,reqName = Requirement.DOI_AUTO_020_NAME)    
    public String extractSelectedRoleFromRequestIfExists() {
        LOG.traceEntry();
        final Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
        final String selectedRole = headers.getFirstValue(SELECTED_ROLE_PARAMETER, "");
        return LOG.traceExit(selectedRole);
    }    

    /**
     * Returns the Mds application.
     * @return the doiApp
     */
    public DoiMdsApplication getDoiApp() {
        return doiApp;
    }

}
