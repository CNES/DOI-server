/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.UnitTest;
import org.junit.experimental.categories.Category;

/**
 * Test class for {@link fr.cnes.doi.settings.DoiSettings}
 * @author Jean-Christophe Malapert
 */
@Category(UnitTest.class)
public class DoiSettingsTest {

    private static DoiSettings instance;

    @BeforeClass
    public static void setUpClass() {
        InitSettingsForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES);
        instance = DoiSettings.getInstance();
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
        String key = Consts.COPYRIGHT;
        String expResult = "Copyright 2017-2021 CNES";
        String result = instance.getString(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of getSecret method, of class DoiSettings.
     */
    @Test
    public void testGetSecret() {
        String key = Consts.INIST_LOGIN;
        String result = instance.getSecret(key);
        assertNotNull(result);
    }

    /**
     * Test of getInt method, of class DoiSettings.
     */
    @Test
    public void testGetInt_String() {
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
        String key = "NoKeyword";
        String defaultValue = "50";
        int expResult = 50;
        int result = instance.getInt(key, defaultValue);
        assertEquals(expResult, result);
    }


    /**
     * Test of getLong method, of class DoiSettings.
     */
    @Test
    public void testGetLong_String() {
        String key = Consts.SERVER_HTTPS_PORT;
        Long expResult = 8183L;
        Long result = instance.getLong(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of getLong method, of class DoiSettings.
     */
    @Test
    public void testGetLong_String_String() {
        String key = Consts.JETTY_RESPONSE_HEADER_SIZE;
        String defaultValue = "8050";
        Long expResult = 8050L;
        Long result = instance.getLong(key, defaultValue);
        assertEquals(expResult, result);
    }    
    
}
