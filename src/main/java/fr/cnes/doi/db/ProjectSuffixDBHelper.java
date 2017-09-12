/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.db;

import java.util.Map;
import java.util.Observable;

/**
 * Interface for handling the project suffix database.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public abstract class ProjectSuffixDBHelper extends Observable {
    
    public static final String ADD_RECORD = "ADD";

    public static final String DELETE_RECORD = "DELETE";    

    public abstract void init(Object configuration);
    
    public abstract boolean addProjectSuffix(int projectID, String projectName);
    
    public abstract void deleteProject(int projectID);
    
    public abstract boolean isExistID(int projectID);

    public abstract boolean isExistProjectName(String projectName);

    public abstract String getProjectFrom(int projectID);

    public abstract int getIDFrom(String projectName);
            
    public abstract Map<String, Integer> getProjects();                
    
}
