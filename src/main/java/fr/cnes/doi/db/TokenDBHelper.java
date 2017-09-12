/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.db;

import java.util.Observable;

/**
 * Interface for handling the token database.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public abstract class TokenDBHelper extends Observable {
    
    public abstract void init(Object configuration);
    
    public abstract boolean addToken(String jwt);
    
    public abstract void deleteToken(String jwt);
    
    public abstract boolean isExist(String jwt);
    
    public abstract boolean isExpirated(String jwt);   
}
