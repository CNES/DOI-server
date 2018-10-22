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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.exception.MailingException;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * Test class for {@link fr.cnes.doi.settings.EmailSettings}
 * @author Jean-Christophe Malapert
 */
public class EmailSettingsTest {

    private static EmailSettings instance;

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() {
        InitSettingsForTest.init();
        instance = EmailSettings.getInstance();
        System.out.println("------ TEST EmailSettings ------");        
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
        System.out.println("TEST: GetTlsEnable");
        String expResult = "false";
        String result = instance.getTlsEnable();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAuthUser method, of class EmailSettings.
     */
    @Test
    public void testGetAuthUser() {
        System.out.println("TEST: GetAuthUser");
        String result = instance.getAuthUser();
        assertNotNull(result);
    }

    /**
     * Test of getAuthPwd method, of class EmailSettings.
     */
    @Test
    public void testGetAuthPwd() {
        System.out.println("TEST: GetAuthPwd");
        String result = instance.getAuthPwd();
        assertNotNull(result);
    }

    /**
     * Test of getContactAdmin method, of class EmailSettings.
     */
    @Test
    public void testGetContactAdmin() {
        System.out.println("TEST: GetContactAdmin");
        String expResult = "L-doi-support@cnes.fr";
        String result = instance.getContactAdmin();
        assertEquals(expResult, result);
    }

    /**
     * Test of getInstance method, of class EmailSettings.
     */
    @Test
    public void testGetInstance() {
        System.out.println("TEST: GetInstance");
        EmailSettings result = EmailSettings.getInstance();
        assertNotNull(result);
    }

    /**
     * Test of setDebug method, of class EmailSettings.
     */
    @Test
    public void testSetDebug() {
        System.out.println("TEST: SetDebug");
        boolean isEnabled = false;
        instance.setDebug(isEnabled);
        assertTrue(true);
    }

    /**
     * Test of isDebug method, of class EmailSettings.
     */
    @Test
    public void testGetDebug() {
        System.out.println("TEST: GetDebug");
        boolean expResult = false;
        boolean result = instance.isDebug();
        assertEquals(expResult, result);
    }

    /**
     * Test of sendMessage method, of class EmailSettings.
     * @throws fr.cnes.doi.exception.MailingException - Host not found
     */
    @Test
    public void testSendMessage() throws MailingException {
        System.out.println("TEST: SendMessage");
        exceptions.expect(MailingException.class);
        exceptions.expectMessage("The SMTP server cannot be reached");
        String subject = "Test";
        String msg = "My message";
        boolean result = instance.sendMessage(subject, msg);
        assertTrue(result);
    }

    /**
     * Test of getSmtpURL method, of class EmailSettings.
     */
    @Test
    public void testGetSmtpURL() {
        System.out.println("TEST: GetSmtpURL");
        String expResult = "smtp://smtp-relay.gmail.com";
        String result = instance.getSmtpURL();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSmtpProtocol method, of class EmailSettings.
     */
    @Test
    public void testGetSmtpProtocol() {
        System.out.println("TEST: GetSmtpProtocol");
        String expResult = "SMTP";
        String result = instance.getSmtpProtocol();
        assertEquals(expResult, result);
    }

}
