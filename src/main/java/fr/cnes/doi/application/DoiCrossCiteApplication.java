/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import java.util.logging.Logger;

import org.restlet.Restlet;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.WadlCnesRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Router;

import fr.cnes.doi.client.ClientCrossCiteCitation;
import fr.cnes.doi.resource.citation.FormatCitationResource;
import fr.cnes.doi.resource.citation.LanguageCitationResource;
import fr.cnes.doi.resource.citation.StyleCitationResource;
import fr.cnes.doi.utils.Requirement;
import fr.cnes.doi.utils.Utils;

/**
 * Provides an application to get citation from a registered DOI. Books and
 * journal articles have long benefited from an infrastructure that makes them
 * easy to cite, a key element in the process of research and academic
 * discourse. In this mind, a data should cited in just the same way. DataCite
 * DOIs help further research and assures reliable, predictable, and unambiguous
 * access to research data in order to:
 * <ul>
 * <li>support proper attribution and credit</li>
 * <li>support collaboration and reuse of data</li>
 * <li>enable reproducibility of findings</li>
 * <li>foster faster and more efficient research progress, and</li>
 * <li>provide the means to share data with future researchers</li>
 * </ul>
 * DataCite also looks to community practices that provide data citation
 * guidance. The Joint Declaration of Data Citation Principles is a set of
 * guiding principles for data within scholarly literature, another dataset, or
 * any other research object (Data Citation Synthesis Group 2014).
 * <p>
 * The FAIR Guiding Principles provide a guideline for the those that want to
 * enhance reuse of their data (Wilkinson 2016).
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 * @see "http://citation.crosscite.org/"
 */
@Requirement(
        reqId = "DOI_SRV_100",
        reqName = "Listing des styles"
)
@Requirement(
        reqId = "DOI_SRV_110",
        reqName = "Listing des langues"
)
@Requirement(
        reqId = "DOI_SRV_120",
        reqName = "Formatage d'une citation"
)
public class DoiCrossCiteApplication extends BaseApplication {

    /**
     * URI to get the styles, which are used to format the citation.
     */
    public static final String STYLES_URI = "/style";
    /**
     * URI to get the languages, which are used to format the citation.
     */
    public static final String LANGUAGE_URI = "/language";
    /**
     * Retrieves the citation.
     */
    public static final String FORMAT_URI = "/format";

    /**
     * Application logger.
     */
    private static final Logger LOGGER = Utils.getAppLogger();
    
    public static final String NAME = "Cross Cite Application";    

    /**
     * Client to query CrossCite.
     */
    private final ClientCrossCiteCitation client = new ClientCrossCiteCitation();

    /**
     * Constructs the application by setting the proxy authentication to the      {@link fr.cnes.doi.client.ClientCrossCiteCitation
	 * ClientCrossCiteCitation} proxy when the configuration is set.
     */
    @Requirement(
            reqId = "DOI_DOC_010",
            reqName = "Documentation des interfaces"
    )      
    public DoiCrossCiteApplication() {
        super();
        LOGGER.entering(DoiCrossCiteApplication.class.getName(), "Constructor");

        setAuthor("Jean-Christophe Malapert");
        StringBuilder description = new StringBuilder();
        description.append("Books and journal articles have long benefited from "
                + "an infrastructure that makes them easy to cite, a key element"
                + " in the process of research and academic discourse. "
                + "We believe that you should cite data in just the same way "
                + "that you can cite other sources of information, " + "such as articles and books.");
        description.append("DataCite DOIs help further research and assures "
                + "reliable, predictable, and unambiguous access to research " + "data in order to:");
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
        setName(NAME);
        setOwner("Centre National d'Etudes Spatiales (CNES)");

        LOGGER.exiting(DoiCrossCiteApplication.class.getName(), "Constructor");
    }

    /**
     * Assigns routes.
     *
     * @return router
     */
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach(STYLES_URI, StyleCitationResource.class);
        router.attach(LANGUAGE_URI, LanguageCitationResource.class);
        router.attach(FORMAT_URI, FormatCitationResource.class);
        return router;
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
     * Creates HTML representation of the WADL.
     *
     * @param applicationInfo Application description
     * @return the HTML representation of the WADL
     */
    @Requirement(
            reqId = "DOI_DOC_010",
            reqName = "Documentation des interfaces"
    )
    @Override
    protected Representation createHtmlRepresentation(ApplicationInfo applicationInfo) {
        WadlCnesRepresentation wadl = new WadlCnesRepresentation(applicationInfo);
        return wadl.getHtmlRepresentation();
    }

}
