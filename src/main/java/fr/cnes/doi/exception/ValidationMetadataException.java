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
package fr.cnes.doi.exception;

/**
 * Exception during the validation of provided metadata.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class ValidationMetadataException extends Exception {
    
    private static final long serialVersionUID = -6347065555603813330L;
    
    /**
     * Constructs a new ValidationMetadataException with null as its detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to ValidationMetadataException.initCause.
     */
    public ValidationMetadataException() {
        super();
    }
    
    /**
     * Constructs a ValidationMetadataException with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to DoiRuntimeException.initCause.
     *
     * @param message the detail message. The detail message is saved for later
     * retrieval by the ValidationMetadataException.getMessage() method
     */    
    public ValidationMetadataException(final String message) {
        super(message);
    }    
    
    /**
     * Constructs a ValidationMetadataException with the specified cause and a detail
     * message of (cause==null ? null : cause.toString()) (which typically
     * contains the class and detail message of cause). This constructor is
     * useful for ValidationMetadataException that are little more than wrappers for other
     * throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     * ValidationMetadataException.getCause() method). (A null value is permitted, and
     * indicates that the cause is nonexistent or unknown.)
     */    
    public ValidationMetadataException(final Throwable cause) {
        super(cause);
    }    
    
    /**
     * Constructs a ValidationMetadataException with the specified detail message and
     * cause. Note that the detail message associated with cause is not
     * automatically incorporated in this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by
     * the ValidationMetadataException.getMessage() method).
     * @param cause the cause (which is saved for later retrieval by the
     * ValidationMetadataException.getCause() method). (A null value is permitted, and
     * indicates that the cause is nonexistent or unknown.)
     */    
    public ValidationMetadataException(final String message, final Throwable cause) {
        super(message, cause);
    }    
    
}
