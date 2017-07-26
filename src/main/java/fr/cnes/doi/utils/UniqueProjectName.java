/**
 * 
 */
package fr.cnes.doi.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

/**
 * Utils class to generate a unique number from the project name
 *
 */
public class UniqueProjectName {

	/**
	 * Default file if the path is not defined in the configuration file
	 */
	private static final String DEFAULT_CACHE_FILE = "data/projects.conf";

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

	/**
	 * Configuration of the projects identifiers
	 */
	private String projectConf;

	/**
	 * Mapping between the identifiers and the projects
	 */
	private Map<Integer, String> idProjMap = new HashMap<>();
	/**
	 * Mapping between the projects and the identifier
	 */
	private Map<String, Integer> projIdMap = new HashMap<>();

	/**
	 * Constructor
	 */
	public UniqueProjectName() {
		String path = DoiSettings.getInstance().getString(Consts.PROJECT_CONF_PATH);
		if (path == null) {
			path = UniqueProjectName.DEFAULT_CACHE_FILE;
		}

		init(path);
	}

	/**
	 * Init the configuration with the configuration file. If the given file
	 * does not exist a new file will be created. The file contains the mapping
	 * between the project name and the identifiers
	 * 
	 * @param projectConf
	 * 
	 */
	public void init(String projectConf) {
		this.projectConf = projectConf;
		File projConfFile = new File(projectConf);
		try {
			// If the file exists, load it
			if (projConfFile.exists()) {
				LOGGER.log(Level.FINEST, "Cache file exists : " + projConfFile.getAbsolutePath());

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
								LOGGER.log(Level.WARNING,
										"La ligne " + line + " n'est pas formattée de manière attendue");
							} else {
								String projectName = split[0];
								int id = Integer.valueOf(split[1]);
								this.idProjMap.put(id, projectName);
								this.projIdMap.put(projectName, id);
							}
						}
					}
				}
			} else {

				// Init the config file
				LOGGER.log(Level.FINEST, "Cache file does not exist, create it : " + projConfFile.getAbsolutePath());
				projConfFile.createNewFile();
				Files.write(projConfFile.toPath(), "Project Name;Id\n".getBytes(), StandardOpenOption.APPEND);

			}

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Cannot access the cache file for the mapping projects/id " + projectConf, e);
		}
	}

	/**
	 * Creates an Id with an uniform distribution.
	 * 
	 * @param maxNumber
	 *            Number max to generate
	 *
	 * @return the Id
	 */
	private int generateId(int maxNumber) {
		final Random rand = new Random();

		return rand.nextInt(maxNumber);
	}

	/**
	 * Convert the project name into unique long
	 *
	 * @param input
	 *            Identifier to convert
	 * @param projectName
	 *            the project name to convert
	 * @param maxNumber
	 *            Number max to generate
	 * @param length
	 *            length of the short name to generate
	 * @return the input that is converted to the base
	 */
	private int convert(final long input, final String projectName, int maxNumber) {
		int result = 0;
		do {
			result = (int) (input ^ (projectName.hashCode() % maxNumber));
		} while (!isIdUnique(result, projectName));

		return result;
	}

	/**
	 * Build a unique String from the project name
	 * 
	 * @param project
	 *            the project name
	 * @param length
	 *            length of the short name to generate (the short name must be
	 *            an int to the length cannot be up to 9)
	 * @return the unique string
	 */
	public int getShortName(final String project, int length) {

		int id = 0;
		if (length > 9) {
			throw new DoiRuntimeException("The short name cannot be built because the length requested is too big");
		} else if (this.projIdMap.containsKey(project)) {
			// Si le projet a déjà un identifiant on ne le recalcule pas
			id = this.projIdMap.get(project);
			LOGGER.log(Level.INFO, "The project " + project + " already has an id : " + id);

		}

		else {
			int maxNumber = (int) Math.pow(10.0, length);

			long idRandom = generateId(maxNumber);

			id = convert(idRandom, project, maxNumber);
		}

		return id;
	}

	/**
	 * Check if the generated id is unique (does not already exists) or not. If
	 * not add it associated with the project
	 * 
	 * @param idToCheck
	 *            the identifier to check
	 * @param projectName
	 *            Project associated to the id
	 * @return true if the Id is OK, false otherwise
	 */
	private synchronized boolean isIdUnique(int idToCheck, String projectName) {

		boolean result = true;
		if (this.idProjMap.containsKey(idToCheck)) {
			result = false;
		} else {
			idProjMap.put(idToCheck, projectName);
			this.projIdMap.put(projectName, idToCheck);
			String line = projectName + ";" + idToCheck + "\n";
			try {
				Files.write(new File(this.projectConf).toPath(), line.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE,
						"The id " + idToCheck + " of the project " + projectName + "cannot be saved in the file", e);
			}
		}

		return result;
	}

}