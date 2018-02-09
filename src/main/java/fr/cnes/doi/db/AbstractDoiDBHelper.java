/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.db;

import java.net.URL;
import java.util.Map;
import java.util.Observable;

/**
 * Interface for handling the DOI suffix database.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public abstract class AbstractDoiDBHelper extends Observable {  
    
    /**
     * Notification message when the record is added {@value #ADD_RECORD}.
     */
    public static final String ADD_RECORD = "ADD";

    /**
     * Notification message when the record is added {@value #DELETE_RECORD}.
     */    
    public static final String DELETE_RECORD = "DELETE";      

    /**
     * Init the connection.
     * @param configuration connection configuration 
     */
    public abstract void init(Object configuration);
    
    /**
     * Creates and Adds a DOI for project in the database related to a landing page.
     * @param doi DOI
     * @param landingPage the landing page
     * @return True when the DOI is added otherwise False
     */
    public abstract boolean addDoi(String doi, URL landingPage);    
    
    /**
     * Tests if the DOI exists in the database.
     * @param doi the DOI
     * @return True when the DOI exists otherwise False
     */
    public abstract boolean isExistDOI(String doi);

    /**
     * Tests if the landing page exists in the database.
     * @param landingPage the landing page
     * @return True when the landing page exists otherwise False.
     */
    public abstract boolean isExistLandingPage(URL landingPage);

    /**
     * Returns the landing page from DOI.
     * @param doi the DOI
     * @return the project name
     */
    public abstract URL getLandingPageFrom(String doi);

    /**
     * Returns the DOI from the landing page.
     * @param landingPage the landing page
     * @return the DOI
     */
    public abstract String getDOIFrom(URL landingPage);
            
    /**
     * Returns the database records.
     * @return the database records
     */
    public abstract Map<URL, String> getRecords();                
    
}
