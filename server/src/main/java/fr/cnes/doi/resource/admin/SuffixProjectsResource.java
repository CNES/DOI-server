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

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.db.model.DOIProject;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.utils.UniqueProjectName;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Provides a unique identifier to the project. This identifier is used as part
 * of the DOI name.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class SuffixProjectsResource extends AbstractResource {

    /**
     * Parameter for the project name {@value #PROJECT_NAME_PARAMETER}. This
     * parameter is send to create an identifier for the project.
     */
    public static final String PROJECT_NAME_PARAMETER = "projectName";
    /**
     * Number of digits ({@value #NB_DIGITS}) in which the suffix project is
     * encoded.
     */
    public static final int NB_DIGITS = 6;

    /**
     * Query parameter for user {@value #USER_PARAMETER}.
     */
    public static final String USER_PARAMETER = "user";

    /**
     * Logger.
     */
    private volatile Logger LOG;

    /**
     * The user name
     */
    private volatile String userName;

    /**
     * Set-up method that can be overridden in order to initialize the state of
     * the resource.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        final AdminApplication app = (AdminApplication) getApplication();
        LOG = app.getLog();
        LOG.traceEntry();

        this.userName = getQueryValue(USER_PARAMETER);
        LOG.debug("USER Parameter : " + this.userName);

        setDescription("This resource handles the project suffix in the DOI name");
        LOG.traceExit();
    }

    /**
     * Returns the list of projects as Json or xml format.
     *
     * @return the list of projects as Json or xml format
     */
    @Requirement(reqId = Requirement.DOI_SRV_140, reqName = Requirement.DOI_SRV_140_NAME)
    @Get("json")
    public String getProjectsNameAsJson() {
        LOG.traceEntry();
        try {
            final List<DOIProject> projects;
            if (this.userName == null || this.userName.isEmpty()) {
                projects = UniqueProjectName.getInstance().getProjects();
            } else {
                projects = UniqueProjectName.getInstance().getProjectsFromUser(this.userName);
            }

            ObjectMapper mapper = new ObjectMapper();

            return mapper.writeValueAsString(projects);

        } catch (DOIDbException ex) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    this.userName + " already exists");
        } catch (JsonProcessingException e) {
        	throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
    }

    /**
     * Creates a suffix projet based on the project name. The project name is
     * passed as parameter( {@link #PROJECT_NAME_PARAMETER} ) in the mediaForm.
     *
     * When a project suffix is created, a role with the same name is also
     * automatically created.
     *
     * @param mediaForm submitted form
     * @return A text representation of the encoded project name
     */
    @Requirement(reqId = Requirement.DOI_SRV_130, reqName = Requirement.DOI_SRV_130_NAME)
    @Post
    public Representation createProject(final Form mediaForm) {
        LOG.traceEntry("Parameters\n\tmediaForm : {}", mediaForm);
        checkInputs(mediaForm);
        final String projectName = mediaForm.getFirstValue(PROJECT_NAME_PARAMETER);
        try {
            final int digits = UniqueProjectName.getInstance().getShortName(projectName, NB_DIGITS);
            return LOG.traceExit(new StringRepresentation(String.valueOf(digits)));
        } catch (DOIDbException ex) {
            throw LOG.throwing(new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, projectName + " already exists !"));
        }
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

    /**
     * projects representation
     *
     * @return Wadl representation for projects
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    private RepresentationInfo projectsRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.APPLICATION_XML);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Projects Representation");
        docInfo.setTextContent("The representation contains informations about all projects.");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    /**
     * Describes a GET method.
     *
     * @param info method information
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    @Override
    protected void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get information about the created projects");
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK,
                "Operation successful",
                projectsRepresentation()
        ));
    }

    /**
     * Describes a POST method.
     *
     * @param info method information
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    @Override
    protected void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("Creates a project suffix");
        addRequestDocToMethod(info, createQueryParamDoc(
                SuffixProjectsResource.PROJECT_NAME_PARAMETER,
                ParameterStyle.MATRIX, "project name", true, "xs:string"));
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, "Operation successful", stringRepresentation()));
    }

}
