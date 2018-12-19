package fr.cnes.doi.persistence.service;


import java.util.List;

import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.model.DOIMetadata;
import fr.cnes.doi.persistence.model.DOIProject;
import fr.cnes.doi.persistence.model.DOIUser;


public interface DOIDataAccessService {
    
	/** Get all DOI users from data base 
	 * @throws DOIDbException */
	public List<DOIUser> getAllDOIusers() throws DOIDbException;
	
	/** Get all DOI projects from data base 
	 * @throws DOIDbException */
	public List<DOIProject> getAllDOIProjects() throws DOIDbException;
	
	/** Get Projects assigned to a given project 
	 * @throws DOIDbException */
	public List<DOIProject> getAllDOIProjectsForUser(DOIUser user) throws DOIDbException;
	
	/** Get Users of a given project 
	 * @throws DOIDbException */
	public List<DOIUser> getAllDOIUsersForProject(DOIProject project) throws DOIDbException;
	
	/** Add a DOI user 
	 * @throws DOIDbException */
	public void addDOIUser(String username, Boolean admin) throws DOIDbException;
	
	/** Add a DOI project 
	 * @throws DOIDbException */
	public void addDOIProject(int suffix, String projectname) throws DOIDbException;
	
	
	/** Assign a DOI project to a user
	 * @throws DOIDbException */
	public void addDOIProjectToUser(String username, int suffix) throws DOIDbException;
	
	
	/** Remove a DOI project from a user
	 * @throws DOIDbException */
	public void removeDOIProjectFromUser(String username, int suffix) throws DOIDbException;
	
	
	/** Add admin right to a user
	 * @throws DOIDbException
	 */
	public void setAdmin(String username) throws DOIDbException;
	
	/** Remove admin right from a user
	 * @throws DOIDbException
	 */
	public void unsetAdmin(String username) throws DOIDbException;
	
	
	/** Rename DOI project
	 *  @throws DOIDbException
	 */
	public void renameDOIProject(int suffix, String newprojectname) throws DOIDbException;
	
	/**
	 * Get all DOI metada 
	 * @return 
	 */
	public List<DOIMetadata> getAllDOIMetadata() throws DOIDbException;
	
}
