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
import fr.cnes.doi.client.ClientSearchDataCite;
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
@Category(IntegrationTest.class)
public class ITClientSearchDatacite {

    public ITClientSearchDatacite() {
    }

    @BeforeClass
    public static void setUpClass() {
        classTitle("ClientSearchDatacite");
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
     * Test of getFormat method, of class ClientSearchDatacite.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetDois() throws Exception {
        testTitle("testGetDois");

        ClientSearchDataCite searchDatacite = new ClientSearchDataCite("10.24400");
        List<String> dois = searchDatacite.getDois();        
        assertTrue("IT Test searchDatacite",dois.size() > 0);
    }

}
