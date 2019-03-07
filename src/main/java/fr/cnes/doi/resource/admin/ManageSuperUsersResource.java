package fr.cnes.doi.resource.admin;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.utils.ManageSuperUsers;
import fr.cnes.doi.utils.UniqueProjectName;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Provide a resource to get all super users and add a user to the super user
 * group.
 */
public class ManageSuperUsersResource extends AbstractResource {

    /**
     * Parameter for the SUPERUSER name {@value #SUPERUSER_NAME_PARAMETER}. This
     * parameter is send to create a new identifier for the SUPERUSER.
     */
    public static final String SUPERUSER_NAME_PARAMETER = "superUserName";

    /**
     * Logger.
     */
    private volatile Logger LOG;

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
	setDescription("This resource handles super users");
	LOG.traceExit();
    }

    // TODO requirement
    /**
     * Rename the SUPERUSER from the SUPERUSER id sent in url.
     * 
     * @param mediaForm
     *            Data sent with request containing the user name to be super user.
     * @return the list of dois
     */
    // @Requirement(reqId = Requirement.DOI_SRV_140, reqName =
    // Requirement.DOI_SRV_140_NAME)
    @Post
    public boolean createSUPERUSER(final Form mediaForm) {
	LOG.traceEntry();
	checkInputs(mediaForm);
	final String newSUPERUSERName = mediaForm.getFirstValue(SUPERUSER_NAME_PARAMETER);
	return LOG.traceExit(ManageSuperUsers.getInstance().addSuperUser(newSUPERUSERName));
    }

    // TODO requirement
    /**
     * Returns the list of superusers as an array format.
     *
     * @return the list of superusers as an array format
     */
    // @Requirement(reqId = Requirement.DOI_SRV_140, reqName =
    // Requirement.DOI_SRV_140_NAME)
    @Get
    public List<String> getSuperUsersAsJson() {
	LOG.traceEntry();
	return LOG.traceExit(ManageSuperUsers.getInstance().getSuperUsers());
    }

    /**
     * Tests if the {@link #SUPERUSER_NAME_PARAMETER} is set.
     *
     * @param mediaForm
     *            the parameters
     * @throws ResourceException
     *             - if SUPERUSER_NAME_PARAMETER is not set
     */
    private void checkInputs(final Form mediaForm) throws ResourceException {
	LOG.traceEntry("Parameter : {}", mediaForm);
	if (isValueNotExist(mediaForm, SUPERUSER_NAME_PARAMETER)) {
	    throw LOG.throwing(Level.DEBUG, new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
		    SUPERUSER_NAME_PARAMETER + " parameter must be set"));
	}
	LOG.debug("The form is valid");
	LOG.traceExit();
    }

}
