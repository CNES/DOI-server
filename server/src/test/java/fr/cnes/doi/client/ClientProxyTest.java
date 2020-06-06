/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.client;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.exception.ClientMdsException;
import java.io.IOException;
import org.restlet.Client;
import org.restlet.resource.ResourceException;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
//@Category(UnitTest.class)
public class ClientProxyTest {
    
    private static Client cl;
    
    public ClientProxyTest() {
        
    }

    /**
     * Init the settings
     * @throws fr.cnes.doi.exception.ClientMdsException
     */    
    @BeforeClass
    public static void setUpClass() throws ClientMdsException {
        InitSettingsForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES); 
    }    

    /**
     * Executed after the test class
     */
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Executed before each test
     */
    @Before
    public void setUp() {
    }

    /**
     * Executed after each test
     */
    @After
    public void tearDown() {
    }

    /**
     * Test the connection through the proxy with BaseClient. Works only if the test is executed
     * behind a proxy
     *
     * @throws Exception - if OutOfMemoryErrors
     */
    @Test
    public void testBaseClient() throws Exception {
        BaseClient baseClient = new BaseClient("https://www.google.com");
        Representation rep = baseClient.getClient().get();
        rep.exhaust();
        Status status = baseClient.getClient().getStatus();
        Assert.assertTrue("Test si la requete est OK", status.isSuccess());
    }

    /**
     * Test the connection through the proxy with BaseClient and crossCite URL. Works only if the
     * test is executed behind a proxy
     *
     */
    @Test
    public void testBaseClientCrossCite() throws IOException {
        BaseClient baseClient = new BaseClient("https://www.google.com");
        Status status;
        try {
            Representation rep = baseClient.getClient().get();            
            status = baseClient.getClient().getStatus();
            rep.exhaust();
        } catch (ResourceException ex) {
            status = ex.getStatus();
        } 
        Assert.assertTrue("Test si la requete est OK", status.isSuccess());
    }

}
