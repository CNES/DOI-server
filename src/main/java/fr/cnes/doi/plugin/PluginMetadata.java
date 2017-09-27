/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.plugin;

import fr.cnes.doi.utils.spec.Requirement;

/**
 * Provides metadata about the plugin.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_ARCHI_030,
        reqName = Requirement.DOI_ARCHI_030_NAME      
)
public interface PluginMetadata {
    
    /**
     * Returns the name of the plugin.
     * @return the name
     */
    String getName();
    
    /**
     * Returns the description of the plugin
     * @return description
     */
    String getDescription();
    
    /**
     * Returns the version of the plugin.
     * @return the version
     */
    String getVersion();
    
    /**
     * Returns the author of the plugin.
     * @return the author
     */
    String getAuthor();
    
    /**
     * Returns the owner of the plugin.
     * @return 
     */
    String getOwner();
    
    /**
     * Returns the license of the plugin.
     * @return 
     */
    String getLicense();
    
}
