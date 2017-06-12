/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import fr.cnes.doi.application.DoiCrossCiteApplication;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.resource.ResourceException;

/**
 * Base resource for CrossCite application.
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class BaseCitationResource extends WadlServerResource {
    
    /**
     * Cross cite application.
     */
    protected DoiCrossCiteApplication app;
    
    /**
     * Init.
     * @throws ResourceException
     */
    @Override
    protected void doInit() throws ResourceException {
        this.app = (DoiCrossCiteApplication)getApplication();        
    }    
}
