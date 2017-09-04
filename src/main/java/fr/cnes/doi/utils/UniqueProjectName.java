/**
 *
 */
package fr.cnes.doi.utils;

import fr.cnes.doi.db.ProjectSuffixDB;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.util.Map;

/**
 * Utils class to generate a unique number from the project name
 *
 */
@Requirement(
        reqId = "DOI_AUTH_010",
        reqName = "Création d'un suffixe projet",
        comment = "La taille de 6 digits est libre à ce niveau. Il est imposé à un autre endroit"
)
public class UniqueProjectName {



    /**
     * logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UniqueProjectName.class.getName());

    /**
     * Class to handle the instance
     *
     */
    private static class UniqueProjectNameHolder {

        /**
         * Unique Instance unique
         */
        private final static UniqueProjectName INSTANCE = new UniqueProjectName();
    }

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static UniqueProjectName getInstance() {
        return UniqueProjectNameHolder.INSTANCE;
    }
    
    private final ProjectSuffixDB projectDB;

    /**
     * Constructor
     */
    private UniqueProjectName() {
        LOGGER.entering(this.getClass().getName(), "Constructor");
        String path = DoiSettings.getInstance().getString(Consts.PROJECT_CONF_PATH);
        if (path == null) {
            this.projectDB = new ProjectSuffixDB();
        } else {
            this.projectDB = new ProjectSuffixDB(path);
        }
        LOGGER.exiting(this.getClass().getName(), "Constructor");        
    }
    
    public Map<String, Integer> getProjects() {
        LOGGER.log(Level.CONFIG, "getProjects : {0}", this.projectDB.getProjects());
        return this.projectDB.getProjects();
    }    

    /**
     * Creates an Id with an uniform distribution.
     *
     * @param maxNumber Number max to generate
     *
     * @return the Id
     */
    private int generateId(int maxNumber) {
        LOGGER.entering(this.getClass().getName(), "generateId", maxNumber);
        final Random rand = new Random();
        int id = rand.nextInt(maxNumber);
        LOGGER.exiting(this.getClass().getName(), "generateId", id);        
        return id;
    }

    /**
     * Convert the project name into unique long
     *
     * @param input Identifier to convert
     * @param projectName the project name to convert
     * @param maxNumber Number max to generate
     * @param length length of the short name to generate
     * @return the input that is converted to the base
     */
    private int convert(final long input, final String projectName, int maxNumber) {
        LOGGER.entering(this.getClass().getName(), "convert", new Object[]{input, projectName, maxNumber});
        int result = 0;
        do {
            result = (int) (input ^ (projectName.hashCode() % maxNumber));
        } while (!isIdUnique(result, projectName));
        LOGGER.entering(this.getClass().getName(), "convert", result);
        return result;
    }

    /**
     * Build a unique String from the project name
     *
     * @param project the project name
     * @param length length of the short name to generate (the short name must
     * be an int to the length cannot be up to 9)
     * @return the unique string
     */
    public int getShortName(final String project, int length) {
        LOGGER.entering(this.getClass().getName(), "getShortName", new Object[]{project, length});
        int id = 0;
        if (length > 9) {
            DoiRuntimeException doiEx = new DoiRuntimeException("The short name cannot be build because the length requested is too big");
            LOGGER.throwing(this.getClass().getName(), "getShortName", doiEx);            
            throw doiEx;
        } else if (this.projectDB.isExistProjectName(project)) {
            // Si le projet a déjà un identifiant on ne le recalcule pas
            id = this.projectDB.getIDFrom(project);
            LOGGER.log(Level.FINE, "The project {0} has already an id : {1}", new Object[]{project, id});
        } else {
            int maxNumber = (int) Math.pow(10.0, length);
            long idRandom = generateId(maxNumber);
            id = convert(idRandom, project, maxNumber);
            LOGGER.log(Level.FINE, "The project {0} has an id : {1}", new Object[]{project, id});
        }
        LOGGER.exiting(this.getClass().getName(), "getShortName", id);
        return id;
    }
        
    /**
     * Check if the generated id is unique (does not already exists) or not. If
     * not add it associated with the project
     *
     * @param idToCheck the identifier to check
     * @param projectName Project associated to the id
     * @return true if the Id is OK, false otherwise
     */
    private synchronized boolean isIdUnique(int idToCheck, String projectName) {
        LOGGER.entering(this.getClass().getName(), "isIdUnique", new Object[]{idToCheck, projectName});
        boolean result;
        if (this.projectDB.isExistID(idToCheck)) {
            result = false;
        } else {
            this.projectDB.addProjectSuffix(idToCheck, projectName);
            result = true;
        }
        LOGGER.exiting(this.getClass().getName(), "isIdUnique", result);
        return result;
    }

}
