package fr.cnes.doi;

import static org.junit.Assert.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.doi.persistence.DOIDBTest;
import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.impl.DOIDbDataAccessServiceImpl;
import fr.cnes.doi.persistence.model.DOIProject;
import fr.cnes.doi.persistence.model.DOIUser;
import fr.cnes.doi.persistence.service.DOIDbDataAccessService;

public class InitDataBaseForTest {
	
	private static Logger logger = LoggerFactory.getLogger(DOIDBTest.class);

	private static DOIDbDataAccessService das = new DOIDbDataAccessServiceImpl();;
    
	private static DOIUser testuser;
	private static DOIProject testProject;
	
    /**
     * Init the database for test by adding test user and role.
     */
    public static void init() {
    	
    	// Test User
    	testuser = new DOIUser();
    	testuser.setUsername("malapert");
    	testuser.setAdmin(true);
    	testuser.setEmail("doidbuser@mail.com");
    	
    	// Test Project
    	testProject = new DOIProject();
    	testProject.setProjectname("doiprojecttest");
    	testProject.setSuffix(828606);
    	
		try {
			das.removeDOIUser(testuser.getUsername());
			das.removeDOIProject(testProject.getSuffix());
			
			// add user
			das.addDOIUser(testuser.getUsername(), testuser.getAdmin(), testuser.getEmail());
			// add project
			das.addDOIProject(testProject.getSuffix(), testProject.getProjectname());
			// assign user to project
			das.addDOIProjectToUser(testuser.getUsername(), testProject.getSuffix());
		} catch (DOIDbException e) {
			logger.error("testDoiUsers failed: unexpected exception: ", e);
			fail();
		}
    } 
    
    /**
     * Stops the server.
     */
    public static void close() {
    	
		try {
			das.removeDOIUser(testuser.getUsername());
			das.removeDOIProject(testProject.getSuffix());
		} catch (DOIDbException e) {
			fail();
		}
    }

}
