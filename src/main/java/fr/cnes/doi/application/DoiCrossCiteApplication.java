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
package fr.cnes.doi.application;

import org.restlet.Restlet;
import org.restlet.routing.Router;

import fr.cnes.doi.client.ClientCrossCiteCitation;
import fr.cnes.doi.resource.citation.FormatCitationResource;
import fr.cnes.doi.resource.citation.LanguageCitationResource;
import fr.cnes.doi.resource.citation.StyleCitationResource;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.utils.spec.Requirement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides an application to get citation from a registered DOI. Books and journal articles have
 * long benefited from an infrastructure that makes them easy to cite, a key element in the process
 * of research and academic discourse. In this mind, a data should cited in just the same way.
 * DataCite DOIs help further research and assures reliable, predictable, and unambiguous access to
 * research data in order to:
 * <ul>
 * <li>support proper attribution and credit</li>
 * <li>support collaboration and reuse of data</li>
 * <li>enable reproducibility of findings</li>
 * <li>foster faster and more efficient research progress, and</li>
 * <li>provide the means to share data with future researchers</li>
 * </ul>
 * DataCite also looks to community practices that provide data citation guidance. The Joint
 * Declaration of Data Citation Principles is a set of guiding principles for data within scholarly
 * literature, another dataset, or any other research object (Data Citation Synthesis Group 2014).
 * <p>
 * The FAIR Guiding Principles provide a guideline for the those that want to enhance reuse of their
 * data (Wilkinson 2016).
 * <p>
 * <b>Security</b><br>
 * --------------<br>
 * No authentication / no autorisation
 * <p>
 * <b>Routing</b><br>
 *  --------------<br>
 * <br>
 * root<br>
 *  |<br>
 *  |__ style<br>
 *  |__ language<br>
 *  |__ format<br>
 * 
 * @see <a href="http://citation.crosscite.org/">Making a citation</a>
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_SRV_100, reqName = Requirement.DOI_SRV_100_NAME)
@Requirement(reqId = Requirement.DOI_SRV_110, reqName = Requirement.DOI_SRV_110_NAME)
@Requirement(reqId = Requirement.DOI_SRV_120, reqName = Requirement.DOI_SRV_120_NAME)
@Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
public class DoiCrossCiteApplication extends AbstractApplication {

    /**
     * URI {@value #STYLES_URI} to get the styles, which are used to format the citation.
     */
    public static final String STYLES_URI = "/style";
    /**
     * URI {@value #LANGUAGE_URI} to get the languages, which are used to format the citation.
     */
    public static final String LANGUAGE_URI = "/language";
    /**
     * URI {@value #FORMAT_URI} to retrieves the citation.
     */
    public static final String FORMAT_URI = "/format";
    /**
     * Application name.
     */
    public static final String NAME = "Cross Cite Application";

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DoiCrossCiteApplication.class.getName());

    /**
     * Client to query CrossCite.
     */
    private final ClientCrossCiteCitation client;

    /**
     * Constructs the application.
     */
    public DoiCrossCiteApplication() {
        super();
        setName(NAME);
        final StringBuilder description = new StringBuilder();
        description.append("Books and journal articles have long benefited from "
                + "an infrastructure that makes them easy to cite, a key element"
                + " in the process of research and academic discourse. "
                + "We believe that you should cite data in just the same way "
                + "that you can cite other sources of information, "
                + "such as articles and books.");
        description.append("DataCite DOIs help further research and assures "
                + "reliable, predictable, and unambiguous access to research "
                + "data in order to:");
        description.append("<ul>");
        description.append("<li>support proper attribution and credit</li>");
        description.append("<li>support collaboration and reuse of data</li>");
        description.append("<li>enable reproducibility of findings</li>");
        description.append("<li>foster faster and more efficient research progress, and</li>");
        description.append("<li>provide the means to share data with future researchers</li>");
        description.append("</ul>");
        description.append("DataCite also looks to community practices that provide"
                + " data citation guidance. The Joint Declaration of Data Citation"
                + " Principles is a set of guiding principles for data within "
                + "scholarly literature, another dataset, or any other research "
                + "object (Data Citation Synthesis Group 2014). The FAIR Guiding "
                + "Principles provide a guideline for the those that want to "
                + "enhance reuse of their data (Wilkinson 2016).");
        setDescription(description.toString());
        final String contextMode = this.getConfig().getString(Consts.CONTEXT_MODE);
        this.client = new ClientCrossCiteCitation(
                ClientCrossCiteCitation.Context.valueOf(contextMode)
        );
    }

    /**
     * Creates router the DOICrossCiteApplication. This routes routes the following resources:
     * <ul>
     * <li>{@link DoiCrossCiteApplication#STYLES_URI} to access to the different styles for a
     * citation</li>
     * <li>{@link DoiCrossCiteApplication#LANGUAGE_URI} to access to the different languages for a
     * citation</li>
     * <li>{@link DoiCrossCiteApplication#FORMAT_URI} to access to the formatted citation</li>
     * </ul>
     *
     * @return router
     */
    @Override
    public Restlet createInboundRoot() {
        LOG.traceEntry();

        final Router router = new Router(getContext());
        router.attach(STYLES_URI, StyleCitationResource.class);
        router.attach(LANGUAGE_URI, LanguageCitationResource.class);
        router.attach(FORMAT_URI, FormatCitationResource.class);

        return LOG.traceExit(router);
    }

    /**
     * Returns the client to query cross cite.
     *
     * @return the client
     */
    public ClientCrossCiteCitation getClient() {
        return this.client;
    }

    /**
     * Returns the logger.
     *
     * @return the logger
     */
    @Override
    public Logger getLog() {
        return LOG;
    }

}
