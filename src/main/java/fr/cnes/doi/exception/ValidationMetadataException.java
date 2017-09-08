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
    
    public ValidationMetadataException() {
        super();
    }
    
    public ValidationMetadataException(String message) {
        super(message);
    }    
    
    public ValidationMetadataException(Throwable cause) {
        super(cause);
    }    
    
    public ValidationMetadataException(String message, Throwable cause) {
        super(message, cause);
    }    
    
}
