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

import org.restlet.data.Status;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class TokenSecurityException extends Exception {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 7133144912794016901L;

    /**
     * HTTP status.
     */
    private final Status status;

    /**
     * Constructs a new exception with HTTP status as its detail message.
     *
     * @param status HTTP status
     */
    public TokenSecurityException(final Status status) {
        super();
        this.status = status;
    }

    /**
     * Constructs a new exception with the specified HTTP status and detail message.
     *
     * @param status HTTP message
     * @param message message
     */
    public TokenSecurityException(final Status status,
            final String message) {
        super(message);
        this.status = status;
    }

    /**
     * Constructs a new exception with the specified detail cause.
     *
     * @param status HTTP status
     * @param cause cause
     */
    public TokenSecurityException(final Status status,
            final Throwable cause) {
        super(cause);
        this.status = status;
    }

    /**
     * Constructs a new exception with the specified detail HTTP status, message and cause.
     *
     * @param status HTTP status
     * @param message message
     * @param cause cause
     */
    public TokenSecurityException(final Status status,
            final String message,
            final Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public Status getStatus() {
        return this.status;
    }

}
