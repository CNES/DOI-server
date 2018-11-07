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

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.UnitTest;
import fr.cnes.doi.security.UtilsCryptography;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.experimental.categories.Category;

/**
 * Test class for {@link fr.cnes.doi.utils.Utils}
 * @author Jean-Christophe Malapert
 */
@Category(UnitTest.class)
public class UtilsTest {
    
    public UtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        InitSettingsForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES); 
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
        String str = "Hello World !";
        String expResult = "6YTGxcaZ3b/qFbzECfnvjw==";
        String result = UtilsCryptography.encrypt(str);
        assertEquals(expResult, result);
    }    
}
