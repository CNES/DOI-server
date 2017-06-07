/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.server;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.restlet.data.Method;

/**
 *
 * @author malapert
 */
public class DoiMonitoringTest {
    
    private DoiMonitoring instance;
    
    public DoiMonitoringTest() {
        
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
     * Test of register method, of class DoiMonitoring.
     */
    @Test
    public void testRegister() {
        System.out.println("register");
        Method name = Method.GET;
        String path = "/test";
        String description = "myTest";
        instance = new DoiMonitoring();
        instance.register(name, path, description);
        assertTrue(true);
    }

    /**
     * Test of addMeasurement method, of class DoiMonitoring.
     */
    @Test
    public void testAddMeasurement() {
        System.out.println("addMeasurement");
        Method name = Method.GET;
        String path = "/test";
        float duration = 10.0F;
        instance.addMeasurement(name, path, duration);
        assertTrue(true);
    }

    /**
     * Test of isRegistered method, of class DoiMonitoring.
     */
    @Test
    public void testIsRegistered() {
        System.out.println("isRegistered");
        Method name = Method.POST;
        String path = "/registered";
        instance.register(name, path, "my description");
        boolean expResult = true;
        boolean result = instance.isRegistered(name, path);
        assertEquals(expResult, result);
    }

    /**
     * Test of getCurrentMean method, of class DoiMonitoring.
     */
    @Test
    public void testGetCurrentMean() {
        System.out.println("getCurrentMean");
        Method name = Method.POST;
        String path = "/mean";
        float expResult = 9.0F;
        instance.register(name, path, "test");
        instance.addMeasurement(name, path, 10);
        instance.addMeasurement(name, path, 8);
        float result = instance.getCurrentMean(name, path);
        assertEquals(expResult, result, 0.001);
    }

    /**
     * Test of getDescription method, of class DoiMonitoring.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        Method name = Method.HEAD;
        String path = "/test";
        instance.register(name, path, "description");
        String expResult = "description";
        String result = instance.getDescription(name, path);
        assertEquals(expResult, result);
    }
    
}
