/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.db;

import fr.cnes.doi.utils.spec.Requirement;
import java.util.Observable;

/**
 * Interface for handling the token database.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_INTER_040,
        reqName = Requirement.DOI_INTER_040_NAME
)
public abstract class AbstractTokenDBHelper extends Observable {
    
    /**
     * Init the connection.
     * @param configuration connection configuration 
     */    
    public abstract void init(Object configuration);
    
    /**
     * Adds a token in the database
     * @param jwt a token
     * @return True when the token is added to the database otherwise False
     */
    public abstract boolean addToken(String jwt);
    
    /**
     * Deletes a token from the database.
     * @param jwt the token
     */
    public abstract void deleteToken(String jwt);
    
    /**
     * Tests if the token exists in the database.
     * @param jwt the token
     * @return True when the token exists in the database otherwise False
     */
    public abstract boolean isExist(String jwt);
    
    /**
     * Tests if the token is expirated.
     * @param jwt the token
     * @return True when the token is expirated otherwise False
     */
    public abstract boolean isExpirated(String jwt);   
}
