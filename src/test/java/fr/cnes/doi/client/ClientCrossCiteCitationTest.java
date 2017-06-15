/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.restlet.data.Status;

/**
 *
 * @author malapert
 */
public class ClientCrossCiteCitationTest {
    
    public ClientCrossCiteCitationTest() {
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
     * Test of getStyles method, of class ClientCrossCiteCitation.
     */
    @Test
    public void testGetStyles() {
        System.out.println("getStyles");
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation();
        String expResult = "academy-of-management-review";
        String result;
        try {
            result = instance.getStyles().get(0);
        } catch (ClientCrossCiteException ex) {
            result = "";
        }
        assertEquals(expResult, result);
    }

    /**
     * Test of getLanguages method, of class ClientCrossCiteCitation.
     */
    @Test
    public void testGetLanguages() {
        System.out.println("getLanguages");
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation();
        String expResult = "af-ZA";
        String result;
        try {
            result = instance.getLanguages().get(0);
        } catch (ClientCrossCiteException ex) {
            result = "";
        }
        assertEquals(expResult, result);
    }

    /**
     * Test of getFormat method, of class ClientCrossCiteCitation.
     */
    @Test
    public void testGetFormat() {
        System.out.println("getFormat");
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation();
        String expResult = "Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI ’15. ACM Press. https://doi.org/10.1145/2783446.2783605.\n";
        String result;
        try {
            result = instance.getFormat(doiName, style, language);
        } catch (ClientCrossCiteException ex) {
            result = "";
        }
        assertEquals(expResult, result);
    }
    
    @Test
    public void testHandleBadParameterException() {
        System.out.println("getFormat");
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-revie";
        String language = "af-ZA";
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation();
        Status expResult = Status.CLIENT_ERROR_BAD_REQUEST;
        String result;
        try {
            result = instance.getFormat(doiName, style, language);
        } catch (ClientCrossCiteException ex) {
            assertEquals(expResult, ex.getStatus());
        }
                
    }  
    
    @Test
    public void testHandleBadDOIException() {
        System.out.println("getFormat");
        String doiName = "10.1145/278344";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation();
        Status expResult = Status.CLIENT_ERROR_NOT_FOUND;
        String result;
        try {
            result = instance.getFormat(doiName, style, language);
        } catch (ClientCrossCiteException ex) {
            assertEquals(expResult, ex.getStatus());            
        }
                
    }    
    
}
