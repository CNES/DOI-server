/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.plugin;

import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Plugin to define users and groups of the system.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_ARCHI_030,
        reqName = Requirement.DOI_ARCHI_030_NAME      
)
public abstract class AbstractUserRolePluginHelper 
        extends AbstractUserRoleDBHelper implements PluginMetadata {    
    
    /**
     * Empty constructor.
     */
    protected AbstractUserRolePluginHelper() {
        super();
    }
}
