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
import org.restlet.data.Form;
import org.restlet.representation.Representation;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class MediaResourceTest {
    
    public MediaResourceTest() {
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
     * Test of getMedias method, of class MediaResource.
     */
    @Test
    public void testGetMedias() {
        System.out.println("getMedias");
        MediaResource instance = new MediaResource();
        Representation expResult = null;
        Representation result = instance.getMedias();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createMedia method, of class MediaResource.
     */
    @Test
    public void testCreateMedia() {
        System.out.println("createMedia");
        Form mediaForm = null;
        MediaResource instance = new MediaResource();
        Representation expResult = null;
        Representation result = instance.createMedia(mediaForm);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
