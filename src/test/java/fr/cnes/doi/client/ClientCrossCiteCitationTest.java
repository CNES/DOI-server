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

import static fr.cnes.doi.AbstractSpec.classTitle;
import static fr.cnes.doi.AbstractSpec.testTitle;
import fr.cnes.doi.CrossCiteSpec;
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.UnitTest;
import static fr.cnes.doi.client.BaseClient.DATACITE_MOCKSERVER_PORT;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

/**
 * Test of Citation resources
 * @author Jean-Christophe Malapert
 * @see fr.cnes.doi.resource.citation
 */
@Category(UnitTest.class)
public class ClientCrossCiteCitationTest {

    /**
     * Stub on Cross Cite Server.
     */
    private static CrossCiteSpec crossCiteServerStub;

    public ClientCrossCiteCitationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        classTitle("ClientCrossCiteCitation");
        InitSettingsForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES);
        crossCiteServerStub = new CrossCiteSpec(DATACITE_MOCKSERVER_PORT);
    }

    @AfterClass
    public static void tearDownClass() {
        crossCiteServerStub.finish();
    }

    @Before
    public void setUp() {        
        crossCiteServerStub.reset();        
    }

    @After
    public void tearDown() {
        
    }
    
    @Rule
    public ExpectedException exceptions = ExpectedException.none();    

    /**
     * Test of getStyles method, of class ClientCrossCiteCitation.
     * @throws java.lang.Exception
     * @see fr.cnes.doi.resource.citation.StyleCitationResource
     */
    @Test
    public void testGetStyles() throws Exception {
        testTitle("testGetStyles");

        // Create the sub on CrossCite
        crossCiteServerStub.createSpec(CrossCiteSpec.Spec.GET_STYLE_200);

        // Create a GET request client API on DOIServer. DOIServer calls the CrossCite stub
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        List<String> expResult = CrossCiteSpec.Spec.GET_STYLE_200.getBodyAsList();
        List<String> result = instance.getStyles();
        assertEquals("Test retrieving styles", expResult, result);

        // Check if the stub has been requested        
        crossCiteServerStub.verifySpec(CrossCiteSpec.Spec.GET_STYLE_200);
    }

    /**
     * Test of getLanguages method, of class ClientCrossCiteCitation.
     * @throws java.lang.Exception
     * @see fr.cnes.doi.resource.citation.LanguageCitationResource
     */
    @Test
    public void testGetLanguages() throws Exception {
        testTitle("testGetLanguages");

        // Create the sub on CrossCite        
        crossCiteServerStub.createSpec(CrossCiteSpec.Spec.GET_LANGUAGE_200);

        // Create a GET request client API on DOIServer. DOIServer calls the CrossCite stub        
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        List<String> expResult = CrossCiteSpec.Spec.GET_LANGUAGE_200.getBodyAsList();
        List<String> result = instance.getLanguages();
        assertEquals("Test retrieving languages", expResult, result);

        // Check if the stub has been requested
        crossCiteServerStub.verifySpec(CrossCiteSpec.Spec.GET_LANGUAGE_200);
    }

    /**
     * Test of getFormat method, of class ClientCrossCiteCitation.
     * @throws java.lang.Exception
     * @see fr.cnes.doi.resource.citation.FormatCitationResource
     */
    @Test
    public void testGetFormat() throws Exception {
        testTitle("testGetFormat");

        // Create the sub on CrossCite        
        crossCiteServerStub.createSpec(CrossCiteSpec.Spec.GET_FORMAT_200);

        // Create a GET request client API on DOIServer. DOIServer calls the CrossCite stub        
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        String expResult = CrossCiteSpec.Spec.GET_FORMAT_200.getBody();
        String result = instance.getFormat(doiName, style, language);
        assertEquals("Test retrieving format", expResult, result);

        // Check if the stub has been requested        
        crossCiteServerStub.verifySpec(CrossCiteSpec.Spec.GET_FORMAT_200);
    }  

}
