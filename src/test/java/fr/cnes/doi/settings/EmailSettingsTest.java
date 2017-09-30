/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author malapert
 */
public class EmailSettingsTest {

    private static EmailSettings instance;

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() {
        InitSettingsForTest.init();
        instance = EmailSettings.getInstance();
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
        System.out.println("getTlsEnable");
        String expResult = "false";
        String result = instance.getTlsEnable();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAuthUser method, of class EmailSettings.
     */
    @Test
    public void testGetAuthUser() {
        System.out.println("getAuthUser");
        String result = instance.getAuthUser();
        assertNotNull(result);
    }

    /**
     * Test of getAuthPwd method, of class EmailSettings.
     */
    @Test
    public void testGetAuthPwd() {
        System.out.println("getAuthPwd");
        String result = instance.getAuthPwd();
        assertNotNull(result);
    }

    /**
     * Test of getContactAdmin method, of class EmailSettings.
     */
    @Test
    public void testGetContactAdmin() {
        System.out.println("getContactAdmin");
        String expResult = "L-doi-support@cnes.fr";
        String result = instance.getContactAdmin();
        assertEquals(expResult, result);
    }

    /**
     * Test of getInstance method, of class EmailSettings.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        EmailSettings result = EmailSettings.getInstance();
        assertNotNull(result);
    }

    /**
     * Test of setDebug method, of class EmailSettings.
     */
    @Test
    public void testSetDebug() {
        System.out.println("setDebug");
        boolean isEnabled = false;
        instance.setDebug(isEnabled);
        assertTrue(true);
    }

    /**
     * Test of isDebug method, of class EmailSettings.
     */
    @Test
    public void testGetDebug() {
        System.out.println("getDebug");
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
        System.out.println("sendMessage");
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
        System.out.println("getSmtpURL");
        String expResult = "smtp://smtp-relay.gmail.com";
        String result = instance.getSmtpURL();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSmtpProtocol method, of class EmailSettings.
     */
    @Test
    public void testGetSmtpProtocol() {
        System.out.println("getSmtpProtocol");
        String expResult = "SMTP";
        String result = instance.getSmtpProtocol();
        assertEquals(expResult, result);
    }

}
