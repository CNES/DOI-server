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

import fr.cnes.doi.application.AbstractApplication;
import fr.cnes.doi.exception.ClientCrossCiteException;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * The supported styles for citation.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class StyleCitationResource extends BaseCitationResource {

    /**
     * Init.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        LOG.traceEntry();
        final StringBuilder description = new StringBuilder();
        description.append("Selects a style");
        description.append("A \"style\" can be chosen from the list of style "
                + "names found in the CSL style repository.");
        description.append("Many styles are supported, including common styles "
                + "such as apa and harvard3.");
        setDescription(description.toString());
        LOG.traceExit();
    }

    /**
     * Returns the styles as JSON array for a citation.
     *
     * @return the possibles styles for a citation
     * @throws ResourceException - Will thrown an Exception when a problem happens during the
     * request to Cross Cite
     */
    @Requirement(reqId = Requirement.DOI_SRV_100, reqName = Requirement.DOI_SRV_100_NAME)
    @Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
    @Get("json|xml")
    public List<String> getStyles() throws ResourceException {
        LOG.traceEntry();
        final List<String> result;
        try {
            result = this.getApp().getClient().getStyles();
        } catch (ClientCrossCiteException ex) {
            ((AbstractApplication) getApplication()).sendAlertWhenDataCiteFailed(ex);
            throw LOG.throwing(Level.DEBUG, new ResourceException(ex.getStatus(), ex.
                    getDetailMessage(), ex));
        }
        return LOG.traceExit(result);
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
        info.setDocumentation("Retrieves the list of supported styles");
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, "Operation successful",
                listRepresentation("Style representation", MediaType.TEXT_XML,
                        "A List of String representing the possible styles"))
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, "Operation successful",
                listRepresentation("Style representation",
                        MediaType.APPLICATION_JSON,
                        "A JSON array representing the possible styles"))
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SERVER_ERROR_INTERNAL, "server internal error, try later "
                + "and if problem persists please contact us")
        );
    }
}
