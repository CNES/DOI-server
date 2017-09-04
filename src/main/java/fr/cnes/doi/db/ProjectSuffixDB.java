/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.db;

import fr.cnes.doi.security.RoleAuthorizer;
import fr.cnes.doi.settings.DoiSettings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class ProjectSuffixDB extends Observable{

    /**
     * Default file if the path is not defined in the configuration file
     */
    private static final String DEFAULT_CACHE_FILE = "data/projects.conf";
    
    public static final String ADD_RECORD = "ADD";
    
    public static final String DELETE_RECORD = "DELETE";

    /**
     * logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProjectSuffixDB.class.getName());

    public ProjectSuffixDB(String projectConf) {
        init(projectConf);
    }

    public ProjectSuffixDB() {
        this(DoiSettings.getInstance().getPathApp()+File.separatorChar+DEFAULT_CACHE_FILE);
    }

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
     * Init the configuration with the configuration file. If the given file
     * does not exist a new file will be created. The file contains the mapping
     * between the project name and the identifiers
     *
     * @param projectConf The file that contains the database     
     */
    public final void init(String projectConf) {
        this.projectConf = projectConf;
        File projConfFile = new File(projectConf);
        try {
            // If the file exists, load it
            if (projConfFile.exists()) {
                loadProjectConf(projConfFile);
            } else {
                createProjectConf(projConfFile);
            }
            this.addObserver(RoleAuthorizer.getInstance());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot access the cache file for the mapping projects/id " + projectConf, e);
        }
        
    }

    /**
     * Loads the database that contains the encoded project name.
     *
     * @param projConfFile File that contains the project configuration
     * @throws IOException Exception when trying to load the file
     */
    private void loadProjectConf(File projConfFile) throws IOException {
        LOGGER.log(Level.FINEST, "Cache file exists : {0}", projConfFile.getAbsolutePath());

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
                        LOGGER.log(Level.WARNING, "The line {0} is not formatted in the expected way", line);
                    } else {
                        String projectName = split[0];
                        int id = Integer.valueOf(split[1]);
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
        LOGGER.log(Level.FINEST, "Cache file does not exist, create it : {0}", projConfFile.getAbsolutePath());
        File directory = new File(projConfFile.getParent());
        Files.createDirectories(directory.toPath());
        Files.createFile(projConfFile.toPath());
        Files.write(projConfFile.toPath(), "Project Name;Id\n".getBytes(), StandardOpenOption.APPEND);
    }

    public synchronized boolean addProjectSuffix(int projectID, String projectName) {
        boolean isAdded = false;                
        try {
            String line = projectName + ";" + projectID + "\n";
            Files.write(new File(this.projectConf).toPath(), line.getBytes(), StandardOpenOption.APPEND);
            this.projIdMap.put(projectName, projectID);            
            this.idProjMap.put(projectID, projectName);            
            isAdded = true;
            setChanged();
            notifyObservers(new String[]{ADD_RECORD, String.valueOf(projectID)});
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,
                    "The id " + projectID + " of the project " + projectName + "cannot be saved in the file", e);
        }
        return isAdded;
    }

    public void deleteProject(String project) {
        throw new RuntimeException("Not implemented");
    }

    public boolean isExistID(int projectID) {
        return this.idProjMap.containsKey(projectID);
    }

    public boolean isExistProjectName(String projectName) {
        return this.projIdMap.containsKey(projectName);
    }

    public String getProjectFrom(int projectID) {
        return this.idProjMap.get(projectID);
    }

    public int getIDFrom(String projectName) {
        return this.projIdMap.get(projectName);
    }

    public Map<String, Integer> getProjects() {
        return this.projIdMap;
    }
    
    

}
