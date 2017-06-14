/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import fr.cnes.doi.utils.Utils;
import java.util.logging.Level;
import org.restlet.data.Status;
import org.restlet.engine.Engine;

/**
 * Exception for Client
 * @author Jean-Christophe Malapert (Jean-Christophe.malapert@cnes.fr)
 */
public class ClientException extends Exception {

    private static final long serialVersionUID = 8075806467746913021L;

    /**
     * Response status.
     */
    protected final transient Status status;

    /**
     * Constructs a new exception with the specified cause and a detail message 
     * of (cause==null ? null : cause.toString()) (which typically contains the
     * class and detail message of cause)
     * @param cause the cause
     */
    public ClientException(final Throwable cause) {
        super(cause);
        this.status = Status.SERVER_ERROR_INTERNAL;
        Engine.getLogger(Utils.APP_LOGGER_NAME).log(Level.SEVERE, null, cause);
    }

    /**
     * Constructs a new exception with the status and the specified cause and a 
     * detail message of (cause==null ? null : cause.toString()) (which 
     * typically contains the class and detail message of cause)
     * @param status the status
     * @param cause the cause
     */    
    public ClientException(final Status status, final Throwable cause) {
        super(cause);
        this.status = status;
        Engine.getLogger(Utils.APP_LOGGER_NAME).log(Level.SEVERE, null, cause);        
    }

    /**
     * Constructs a new exception with the specified detail message and a status.
     * @param status status
     * @param description the detail description
     */
    public ClientException(final Status status, final String description) {
        super(description);
        this.status = status;
        Engine.getLogger(Utils.APP_LOGGER_NAME).log(Level.SEVERE, description);
    }

    /**
     * Constructs a new exception with the specified detail message, a status and cause.
     * @param status status
     * @param description a detailed message
     * @param cause the cause
     */
    public ClientException(final Status status, final String description, final Throwable cause) {
        super(description, cause);
        this.status = status;
        Engine.getLogger(Utils.APP_LOGGER_NAME).log(Level.SEVERE, description);
    }

    /**
     * Returns a status.
     * @return status
     */
    public Status getStatus() {
        return this.status;
    }

}
