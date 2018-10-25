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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.ChallengeResponse;

import fr.cnes.doi.InitSettingsForTest;

/**
 * Test class for {@link fr.cnes.doi.settings.ProxySettings}
 * @author Jean-Christophe Malapert
 */
public class ProxySettingsTest {

    private static ProxySettings instance;

    /**
     * Init the configuration file
     */
    @BeforeClass
    public static void setUpClass() {
        InitSettingsForTest.init();
        instance = ProxySettings.getInstance();
        classTitle("ProxySettings");
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

    /**
     * Test of isWithProxy method, of class ProxySettings.
     */
    @Test
    public void testIsWithProxy() {
        System.out.println("TEST: IsWithProxy");
        boolean expResult = false;
        boolean result = instance.isWithProxy();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProxyHost method, of class ProxySettings.
     */
    @Test
    public void testGetProxyHost() {
        System.out.println("TEST: GetProxyHost");
        String expResult = "proxy-HTTP2.cnes.fr";
        String result = instance.getProxyHost();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProxyPort method, of class ProxySettings.
     */
    @Test
    public void testGetProxyPort() {
        System.out.println("TEST: GetProxyPort");
        String expResult = "8050";
        String result = instance.getProxyPort();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProxyUser method, of class ProxySettings.
     */
    @Test
    public void testGetProxyUser() {
        System.out.println("TEST: GetProxyUser");
        String result = instance.getProxyUser();
        assertNotNull(result);
    }

    /**
     * Test of getProxyPassword method, of class ProxySettings.
     */
    @Test
    public void testGetProxyPassword() {
        System.out.println("TEST: GetProxyPassword");
        String result = instance.getProxyPassword();
        assertNotNull(result);
    }

    /**
     * Test of getProxyAuthentication method, of class ProxySettings.
     */
    @Test
    public void testGetProxyAuthentication() {
        System.out.println("TEST: GetProxyAuthentication");
        ChallengeResponse result = instance.getProxyAuthentication();
        assertNotNull(result);
    }

    /**
     * Test of getNonProxyHosts method, of class ProxySettings.
     */
    @Test
    public void testGetNonProxyHosts() {
        System.out.println("TEST: GetNonProxyHosts");
        String expResult = "";
        String result = instance.getNonProxyHosts();
        assertEquals(expResult, result);
    }

}
