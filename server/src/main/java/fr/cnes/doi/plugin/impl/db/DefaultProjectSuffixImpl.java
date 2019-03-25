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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.db.model.DOIProject;
import fr.cnes.doi.plugin.AbstractProjectSuffixPluginHelper;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.exception.DoiRuntimeException;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_MAX_ACTIVE_CONNECTIONS;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_MAX_IDLE_CONNECTIONS;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_MIN_IDLE_CONNECTIONS;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_PWD;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_URL;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_USER;
import fr.cnes.doi.utils.Utils;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Default implementation of the project suffix database.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class DefaultProjectSuffixImpl extends AbstractProjectSuffixPluginHelper {

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
    private DOIDbDataAccessService das;

    /**
     * Configuration file.
     */
    private Map<String, String> conf;

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
        this.conf = (Map<String, String>) configuration;
        final String dbUrl = this.conf.get(DB_URL);
        final String dbUser = this.conf.get(DB_USER);
        final String dbPwd = this.conf.get(DB_PWD);
        final Map<String, Integer> options = new HashMap<>();
        if (this.conf.containsKey(DB_MIN_IDLE_CONNECTIONS)) {
            options.put(DB_MIN_IDLE_CONNECTIONS,
                    Integer.valueOf(this.conf.get(DB_MIN_IDLE_CONNECTIONS)));
        }
        if (this.conf.containsKey(DB_MAX_IDLE_CONNECTIONS)) {
            options.put(DB_MAX_IDLE_CONNECTIONS,
                    Integer.valueOf(this.conf.get(DB_MAX_IDLE_CONNECTIONS)));
        }
        if (this.conf.containsKey(DB_MAX_ACTIVE_CONNECTIONS)) {
            options.put(DB_MAX_ACTIVE_CONNECTIONS,
                    Integer.valueOf(this.conf.get(DB_MAX_ACTIVE_CONNECTIONS)));
        }
        LOG.info("[CONF] Plugin database URL : {}", dbUrl);
        LOG.info("[CONF] Plugin database user : {}", dbUser);
        LOG.info("[CONF] Plugin database password : {}", Utils.transformPasswordToStars(dbPwd));
        LOG.info("[CONF] Plugin options : {}", options);      
        try {
            DatabaseSingleton.getInstance().init(dbUrl, dbUser, dbPwd, options);
            this.das = DatabaseSingleton.getInstance().getDatabaseAccess();
        } catch (DoiRuntimeException ex) {
            LOG.warn(ex);
        }

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
        boolean isExist;
        try {
            final List<DOIProject> projects = getProjects();            
            if (projects.isEmpty()) {
                isExist = false;
            } else {
                final Map<String, Integer> map = projects.stream().collect(
                        Collectors.toMap(DOIProject::getProjectname, DOIProject::getSuffix));
                isExist = map.containsValue(projectID);
            }
            return isExist;
        } catch (DOIDbException ex) {
            isExist = false;
            LOG.fatal(ex);
        }
        return isExist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExistProjectName(final String projectName) {
        boolean isExist;
        try {
            final List<DOIProject> projects = getProjects();
            
            if (projects.isEmpty()) {
                isExist = false;
            } else {
                final Map<String, Integer> map = projects.stream().collect(
                        Collectors.toMap(DOIProject::getProjectname, DOIProject::getSuffix));
                isExist = map.containsKey(projectName);
            }
        } catch (DOIDbException ex) {
            isExist = false;
            LOG.fatal(ex);
        }
        return isExist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectFrom(final int projectID) throws DOIDbException {
        return das.getDOIProjectName(projectID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIDFrom(final String projectName) throws DOIDbException {
        final List<DOIProject> projects = getProjects();
        if (projects.isEmpty()) {
            throw new DoiRuntimeException("The projects list is empty");
        }
        final Map<String, Integer> map = projects.stream().collect(
                Collectors.toMap(DOIProject::getProjectname, DOIProject::getSuffix));
        return map.get(projectName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DOIProject> getProjects() throws DOIDbException {
        return das.getAllDOIProjects();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DOIProject> getProjectsFromUser(final String userName) throws DOIDbException {
        return das.getAllDOIProjectsForUser(userName);
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
            LOG.fatal("An error occured while trying to get all "
                    + "DOI users from project " + doiSuffix, ex);
        }
        return doiUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuilder validate() {
        return new StringBuilder();
    }

    /**
     * Checks if the keyword is a password.
     * @param key keyword to check
     * @return True when the keyword is a password otherwise False
     */
    public static boolean isPassword(String key) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        this.conf = null;
        try {
            this.das.close();
        } catch (DOIDbException ex) {
        }
    }

}
