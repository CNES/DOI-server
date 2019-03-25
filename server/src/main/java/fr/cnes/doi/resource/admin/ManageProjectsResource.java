/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.resource.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.application.AdminApplication;
import static fr.cnes.doi.application.AdminApplication.SUFFIX_PROJECT_NAME_TEMPLATE;
import fr.cnes.doi.client.ClientSearchDataCite;
import fr.cnes.doi.db.AbstractProjectSuffixDBHelper;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.utils.spec.Requirement;
import org.restlet.data.Method;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;

/**
 * Provide a resource to delete and rename a project from database.
 */
public class ManageProjectsResource extends AbstractResource {

    /**
     * Parameter for the project name {@value #PROJECT_NAME_PARAMETER}. This parameter is send to
     * create a new identifier for the project.
     */
    public static final String PROJECT_NAME_PARAMETER = "newProjectName";
    
    

    /**
     * Logger.
     */
    private volatile Logger LOG;

    /**
     * Suffix of the project.
     */
    private volatile String suffixProject;

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
        setDescription("This resource handles deletion and renaming of a project");
        this.suffixProject = getAttribute(SUFFIX_PROJECT_NAME_TEMPLATE);
        LOG.debug(this.suffixProject);
        LOG.traceExit();
    }

    // TODO requirement
    /**
     * Rename the project from the project id sent in url.
     *
     * @param mediaForm form
     */
    @Requirement(reqId = Requirement.DOI_SRV_140, reqName = Requirement.DOI_SRV_140_NAME)
    @Post
    public void renameProject(final Form mediaForm) {
        LOG.traceEntry("Parameters\n\tmediaForm : {}", mediaForm);
        checkInputs(mediaForm);
        final String newProjectName = mediaForm.getFirstValue(PROJECT_NAME_PARAMETER);
        final AbstractProjectSuffixDBHelper manageProjects = PluginFactory.getProjectSuffix();
        final boolean isRenamed = manageProjects.renameProject(Integer.parseInt(suffixProject),
                newProjectName);
        if(isRenamed) {
            setStatus(Status.SUCCESS_NO_CONTENT);
        } else {
            throw LOG.throwing(new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, "Cannot rename project to "+newProjectName));
        }
        LOG.traceExit();
    }

    // TODO requirement
    /**
     * Delete the project from database.     
     */
    @Requirement(reqId = Requirement.DOI_SRV_140, reqName = Requirement.DOI_SRV_140_NAME)
    @Delete
    public void deleteProject() {
        LOG.traceEntry();

        final ClientSearchDataCite client;
        final List<String> response = new ArrayList<>();
        try {
            client = new ClientSearchDataCite();
            response.addAll(client.getDois(suffixProject));

        } catch (Exception ex) {
            LOG.error(ex + "\n"
                    + "Error in SuffixProjectsDoisResource while searching for dois in project "
                    + suffixProject);
        }
        
        final AbstractProjectSuffixDBHelper manageProjects = PluginFactory.getProjectSuffix();

        // No DOIs have to be attached to a project before deleting it
        if (!response.isEmpty()) {
            throw LOG.throwing(new ResourceException(
                    Status.CLIENT_ERROR_NOT_FOUND, suffixProject+" not found"));
        } else if(manageProjects.deleteProject(Integer.parseInt(suffixProject))) {
            setStatus(Status.SUCCESS_NO_CONTENT);
        } else {
            throw LOG.throwing(new ResourceException(
                    Status.SERVER_ERROR_INTERNAL, "Cannot delete the project "+suffixProject));
        }
        LOG.traceExit();
    }

    /**
     * Tests if the {@link #PROJECT_NAME_PARAMETER} is set.
     *
     * @param mediaForm the parameters
     * @throws ResourceException - if PROJECT_NAME_PARAMETER is not set
     */
    private void checkInputs(final Form mediaForm) throws ResourceException {
        LOG.traceEntry("Parameters\n\tmediaForm : {}", mediaForm);
        if (isValueNotExist(mediaForm, PROJECT_NAME_PARAMETER)) {
            throw LOG.throwing(Level.ERROR, new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    PROJECT_NAME_PARAMETER + " parameter must be set"));
        }
        LOG.debug("The form is valid");
        LOG.traceExit();
    }
    
    @Override
    protected void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("Rename the project");
        addRequestDocToMethod(info, createQueryParamDoc(
                SUFFIX_PROJECT_NAME_TEMPLATE, ParameterStyle.TEMPLATE,
                "projectID", true, "xs:integer")
        );        
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_NO_CONTENT, "Operation successful",
                stringRepresentation())
        );
        
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_BAD_REQUEST, "Cannot rename project",
                htmlRepresentation())
        );        
    }  
    
    @Override
    protected void describeDelete(final MethodInfo info) {
        info.setName(Method.DELETE);
        info.setDocumentation("Delete a project");
        addRequestDocToMethod(info, createQueryParamDoc(
                SUFFIX_PROJECT_NAME_TEMPLATE, ParameterStyle.TEMPLATE,
                "projectID", true, "xs:integer")
        );        
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_NO_CONTENT, "Operation successful",
                stringRepresentation())
        );
        
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_NOT_FOUND, "projectID not found",
                htmlRepresentation())
        );        
        
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_BAD_REQUEST, "Cannot delete the project",
                htmlRepresentation())
        );        
    }     

}
