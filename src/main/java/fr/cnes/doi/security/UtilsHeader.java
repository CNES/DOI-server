/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.utils.spec.Requirement;

/**
 * Utility class.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_AUTO_020,
        reqName = Requirement.DOI_AUTO_020_NAME
)
public class UtilsHeader {
    
    /**
     * The parameter to select the role of the operator when he is implied in different groups.
     */
    public static final String SELECTED_ROLE_PARAMETER = "selectedRole";    
    
}
