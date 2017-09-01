/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils;

import fr.cnes.doi.security.UtilsCryptography;
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
public class UtilsTest {
    
    public UtilsTest() {
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
     * Test of isEmpty method, of class Utils.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        CharSequence cs = null;
        boolean expResult = true;
        boolean result = Utils.isEmpty(cs);
        assertEquals(expResult, result);
    }

    /**
     * Test of decrypt method, of class Utils.
     */
    @Test
    public void testDecrypt() {
        System.out.println("decrypt");
        String encryptedInput = "6YTGxcaZ3b/qFbzECfnvjw==";
        String expResult = "Hello World !";
        String result = UtilsCryptography.decrypt(encryptedInput);
        assertEquals(expResult, result);
    }

    /**
     * Test of encrypt method, of class Utils.
     */
    @Test
    public void testEncrypt() {
        System.out.println("encrypt");
        String str = "Hello World !";
        String expResult = "6YTGxcaZ3b/qFbzECfnvjw==";
        String result = UtilsCryptography.encrypt(str);
        assertEquals(expResult, result);
    }
    
}
