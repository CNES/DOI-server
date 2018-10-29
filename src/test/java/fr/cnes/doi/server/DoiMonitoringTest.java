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
package fr.cnes.doi.server;

import static fr.cnes.doi.AbstractSpec.classTitle;
import static fr.cnes.doi.AbstractSpec.testTitle;
import fr.cnes.doi.UnitTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.restlet.data.Method;

import fr.cnes.doi.services.DoiMonitoring;
import org.junit.experimental.categories.Category;

/**
 * Test class for {@link fr.cnes.doi.services.DoiMonitoring}
 * @author Jean-Christophe Malapert
 */
@Category(UnitTest.class)
public class DoiMonitoringTest {
    
    private final DoiMonitoring instance;
    
    public DoiMonitoringTest() {
        instance = new DoiMonitoring();
    }
    
    @BeforeClass
    public static void setUpClass() {
        classTitle("DoiMonitoring");
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
        testTitle("testRegister");
        Method name = Method.GET;
        String path = "/test";
        String description = "myTest";        
        instance.register(name, path, description);
        assertTrue(true);
    }

    /**
     * Test of addMeasurement method, of class DoiMonitoring.
     */
    @Test
    public void testAddMeasurement() {
        testTitle("testAddMeasurement");
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
        testTitle("testIsRegistered");
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
        testTitle("testGetCurrentMean");
        Method name = Method.POST;
        String path = "/mean";
        float expResult = 9.0F;
        instance.register(name, path, "test");
        instance.addMeasurement(name, path, 10);
        instance.addMeasurement(name, path, 8);
        float result = instance.getCurrentAverage(name, path);
        assertEquals(expResult, result, 0.001);
    }

    /**
     * Test of getDescription method, of class DoiMonitoring.
     */
    @Test
    public void testGetDescription() {
        testTitle("testGetDescription");
        Method name = Method.HEAD;
        String path = "/test";
        instance.register(name, path, "description");
        String expResult = "description";
        String result = instance.getDescription(name, path);
        assertEquals(expResult, result);
    }
    
}
