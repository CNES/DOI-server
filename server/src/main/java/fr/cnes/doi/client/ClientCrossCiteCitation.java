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
package fr.cnes.doi.client;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.restlet.data.CharacterSet;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cnes.doi.exception.ClientCrossCiteException;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Client to query the citation service. This class queries
 * <a href="https://citation.crosscite.org/">CrossCite</a>.
 *
 * @author Jean-Christophe Malapert (Jean-Christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_INTER_020, reqName = Requirement.DOI_INTER_020_NAME)
public class ClientCrossCiteCitation extends BaseClient {

    /**
     * Service end point {@value #CROSS_CITE_URL}.
     */
    public static final String CROSS_CITE_URL = "http://citation.crosscite.org";

    /**
     * Service end point {@value #CROSS_CITE_MOCK_URL}.
     */
    public static final String CROSS_CITE_MOCK_URL = "http://localhost:" + DATACITE_MOCKSERVER_PORT;

    /**
     * URI to get styles {@value #STYLE_URI}.
     */
    public static final String STYLE_URI = "styles";

    /**
     * URI to get locales {@value #LOCALE_URI}.
     */
    public static final String LOCALE_URI = "locales";

    /**
     * URI to get format {@value #FORMAT_URI}.
     */
    public static final String FORMAT_URI = "format";

    /**
     * Context use.
     */
    private final Context contextUse;

    /**
     * Creates a client to {@value CROSS_CITE_URL} with a
     * {@link Context#PROD prod} context and without a proxy.
     */
    public ClientCrossCiteCitation() {
        super(CROSS_CITE_URL);
        this.contextUse = Context.PROD;
    }

    /**
     * Creates a client to an URI related to the context.
     *
     * @param context context
     */
    public ClientCrossCiteCitation(final Context context) {
        super(context.getCrossCiteUrl());
        this.contextUse = context;
    }

    /**
     * Init the endpoint.
     */
    protected void init() {
        this.getClient().setReference(new Reference(this.contextUse.getCrossCiteUrl()));
    }

    /**
     * Returns the response as a list of String of an URI.
     *
     * @param segment resource name
     * @return the response
     * @throws ClientCrossCiteException - if an error happens when requesting
     * CrossCite
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
     * Returns styles by calling {@value #STYLE_URI}.
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
     * Returns languages by calling {@value #LOCALE_URI}.
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
    public String getFormat(final String doiName,
            final String style,
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
            	rep.setCharacterSet(CharacterSet.UTF_8);
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

    /**
     * Options for each context.
     */
    public enum Context {

        /**
         * Development context. This context uses the
         * {@link #CROSS_CITE_MOCK_URL} end point with a log level sets to OFF.
         */
        DEV(CROSS_CITE_MOCK_URL, Level.OFF),
        /**
         * Post development context. This context uses the
         * {@link #CROSS_CITE_URL} end point with a log level sets to ALL.
         */
        POST_DEV(CROSS_CITE_URL, Level.ALL),
        /**
         * Pre production context. This context uses the {@link #CROSS_CITE_URL}
         * end point with a log level sets to FINE.
         */
        PRE_PROD(CROSS_CITE_URL, Level.FINE),
        /**
         * Production context. This context uses the {@link #CROSS_CITE_URL} end
         * point with a log level sets to INFO.
         */
        PROD(CROSS_CITE_URL, Level.INFO);

        /**
         * Level log.
         */
        private Level levelLog;

        /**
         * CrossCite URL.
         */
        private String crossCiteUrl;

        /**
         * Construct a development context
         *
         * @param dataciteUrl dataciteURL according to the context
         * @param levelLog level log
         */
        Context(final String dataciteUrl,
                final Level levelLog) {
            this.crossCiteUrl = dataciteUrl;
            this.levelLog = levelLog;
        }

        /**
         * Returns the log level.
         *
         * @return the log level
         */
        public Level getLevelLog() {
            return this.levelLog;
        }

        /**
         * Returns the service end point.
         *
         * @return the service end point
         */
        public String getCrossCiteUrl() {
            return this.crossCiteUrl;
        }

        /**
         * Sets the level log for the context
         *
         * @param levelLog level log
         */
        private void setLevelLog(final Level levelLog) {
            this.levelLog = levelLog;
        }

        /**
         * Sets the Cross Cite URL for the context.
         *
         * @param crossCiteUrl Cross Cite URL
         */
        private void setCrossCiteUrl(final String crossCiteUrl) {
            this.crossCiteUrl = crossCiteUrl;
        }

        /**
         * Sets the level log for a given context
         *
         * @param context the context
         * @param levelLog the level log
         */
        public static void setLevelLog(final Context context,
                final Level levelLog) {
            context.setLevelLog(levelLog);
        }

        /**
         * Sets the Cross Cite URL for a given context
         *
         * @param context the context
         * @param crossCiteUrl Cross Cite URL
         */
        public static void setCrossCiteUrl(final Context context,
                final String crossCiteUrl) {
            context.setCrossCiteUrl(crossCiteUrl);
        }

    }
}
