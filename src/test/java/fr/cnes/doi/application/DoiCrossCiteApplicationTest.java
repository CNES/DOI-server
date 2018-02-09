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
package fr.cnes.doi.application;

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * Tests the API description for the DoiCrossCite application.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DoiCrossCiteApplicationTest {
    
    private static Client cl;
    
    public DoiCrossCiteApplicationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        InitServerForTest.init();
        cl = new Client(new Context(), Protocol.HTTPS);
        Series<Parameter> parameters = cl.getContext().getParameters();
        parameters.add("truststorePath", "jks/doiServerKey.jks");
        parameters.add("truststorePassword", DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
        parameters.add("truststoreType", "JKS");        
    }
    
    @AfterClass
    public static void tearDownClass() {
        InitServerForTest.close();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of the API description with a HTTP server, of class DoiCrossCiteApplication.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testApiWithHttp() throws IOException {
        System.out.println("API through HTTP");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);        
        ClientResource client = new ClientResource("http://localhost:"+port+"/citation/");
        Representation repApi = client.options();
        String txt = repApi.getText();
        client.release();
        assertTrue("API through HTTP",txt!=null && !txt.isEmpty());
    }

    /**
     * Test of the API description with a HTTPS server, of class DoiCrossCiteApplication.
     * @throws java.io.IOException -if OutOfMemoryErrors
     */
    @Test
    public void testApiWithHttps() throws IOException {
        System.out.println("API through HTTPS");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);        
        ClientResource client = new ClientResource("https://localhost:"+port+"/mds/");
        client.setNext(cl);
        Representation repApi = client.options();
        String txt = repApi.getText();
        client.release();
        assertTrue("API through HTTPS",txt!=null && !txt.isEmpty());
    }    
}
