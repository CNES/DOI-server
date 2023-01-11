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
package fr.cnes.doi.resource.citation;

import fr.cnes.doi.application.AbstractApplication;
import fr.cnes.doi.exception.ClientCrossCiteException;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.Arrays;
import org.apache.logging.log4j.Level;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Formats a citation. CrossRef, DataCite and mEDRA support formatted citations
 * via the text/bibliography content type. These are the output of the Citation
 * Style Language processor, citeproc-js. The content type can take two
 * additional parameters to customise its response format.
 * <p>
 * "\"style\" can be chosen from the list of style names found in the CSL style
 * repository. Many styles are supported, including common styles such as apa
 * and harvard3
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class FormatCitationResource extends BaseCitationResource {

    /**
     * The digital object identifier to format.
     */
    private volatile String doiName;

    /**
     * The style of formatting.
     */
    private volatile String style;

    /**
     * The language to format.
     */
    private volatile String language;

    /**
     * Init by getting doi, lang and style values.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        LOG.traceEntry();
        this.doiName = getQueryValue(DOI_PARAMETER);
        this.language = getQueryValue(LANG_PARAMETER);
        this.style = getQueryValue(STYLE_PARAMETER);

        LOG.debug("DOI Parameter : " + this.doiName);
        LOG.debug("LANGUAGE Parameter : " + this.language);
        LOG.debug("STYLE Parameter : " + this.language);

        final StringBuilder description = new StringBuilder();
        description.append("CrossRef, DataCite and mEDRA support formatted "
                + "citations via the text/bibliography content type. These are "
                + "the output of the Citation Style Language processor, "
                + "citeproc-js. The content type can take two additional "
                + "parameters to customise its response format.");
        description.append("\"style\" can be chosen from the list of style names"
                + " found in the CSL style repository. Many styles are supported,"
                + " including common styles such as apa and harvard3");
        setDescription(description.toString());
        LOG.traceExit();
    }

	/**
	 * Returns the formatted citation.
	 *
	 * @return the formatted citation
	 * @throws ResourceException
	 *             - if a problem happens when requesting Cross Cite or if
	 *             {@link #DOI_PARAMETER} and {@link #LANG_PARAMETER} and
	 *             {@link #STYLE_PARAMETER} are not set
	 */
	@Requirement(reqId = Requirement.DOI_SRV_120, reqName = Requirement.DOI_SRV_120_NAME)
	@Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
	@Get
	public String getFormat() throws ResourceException {
		LOG.traceEntry();
		try {
			checkInputs();
			return LOG.traceExit(this.getApp().getClient()
					.getFormat(this.doiName, this.style, this.language));
		} catch (ClientCrossCiteException ex) {
			((AbstractApplication) getApplication())
					.sendAlertWhenDataCiteFailed(ex);
			throw LOG.throwing(Level.ERROR, new ResourceException(
					ex.getStatus(), ex.getDetailMessage(), ex));
		}
	}

    /**
     * Checks input parameters
     *
     * @throws ResourceException - if {@link #DOI_PARAMETER} and
     * {@link #LANG_PARAMETER} and {@link #STYLE_PARAMETER} are not set
     */
    private void checkInputs() throws ResourceException {
        LOG.traceEntry();
        final StringBuilder errorMsg = new StringBuilder();
        if (this.doiName == null || this.doiName.isEmpty()) {
            errorMsg.append(DOI_PARAMETER).append(" value is not set.");
        }
        if (this.language == null || this.language.isEmpty()) {
            errorMsg.append(LANG_PARAMETER).append(" value is not set.");
        }
        if (this.style == null || this.style.isEmpty()) {
            errorMsg.append(STYLE_PARAMETER).append(" value is not set.");
        }
        if (errorMsg.length() == 0) {
            LOG.debug("The parameters are valid");
        } else {
            throw LOG.throwing(Level.ERROR, new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, errorMsg.toString()));
        }
        LOG.traceExit();
    }

    /**
     * Describes the Get Method.
     *
     * @param info Wadl description
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Select Formatting Style");
        addRequestDocToMethod(info, Arrays.asList(
                createQueryParamDoc(DOI_PARAMETER, ParameterStyle.QUERY,
                        "doi to format for the citation", true, "xs:string"),
                createQueryParamDoc(LANG_PARAMETER, ParameterStyle.QUERY,
                        "language for the citation formating", true, "xs:string"),
                createQueryParamDoc(STYLE_PARAMETER, ParameterStyle.QUERY,
                        "style fot the citation formating", true, "xs:string")
        ));
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK,
                "Operation successful",
                listRepresentation("Format representation", MediaType.TEXT_PLAIN,
                        "The formatted citation"))
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_NOT_FOUND,
                "DOI not found",
                listRepresentation("Error representation",
                        MediaType.TEXT_HTML, "Error"))
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "Wrong input parameters",
                listRepresentation("Error representation",
                        MediaType.TEXT_HTML, "Error"))
        );
    }

}
