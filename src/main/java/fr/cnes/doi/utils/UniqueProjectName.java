/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utils class to generate a unique number from the project name
 *
 */
@Requirement(reqId = Requirement.DOI_INTER_030, reqName = Requirement.DOI_INTER_030_NAME)
public class UniqueProjectName {

    /**
     * Class name.
     */
    private static final String CLASS_NAME = UniqueProjectName.class.getName();

    /**
     * logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static UniqueProjectName getInstance() {
        return UniqueProjectNameHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private UniqueProjectName() {
        LOGGER.entering(CLASS_NAME, "Constructor");
        LOGGER.exiting(CLASS_NAME, "Constructor");
    }

    /**
     * Returns the projects from the database.
     *
     * @return the projects
     */
    public Map<String, Integer> getProjects() {
        return ManageProjects.getInstance().getProjects();
    }

    /**
     * Returns the projects associated to an user from the database.
     *
     * @return the projects
     */
    public Map<String, Integer> getProjectsFromUser(String userName) {
        return ManageProjects.getInstance().getProjectsFromUser(userName);
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
        final int identifier = Math.abs(rand.nextInt(maxNumber));
        LOGGER.exiting(CLASS_NAME, "generateId", identifier);
        return identifier;
    }

    /**
     * Convert the project name into unique long
     *
     * @param input Identifier to convert
     * @param projectName the project name to convert
     * @param maxNumber Number max to generate
     * @return the input that is converted to the base
     */
    private int convert(final long input,
            final String projectName,
            final int maxNumber) {
        LOGGER.entering(CLASS_NAME, "convert", new Object[]{input, projectName, maxNumber});
        int result = (int) input;
        do {
            result = (int) (result ^ (projectName.hashCode() % maxNumber));
        } while (result > maxNumber || result < 0 || !isIdUnique(result, projectName));
        LOGGER.entering(CLASS_NAME, "convert", result);
        return result;
    }

    /**
     * Build a unique String from the project name
     *
     * @param project the project name
     * @param length length of the short name to generate (the short name must be an int to the
     * length cannot be up to 9)
     * @return the unique string
     */
    public int getShortName(final String project,
            final int length) {
        LOGGER.entering(CLASS_NAME, "getShortName", new Object[]{project, length});
        final int suffixID;
        if (length > 9) {
            final DoiRuntimeException doiEx = new DoiRuntimeException(
                    "The short name cannot be build because the length requested is too big");
            LOGGER.throwing(CLASS_NAME, "getShortName", doiEx);
            throw doiEx;
        } else if (ManageProjects.getInstance().isExistProjectName(project)) {
            // Si le projet a déjà un identifiant on ne le recalcule pas
            suffixID = ManageProjects.getInstance().getIDFrom(project);
            LOGGER.log(Level.FINE, "The project {0} has already an id : {1}",
                    new Object[]{project, suffixID});
        } else {
            final int maxNumber = (int) Math.pow(10.0, length);
            final long idRandom = generateId(maxNumber);
            suffixID = convert(idRandom, project, maxNumber);
            LOGGER.log(Level.FINE, "The project {0} has an id : {1}",
                    new Object[]{project, suffixID});
        }
        LOGGER.exiting(CLASS_NAME, "getShortName", suffixID);
        return suffixID;
    }

    /**
     * Check if the generated suffixID is unique (does not already exists) or not. If not add it
     * associated with the project
     *
     * @param idToCheck the identifier to check
     * @param projectName Project associated to the suffixID
     * @return true if the Id is OK, false otherwise
     */
    private synchronized boolean isIdUnique(final int idToCheck,
            final String projectName) {
        LOGGER.entering(CLASS_NAME, "isIdUnique", new Object[]{idToCheck, projectName});
        final boolean result;
        if (ManageProjects.getInstance().isExistID(idToCheck)) {
            result = false;
        } else {
            result = ManageProjects.getInstance().addProjectSuffix(idToCheck, projectName);
        }
        LOGGER.exiting(CLASS_NAME, "isIdUnique", result);
        return result;
    }

    /**
     * Class to handle the instance
     *
     */
    private static class UniqueProjectNameHolder {

        /**
         * Unique Instance unique
         */
        private static final UniqueProjectName INSTANCE = new UniqueProjectName();
    }

}
