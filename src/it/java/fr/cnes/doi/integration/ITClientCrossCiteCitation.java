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
package fr.cnes.doi.integration;

import static fr.cnes.doi.AbstractSpec.classTitle;
import static fr.cnes.doi.AbstractSpec.testTitle;
import fr.cnes.doi.client.ClientCrossCiteCitation;
import fr.cnes.doi.exception.ClientCrossCiteException;
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
 *
 * @author Jean-Christophe Malapert
 * @see fr.cnes.doi.resource.citation
 */
@Category(IntegrationTest.class)
public class ITClientCrossCiteCitation {

    public ITClientCrossCiteCitation() {
    }

    @BeforeClass
    public static void setUpClass() {
        classTitle("ClientCrossCiteCitation");
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

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    /**
     * Test of getFormat method, of class ClientCrossCiteCitation.
     *
     * @throws fr.cnes.doi.exception.ClientCrossCiteException
     * @see fr.cnes.doi.resource.citation.FormatCitationResource
     */
    @Test
    public void testGetFormat() throws ClientCrossCiteException {
        testTitle("testGetFormat");

        // Create a GET request client API on DOIServer. DOIServer calls the CrossCite stub
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.PROD);
        String result = instance.getFormat(doiName, style, language);
        assertTrue("IT Test retrieving format", result.length() > 0);
    }

}
