/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
