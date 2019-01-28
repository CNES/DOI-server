/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
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
import fr.cnes.doi.application.DoiMdsApplication.API_MDS;
import fr.cnes.doi.exception.DoiServerException;
import fr.cnes.doi.resource.AbstractResource;
import static fr.cnes.doi.security.UtilsHeader.SELECTED_ROLE_PARAMETER;

import fr.cnes.doi.utils.UniqueProjectName;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.security.Role;
import org.restlet.util.Series;

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
     * Logger.
     */
    protected volatile Logger LOG;

    /**
     * DOI Mds application.
     */
    private volatile DoiMdsApplication doiApp;

    /**
     * Init.
     *
     * @throws DoiServerException - if a problem happens
     */
    @Override
    protected void doInit() throws DoiServerException {
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
        LOG.traceEntry("Parameter : {}", roles);
        return LOG.traceExit(roles.size() == 1);
    }

    /**
     * Tests if the user has no role.
     *
     * @param roles roles
     * @return True when the user has no role otherwise False
     */
    private boolean hasNoRole(final List<Role> roles) {
        LOG.traceEntry("Parameter : {}", roles);
        return LOG.traceExit(roles.isEmpty());
    }

    /**
     * Tests if the user has selected a role.
     *
     * @param suffusedWithRole selected role
     * @return True when the user has selected a role otherwise False
     */
    private boolean hasSelectedRole(final String suffusedWithRole) {
        LOG.traceEntry("Parameter : {}", suffusedWithRole);
        return LOG.traceExit(!suffusedWithRole.isEmpty());
    }

    /**
     * Returns the suffix project with which the user is associated. For each suffix project, a role
     * is associated. To get the suffix projet, the role must be get in the HTTP header. When
     * several roles are associated to a user, a <i>selectedRole</i> must be provided to select the
     * role that the user wants to get.
     *
     * @param selectedRole selected role
     * @return the project name associated to the user
     * @throws DoiServerException - CLIENT_ERROR_FORBIDDEN if the role is not allowed to use this
     * feature - CLIENT_ERROR_UNAUTHORIZED if no role is provided - CLIENT_ERROR_CONFLICT if a user
     * is associated to more than one role
     */
    private String getRoleName(final String selectedRole) throws DoiServerException {
        LOG.traceEntry("Parameter : {}", selectedRole);
        final String roleName;
        if (hasSelectedRole(selectedRole)) {
            roleName = getRoleNameWhenRoleInHeader(selectedRole);
        } else {
            roleName = getRoleNameWhenNotProvidedInHeader();
        }
        return LOG.traceExit(roleName);
    }

    /**
     * Get the role when the role is not provided in the header.
     *
     * @return the role name
     */
    private String getRoleNameWhenNotProvidedInHeader() {
        LOG.traceEntry();
        // no role is provided in the header
        final List<Role> roles = getClientInfo().getRoles();
        if (hasNoRole(roles)) {
            // the user has no role, go out
            throw LOG.throwing(Level.DEBUG,
                    new DoiServerException(getApplication(), API_MDS.SECURITY_USER_NO_ROLE,
                            "User not contained in any role")
            );
        } else if (hasSingleRole(roles)) {
            // the user has only one role, ok do it
            final Role role = roles.get(0);
            LOG.debug("User has a single Role " + role);
            return LOG.traceExit(role.getName());
        } else {
            // the user has several roles, he has to select a profile, go out
            LOG.info("DOIServer : Cannot know which role must be applied");
            throw LOG.throwing(Level.DEBUG,
                    new DoiServerException(getApplication(), API_MDS.SECURITY_USER_CONFLICT,
                            "Cannot know which role must be applied")
            );
        }
    }

    /**
     * Get the role when the role is in the header.
     *
     * @param selectedRole role from the header
     * @return the role name
     */
    private String getRoleNameWhenRoleInHeader(final String selectedRole) {
        LOG.traceEntry("Parameter : {}", selectedRole);
        // the role is selected in the Header
        LOG.debug("Role selected : " + selectedRole);
        if (isInRole(selectedRole)) {
            // the selected role is well related to the user
            LOG.debug("User is in Role : " + selectedRole);
            return LOG.traceExit(selectedRole);
        } else {
            // the user is not contained in the selected role => a possible hacking
            LOG.debug("User is not in Role :" + selectedRole);
            LOG.info("DOIServer : The role {} is not allowed to use this feature", selectedRole);
            throw LOG.throwing(Level.DEBUG,
                    new DoiServerException(getApplication(),
                            API_MDS.SECURITY_USER_NOT_IN_SELECTED_ROLE,
                            "Fail to make this request with this role (" + selectedRole + ")")
            );
        }
    }

    /**
     * Checks permissions to access according to the role of the user and the DOI name. The
     * authorization in DOI server is based on <b>role</b>. Each projectSuffix is
     * <ul>
     * <li>a <b>role</b></li>
     * <li>related to a projectName</li>
     * </ul>
     * When the DOI name does not start with <i>prefixDOI/role</i>, an exception is send.
     *
     * @param doiName DOI name
     * @param selectedRole Selected role
     * @throws DoiServerException
     * <ul>
     * <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
     * <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li>
     * </ul>
     * @see fr.cnes.doi.resource.admin.SuffixProjectsResource Creates a suffixProject for a given
     * project name
     */
    @Requirement(reqId = Requirement.DOI_AUTO_030, reqName = Requirement.DOI_AUTO_030_NAME)
    protected void checkPermission(final String doiName,
            final String selectedRole)
            throws DoiServerException {
        LOG.traceEntry("Parameters : {} and {}", doiName, selectedRole);
        
        final String prefixCNES = this.getDoiApp().getDataCentrePrefix();
        
        // Token-ihm isn't attached to any project, in this case, check role in database
        if(selectedRole.equals("null")) {
        	String user = getClientInfo().getUser().getIdentifier();
        	Map<String, Integer> map = UniqueProjectName.getInstance().getProjectsFromUser(user);
        	boolean isAuthorized = false;
        	for(int suffixProject : map.values()) {
        		 if (doiName.startsWith(prefixCNES + "/" + suffixProject + "/")){
        			 isAuthorized = true;
        			 break;
        	     }
        	}
        	if(!isAuthorized) {
        		LOG.debug("The DOI {}  does not match with any user's role", doiName);
        		throw LOG.throwing(Level.DEBUG,
        				new DoiServerException(getApplication(), API_MDS.SECURITY_USER_PERMISSION,
        						"The DOI " + doiName + " does not match with any user's role")
        				);
        	}
        } else {
        	final String projectRole = getRoleName(selectedRole);
        	
        	if (!doiName.startsWith(prefixCNES + "/" + projectRole + "/")) {
        		LOG.debug("The DOI {}  does not match with {}", doiName, prefixCNES + "/" + projectRole);
        		throw LOG.throwing(Level.DEBUG,
        				new DoiServerException(getApplication(), API_MDS.SECURITY_USER_PERMISSION,
        						"The DOI " + doiName + " does not match with" + prefixCNES + "/" + projectRole)
        				);
        	}
        }
        LOG.traceExit();
    }

    /**
     * Extract <i>selectedRole</i> from HTTP header.
     *
     * @return the selected role or an empty string when there is no selected role
     */
    @Requirement(reqId = Requirement.DOI_AUTO_020, reqName = Requirement.DOI_AUTO_020_NAME)
    public String extractSelectedRoleFromRequestIfExists() {
        LOG.traceEntry();
        final Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
        final String selectedRole = headers.getFirstValue(SELECTED_ROLE_PARAMETER, "");
        return LOG.traceExit(selectedRole);
    }

    /**
     * Body representation.
     *
     * @return Wadl description for a body representation
     */
    protected RepresentationInfo explainRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setIdentifier("explainRepresentationID");
        repInfo.setMediaType(MediaType.TEXT_PLAIN);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Body representation");
        docInfo.setTextContent("short explanation of status code");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    /**
     * Describes WADL representation for {@link #checkPermission} method. The different
     * representations are the followings:
     * <ul>
     * <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
     * <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li>
     * </ul>
     *
     * @param info Method information
     */
    @Override
    protected void describeDelete(final MethodInfo info) {
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.SECURITY_USER_NO_ROLE.getStatus(),
                API_MDS.SECURITY_USER_NO_ROLE.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.SECURITY_USER_NOT_IN_SELECTED_ROLE.getStatus(),
                API_MDS.SECURITY_USER_NOT_IN_SELECTED_ROLE.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.SECURITY_USER_PERMISSION.getStatus(),
                API_MDS.SECURITY_USER_PERMISSION.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.SECURITY_USER_CONFLICT.getStatus(),
                API_MDS.SECURITY_USER_CONFLICT.getShortMessage(),
                "explainRepresentationID")
        );
    }

    /**
     * Describes WADL representation for {@link #checkPermission} method. The different
     * representations are the followings:
     * <ul>
     * <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
     * <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li>
     * </ul>
     *
     * @param info Method information
     */
    @Override
    protected void describePost(final MethodInfo info) {
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.SECURITY_USER_NO_ROLE.getStatus(),
                API_MDS.SECURITY_USER_NO_ROLE.getShortMessage(),
                explainRepresentation())
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.SECURITY_USER_NOT_IN_SELECTED_ROLE.getStatus(),
                API_MDS.SECURITY_USER_NOT_IN_SELECTED_ROLE.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.SECURITY_USER_PERMISSION.getStatus(),
                API_MDS.SECURITY_USER_PERMISSION.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.SECURITY_USER_CONFLICT.getStatus(),
                API_MDS.SECURITY_USER_CONFLICT.getShortMessage(),
                "explainRepresentationID")
        );
    }

    /**
     * Returns the Mds application.
     *
     * @return the doiApp
     */
    public DoiMdsApplication getDoiApp() {
        return doiApp;
    }

}
