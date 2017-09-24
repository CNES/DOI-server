/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite of tests
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({fr.cnes.doi.application.DoiCrossCiteApplicationTest.class, fr.cnes.doi.application.AdminApplicationTest.class, fr.cnes.doi.application.DoiMdsApplicationTest.class})
public class ApplicationSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
