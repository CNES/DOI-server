/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging.business;

import java.io.IOException;
import java.util.logging.FileHandler;

/**
 * File Handler to log business information.
 * @author Jean-Christophe Malapert
 */
public class FileHandlerApplication extends FileHandler {
    
  public FileHandlerApplication() throws IOException, SecurityException {
    super();
  }    
    
}
