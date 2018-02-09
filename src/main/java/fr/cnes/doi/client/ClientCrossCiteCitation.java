/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cnes.doi.exception.ClientCrossCiteException;
import fr.cnes.doi.utils.spec.Requirement;

import java.io.IOException;
import java.util.List;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

/**
 * Client to query the citation service.
 *
 * @author Jean-Christophe Malapert (Jean-Christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_INTER_020,
        reqName = Requirement.DOI_INTER_020_NAME
)
public class ClientCrossCiteCitation extends BaseClient {

    /**
     * Service end point.
     */
    public static final String CROSS_CITE_URL = "http://citation.crosscite.org";

    /**
     * Resource to get styles.
     */
    public static final String STYLE_URI = "/styles";

    /**
     * Resource to get locales.
     */
    public static final String LOCALE_URI = "/locales";

    /**
     * Resource to get format.
     */
    public static final String FORMAT_URI = "/format";

    /**
     * Empty constructor.
     */
    public ClientCrossCiteCitation() {
        super(CROSS_CITE_URL);
    }

    /**
     * Init the endpoint.
     */
    protected void init() {
        this.getClient().setReference(new Reference(CROSS_CITE_URL));
    }

    /**
     * Returns the response as a list of String of an URI.
     *
     * @param segment resource name
     * @return the response
     * @throws ClientCrossCiteException - if an error happens when requesting CrossCite
     */
    private List<String> getList(final String segment) throws ClientCrossCiteException {
        try {
            final Reference ref = getClient().addSegment(segment);
            this.getClient().setReference(ref);
            final Representation rep = this.getClient().get();
            final Status status = this.getClient().getStatus();
            if (status.isSuccess()) {
                final ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(rep.getStream(), List.class);
            } else {                
                throw new ClientCrossCiteException(status, status.getDescription());
            }
        } catch (IOException | ResourceException ex) {
            throw new ClientCrossCiteException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(), ex);
        } finally {
            this.getClient().release();
        }
    }

    /**
     * Returns styles
     *
     * @return list of possible styles
     * @throws fr.cnes.doi.exception.ClientCrossCiteException Will thrown an
     * Exception when a problem happens during the request to Cross Cite
     */
    public List<String> getStyles() throws ClientCrossCiteException {
        init();
        return getList(STYLE_URI);
    }

    /**
     * Returns languages
     *
     * @return List of possible languages
     * @throws fr.cnes.doi.exception.ClientCrossCiteException Will thrown an
     * Exception when a problem happens during the request to Cross Cite
     */
    public List<String> getLanguages() throws ClientCrossCiteException {
        init();
        return getList(LOCALE_URI);
    }

    /**
     * Returns the citation of a DOI based on the selected style and language.
     *
     * @param doiName DOI name
     * @param style Selected style to format the citation
     * @param language Selected language to format the citation
     * @return The formatted citation
     * @throws fr.cnes.doi.exception.ClientCrossCiteException Will thrown an
     * Exception when a problem happens during the request to Cross Cite
     */
    public String getFormat(final String doiName, final String style, 
            final String language) throws ClientCrossCiteException {
        init();
        final String result;
        try {
            Reference ref = this.getClient().addSegment(FORMAT_URI);
            ref = ref.addQueryParameter("doi", doiName);
            ref = ref.addQueryParameter("style", style);
            ref = ref.addQueryParameter("lang", language);
            this.getClient().setReference(ref);
            final Representation rep = this.getClient().get();
            final Status status = this.getClient().getStatus();
            if (status.isSuccess()) {
                result = rep.getText();
            } else {
                throw new ClientCrossCiteException(status, status.getDescription());
            }
            return result;
        } catch (IOException ex) {
            throw new ClientCrossCiteException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(), ex);
        } catch (ResourceException ex) {
            throw new ClientCrossCiteException(ex.getStatus(), ex.getMessage(), ex);
        } finally {
            this.getClient().release();
        }
    }
}
