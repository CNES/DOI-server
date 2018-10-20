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
        System.out.println("------ TEST Utils ------");        
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
        System.out.println("TEST: IsEmpty");
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
        System.out.println("TEST: Decrypt");
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
        System.out.println("TEST: Encrypt");
        String str = "Hello World !";
        String expResult = "6YTGxcaZ3b/qFbzECfnvjw==";
        String result = UtilsCryptography.encrypt(str);
        assertEquals(expResult, result);
    }
    
}
