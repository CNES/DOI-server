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
public class EmailSettingsTest {
    
    private InputStream inputStream = SettingsSuite.class.getResourceAsStream("/resources/doi.properties");
    private EmailSettings instance = EmailSettings.getInstance();    
    
    public EmailSettingsTest() {
        DoiSettings doiSettings = DoiSettings.getInstance();  
        try {
            doiSettings.setPropertiesFile(inputStream);
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
     * Test of sendEmail method, of class EmailSettings.
     */
    @Test
    public void testSendEmail() {
        System.out.println("sendEmail");
        String subject = "Mysubject";
        String msg = "message";
        instance.sendEmail(subject, msg);
        assertTrue(true);
    }

    /**
     * Test of getHostName method, of class EmailSettings.
     */
    @Test
    public void testGetHostName() {
        System.out.println("getHostName");
        String expResult = "";
        String result = instance.getHostName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPort method, of class EmailSettings.
     */
    @Test
    public void testGetPort() {
        System.out.println("getPort");
        String expResult = "";
        String result = instance.getPort();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTlsEnable method, of class EmailSettings.
     */
    @Test
    public void testGetTlsEnable() {
        System.out.println("getTlsEnable");
        String expResult = "";
        String result = instance.getTlsEnable();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAuthUser method, of class EmailSettings.
     */
    @Test
    public void testGetAuthUser() {
        System.out.println("getAuthUser");
        String expResult = "";
        String result = instance.getAuthUser();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAuthPwd method, of class EmailSettings.
     */
    @Test
    public void testGetAuthPwd() {
        System.out.println("getAuthPwd");
        String expResult = "";
        String result = instance.getAuthPwd();
        assertEquals(expResult, result);
    }

    /**
     * Test of getContactAdmin method, of class EmailSettings.
     */
    @Test
    public void testGetContactAdmin() {
        System.out.println("getContactAdmin");
        String expResult = "";
        String result = instance.getContactAdmin();
        assertEquals(expResult, result);
    }
    
}
