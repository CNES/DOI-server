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
public class EmailSettingsTest {
    
    private InputStream inputStream = SettingsSuite.class.getResourceAsStream("/config.properties");
    private EmailSettings instance = EmailSettings.getInstance();    
    
    public EmailSettingsTest() {
        DoiSettings doiSettings = DoiSettings.getInstance();  
        String result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
        String secretKey = System.getProperty("private.key");        
        result = Utils.decrypt(result, secretKey);
        InputStream stream = new ByteArrayInputStream(result.getBytes(Charset.forName("UTF-8")));
        try {
            doiSettings.setPropertiesFile(stream);
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
     * Test of getDebug method, of class EmailSettings.
     */
    @Test
    public void testGetDebug() {
        System.out.println("getDebug");
        boolean expResult = false;
        boolean result = instance.getDebug();
        assertEquals(expResult, result);
    }

    /**
     * Test of sendMessage method, of class EmailSettings.
     */
    @Test
    public void testSendMessage() {
        System.out.println("sendMessage");
        String subject = "Test";
        String msg = "My message";        
        assertTrue(instance.sendMessage(subject, msg));
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
