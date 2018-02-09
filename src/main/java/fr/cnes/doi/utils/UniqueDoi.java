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
package fr.cnes.doi.utils;

import fr.cnes.doi.db.AbstractDoiDBHelper;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.resource.admin.SuffixProjectsResource;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.net.URL;
import java.util.Map;

/**
 * Utils class to generate a unique DOI
 *
 */
public class UniqueDoi {
    
    /**
     * Class name.
     */
    private static final String CLASS_NAME = UniqueDoi.class.getName();
    
    /**
     * logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
        
    /**
     * Project Suffix database.
     */
    private final AbstractDoiDBHelper doiDB;
    
    /**
     * Class to handle the instance
     *
     */
    private static class UniqueDoiHolder {

        /**
         * Unique Instance unique
         */
        private static final UniqueDoi INSTANCE = new UniqueDoi();
    }

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static UniqueDoi getInstance() {
        return UniqueDoiHolder.INSTANCE;
    }
    
    /**
     * Constructor
     */
    private UniqueDoi() {
        LOGGER.entering(CLASS_NAME, "Constructor");
        final String path = DoiSettings.getInstance().getString(Consts.DOI_CONF_PATH); 
        this.doiDB = PluginFactory.getDoi();        
        this.doiDB.init(path);        
        LOGGER.exiting(CLASS_NAME, "Constructor");        
    }
    
    /**
     * Returns the projects from the database.
     * @return the projects
     */
    public Map<URL, String> getRecords() {
        LOGGER.log(Level.CONFIG, "getProjects : {0}", this.doiDB.getRecords());
        return this.doiDB.getRecords();
    }    

    /**
     * Creates an Id with an uniform distribution.
     *
     * @param maxNumber Number max to generate
     *
     * @return the IdRequirement.DOI_AUTH_010
     */
    private int generateId(final int maxNumber) {
        LOGGER.entering(CLASS_NAME, "generateId", maxNumber);
        final Random rand = new Random();
        final int identifier = rand.nextInt(maxNumber);
        LOGGER.exiting(CLASS_NAME, "generateId", identifier);        
        return identifier;
    }

    /**
     * Convert the project name into unique long
     *
     * @param projectID project ID     
     * @param input Identifier to convert
     * @param landingPage  the project name to convert
     * @param maxNumber Number max to generate
     * @return the DOI
     */
    private String convert(final String inistPrefix, final int projectID, final long input, final URL landingPage, final int maxNumber) {
        LOGGER.entering(CLASS_NAME, "convert", new Object[]{input, landingPage, maxNumber});
        String doi = null;
        do {
            int result = Math.abs((int) (input ^ (landingPage.hashCode() % maxNumber)));
            doi = inistPrefix+"/"+projectID+"/"+result;
        } while (!isIdUnique(doi, landingPage));
        LOGGER.exiting(CLASS_NAME, "convert", doi);
        return doi;
    }

    /**
     * Build a DOI from the project name
     *
     * @param inist_prefix INIST prefix for DOI
     * @param projectName project name     
     * @param landingPage the landingPage
     * @param length length of the short name to generate (the short name must
     * be an int to the length cannot be up to 9)
     * @return the DOI
     */
    public String createDOI(final String inist_prefix, final String projectName, final URL landingPage, final int length) {
        LOGGER.entering(CLASS_NAME, "getShortName", new Object[]{projectName, landingPage, length});       
        final String doi;
        if (length > 9) {
            final DoiRuntimeException doiEx = new DoiRuntimeException("The short name cannot be build because the length requested is too big");
            LOGGER.throwing(CLASS_NAME, "getShortName", doiEx);            
            throw doiEx;
        } else if (this.doiDB.isExistLandingPage(landingPage)) {
            // Si le projet a déjà un identifiant on ne le recalcule pas
            doi = this.doiDB.getDOIFrom(landingPage);
            LOGGER.log(Level.FINE, "The landingPage {0} has already a DOI : {1}", new Object[]{landingPage, doi});
        } else {
            final int maxNumber = (int) Math.pow(10.0, length);
            final long idRandom = generateId(maxNumber);
            int projectID = UniqueProjectName.getInstance().getShortName(projectName, SuffixProjectsResource.NB_DIGITS);
            doi = convert(inist_prefix, projectID, idRandom, landingPage, maxNumber);
            LOGGER.log(Level.FINE, "The landing page {0} has a doi : {1}", new Object[]{landingPage, doi});
        }
        LOGGER.exiting(CLASS_NAME, "getShortName", doi);
        return doi;
    }
        
    /**
     * Check if the generated DOI is unique (does not already exists) or not. If
     * not add it associated with the landing page
     *
     * @param doiToCheck the DOI to check
     * @param landingPage LAnding page related to the DOI
     * @return true if the Id is OK, false otherwise
     */
    private synchronized boolean isIdUnique(final String doiToCheck, final URL landingPage) {
        LOGGER.entering(CLASS_NAME, "isIdUnique", new Object[]{doiToCheck, landingPage});
        final boolean result;
        if (this.doiDB.isExistDOI(doiToCheck)) {
            result = false;
        } else {
            this.doiDB.addDoi(doiToCheck, landingPage);
            result = true;
        }
        LOGGER.exiting(CLASS_NAME, "isIdUnique", result);
        return result;
    }

}
