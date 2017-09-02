/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.admin;

import fr.cnes.doi.resource.BaseResource;
import fr.cnes.doi.utils.UniqueProjectName;
import java.util.Map;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class SuffixProjectsResource extends BaseResource {
    
    /**
     * Parameter for the project name.
     * This parameter is send to create an identifier for the project.
     */
    public static final String PROJECT_NAME_PARAMETER = "projectName";
    public static final int NB_DIGITS = 6;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit(); //To change body of generated methods, choose Tools | Templates.
    }

    @Get("json")
    public Map<String, Integer>  getProjectsNameAsJson() {
        return UniqueProjectName.getInstance().getProjects();                            
    }
    
    @Get("xml")
    public Map<String, Integer>  getProjectsNameAsXml() {
        return UniqueProjectName.getInstance().getProjects();                            
    }            
    
    @Post
    public Representation createProject(final Form mediaForm) {
        getLogger().entering(SuffixProjectsResource.class.getName(), "createProject", mediaForm);
        
        checkInputs(mediaForm);
        String projectName = mediaForm.getFirstValue(PROJECT_NAME_PARAMETER);        
        int digits = UniqueProjectName.getInstance().getShortName(projectName, NB_DIGITS);
        
        getLogger().exiting(SuffixProjectsResource.class.getName(), "createProject",digits);        
        return new StringRepresentation(String.valueOf(digits));
    }
    
    /**
     * Checks input parameters
     * @param mediaForm the parameters
     * @ResourceException if PROJECT_NAME_PARAMETER is not set
     */
    private void checkInputs(final Form mediaForm) {
        if (isValueNotExist(mediaForm, PROJECT_NAME_PARAMETER)) {
            getLogger().fine(PROJECT_NAME_PARAMETER+ " value is not set");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, PROJECT_NAME_PARAMETER+" parameter must be set");
        } 
        getLogger().fine("The form is valid");        
    }
}
