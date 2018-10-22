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
import static fr.cnes.doi.client.BaseClient.DATACITE_MOCKSERVER_PORT;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.mockserver.junit.MockServerRule;

/**
 * Test of Citation resources
 * @author Jean-Christophe Malapert
 * @see fr.cnes.doi.resource.citation
 */
public class ClientCrossCiteCitationTest {

    /**
     * Stub on Cross Cite Server.
     */
    private CrossCiteSpec crossCiteServerStub;

    public ClientCrossCiteCitationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("------ TEST ClientCrossCiteCitation ------");        
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        crossCiteServerStub = new CrossCiteSpec(DATACITE_MOCKSERVER_PORT);
    }

    @After
    public void tearDown() {
        crossCiteServerStub.finish();
    }

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    /**
     * Test of getStyles method, of class ClientCrossCiteCitation.
     * @throws java.lang.Exception
     * @see fr.cnes.doi.resource.citation.StyleCitationResource
     */
    @Test
    public void testGetStyles() throws Exception {
        System.out.println("TEST getStyles");

        // Create the sub on CrossCite
        this.crossCiteServerStub.createSpec(CrossCiteSpec.Spec.GET_STYLE_200);

        // Create a GET request client API on DOIServer. DOIServer calls the CrossCite stub
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        List<String> expResult = CrossCiteSpec.Spec.GET_STYLE_200.getBodyAsList();
        List<String> result = instance.getStyles();
        assertEquals("Test retrieving styles", expResult, result);

        // Check if the stub has been requested        
        this.crossCiteServerStub.verifySpec(CrossCiteSpec.Spec.GET_STYLE_200);
    }

    /**
     * Test of getLanguages method, of class ClientCrossCiteCitation.
     * @throws java.lang.Exception
     * @see fr.cnes.doi.resource.citation.LanguageCitationResource
     */
    @Test
    public void testGetLanguages() throws Exception {
        System.out.println("TEST getLanguages");

        // Create the sub on CrossCite        
        this.crossCiteServerStub.createSpec(CrossCiteSpec.Spec.GET_LANGUAGE_200);

        // Create a GET request client API on DOIServer. DOIServer calls the CrossCite stub        
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        List<String> expResult = CrossCiteSpec.Spec.GET_LANGUAGE_200.getBodyAsList();
        List<String> result = instance.getLanguages();
        assertEquals("Test retrieving languages", expResult, result);

        // Check if the stub has been requested
        this.crossCiteServerStub.verifySpec(CrossCiteSpec.Spec.GET_LANGUAGE_200);
    }

    /**
     * Test of getFormat method, of class ClientCrossCiteCitation.
     * @throws java.lang.Exception
     * @see fr.cnes.doi.resource.citation.FormatCitationResource
     */
    @Test
    public void testGetFormat() throws Exception {
        System.out.println("TEST getFormat");

        // Create the sub on CrossCite        
        this.crossCiteServerStub.createSpec(CrossCiteSpec.Spec.GET_FORMAT_200);

        // Create a GET request client API on DOIServer. DOIServer calls the CrossCite stub        
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        String expResult = CrossCiteSpec.Spec.GET_FORMAT_200.getBody();
        String result = instance.getFormat(doiName, style, language);
        assertEquals("Test retrieving format", expResult, result);

        // Check if the stub has been requested        
        this.crossCiteServerStub.verifySpec(CrossCiteSpec.Spec.GET_FORMAT_200);
    }

}
