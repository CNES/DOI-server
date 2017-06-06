/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.restlet.data.ChallengeResponse;

/**
 *
 * @author malapert
 */
public class ProxySettingsTest {
    
    private InputStream inputStream = SettingsSuite.class.getResourceAsStream("/doi.properties");
    private ProxySettings instance = ProxySettings.getInstance();        
    
    public ProxySettingsTest() {
        DoiSettings doiSettings = DoiSettings.getInstance();  
        try {
            doiSettings.setPropertiesFile(inputStream);
        } catch (IOException ex) {
            Logger.getLogger(EmailSettingsTest.class.getName()).log(Level.SEVERE, null, ex);
        }   
        instance.init(doiSettings);                
    }    
    
    @BeforeClass
    public static void setUpClass() {
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
     * Test of reset method, of class ProxySettings.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        ProxySettings instance = null;
        instance.reset();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isWithProxy method, of class ProxySettings.
     */
    @Test
    public void testIsWithProxy() {
        System.out.println("isWithProxy");
        ProxySettings instance = null;
        boolean expResult = false;
        boolean result = instance.isWithProxy();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWithProxy method, of class ProxySettings.
     */
    @Test
    public void testSetWithProxy() {
        System.out.println("setWithProxy");
        boolean withProxy = false;
        ProxySettings instance = null;
        instance.setWithProxy(withProxy);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProxyHost method, of class ProxySettings.
     */
    @Test
    public void testGetProxyHost() {
        System.out.println("getProxyHost");
        ProxySettings instance = null;
        String expResult = "";
        String result = instance.getProxyHost();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProxyPort method, of class ProxySettings.
     */
    @Test
    public void testGetProxyPort() {
        System.out.println("getProxyPort");
        ProxySettings instance = null;
        String expResult = "";
        String result = instance.getProxyPort();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProxyUser method, of class ProxySettings.
     */
    @Test
    public void testGetProxyUser() {
        System.out.println("getProxyUser");
        ProxySettings instance = null;
        String expResult = "";
        String result = instance.getProxyUser();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProxyPassword method, of class ProxySettings.
     */
    @Test
    public void testGetProxyPassword() {
        System.out.println("getProxyPassword");
        ProxySettings instance = null;
        String expResult = "";
        String result = instance.getProxyPassword();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProxyAuthentication method, of class ProxySettings.
     */
    @Test
    public void testGetProxyAuthentication() {
        System.out.println("getProxyAuthentication");
        ProxySettings instance = null;
        ChallengeResponse expResult = null;
        ChallengeResponse result = instance.getProxyAuthentication();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNonProxyHosts method, of class ProxySettings.
     */
    @Test
    public void testGetNonProxyHosts() {
        System.out.println("getNonProxyHosts");
        ProxySettings instance = null;
        String expResult = "";
        String result = instance.getNonProxyHosts();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
