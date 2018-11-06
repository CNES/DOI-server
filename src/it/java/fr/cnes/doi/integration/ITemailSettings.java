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
package fr.cnes.doi.integration;

import static fr.cnes.doi.AbstractSpec.classTitle;
import static fr.cnes.doi.AbstractSpec.testTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.settings.EmailSettings;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

/**
 * Test class for {@link fr.cnes.doi.settings.EmailSettings}
 * @author Jean-Christophe Malapert
 */
@Category(IntegrationTest.class)
public class ITemailSettings {

    private static EmailSettings instance;

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() {
        InitSettingsForTest.init(InitSettingsForTest.CONFIG_IT_PROPERTIES);
        instance = EmailSettings.getInstance();
        classTitle("EmailSettings");
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
     * Test of getTlsEnable method, of class EmailSettings.
     */
    @Test
    public void testGetTlsEnable() {
        testTitle("testGetTlsEnable");
        String expResult = "false";
        String result = instance.getTlsEnable();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAuthUser method, of class EmailSettings.
     */
    @Test
    public void testGetAuthUser() {
        testTitle("testGetAuthUser");
        String result = instance.getAuthUser();
        assertNotNull(result);
    }

    /**
     * Test of getAuthPwd method, of class EmailSettings.
     */
    @Test
    public void testGetAuthPwd() {
        testTitle("testGetAuthPwd");
        String result = instance.getAuthPwd();
        assertNotNull(result);
    }

    /**
     * Test of getContactAdmin method, of class EmailSettings.
     */
    @Test
    public void testGetContactAdmin() {
        testTitle("testGetContactAdmin");
        String expResult = "L-doi-support@cnes.fr";
        String result = instance.getContactAdmin();
        assertEquals(expResult, result);
    }

    /**
     * Test of getInstance method, of class EmailSettings.
     */
    @Test
    public void testGetInstance() {
        testTitle("testGetInstance");
        EmailSettings result = EmailSettings.getInstance();
        assertNotNull(result);
    }

    /**
     * Test of setDebug method, of class EmailSettings.
     */
    @Test
    public void testSetDebug() {
        testTitle("testSetDebug");
        boolean isEnabled = false;
        instance.setDebug(isEnabled);
        assertTrue(true);
    }

    /**
     * Test of isDebug method, of class EmailSettings.
     */
    @Test
    public void testGetDebug() {
        testTitle("testGetDebug");
        boolean expResult = false;
        boolean result = instance.isDebug();
        assertEquals(expResult, result);
    }

    /**
     * Test of sendMessage method, of class EmailSettings.
     */
    @Test
    public void testSendMessage() {
        testTitle("testSendMessage");
        String subject = "Test";
        String msg = "My message";
        boolean result = instance.sendMessage(subject, msg);
        assertTrue(!result);
    }

    /**
     * Test of getSmtpURL method, of class EmailSettings.
     */
    @Test
    public void testGetSmtpURL() {
        testTitle("testGetSmtpURL");
        String expResult = "smtp://smtp-relay.gmail.com";
        String result = instance.getSmtpURL();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSmtpProtocol method, of class EmailSettings.
     */
    @Test
    public void testGetSmtpProtocol() {
        testTitle("testGetSmtpProtocol");
        String expResult = "SMTP";
        String result = instance.getSmtpProtocol();
        assertEquals(expResult, result);
    }

}
