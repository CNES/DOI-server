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
package fr.cnes.doi.resource.citation;

import fr.cnes.doi.application.DoiCrossCiteApplication;
import fr.cnes.doi.resource.AbstractResource;
import org.apache.logging.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.resource.ResourceException;

/**
 * Base resource for CrossCite application.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class BaseCitationResource extends AbstractResource {

    /**
     * Parameter providing the digital object identifier {@value #DOI_PARAMETER}.
     */
    public static final String DOI_PARAMETER = "doi";

    /**
     * Language parameter to format the citation {@value #LANG_PARAMETER}.
     */
    public static final String LANG_PARAMETER = "lang";

    /**
     * Style parameter to format the citation {@value #STYLE_PARAMETER}.
     */
    public static final String STYLE_PARAMETER = "style";

    /**
     * Logger.
     */
    protected volatile Logger LOG;

    /**
     * Cross cite application.
     */
    private volatile DoiCrossCiteApplication app;

    /**
     * Init.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.app = (DoiCrossCiteApplication) getApplication();
        this.LOG = this.app.getLog();
    }

    /**
     * List representation.
     *
     * @param title Title of the representation
     * @param media Media type of the response
     * @param content Explanation of the representation
     * @return the Wadl representation of this representation
     */
    protected RepresentationInfo listRepresentation(final String title,
            final MediaType media,
            final String content) {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(media);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle(title);
        docInfo.setTextContent(content);
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    /**
     * Returns CrossCiteApplication.
     *
     * @return the app
     */
    public DoiCrossCiteApplication getApp() {
        return app;
    }
}
