/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import fr.cnes.doi.utils.Utils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
               
    private InputStream inputStream = SettingsSuite.class.getResourceAsStream("/config.properties");
    private DoiSettings instance;
    
    public DoiSettingsTest() {
        instance = DoiSettings.getInstance();  
        String result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
        String secretKey = System.getProperty("private.key");        
        result = Utils.decrypt(result, secretKey);
        InputStream stream = new ByteArrayInputStream(result.getBytes(Charset.forName("UTF-8")));
        try {
            instance.setPropertiesFile(stream);
        } catch (IOException ex) {
            Logger.getLogger(EmailSettingsTest.class.getName()).log(Level.SEVERE, null, ex);
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
        String key = Consts.COPYRIGHT;        
        String expResult = "Copyright 2017 CNES";
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
        String result = instance.getSecret(key);
        assertNotNull(result);
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
        String key = Consts.SERVER_PROXY_PORT;
        Long expResult = 8050L;
        Long result = instance.getLong(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of getLong method, of class DoiSettings.
     */
    @Test
    public void testGetLong_String_String() {
        System.out.println("getLong");
        String key = Consts.SERVER_PROXY_PORT;
        String defaultValue = "";
        Long expResult = 8050L;
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
