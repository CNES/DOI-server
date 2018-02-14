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
package fr.cnes.doi.exception;

/**
 * Runtime Exception for the project
 *
 * @author Claire
 *
 */
public class DoiRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1589749416315841115L;

    /**
     * Constructs a new runtime exception with null as its detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to DoiRuntimeException.initCause.
     */
    public DoiRuntimeException() {
        super();
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause. Note that the detail message associated with cause is not
     * automatically incorporated in this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by
     * the DoiRuntimeException.getMessage() method).
     * @param cause the cause (which is saved for later retrieval by the
     * DoiRuntimeException.getCause() method). (A null value is permitted, and
     * indicates that the cause is nonexistent or unknown.)
     */
    public DoiRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new runtime exception with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to DoiRuntimeException.initCause.
     *
     * @param message the detail message. The detail message is saved for later
     * retrieval by the DoiRuntimeException.getMessage() method
     */
    public DoiRuntimeException(final String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified cause and a detail
     * message of (cause==null ? null : cause.toString()) (which typically
     * contains the class and detail message of cause). This constructor is
     * useful for runtime exceptions that are little more than wrappers for
     * other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     * DoiRuntimeException.getCause() method). (A null value is permitted, and
     * indicates that the cause is nonexistent or unknown.)
     */
    public DoiRuntimeException(final Throwable cause) {
        super(cause);
    }

}
