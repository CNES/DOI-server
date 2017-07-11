/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.exception;

import org.restlet.data.Status;

/**
 * Exception for Client Cross Cite.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class ClientCrossCiteException extends Exception {

    private static final long serialVersionUID = -246999030222838204L;
    private final String detailMessage;
    private final Status status;
    
    /**
     * Constructs a new exception with HTTP status as its detail message.
     * @param status HTTP status
     */
    public ClientCrossCiteException(final Status status) {
        super();
        this.detailMessage = computeDetailMessage(status);
        this.status = status;
    }
    
    /**
     * Constructs a new exception with the specified HTTP status and detail message.
     * @param status HTTP message
     * @param message message
     */
    public ClientCrossCiteException(final Status status, final String message) {
        super(message);
        this.detailMessage = computeDetailMessage(status);
        this.status = status;
    }    
    
    /**
     * Constructs a new exception with the specified detail cause.
     * @param status HTTP status
     * @param cause cause
     */
    public ClientCrossCiteException(final Status status,final Throwable cause) {
        super(cause);
        this.detailMessage = computeDetailMessage(status);
        this.status = status;
    }    
    
    /**
     * Constructs a new exception with the specified detail HTTP status, message
     * and cause.
     * @param status HTTP status
     * @param message message
     * @param cause cause
     */
    public ClientCrossCiteException(final Status status, final String message, final Throwable cause) {
        super(message, cause);
        this.detailMessage = computeDetailMessage(status);
        this.status = status;
    }
    
    /**
     * Returns the detail message according to the status.
     * @param status HTTP status
     * @return the detail message
     */
    private String computeDetailMessage(final Status status) {
        final String result;
        switch(status.getCode()) {
            case 200:
                result = "Operation successful";
                break;
            case 404:
                result = "DOI not found";
                break;
            case 400:
                result = "Wrong input parameters";
                break;
            default:
                result = "Internal error";
        }
        return result;
    } 
    
    public Status getStatus() {
        return this.status;
    }
    
    /**
     * Returns detail message;
     * @return the detail message
     */
    public String getDetailMessage() {
        return this.detailMessage;
    }
    
}
