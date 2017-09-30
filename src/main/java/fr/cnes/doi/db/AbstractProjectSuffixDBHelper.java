/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.db;

import fr.cnes.doi.utils.spec.Requirement;
import java.util.Map;
import java.util.Observable;

/**
 * Interface for handling the project suffix database.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_INTER_030,
        reqName = Requirement.DOI_INTER_030_NAME
)
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
     * Init the connection.
     * @param configuration connection configuration 
     */
    public abstract void init(Object configuration);
    
    /**
     * Adds a suffix project in the database.
     * @param projectID suffix project
     * @param projectName project name
     * @return True when the suffix project is added otherwise False
     */
    public abstract boolean addProjectSuffix(int projectID, String projectName);
    
    /**
     * Deletes a suffix project from the database.
     * @param projectID the suffix project
     */
    public abstract void deleteProject(int projectID);
    
    /**
     * Tests is a suffix project exists in the database.
     * @param projectID suffix project
     * @return True when the suffix project exists otherwise False
     */
    public abstract boolean isExistID(int projectID);

    /**
     * Tests is the project name exists in the database.
     * @param projectName the project name
     * @return True when the project name exists otherwise False.
     */
    public abstract boolean isExistProjectName(String projectName);

    /**
     * Returns the project name based on the suffix project.
     * @param projectID the suffix project
     * @return the project name
     */
    public abstract String getProjectFrom(int projectID);

    /**
     * Returns the project suffix based on the project name.
     * @param projectName the project name
     * @return the suffix project
     */
    public abstract int getIDFrom(String projectName);
            
    /**
     * Returns the database records.
     * @return the database records
     */
    public abstract Map<String, Integer> getProjects();                
    
}
