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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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
import fr.cnes.doi.security.RoleAuthorizer;
import fr.cnes.doi.settings.DoiSettings;

/**
 * Default implementation of the project suffix database.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DefaultProjectSuffixImplTEST extends AbstractProjectSuffixPluginHelper {

    /**
     * Default file if the path is not defined in the configuration file
     */
    private static final String DEFAULT_CACHE_FILE = "data" + File.separator + "projects.conf";

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DefaultTokenImpl.class.getName());

    private static final String DESCRIPTION = "Provides a pre-defined list of users and groups";
    private static final String VERSION = "1.0.0";
    private static final String OWNER = "CNES";
    private static final String AUTHOR = "Jean-Christophe Malapert";
    private static final String LICENSE = "LGPLV3";
    private final String NAME = this.getClass().getName();

    /**
     * Configuration of the projects identifiers
     */
    private String projectConf;

    /**
     * Mapping between the identifiers and the projects
     */
    private final Map<Integer, String> idProjMap = new ConcurrentHashMap<>();
    /**
     * Mapping between the projects and the identifier
     */
    private final Map<String, Integer> projIdMap = new ConcurrentHashMap<>();

    /**
     * Default constructor of the project suffix database
     */
    public DefaultProjectSuffixImplTEST() {
        super();
    }
    
    private final DOIDbDataAccessService das = new DOIDbDataAccessServiceImpl();;

    
    
    /**
     * Init the configuration with the configuration file. If the given file does not exist a new
     * file will be created. The file contains the mapping between the project name and the
     * identifiers
     *
     * @param configuration The file that contains the database
     */
    @Override
    public void init(Object configuration) {
        if (configuration == null) {
            this.projectConf = DoiSettings.getInstance().getPathApp()
                    + File.separatorChar + DEFAULT_CACHE_FILE;
        } else {
            this.projectConf = String.valueOf(configuration);
        }
        File projConfFile = new File(projectConf);
        try {
            // If the file exists, load it
            if (projConfFile.exists()) {
                LOG.info("Loads the project suffix database :" + projectConf);
                loadProjectConf(projConfFile);
            } else {
                LOG.info("create the database :" + projectConf);
                createProjectConf(projConfFile);
            }
            this.addObserver(RoleAuthorizer.getInstance());
        } catch (IOException e) {
            LOG.fatal("Cannot access the cache file for the mapping projects/id " + projectConf, e);
        }

    }

    /**
     * Loads the database that contains the encoded project name.
     *
     * @param projConfFile File that contains the project configuration
     * @throws IOException Exception when trying to load the file
     */
    private void loadProjectConf(File projConfFile) throws IOException {
        LOG.debug("Cache file exists : {}", projConfFile.getAbsolutePath());

        List<String> lines = Files.readAllLines(projConfFile.toPath());
        // Si le fichier contient autre chose que la ligne d'entete
        if (lines.size() > 1) {
            boolean firstLine = true;
            // lit chaque ligne et ajoute les infos dans la map
            for (String line : lines) {
                if (firstLine) {
                    firstLine = false;
                } else {
                    String[] split = line.split(";");
                    if (split.length != 2) {
                        LOG.debug("The line {} is not formatted in the expected way", line);
                    } else {
                        String projectName = split[0];
                        int id = Integer.parseInt(split[1]);
                        this.idProjMap.put(id, projectName);
                        this.projIdMap.put(projectName, id);
                    }
                }
            }
        }
    }

    /**
     * Creates the project configuration with the header Project Name;Id
     *
     * @param projConfFile File to create
     * @throws IOException Exception when trying to create the file
     */
    private void createProjectConf(File projConfFile) throws IOException {
        // Init the config file
        LOG.debug("Cache file does not exist, create it : {}", projConfFile.getAbsolutePath());
        File directory = new File(projConfFile.getParent());
        Files.createDirectories(directory.toPath());
        Files.createFile(projConfFile.toPath());
        Files.write(
                projConfFile.toPath(),
                "Project Name;Id\n".getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.APPEND
        );
        // current projects for which a role has already been given
        Files.write(
                projConfFile.toPath(),
                "CFOSAT;828606\n".getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.APPEND
        );
        Files.write(
                projConfFile.toPath(),
                "THEIA;329360\n".getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.APPEND
        );        
    }

    @Override
    public synchronized boolean addProjectSuffix(int projectID,
            String projectName) {
        boolean isAdded = false;
        try {
            String line = projectName + ";" + projectID + "\n";
            LOG.info("Add projectSuffix in the database {} / {}", projectID, projectName);
            Files.write(
                    new File(this.projectConf).toPath(),
                    line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND
            );
            this.projIdMap.put(projectName, projectID);
            this.idProjMap.put(projectID, projectName);
            isAdded = true;
            setChanged();
            notifyObservers(new String[]{ADD_RECORD, String.valueOf(projectID)});
        } catch (IOException e) {
            LOG.fatal("The id " + projectID + " of the project " + projectName
                    + "cannot be saved in the file", e);
        }
        return isAdded;
    }

    @Override
    public synchronized void deleteProject(int projectID) {
        throw new RuntimeException("Not implemented");
        //setChanged();
        //notifyObservers(new String[]{DELETE_RECORD, String.valueOf(projectID)});    
    }

    @Override
    public boolean isExistID(int projectID) {
        return this.idProjMap.containsKey(projectID);
    }

    @Override
    public boolean isExistProjectName(String projectName) {
        return this.projIdMap.containsKey(projectName);
    }

    @Override
    public String getProjectFrom(int projectID) {
        return this.idProjMap.get(projectID);
    }

    @Override
    public int getIDFrom(String projectName) {
        return this.projIdMap.get(projectName);
    }

    @Override
    public Map<String, Integer> getProjects() {
    	try {
			List<DOIProject> tt = das.getAllDOIProjects();
			Map<String, Integer> map = new HashMap<String, Integer>();
			for(DOIProject x : tt) {
				map.put(x.getProjectname(), x.getSuffix());
			}
			return map;
		} catch (DOIDbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new HashMap<String, Integer>();
		}
//        return Collections.unmodifiableMap(this.projIdMap);
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
