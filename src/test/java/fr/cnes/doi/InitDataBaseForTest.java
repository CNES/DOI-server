/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.doi;

import fr.cnes.doi.plugin.impl.db.persistence.service.DatabaseSingleton;
import static org.junit.Assert.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.doi.persistence.DOIDBTest;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.plugin.impl.db.persistence.impl.DOIDbDataAccessServiceImpl;
import fr.cnes.doi.db.model.DOIProject;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.plugin.impl.db.persistence.service.DOIDbDataAccessService;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

/**
 * Class to initialize database with test users and project
 */
public final class InitDataBaseForTest {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(DOIDBTest.class);

    /**
     * Database access
     */
    private static DOIDbDataAccessService das;
    
    /**
     * "Static" class cannot be instantiated
     */
    private InitDataBaseForTest() {}

    /**
     * Init the database for test by adding test user and role.
     */
    public static void init() {

        das = DatabaseSingleton.getInstance().getDatabaseAccess();
        
        // Test LAPD User
        final DOIUser testLdapUser = new DOIUser();
        testLdapUser.setUsername("malapertjc");
        testLdapUser.setAdmin(true);
        testLdapUser.setEmail("doidbuser@mail.com");        
        
        // Test User
        final DOIUser testuser = new DOIUser();
        testuser.setUsername("malapert");
        testuser.setAdmin(true);
        testuser.setEmail("doidbuser@mail.com");

        // Test User
        DOIUser admin = new DOIUser();
        admin.setUsername("admin");
        admin.setAdmin(true);
        admin.setEmail("admin@mail.com");

        // Test User
        DOIUser norole = new DOIUser();
        norole.setUsername("norole");
        norole.setAdmin(false);
        norole.setEmail("norole@mail.com");

        // Test User
        DOIUser kerberos = new DOIUser();
        kerberos.setUsername("doi_kerberos");
        kerberos.setAdmin(false);
        kerberos.setEmail("kerberos@mail.com");

        // Test Project
        final DOIProject  testProject = new DOIProject();
        testProject.setProjectname("CFOSAT");
        testProject.setSuffix(828606);
        
        final DOIProject  testProject2 = new DOIProject();
        testProject2.setProjectname("TestProj");
        testProject2.setSuffix(100378);        

        try {
            close();

            das.addDOIUser(testLdapUser.getUsername(), testLdapUser.isAdmin(), testLdapUser.getEmail());
            // add user
            das.addDOIUser(testuser.getUsername(), testuser.isAdmin(), testuser.getEmail());
            // add admin
            das.addDOIUser(admin.getUsername(), admin.isAdmin(), admin.getEmail());
            // add norole
            das.addDOIUser(norole.getUsername(), norole.isAdmin(), norole.getEmail());
            // add doi_kerberos
            das.addDOIUser(kerberos.getUsername(), kerberos.isAdmin(), kerberos.getEmail());

            // add project
            das.addDOIProject(testProject.getSuffix(), testProject.getProjectname());
            das.addDOIProject(testProject2.getSuffix(), testProject2.getProjectname());
            // assign user to project
            das.addDOIProjectToUser(testuser.getUsername(), testProject.getSuffix());
            das.addDOIProjectToUser(testLdapUser.getUsername(), testProject.getSuffix());
            das.addDOIProjectToUser(testLdapUser.getUsername(), testProject2.getSuffix());            
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
            for (String token : das.getTokens()) {
                das.deleteToken(token);
            }
            
        } catch (DOIDbException e) {
            fail("Please create the database with the following informations:\nURL:" + DoiSettings.
                    getInstance().getString(Consts.DB_URL) + "\nUser: " + DoiSettings.getInstance().
                    getString(Consts.DB_USER) + "\nPwd: " + DoiSettings.getInstance().getSecret(
                    Consts.DB_PWD));
        }
    }

}
