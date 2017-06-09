/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.application.DoiCrossCiteApplication;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.resource.ResourceException;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class BaseCitationResource extends WadlServerResource {
    
    protected DoiCrossCiteApplication app;
    
    /**
     *
     * @throws ResourceException
     */
    @Override
    protected void doInit() throws ResourceException {
        this.app = (DoiCrossCiteApplication)getApplication();        
    }    
}
