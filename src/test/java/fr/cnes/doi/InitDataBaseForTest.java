package fr.cnes.doi;

import static org.junit.Assert.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.doi.persistence.DOIDBTest;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.plugin.impl.db.persistence.impl.DOIDbDataAccessServiceImpl;
import fr.cnes.doi.plugin.impl.db.persistence.model.DOIProject;
import fr.cnes.doi.utils.DOIUser;
import fr.cnes.doi.plugin.impl.db.persistence.service.DOIDbDataAccessService;

public class InitDataBaseForTest {

    private static Logger logger = LoggerFactory.getLogger(DOIDBTest.class);

    private static DOIDbDataAccessService das = new DOIDbDataAccessServiceImpl(
            InitDataBaseForTest.class.getClassLoader().getResource("config-test.properties").
                    getFile());

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

        // Test User
        DOIUser admin = new DOIUser();
        admin.setUsername("admin");
        admin.setAdmin(true);
        admin.setEmail("admin@mail.com");

        // Test Project
        testProject = new DOIProject();
        testProject.setProjectname("CFOSAT");
        testProject.setSuffix(828606);

        try {
            das.removeDOIUser(testuser.getUsername());
            das.removeDOIUser(admin.getUsername());
            das.removeDOIProject(testProject.getSuffix());

            // add user
            das.addDOIUser(testuser.getUsername(), testuser.getAdmin(), testuser.getEmail());
            // add admin
            das.addDOIUser(admin.getUsername(), admin.getAdmin(), admin.getEmail());
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
            for (DOIUser user : das.getAllDOIusers()) {
                das.removeDOIUser(user.getUsername());
            }
            for (DOIProject project : das.getAllDOIProjects()) {
                das.removeDOIProject(project.getSuffix());
            }
        } catch (DOIDbException e) {
            fail();
        }
    }

}
