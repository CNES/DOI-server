/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.datacite;

import java.util.logging.Level;
import org.restlet.data.Status;
import org.restlet.engine.Engine;

/**
 *
 * @author malapert
 */
public class ClientDataCiteException extends Exception {

    private Status status;

    public ClientDataCiteException(Throwable cause) {
        super(cause);
        this.status = Status.SERVER_ERROR_INTERNAL;
        Engine.getLogger(ClientDataCite.class.getName()).log(Level.SEVERE, null, cause);
    }

    public ClientDataCiteException(Status status, Throwable cause) {
        super(cause);
        this.status = status;
        Engine.getLogger(ClientDataCite.class.getName()).log(Level.SEVERE, null, cause);        
    }

    public ClientDataCiteException(Status status, String description) {
        super(description);
        this.status = status;
        Engine.getLogger(ClientDataCite.class.getName()).log(Level.SEVERE, description);
    }

    public ClientDataCiteException(Status status, String description, Throwable cause) {
        super(description, cause);
        this.status = status;
        Engine.getLogger(ClientDataCite.class.getName()).log(Level.SEVERE, description);
    }

    public Status getStatus() {
        return this.status;
    }

}
