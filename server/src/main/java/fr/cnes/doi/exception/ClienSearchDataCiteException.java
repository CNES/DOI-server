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
package fr.cnes.doi.exception;

import fr.cnes.doi.client.ClientMDS.DATACITE_API_RESPONSE;
import java.io.IOException;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

/**
 * Exception for Client Cross Cite.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class ClienSearchDataCiteException extends Exception {

    /**
     * Operation successful. 201
     */
    public static final int SUCCESS_CREATED = 201;

    /**
     * Operation successful. 200
     */
    public static final int SUCCESS_OK = 200;

    /**
     * no DOIs founds. 204
     */
    public static final int SUCCESS_NO_CONTENT = 204;

    /**
     * invalid XML, wrong prefix or request body must be exactly two lines: "DOI and URL; wrong
     * domain, wrong prefix". 400
     */
    public static final int CLIENT_BAD_REQUEST = 400;

    /**
     * no login. 1001
     */
    public static final int CONNECTOR_ERROR_COMMUNICATION = 1001;

    /**
     * DOI does not exist in our database. 404
     */
    public static final int CLIENT_ERROR_NOT_FOUND = 404;


    /**
     * SeralVersionUID
     */
    private static final long serialVersionUID = -5061913391706889102L;

    /**
     * Detail message.
     */
    private final String detailMessage;

    /**
     * HTTP status.
     */
    private final Status status;

    /**
     * Constructs a new exception with HTTP status as its detail message.
     *
     * @param status HTTP status
     */
    public ClienSearchDataCiteException(final Status status) {
        super();
        this.detailMessage = computeDetailMessage(status);
        this.status = computeStatus(status);
    }

    /**
     * Constructs a new exception with the specified HTTP status and detail message.
     *
     * @param status HTTP message
     * @param message message
     */
    public ClienSearchDataCiteException(final Status status,
            final String message) {
        super(message);
        this.detailMessage = computeDetailMessage(status);
        this.status = computeStatus(status);
    }

    /**
     * Constructs a new exception with the specified detail cause.
     *
     * @param status HTTP status
     * @param cause cause
     */
    public ClienSearchDataCiteException(final Status status,
            final Throwable cause) {
        super(cause);
        this.detailMessage = computeDetailMessage(status);
        this.status = computeStatus(status);
    }

    /**
     * Constructs a new exception with the specified detail HTTP status, message and cause.
     *
     * @param status HTTP status
     * @param message message
     * @param cause cause
     */
    public ClienSearchDataCiteException(final Status status,
            final String message,
            final Throwable cause) {
        super(message, cause);
        this.detailMessage = computeDetailMessage(status);
        this.status = computeStatus(status);
    }

    /**
     * Constructs a new exception with the specified detail HTTP status, response and cause.
     *
     * @param status HTTP status
     * @param message message
     * @param responseEntity Representation of the response
     * @param cause cause
     */
    public ClienSearchDataCiteException(final Status status,
            final String message,
            final Representation responseEntity,
            final Throwable cause) {
        super(message, cause);
        this.status = computeStatus(status);
        String txt;
        try {
            txt = responseEntity.getText();
        } catch (IOException ex) {
            txt = computeDetailMessage(this.status);
        }
        this.detailMessage = txt;
    }

    /**
     * Computes status
     *
     * @param status status
     * @return status
     */
    private Status computeStatus(final Status status) {
        return status;
    }

    /**
     * Returns the detail message according to the status.
     *
     * @param status HTTP status
     * @return the detail message
     */
    private String computeDetailMessage(final Status status) {
        return DATACITE_API_RESPONSE.getMessageFromStatus(status);
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Returns detail message;
     *
     * @return the detail message
     */
    public String getDetailMessage() {
        return this.detailMessage;
    }
}
