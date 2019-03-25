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

import fr.cnes.doi.db.model.DOIProject;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utils class to generate a unique number from the project name
 *
 */
@Requirement(reqId = Requirement.DOI_INTER_030, reqName = Requirement.DOI_INTER_030_NAME)
public final class UniqueProjectName {

    /**
     * logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(UniqueProjectName.class.getName());

    /**
     * Constructor
     */
    private UniqueProjectName() {
    }

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static UniqueProjectName getInstance() {
        return UniqueProjectNameHolder.INSTANCE;
    }

    /**
     * Returns the projects from the database.
     *
     * @return the projects
     * @throws fr.cnes.doi.exception.DOIDbException When a problem occurs
     */
    public List<DOIProject> getProjects() throws DOIDbException {
        LOGGER.traceEntry();
        final List<DOIProject> projects = PluginFactory.getProjectSuffix().getProjects();
        return LOGGER.traceExit("List of projects", projects);
    }

    /**
     * Returns the projects associated to an user from the database.
     *
     * @param userName user name
     * @return the projects
     * @throws fr.cnes.doi.exception.DOIDbException When a problem occurs
     */
    public List<DOIProject> getProjectsFromUser(final String userName) throws DOIDbException {
        LOGGER.traceEntry("Parameter\n  userName:{}", userName);        
        final List<DOIProject> projects = PluginFactory.getProjectSuffix().getProjectsFromUser(userName);
        return LOGGER.traceExit("Projects for userName", projects);
    }

    /**
     * Creates an Id with an uniform distribution.
     *
     * @param maxNumber Number max to generate
     *
     * @return the IdRequirement.DOI_AUTH_010
     */
    private int generateId(final int maxNumber) {
        LOGGER.traceEntry("Parameter\n   maxNumber: {}", maxNumber);
        final Random rand = new Random();
        final int identifier = Math.abs(rand.nextInt(maxNumber));
        return LOGGER.traceExit("ID", identifier);
    }

    /**
     * Convert the project name into unique long
     *
     * @param input Identifier to convert
     * @param projectName the project name to convert
     * @param maxNumber Number max to generate
     * @return the input that is converted to the base
     */
    private int convert(final long input, final String projectName, final int maxNumber) {
        LOGGER.traceEntry("Parameters\n  input:{}\n  projectName:{}\n  maxNumer:{}", input,
                projectName, maxNumber);
        int result = (int) input;
        do {
            result = (int) (result ^ (projectName.hashCode() % maxNumber));
        } while (result > maxNumber || result < 0 || !isIdUnique(result, projectName));
        return LOGGER.traceExit("ID", result);
    }

    /**
     * Build a unique String from the project name
     *
     * @param project the project name
     * @param length length of the short name to generate (the short name must be an int to the
     * length cannot be up to 9)
     * @return the unique string
     * @throws fr.cnes.doi.exception.DOIDbException When a problem occurs
     */
    public int getShortName(final String project, final int length) throws DOIDbException {
        LOGGER.traceEntry("Parameters\n project:{}\n  length:{}", project, length);
        int suffixID;
        if (length > 9) {
            final DoiRuntimeException doiEx = new DoiRuntimeException(
                    "The short name cannot be build because the length requested is too big");
            throw LOGGER.throwing(doiEx);
        } else if (PluginFactory.getProjectSuffix().isExistProjectName(project)) {
            // Si le projet a déjà un identifiant on ne le recalcule pas
            suffixID = PluginFactory.getProjectSuffix().getIDFrom(project);
            LOGGER.warn("The project {} has already an id : {}", project, suffixID);
        } else {
            final int maxNumber = (int) Math.pow(10.0, length);
            final long idRandom = generateId(maxNumber);
            suffixID = convert(idRandom, project, maxNumber);
            LOGGER.warn("The project {} has an id : {}", project, suffixID);
        }
        return LOGGER.traceExit(suffixID);
    }

    /**
     * Check if the generated suffixID is unique (does not already exists) or not. If not add it
     * associated with the project
     *
     * @param idToCheck the identifier to check
     * @param projectName Project associated to the suffixID
     * @return true if the Id is OK, false otherwise
     */
    private synchronized boolean isIdUnique(final int idToCheck, final String projectName) {
        LOGGER.traceEntry("Parameters\n  idToCheck:{}\n  projectName:{}\n", idToCheck, projectName);
        final boolean result;
        if (PluginFactory.getProjectSuffix().isExistID(idToCheck)) {
            result = false;
        } else {
            result = PluginFactory.getProjectSuffix().addProjectSuffix(idToCheck, projectName);
        }
        return LOGGER.traceExit(result);
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
