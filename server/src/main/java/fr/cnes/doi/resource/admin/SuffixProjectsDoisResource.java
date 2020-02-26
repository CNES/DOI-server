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

import org.apache.logging.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Provides a unique identifier to the project. This identifier is used as part
 * of the DOI name.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class SuffixProjectsDoisResource extends AbstractResource {

    /**
     * Logger.
     */
    private volatile Logger LOG;

    /**
     * Suffix of the project.
     */
    private volatile String suffixProject;

    /**
     * Set-up method that can be overridden in order to initialize the state of
     * the resource.
     *
     * Init by getting the DOI name in the template URL.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        final AdminApplication app = (AdminApplication) getApplication();
        LOG = app.getLog();
        LOG.traceEntry();
        setDescription("This resource handles the project suffix in the DOI name");
        this.suffixProject = getResourcePath().replace(
                AdminApplication.ADMIN_URI + AdminApplication.SUFFIX_PROJECT_URI + "/", "");
        final int startIndex = this.suffixProject.indexOf(AdminApplication.DOIS_URI);
        this.suffixProject = startIndex == -1 ? null : this.suffixProject.substring(0, startIndex);
        LOG.debug(this.suffixProject);

        LOG.traceExit();
    }

    //TODO requirement
    /**
     * Returns the list of dois from the project suffix sent in url.
     *
     * @return the list of dois
     */
    @Requirement(reqId = Requirement.DOI_SRV_140, reqName = Requirement.DOI_SRV_140_NAME)
    @Get
    public List<String> getProjectsNameAsJson() {
        LOG.traceEntry();
//        checkInput(this.suffixProject);

        List<String> response = new ArrayList<>();

        try {
            response = (((AdminApplication)getApplication()).getDois(suffixProject));
        } catch (Exception ex) {
            LOG.error("Error in SuffixProjectsDoisResource while searching for dois in project {}",
                    this.suffixProject, ex);
        }
        return LOG.traceExit(response);
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

    //TODO describres get method
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

}
