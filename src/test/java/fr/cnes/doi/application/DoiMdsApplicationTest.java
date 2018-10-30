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

import static fr.cnes.doi.AbstractSpec.classTitle;
import static fr.cnes.doi.AbstractSpec.testTitle;
import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.UnitTest;
import fr.cnes.doi.exception.ClientMdsException;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.experimental.categories.Category;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * Tests the API description for the DoiMds application.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(UnitTest.class)
public class DoiMdsApplicationTest {
    
    private static Client cl;
    
    public DoiMdsApplicationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws ClientMdsException{
        InitServerForTest.init();       
        cl = new Client(new Context(), Protocol.HTTPS);
        Series<Parameter> parameters = cl.getContext().getParameters();
        parameters.set(RESTLET_MAX_TOTAL_CONNECTIONS, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS));        
        parameters.set(RESTLET_MAX_CONNECTIONS_PER_HOST, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST, DEFAULT_MAX_CONNECTIONS_PER_HOST));
        parameters.add("truststorePath", JKS_DIRECTORY+File.separatorChar+JKS_FILE);        
        parameters.add("truststorePassword", DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
        parameters.add("truststoreType", "JKS");  
        classTitle("DoiMdsApplication");
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
     * Test of the API description with a HTTP server, of class DoiMdsApplication.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testApiWithHttp() throws IOException {
        testTitle("testApiWithHttp");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);        
        ClientResource client = new ClientResource("http://localhost:"+port+"/mds/");
        Representation repApi = client.options();
        String txt = repApi.getText();
        client.release();
        assertTrue("WADL API through HTTP",txt!=null && !txt.isEmpty() && txt.contains("wadl"));
    }

    /**
     * Test of the API description with a HTTP server, of class DoiMdsApplication.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testApiWithHttps() throws IOException {
        testTitle("testApiWithHttps");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);        
        ClientResource client = new ClientResource("https://localhost:"+port+"/mds/");
        client.setNext(cl);
        Representation repApi = client.options();
        String txt = repApi.getText();
        client.release();
        assertTrue("WADL API through HTTPS",txt!=null && !txt.isEmpty() && txt.contains("wadl"));
    } 
    
    /**
     * Test of API generation in HTML.
     * @throws Exception 
     */
    @Test
    public void generateAPIWadl() throws Exception {
        testTitle("generateAPIWadl");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);        
        ClientResource client = new ClientResource("http://localhost:"+port+"/mds?media=text/html");
        Representation repApi = client.options();
        String txt = repApi.getText();
        client.release();
        try (FileWriter writer = new FileWriter("mds_api.html")) {
            writer.write(txt);
            writer.flush();
        }
        assertTrue("HTML API through HTTPS",txt!=null && !txt.isEmpty() && txt.contains("html"));
    }
}
