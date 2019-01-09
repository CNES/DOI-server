package fr.cnes.doi.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnes.doi.db.AbstractProjectSuffixDBHelper;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

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
        final String path = DoiSettings.getInstance().getString(Consts.PROJECT_CONF_PATH);
        this.projectDB = PluginFactory.getProjectSuffix();
        this.projectDB.init(path);
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
