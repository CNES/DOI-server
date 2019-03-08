package fr.cnes.doi.resource.admin;

import org.apache.logging.log4j.Logger;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.utils.ManageSuperUsers;
import fr.cnes.doi.utils.ManageUsers;

/**
 * Provide a resource to ask if a user belong to the super user group and
 * another one to remove a user from the super user group.
 */
public class ManageSuperUserResource extends AbstractResource {

    /**
     * Logger.
     */
    private volatile Logger LOG;

    /**
     * User name.
     */
    private volatile String userName;

    /**
     * Set-up method that can be overridden in order to initialize the state of the
     * resource.
     *
     * @throws ResourceException
     *             - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
	super.doInit();
	final AdminApplication app = (AdminApplication) getApplication();
	LOG = app.getLog();
	LOG.traceEntry();
	this.userName = getAttribute("userName");
	LOG.debug(this.userName);
	setDescription("This resource handles super user");
	LOG.traceExit();
    }

    // TODO requirement
    /**
     * Returns null is user doesn't exist otherwise return true or false if user is
     * admin or not.
     *
     * @return boolean (may be null).
     */
    @Get
    public Boolean isUserExistAndAdmin() {
	LOG.traceEntry();
	return LOG.traceExit(ManageSuperUsers.getInstance().isSuperUser(userName));
    }

    // TODO requirement
    /**
     * Delete the SUPERUSER from database.
     *
     * @return the list of dois
     */
    // @Requirement(reqId = Requirement.DOI_SRV_140, reqName =
    // Requirement.DOI_SRV_140_NAME)
    @Delete
    public boolean deleteSUPERUSER() {
	LOG.traceEntry();
	return LOG.traceExit(ManageSuperUsers.getInstance().deleteSuperUser(userName));
    }

}
