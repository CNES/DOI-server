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

import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnes.doi.db.AbstractProjectSuffixDBHelper;
import fr.cnes.doi.plugin.PluginFactory;
import java.util.List;
import java.util.Map;

/**
 * Utils class to rename or delete a project from database
 *
 */
public class ManageProjects {

    /**
     * Class name.
     */
    private static final String CLASS_NAME = ManageProjects.class.getName();

    /**
     * logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static ManageProjects getInstance() {
        return ManageProjectsHolder.INSTANCE;
    }
    /**
     * Project Suffix database.
     */
    private final AbstractProjectSuffixDBHelper projectDB;

    /**
     * Constructor
     */
    private ManageProjects() {
        LOGGER.entering(CLASS_NAME, "Constructor");
        this.projectDB = PluginFactory.getProjectSuffix();
        this.projectDB.init();
        LOGGER.exiting(CLASS_NAME, "Constructor");
    }

    // TODO vérification aucun DOI associé au projet
    public boolean deleteProject(int projectId) {
        LOGGER.log(Level.CONFIG, "deleteProject : {0}", this.projectDB.deleteProject(projectId));
        return this.projectDB.deleteProject(projectId);
    }

    public boolean renameProject(int projectId, String newProjectName) {
        LOGGER.entering(CLASS_NAME, "renameProject", new Object[]{projectId, newProjectName});
        return this.projectDB.renameProject(projectId, newProjectName);
    }

    public List<DOIUser> getAllDOIUsersForProject(int doiSuffix) {
        return this.projectDB.getAllDOIUsersForProject(doiSuffix);
    }

    public Map<String, Integer> getProjects() {
        return this.projectDB.getProjects();
    }

    public Map<String, Integer> getProjectsFromUser(String userName) {
        return this.projectDB.getProjectsFromUser(userName);
    }

    public boolean isExistProjectName(String project) {
        return this.projectDB.isExistProjectName(project);
    }

    public int getIDFrom(String project) {
        return this.projectDB.getIDFrom(project);
    }

    public boolean isExistID(int idToCheck) {
        return this.projectDB.isExistID(idToCheck);
    }

    public boolean addProjectSuffix(int idToCheck, String projectName) {
        return this.projectDB.addProjectSuffix(idToCheck, projectName);
    }

    /**
     * Class to handle the instance
     *
     */
    private static class ManageProjectsHolder {

        /**
         * Unique Instance unique
         */
        private static final ManageProjects INSTANCE = new ManageProjects();
    }
}
