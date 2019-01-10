package fr.cnes.doi.persistence.service;


import java.util.List;

import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.model.DOIMetadata;
import fr.cnes.doi.persistence.model.DOIProject;
import fr.cnes.doi.persistence.model.DOIUser;


public interface DOIDbDataAccessService {
    
	/** Get all DOI users from data base 
	 * @throws DOIDbException */
	public List<DOIUser> getAllDOIusers() throws DOIDbException;
	
	/** Get all DOI projects from data base 
	 * @throws DOIDbException */
	public List<DOIProject> getAllDOIProjects() throws DOIDbException;
	
	/** Get Projects assigned to a given project 
	 * @throws DOIDbException */
	public List<DOIProject> getAllDOIProjectsForUser(String username) throws DOIDbException;
	
	/** Get Users of a given project 
	 * @throws DOIDbException */
	public List<DOIUser> getAllDOIUsersForProject(int suffix) throws DOIDbException;
	
	/** Add a DOI user 
	 * @throws DOIDbException */
	public void addDOIUser(String username, Boolean admin) throws DOIDbException;
	
	/** Add a DOI user 
	 * @throws DOIDbException */
	public void addDOIUser(String username, Boolean admin, String email) throws DOIDbException;
	
	
	/** Remove a DOI user 
	 * @throws DOIDbException */
	public void removeDOIUser(String username) throws DOIDbException;
	
	/** Remove a DOI project 
	 * @throws DOIDbException */
	public void removeDOIProject(int suffix) throws DOIDbException;
	
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
	
	/** Check if user is an admin user
	 * @throws DOIDbException
	 */
	public boolean isAdmin(String username) throws DOIDbException;

	
	/** Check if user exists in database
	 * @throws DOIDbException
	 * @return null if user does not exist
	 */
	public boolean isUserExist(String username) throws DOIDbException;

	
	
	/** Rename DOI project
	 *  @throws DOIDbException
	 */
	public void renameDOIProject(int suffix, String newprojectname) throws DOIDbException;
	
	/**
	 * Get Project name
	 */
	public String getDOIProjectName(int suffix) throws DOIDbException;
	
	
	/**
	 * Add token
	 */
	public void addToken(String token)  throws DOIDbException;
	
	/**
	 * Delete token
	 */
	public void deleteToken(String token)  throws DOIDbException;
	
	/**
	 * Get tokens
	 */
	public List<String> getTokens()  throws DOIDbException;
	
	
	
}
