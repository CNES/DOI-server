/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author malapert
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({fr.cnes.doi.settings.ProxySettingsTest.class, fr.cnes.doi.settings.JettySettingsTest.class, fr.cnes.doi.settings.EmailSettingsTest.class, fr.cnes.doi.settings.DoiSettingsTest.class})
public class SettingsSuite {

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
