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
package fr.cnes.doi.resource.citation;

import fr.cnes.doi.CrossCiteSpec;
import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.UnitTest;
import static fr.cnes.doi.client.BaseClient.DATACITE_MOCKSERVER_PORT;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;


/**
 * Tests the citation style resource.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(UnitTest.class)
public class StyleCitationResourceTest {

    private static Client cl;
    private static CrossCiteSpec spec;   

    public StyleCitationResourceTest() throws InterruptedException, Exception {
    }

    @BeforeClass
    public static void setUpClass() {
        InitServerForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES);
        cl = new Client(new Context(), Protocol.HTTPS);
        Series<Parameter> parameters = cl.getContext().getParameters();
        parameters.set(RESTLET_MAX_TOTAL_CONNECTIONS, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS));        
        parameters.set(RESTLET_MAX_CONNECTIONS_PER_HOST, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST, DEFAULT_MAX_CONNECTIONS_PER_HOST));
        parameters.add("truststorePath", JKS_DIRECTORY+File.separatorChar+JKS_FILE);
        parameters.add("truststorePassword", DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
        parameters.add("truststoreType", "JKS");
        spec = new CrossCiteSpec(DATACITE_MOCKSERVER_PORT);
    }

    @AfterClass
    public static void tearDownClass() {
        spec.finish();
        InitServerForTest.close();
    }

    @Before
    public void setUp() {
        spec.reset();
    }

    @After
    public void tearDown() throws Exception {
    } 
    
    /**
     * Test of getStyles method through a HTTPS server, of class
     * StyleCitationResource.
     */
    @Test
    public void testGetStylesHttps() {        
        spec.createSpec(CrossCiteSpec.Spec.GET_STYLE_200);
        final Series<Parameter> parameters = cl.getContext().getParameters();
        System.setProperty("javax.net.ssl.trustStore", parameters.getFirstValue("truststorePath"));        
        System.setProperty("javax.net.ssl.trustStorePassword", parameters.getFirstValue("truststorePassword"));        
        String expResult = "academy-of-management-review";
        String result = "";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/citation/style");
        //client.setNext(cl);
        try {
            List<String> rep = client.get(List.class);
            result = rep.get(0);
        } catch (ResourceException ex) {
        } finally {
            client.release();
            assertEquals("Test if the server returns the right response", expResult, result);
        }
        
        spec.verifySpec(CrossCiteSpec.Spec.GET_STYLE_200);
        
    }

    /**
     * Test of getStyles method through a HTTP server, of class
     * StyleCitationResource.
     */
    @Test
    public void testGetStylesHttp() {        
        spec.createSpec(CrossCiteSpec.Spec.GET_STYLE_200);      
        final Series<Parameter> parameters = cl.getContext().getParameters();
        System.setProperty("javax.net.ssl.trustStore", parameters.getFirstValue("truststorePath"));        
        System.setProperty("javax.net.ssl.trustStorePassword", parameters.getFirstValue("truststorePassword"));        
        String expResult = "academy-of-management-review";
        String result = "";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + "/citation/style");
        //client.setNext(cl);
        try {
            List<String> rep = client.get(List.class);
            result = rep.get(0);
        } catch (ResourceException ex) {
        } finally {
            client.release();
            assertEquals("Test if the server returns the right response", expResult, result);
        }
        
        spec.verifySpec(CrossCiteSpec.Spec.GET_STYLE_200);
    }

    /**
     * Test of getStyles method as a JSON response, of class
     * StyleCitationResource.
     */
    @Test
    public void testGetStylesHttpsAsJSON() {        
        spec.createSpec(CrossCiteSpec.Spec.GET_STYLE_200);     
        final Series<Parameter> parameters = cl.getContext().getParameters();
        System.setProperty("javax.net.ssl.trustStore", parameters.getFirstValue("truststorePath"));        
        System.setProperty("javax.net.ssl.trustStorePassword", parameters.getFirstValue("truststorePassword"));          
        String result = "";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/citation/style");
        //client.setNext(cl);
        try {
            Representation rep = client.get(MediaType.APPLICATION_JSON);
            result = rep.getText();
        } catch (ResourceException | IOException ex) {
        } finally {
            client.release();
            Assert.assertTrue("Test is the server returns a JSON response", result.contains("["));
        }
        
        spec.verifySpec(CrossCiteSpec.Spec.GET_STYLE_200);
    }

    /**
     * Test of getStyles method as a XML response, of class
     * StyleCitationResource.
     */
    @Test
    public void testGetStylesHttpsAsXML() {        
        spec.createSpec(CrossCiteSpec.Spec.GET_STYLE_200);
        final Series<Parameter> parameters = cl.getContext().getParameters();
        System.setProperty("javax.net.ssl.trustStore", parameters.getFirstValue("truststorePath"));        
        System.setProperty("javax.net.ssl.trustStorePassword", parameters.getFirstValue("truststorePassword"));        
        String expResult = CrossCiteSpec.Spec.GET_STYLE_200.getBody();
        String result = "";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/citation/style");
        //client.setNext(cl);
        try {
            Representation rep = client.get(MediaType.APPLICATION_XML);
            result = rep.getText();
        } catch (ResourceException | IOException ex) {
        } finally {
            client.release();
            Assert.assertTrue("Test is the server returns a XML response", result.contains("<"));
        }
        
        // verify server has received exactly one request
        spec.verifySpec(CrossCiteSpec.Spec.GET_STYLE_200);
        
    }

}
