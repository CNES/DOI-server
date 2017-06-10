/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging.api;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import org.restlet.engine.log.AccessLogFileHandler;

/**
 * FileHandler to log request and responses.
 * @author Jean-Christophe Malapert
 */
public class FileHandlerLogAPIServer extends AccessLogFileHandler { 
    
    /**
     *
     * @throws IOException
     * @throws SecurityException
     */
    public FileHandlerLogAPIServer() throws IOException, SecurityException {
    super();
    this.setLevel(Level.INFO);    
  }
    
    
}
