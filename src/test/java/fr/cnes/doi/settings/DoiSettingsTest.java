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

/**
 *
 * @author malapert
 */
public class DoiSettingsTest {
               
    private InputStream inputStream = SettingsSuite.class.getResourceAsStream("/doi.properties");
    private DoiSettings instance;
    
    public DoiSettingsTest() {
        instance = DoiSettings.getInstance();  
        try {
            instance.setPropertiesFile(inputStream);
        } catch (IOException ex) {
            Logger.getLogger(DoiSettingsTest.class.getName()).log(Level.SEVERE, null, ex);
        }        
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
     * Test of getString method, of class DoiSettings.
     */
    @Test
    public void testGetString_String_String() {
        System.out.println("getString");
        String key = "NoKeyword";
        String defaultValue = "TEST";       
        String expResult = "TEST";
        String result = instance.getString(key, defaultValue);
        assertEquals(expResult, result);
    }

    /**
     * Test of getString method, of class DoiSettings.
     */
    @Test
    public void testGetString_String() {
        System.out.println("getString");
        String key = Consts.APP_NAME;        
        String expResult = "Data Object Identifier Server";
        String result = instance.getString(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of getSecret method, of class DoiSettings.
     */
    @Test
    public void testGetSecret() {
        System.out.println("getSecret");
        String key = Consts.INIST_LOGIN;       
        String expResult = "myLoginDoi";
        String result = instance.getSecret(key);
    }

    /**
     * Test of getInt method, of class DoiSettings.
     */
    @Test
    public void testGetInt_String() {
        System.out.println("getInt");
        String key = Consts.SERVER_HTTP_PORT;        
        int expResult = 8182;
        int result = instance.getInt(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of getInt method, of class DoiSettings.
     */
    @Test
    public void testGetInt_String_String() {
        System.out.println("getInt");
        String key = "NoKeyword";
        String defaultValue = "50";       
        int expResult = 50;
        int result = instance.getInt(key, defaultValue);
        assertEquals(expResult, result);
    }

    /**
     * Test of getBoolean method, of class DoiSettings.
     */
    @Test
    public void testGetBoolean() {
        System.out.println("getBoolean");
        String key = Consts.SERVER_PROXY_USED;        
        boolean expResult = false;
        boolean result = instance.getBoolean(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of getLong method, of class DoiSettings.
     */
    @Test
    public void testGetLong_String() {
        System.out.println("getLong");
        String key = Consts.PROXY_PORT;
        Long expResult = 8888L;
        Long result = instance.getLong(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of getLong method, of class DoiSettings.
     */
    @Test
    public void testGetLong_String_String() {
        System.out.println("getLong");
        String key = Consts.PROXY_PORT;
        String defaultValue = "";
        Long expResult = 8888L;
        Long result = instance.getLong(key, defaultValue);
        assertEquals(expResult, result);
    }

    /**
     * Test of displayConfigFile method, of class DoiSettings.
     */
    @Test
    public void testDisplayConfigFile() {
        System.out.println("displayConfigFile");
        instance.displayConfigFile();
        assertTrue(true);
    }
    
}
