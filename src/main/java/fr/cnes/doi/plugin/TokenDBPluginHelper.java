/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.plugin;

import fr.cnes.doi.db.TokenDBHelper;
import fr.cnes.doi.utils.spec.Requirement;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_ARCHI_030,
        reqName = Requirement.DOI_ARCHI_030_NAME      
)
public abstract class TokenDBPluginHelper extends TokenDBHelper implements PluginMetadata {
    
}
