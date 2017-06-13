/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import fr.cnes.doi.application.DoiCrossCiteApplication;
import fr.cnes.doi.resource.BaseResource;
import org.restlet.data.MediaType;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.resource.ResourceException;

/**
 * Base resource for CrossCite application.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class BaseCitationResource extends BaseResource {
    
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

    /**
     * List representation.
     * @param title Title of the representation
     * @param content Explanation of the representation
     * @return the Wadl representation of this representation
     */
    protected RepresentationInfo listRepresentation(final String title, final String content) {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);        
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle(title);
        docInfo.setTextContent(content);
        repInfo.setDocumentation(docInfo);        
        return repInfo;        
    }
}
