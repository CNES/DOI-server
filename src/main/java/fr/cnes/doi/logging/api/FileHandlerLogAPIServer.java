/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging.api;

import fr.cnes.doi.utils.spec.CoverageAnnotation;
import fr.cnes.doi.utils.spec.Requirement;
import java.io.IOException;
import java.util.logging.Level;
import org.restlet.engine.log.AccessLogFileHandler;

/**
 * FileHandler to log request and responses.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_ARCHI_020,
        reqName = Requirement.DOI_ARCHI_020_NAME,
        coverage = CoverageAnnotation.PARTIAL,
        comment = "Log4J n'est pas utilisé"        
)
public final class FileHandlerLogAPIServer extends AccessLogFileHandler {

    /**
     * File handler for API.
     *
     * @throws IOException - Exception
     * @throws SecurityException - Exception
     */
    public FileHandlerLogAPIServer() throws IOException, SecurityException {
        super();
        this.setLevel(Level.INFO);
    }

    @Override
    public synchronized void setLevel(final Level level) {
        super.setLevel(level);
    }

}
