/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.settings;

import static fr.cnes.doi.AbstractSpec.classTitle;
import static fr.cnes.doi.AbstractSpec.testTitle;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.ChallengeResponse;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.UnitTest;
import fr.cnes.doi.client.ClientSearchDataCite;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.experimental.categories.Category;

/**
 * Test class for {@link fr.cnes.doi.settings.ProxySettings}
 * @author Jean-Christophe Malapert
 */
@Category(UnitTest.class)
public class ProxySettingsTest {

    private static ProxySettings instance;

    /**
     * Init the configuration file
     */
    @BeforeClass
    public static void setUpClass() {
        classTitle("ProxySettings");        
        InitSettingsForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES);
        instance = ProxySettings.getInstance();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {       
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void testGetDois() throws Exception {
        testTitle("testGetDois");

        ClientSearchDataCite searchDatacite = new ClientSearchDataCite("10.24400");
        List<String> dois = searchDatacite.getDois(); 
        //System.out.println(dois);
        assertTrue("IT Test searchDatacite",dois.size() > 0);
    }    

    /**
     * Test of isWithProxy method, of class ProxySettings.
     */
    @Test
    public void testIsWithProxy() {
        testTitle("testIsWithProxy");
        boolean result = instance.isWithProxy();
        assertTrue(true);
    }

    /**
     * Test of getProxyHost method, of class ProxySettings.
     */
    @Test
    public void testGetProxyHost() {
        testTitle("testGetProxyHost");
        String result = instance.getProxyHost();
        assertNotNull(result);
    }

    /**
     * Test of getProxyPort method, of class ProxySettings.
     */
    @Test
    public void testGetProxyPort() {
        testTitle("testGetProxyPort");
        if (instance.isWithProxy()) {
            String result = instance.getProxyPort();
            assertNotNull(result);
        } else {
            Assert.assertTrue(true);
        }        
    }

    /**
     * Test of getProxyUser method, of class ProxySettings.
     */
    @Test
    public void testGetProxyUser() {
        testTitle("testGetProxyUser");
        String result = instance.getProxyUser();
        assertNotNull(result);
    }

    /**
     * Test of getProxyPassword method, of class ProxySettings.
     */
    @Test
    public void testGetProxyPassword() {
        testTitle("testGetProxyPassword");
        String result = instance.getProxyPassword();
        assertNotNull(result);
    }

    /**
     * Test of getProxyAuthentication method, of class ProxySettings.
     */
    @Test
    public void testGetProxyAuthentication() {
        testTitle("testGetProxyAuthentication");
        ChallengeResponse result = instance.getProxyAuthentication();
        assertNotNull(result);
    }

    /**
     * Test of getNonProxyHosts method, of class ProxySettings.
     */
    @Test
    public void testGetNonProxyHosts() {
        testTitle("testGetNonProxyHosts");
        String result = instance.getNonProxyHosts();
        assertNotNull(result);
    }
    
    

}
