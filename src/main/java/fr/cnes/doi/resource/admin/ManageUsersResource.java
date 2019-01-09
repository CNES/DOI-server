package fr.cnes.doi.resource.admin;

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
import fr.cnes.doi.utils.ManageProjects;
import fr.cnes.doi.utils.spec.Requirement;

public class ManageUsersResource extends AbstractResource {
	
	/**
     * Logger.
     */
    private volatile Logger LOG;
    
    /**
     * Suffix of the project.
     */
    private volatile String suffixProject;
    
    /**
     * User name.
     */
    private volatile String userName;
    
    /**
     * Parameter for the user name {@value #USER_NAME_PARAMETER}. This parameter is send to
     * associate an user to a project.
     */
    public static final String USER_NAME_PARAMETER = "user";
    
    /**
     * Set-up method that can be overridden in order to initialize the state of the resource.
     * 
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        final AdminApplication app = (AdminApplication) getApplication();
        LOG = app.getLog();
        LOG.traceEntry();
        setDescription("This resource handles association between users and projects");
        this.suffixProject = getAttribute("suffixProject");
        this.userName = getAttribute("userName");
        LOG.debug(this.suffixProject);
        LOG.debug(this.userName);
        
        System.out.println(this.suffixProject + " <<<< suffix Project");
        System.out.println(this.userName + " <<<< userName");
        LOG.traceExit();
    }
    
    //TODO requirement
    @Get
    public void getUsers() {
    	
    }
    
    //TODO requirement
    /**
     * Rename the project from the project id sent in url.
     *
     * @return the list of dois
     */
    @Requirement(reqId = Requirement.DOI_SRV_140, reqName = Requirement.DOI_SRV_140_NAME)
    @Post
    public boolean renameProject(final Form mediaForm) {
    	 LOG.traceEntry();
         checkInputs(mediaForm);
//         final String newProjectName = mediaForm.getFirstValue(PROJECT_NAME_PARAMETER);
//         boolean isRenamed = ManageProjects.getInstance().renameProject(
//        		 Integer.parseInt(suffixProject), newProjectName);
         
//         return LOG.traceExit(isRenamed);
         return LOG.traceExit(false);
    }
    
  //TODO requirement
    /**
     * Delete the project from database.
     *
     * @return the list of dois
     */
    @Requirement(reqId = Requirement.DOI_SRV_140, reqName = Requirement.DOI_SRV_140_NAME)
    @Delete
    public boolean deleteProject() {
    	 LOG.traceEntry();
         boolean isDeleted = ManageProjects.getInstance().deleteProject(
        		 Integer.parseInt(suffixProject));
         
         return LOG.traceExit(isDeleted);
    }
    
    /**
     * Tests if the {@link #USER_NAME_PARAMETER} is set.
     *
     * @param mediaForm the parameters
     * @throws ResourceException - if USER_NAME_PARAMETER is not set
     */
    private void checkInputs(final Form mediaForm) throws ResourceException {
        LOG.traceEntry("Parameter : {}", mediaForm);
        if (isValueNotExist(mediaForm, USER_NAME_PARAMETER)) {
            throw LOG.throwing(Level.DEBUG, new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    USER_NAME_PARAMETER + " parameter must be set"));
        }
        LOG.debug("The form is valid");
        LOG.traceExit();
    }
    
    
}
