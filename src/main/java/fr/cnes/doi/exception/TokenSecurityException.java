/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.exception;

import fr.cnes.doi.utils.Utils;
import java.util.logging.Level;
import org.restlet.data.Status;
import org.restlet.engine.Engine;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class TokenSecurityException extends Exception {

    private static final long serialVersionUID = 7133144912794016901L;
    
    private final Status status;
    
    /**
     * Constructs a new exception with HTTP status as its detail message.
     * @param status HTTP status
     */
    public TokenSecurityException(final Status status) {
        super();
        this.status = status;
        Engine.getLogger(Utils.APP_LOGGER_NAME).log(Level.SEVERE, status.getDescription());
    }
    
    /**
     * Constructs a new exception with the specified HTTP status and detail message.
     * @param status HTTP message
     * @param message message
     */
    public TokenSecurityException(final Status status, final String message) {
        super(message);
        this.status = status;
        Engine.getLogger(Utils.APP_LOGGER_NAME).log(Level.SEVERE, this.getMessage());
    }    
    
    /**
     * Constructs a new exception with the specified detail cause.
     * @param status HTTP status
     * @param cause cause
     */
    public TokenSecurityException(final Status status,final Throwable cause) {
        super(cause);
        this.status = status;
        Engine.getLogger(Utils.APP_LOGGER_NAME).log(Level.SEVERE, cause.getMessage());
    }    
    
    /**
     * Constructs a new exception with the specified detail HTTP status, message
     * and cause.
     * @param status HTTP status
     * @param message message
     * @param cause cause
     */
    public TokenSecurityException(final Status status, final String message, final Throwable cause) {
        super(message, cause);
        this.status = status;
        Engine.getLogger(Utils.APP_LOGGER_NAME).log(Level.SEVERE, this.getMessage(), this.getCause());        
    }
    
    public Status getStatus() {
        return this.status;
    }            
    
}
