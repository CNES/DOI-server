/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.plugin.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.impl.DOIDbDataAccessServiceImpl;
import fr.cnes.doi.persistence.model.DOIProject;
import fr.cnes.doi.persistence.service.DOIDbDataAccessService;
import fr.cnes.doi.plugin.AbstractProjectSuffixPluginHelper;

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

    private static final String DESCRIPTION = "Provides a pre-defined list of users and groups";
    private static final String VERSION = "1.0.0";
    private static final String OWNER = "CNES";
    private static final String AUTHOR = "Jean-Christophe Malapert";
    private static final String LICENSE = "LGPLV3";
    private final String NAME = this.getClass().getName();

    /**
     * Default constructor of the project suffix database
     */
    public DefaultProjectSuffixImpl() {
        super();
    }
    
    private final DOIDbDataAccessService das = new DOIDbDataAccessServiceImpl();
    
    
    /**
     * Init the configuration with the configuration file. If the given file does not exist a new
     * file will be created. The file contains the mapping between the project name and the
     * identifiers
     *
     * @param configuration The file that contains the database
     */
    @Override
    public void init(Object configuration) {
    }

    @Override
    public synchronized boolean addProjectSuffix(int projectID,
            String projectName) {
        boolean isAdded = false;
        try {
        	LOG.info("Add projectSuffix in the database {} / {}", projectID, projectName);
			das.addDOIProject(projectID, projectName);
			setChanged();
			notifyObservers(new String[]{ADD_RECORD, String.valueOf(projectID)});
			isAdded = true;
		} catch (DOIDbException e) {
			LOG.fatal("The id " + projectID + " of the project " + projectName
                    + "cannot be saved in the file", e);
		} 
		return isAdded;
		
    }

    @Override
    public synchronized boolean deleteProject(int projectID) {
    	boolean isDeleted = false;
    	try {
			das.removeDOIProject(projectID);
			setChanged();
			notifyObservers(new String[]{DELETE_RECORD, String.valueOf(projectID)});    
			isDeleted = true;
		} catch (DOIDbException e) {
			LOG.fatal("The id " + projectID + " cannot be deleted or doest not exist", e);
		}
    	return isDeleted;
    }

    @Override
    public boolean isExistID(int projectID) {
    	Map<String, Integer> map = getProjects();
    	if(map.size() == 0) {
    		return false;
    	}
        return map.containsValue(projectID);
    }

    @Override
    public boolean isExistProjectName(String projectName) {
    	Map<String, Integer> map = getProjects();
    	if(map.size() == 0) {
    		return false;
    	}
        return map.containsKey(projectName);
    }

    @Override
    public String getProjectFrom(int projectID) {
    	String projectName = "";
    	try {
			projectName = das.getDOIProjectName(projectID);
		} catch (DOIDbException e) {
			LOG.fatal("An error occured while trying to get the name of the project from the id "+ projectID ,e);
		}
        return projectName;
    }

    @Override
    public int getIDFrom(String projectName) {
    	Map<String, Integer> map = getProjects();
    	if(map.size() == 0) {
    		throw new RuntimeException("The projects list is empty");
    	}
        return map.get(projectName);
    }

    @Override
    public Map<String, Integer> getProjects() {
    	Map<String, Integer> map = new HashMap<String, Integer>();
    	try {
			List<DOIProject> doiProjects = das.getAllDOIProjects();
			for(DOIProject doiProject : doiProjects) {
				map.put(doiProject.getProjectname(), doiProject.getSuffix());
			}
		} catch (DOIDbException e) {
			LOG.fatal("An error occured while trying to get all DOI projects" ,e);
		}
    	
    	return Collections.unmodifiableMap(map);
    }
    
    @Override
    public Map<String, Integer> getProjectsFromUser(String userName) {
    	Map<String, Integer> map = new HashMap<String, Integer>();
    	try {
			List<DOIProject> doiProjects = das.getAllDOIProjectsForUser(userName);
			for(DOIProject doiProject : doiProjects) {
				map.put(doiProject.getProjectname(), doiProject.getSuffix());
			}
		} catch (DOIDbException e) {
			LOG.fatal("An error occured while trying to get all DOI projects" ,e);
		}
    	
    	return Collections.unmodifiableMap(map);
    }
    
    @Override
    public boolean renameProject(int projectId, String newProjectName) {
    	boolean isRenamed = false;
    	try {
			das.renameDOIProject(projectId, newProjectName);
			setChanged();
			notifyObservers(new String[]{RENAME_RECORD, String.valueOf(projectId)});   
			isRenamed = true;
		} catch (DOIDbException e) {
			LOG.fatal("An error occured while trying to rename the project " + projectId ,e);
		}
    	return isRenamed;
    }
    

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getOwner() {
        return OWNER;
    }

    @Override
    public String getLicense() {
        return LICENSE;
    }

}
