/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.restlet.representation.Representation;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class MetadataResourceTest {
    
    public MetadataResourceTest() {
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
     * Test of getMetadata method, of class MetadataResource.
     */
    @Test
    public void testGetMetadata() {
        System.out.println("getMetadata");
        MetadataResource instance = new MetadataResource();
        Representation expResult = null;
        Representation result = instance.getMetadata();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteMetadata method, of class MetadataResource.
     */
    @Test
    public void testDeleteMetadata() {
        System.out.println("deleteMetadata");
        MetadataResource instance = new MetadataResource();
        Representation expResult = null;
        Representation result = instance.deleteMetadata();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
