/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.doi.db;

import java.net.URI;
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
    public abstract boolean addDoi(String doi, URI landingPage);    
    
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
    public abstract boolean isExistLandingPage(URI landingPage);

    /**
     * Returns the landing page from DOI.
     * @param doi the DOI
     * @return the project name
     */
    public abstract URI getLandingPageFrom(String doi);

    /**
     * Returns the DOI from the landing page.
     * @param landingPage the landing page
     * @return the DOI
     */
    public abstract String getDOIFrom(URI landingPage);
            
    /**
     * Returns the database records.
     * @return the database records
     */
    public abstract Map<URI, String> getRecords();                
    
}
