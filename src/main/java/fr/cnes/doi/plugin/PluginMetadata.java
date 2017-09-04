/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.plugin;

/**
 * Provides metadata about the plugin.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public interface PluginMetadata {
    
    /**
     * Returns the name of the plugin.
     * @return the name
     */
    public String getName();
    
    /**
     * Returns the description of the plugin
     * @return description
     */
    public String getDescription();
    
    /**
     * Returns the version of the plugin.
     * @return the version
     */
    public String getVersion();
    
    /**
     * Returns the author of the plugin.
     * @return the author
     */
    public String getAuthor();
    
    /**
     * Returns the owner of the plugin.
     * @return 
     */
    public String getOwner();
    
    /**
     * Returns the license of the plugin.
     * @return 
     */
    public String getLicense();
    
}
