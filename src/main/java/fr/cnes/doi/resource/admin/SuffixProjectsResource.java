/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.admin;

import fr.cnes.doi.resource.BaseResource;
import fr.cnes.doi.utils.UniqueProjectName;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.Map;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * Provides a unique identifier to the project. This identifier is used as part
 * of the DOI name.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class SuffixProjectsResource extends BaseResource {

    /**
     * Parameter for the project name. This parameter is send to create an
     * identifier for the project.
     */
    public static final String PROJECT_NAME_PARAMETER = "projectName";
    public static final int NB_DIGITS = 6;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setDescription("This resource handles the project suffix in the DOI name");
    }

    @Requirement(
            reqId = Requirement.DOI_SRV_140,
            reqName = Requirement.DOI_SRV_140_NAME
    )     
    @Get("json")
    public Map<String, Integer> getProjectsNameAsJson() {
        return UniqueProjectName.getInstance().getProjects();
    }

    @Requirement(
            reqId = Requirement.DOI_SRV_140,
            reqName = Requirement.DOI_SRV_140_NAME
    )     
    @Get("xml")
    public Map<String, Integer> getProjectsNameAsXml() {
        return UniqueProjectName.getInstance().getProjects();
    }

    @Requirement(
            reqId = Requirement.DOI_SRV_130,
            reqName = Requirement.DOI_SRV_130_NAME
    )     
    @Post
    public Representation createProject(final Form mediaForm) {
        getLogger().entering(SuffixProjectsResource.class.getName(), "createProject", mediaForm);

        checkInputs(mediaForm);
        String projectName = mediaForm.getFirstValue(PROJECT_NAME_PARAMETER);
        int digits = UniqueProjectName.getInstance().getShortName(projectName, NB_DIGITS);

        getLogger().exiting(SuffixProjectsResource.class.getName(), "createProject", digits);
        return new StringRepresentation(String.valueOf(digits));
    }

    /**
     * Checks input parameters
     *
     * @param mediaForm the parameters
     * @throws ResourceException - if PROJECT_NAME_PARAMETER is not set
     */
    private void checkInputs(final Form mediaForm) throws ResourceException {
        if (isValueNotExist(mediaForm, PROJECT_NAME_PARAMETER)) {
            getLogger().fine(PROJECT_NAME_PARAMETER + " value is not set");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, PROJECT_NAME_PARAMETER + " parameter must be set");
        }
        getLogger().fine("The form is valid");
    }

    /**
     * projects representation
     *
     * @return Wadl representation for projects
     */
    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )    
    private RepresentationInfo projectsRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.APPLICATION_XML);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Projects Representation");
        docInfo.setTextContent("The representation contains informations about all projects.");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    @Override
    protected void describeGet(MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get information about the created projects");
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", projectsRepresentation()));
    }

    @Requirement(
            reqId = Requirement.DOI_DOC_010,
            reqName = Requirement.DOI_DOC_010_NAME
    )      
    @Override
    protected void describePost(MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("Creates a project suffix");
        addRequestDocToMethod(info, createQueryParamDoc(SuffixProjectsResource.PROJECT_NAME_PARAMETER, ParameterStyle.MATRIX, "project name", true, "xs:string"));
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", stringRepresentation()));
    }

}
