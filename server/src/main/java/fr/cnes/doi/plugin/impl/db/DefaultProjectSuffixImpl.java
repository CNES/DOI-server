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
package fr.cnes.doi.plugin.impl.db;

import fr.cnes.doi.plugin.impl.db.service.DatabaseSingleton;
import fr.cnes.doi.plugin.impl.db.service.DOIDbDataAccessService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.db.model.DOIProject;
import fr.cnes.doi.plugin.AbstractProjectSuffixPluginHelper;
import fr.cnes.doi.db.model.DOIUser;
import java.util.ArrayList;

/**
 * Default implementation of the project suffix database.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DefaultProjectSuffixImpl extends AbstractProjectSuffixPluginHelper {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DefaultProjectSuffixImpl.class.getName());

    /**
     * Plugin description.
     */
    private static final String DESCRIPTION = "Provides a pre-defined list of users and groups";
    /**
     * Plugin version.
     */
    private static final String VERSION = "1.0.0";
    /**
     * Plugin owner.
     */
    private static final String OWNER = "CNES";
    /**
     * Plugin author.
     */
    private static final String AUTHOR = "Jean-Christophe Malapert";
    /**
     * Plugin license.
     */
    private static final String LICENSE = "LGPLV3";
    /**
     * Plugin name.
     */
    private final String NAME = this.getClass().getName();
    /**
     * DOI database access.
     */
    private final DOIDbDataAccessService das = DatabaseSingleton.getInstance().getDatabaseAccess();

    /**
     * Default constructor of the project suffix database
     */
    public DefaultProjectSuffixImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(final Object configuration) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean addProjectSuffix(final int projectID, final String projectName) {
        boolean isAdded = false;
        try {
            das.addDOIProject(projectID, projectName);
            LOG.info("Add projectSuffix in the database {} / {}", projectID, projectName);
            setChanged();
            notifyObservers(new String[]{ADD_RECORD, String.valueOf(projectID)});
            isAdded = true;
        } catch (DOIDbException e) {
            LOG.fatal("The id " + projectID + " of the project " + projectName
                    + "cannot be saved in the database", e);
        }
        return isAdded;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean deleteProject(final int projectID) {
        boolean isDeleted = false;
        try {
            das.removeDOIProject(projectID);
            LOG.info("Delete projectSuffix in the database {} ", projectID);
            setChanged();
            notifyObservers(new String[]{DELETE_RECORD, String.valueOf(projectID)});
            isDeleted = true;
        } catch (DOIDbException e) {
            LOG.fatal("The id " + projectID + " cannot be deleted or doest not exist", e);
        }
        return isDeleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExistID(final int projectID) {
        final Map<String, Integer> map = getProjects();
        final boolean isExist;
        if (map.isEmpty()) {
            isExist = false;
        } else {
            isExist = map.containsValue(projectID);
        }
        return isExist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExistProjectName(final String projectName) {
        final Map<String, Integer> map = getProjects();
        final boolean isExist;
        if (map.isEmpty()) {
            isExist = false;
        } else {
            isExist = map.containsKey(projectName);
        }
        return isExist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectFrom(final int projectID) {
        String projectName = "";
        try {
            projectName = das.getDOIProjectName(projectID);
        } catch (DOIDbException e) {
            LOG.fatal(
                    "An error occured while trying to get the name of the project from the id " + projectID,
                    e);
        }
        return projectName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIDFrom(final String projectName) {
        final Map<String, Integer> map = getProjects();
        if (map.isEmpty()) {
            throw new RuntimeException("The projects list is empty");
        }
        return map.get(projectName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Integer> getProjects() {
        final Map<String, Integer> map = new HashMap<>();
        try {
            final List<DOIProject> doiProjects = das.getAllDOIProjects();
            for (final DOIProject doiProject : doiProjects) {
                map.put(doiProject.getProjectname(), doiProject.getSuffix());
            }
        } catch (DOIDbException e) {
            LOG.fatal("An error occured while trying to get all DOI projects", e);
        }

        return Collections.unmodifiableMap(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Integer> getProjectsFromUser(final String userName) {
        final Map<String, Integer> map = new HashMap<>();
        try {
            final List<DOIProject> doiProjects = das.getAllDOIProjectsForUser(userName);
            for (final DOIProject doiProject : doiProjects) {
                map.put(doiProject.getProjectname(), doiProject.getSuffix());
            }
        } catch (DOIDbException e) {
            LOG.fatal("An error occured while trying to get all DOI projects", e);
        }

        return Collections.unmodifiableMap(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean renameProject(final int projectId, final String newProjectName) {
        boolean isRenamed = false;
        try {
            das.renameDOIProject(projectId, newProjectName);
            LOG.info("Rename project in the database {} to {}", projectId, newProjectName);
            setChanged();
            notifyObservers(new String[]{RENAME_RECORD, String.valueOf(projectId)});
            isRenamed = true;
        } catch (DOIDbException e) {
            LOG.fatal("An error occured while trying to rename the project " + projectId, e);
        }
        return isRenamed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOwner() {
        return OWNER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLicense() {
        return LICENSE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DOIUser> getAllDOIUsersForProject(final int doiSuffix) {
        final List<DOIUser> doiUsers = new ArrayList<>();
        try {
            doiUsers.addAll(this.das.getAllDOIUsersForProject(doiSuffix));
        } catch (DOIDbException ex) {
            LOG.
                    fatal("An error occured while trying to get all DOI users from project " + doiSuffix,
                            ex);
        }
        return doiUsers;
    }

}
