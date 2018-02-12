/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package fr.cnes.doi.client;

import fr.cnes.doi.CrossCiteSpec;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class ClientCrossCiteCitationTest {
    
    private CrossCiteSpec spec;
        
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
        spec = new CrossCiteSpec();
    }
    
    @After
    public void tearDown() {
        spec.finish();
    }   

    /**
     * Test of getStyles method, of class ClientCrossCiteCitation.
     */
    @Test
    public void testGetStyles() throws Exception {        
        System.out.println("getStyles");
        
        this.spec.createSpec(CrossCiteSpec.Spec.GET_STYLE_200);               

        // create a GET request client API
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        List<String> expResult = CrossCiteSpec.Spec.GET_STYLE_200.getBodyAsList();
        List<String> result = instance.getStyles();
        assertEquals("Test retrieving styles", expResult, result);        


        this.spec.verifySpec(CrossCiteSpec.Spec.GET_STYLE_200);       
    }

    /**
     * Test of getLanguages method, of class ClientCrossCiteCitation.
     */
    @Test
    public void testGetLanguages() throws Exception {
        System.out.println("getLanguages");

        this.spec.createSpec(CrossCiteSpec.Spec.GET_LANGUAGE_200);               
        
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        List<String> expResult = CrossCiteSpec.Spec.GET_LANGUAGE_200.getBodyAsList();
        List<String> result = instance.getLanguages();
        assertEquals("Test retrieving languages", expResult, result);

        this.spec.verifySpec(CrossCiteSpec.Spec.GET_LANGUAGE_200);
    }

    /**
     * Test of getFormat method, of class ClientCrossCiteCitation.
     */
    @Test
    public void testGetFormat() throws Exception {
        System.out.println("getFormat");
        
        this.spec.createSpec(CrossCiteSpec.Spec.GET_FORMAT_200);
        
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        String expResult = CrossCiteSpec.Spec.GET_FORMAT_200.getBody();
        String result = instance.getFormat(doiName, style, language);
        assertEquals("Test retrieving format", expResult, result);

        this.spec.verifySpec(CrossCiteSpec.Spec.GET_FORMAT_200);
    }
    
}
