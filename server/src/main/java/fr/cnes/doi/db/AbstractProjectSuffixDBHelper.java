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
package fr.cnes.doi.db;

import fr.cnes.doi.db.model.DOIProject;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.List;
import java.util.Observable;

/**
 * Interface for handling the project suffix database.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_INTER_030, reqName = Requirement.DOI_INTER_030_NAME)
public abstract class AbstractProjectSuffixDBHelper extends Observable {

    /**
     * Notification message when the record is added {@value #ADD_RECORD}.
     */
    public static final String ADD_RECORD = "ADD";

    /**
     * Notification message when the record is added {@value #DELETE_RECORD}.
     */
    public static final String DELETE_RECORD = "DELETE";

    /**
     * Notification message when the record is added {@value #RENAME_RECORD}.
     */
    public static final String RENAME_RECORD = "RENAME";

    /**
     * Adds a suffix project in the database.
     *
     * @param projectID suffix project
     * @param projectName project name
     * @return True when the suffix project is added otherwise False
     */
    public abstract boolean addProjectSuffix(int projectID, String projectName);

    /**
     * Rename a project in the database.
     *
     * @param projectID suffix project to be renamed
     * @param newProjectName the new project name
     * @return True when the project has been renamed otherwise False
     */
    public abstract boolean renameProject(int projectID, String newProjectName);

    /**
     * Deletes a suffix project from the database.
     *
     * @param projectID the suffix project
     * @return True when the suffix project has been deleted otherwise False
     */
    public abstract boolean deleteProject(int projectID);

    /**
     * Tests is a suffix project exists in the database.
     *
     * @param projectID suffix project
     * @return True when the suffix project exists otherwise False
     */
    public abstract boolean isExistID(int projectID);

    /**
     * Tests is the project name exists in the database.
     *
     * @param projectName the project name
     * @return True when the project name exists otherwise False.
     */
    public abstract boolean isExistProjectName(String projectName);

    /**
     * Returns the project name based on the suffix project.
     *
     * @param projectID the suffix project
     * @return the project name
     * @throws fr.cnes.doi.exception.DOIDbException when an error occurs
     */
    public abstract String getProjectFrom(int projectID) throws DOIDbException;

    /**
     * Returns the project suffix based on the project name.
     *
     * @param projectName the project name
     * @return the suffix project
     * @throws fr.cnes.doi.exception.DOIDbException When an error occurs
     */
    public abstract int getIDFrom(String projectName) throws DOIDbException;

    /**
     * Returns the database records.
     *
     * @return the database records
     * @throws fr.cnes.doi.exception.DOIDbException When an error occurs
     */
    public abstract List<DOIProject> getProjects() throws DOIDbException;

    /**
     * Returns the projects related to a specific user.
     *
     * @param userName username
     * @return the projected to an user
     * @throws fr.cnes.doi.exception.DOIDbException When an error occurs
     */
    public abstract List<DOIProject> getProjectsFromUser(final String userName) throws
            DOIDbException;

    /**
     * Returns the users related to a project.
     *
     * @param doiSuffix project
     * @return the users
     * @throws fr.cnes.doi.exception.DOIDbException When an error occurs.
     */
    public abstract List<DOIUser> getAllDOIUsersForProject(final int doiSuffix) throws
            DOIDbException;

}
