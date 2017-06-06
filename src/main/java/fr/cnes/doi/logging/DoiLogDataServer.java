/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging;

import java.util.logging.Logger;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.service.LogService;

/**
 * Creates a default logger called "fr.cnes.doi" when no logName is given.
 * @author Jean-Christophe Malapert
 */
public class DoiLogDataServer extends LogService {

    /**
     * Logger for DataServerService
     */
    private Logger logger = Engine.getLogger("fr.cnes.doi.api");  
    
    /**
     * Constructs a new logger.
     * @param logName logger name
     * @param isEnabled  true when logger is enabled otherwise false
     */
    public DoiLogDataServer(final String logName, final boolean isEnabled) {
        super(isEnabled);
        try {
            this.setLoggerName(logName);
            
            if ((logName != null) && !logName.equals("")) {
                logger = Engine.getLogger(logName);
            }
        } catch (SecurityException ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex);
        }
    }    
    
}
