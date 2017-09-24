/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import fr.cnes.doi.InitServerForTest;
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
import java.io.IOException;
import org.junit.Assert;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

/**
 * Tests the citation style resource.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class StyleCitationResourceTest {

    private static Client cl;

    public StyleCitationResourceTest() throws InterruptedException, Exception {
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
    public void tearDown() throws Exception {

    }

    /**
     * Test of getStyles method through a HTTPS server, of class
     * StyleCitationResource.
     */
    @Test
    public void testGetStylesHttps() {
        System.out.println("getStyles through a HTTPS server");
        String expResult = "academy-of-management-review";
        String result = "";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/citation/style");
        client.setNext(cl);
        try {
            List<String> rep = client.get(List.class);
            result = rep.get(0);
        } catch (Exception ex) {
        } finally {
            client.release();
            assertEquals("Test if the server returns the right response", expResult, result);
        }
    }

    /**
     * Test of getStyles method through a HTTP server, of class
     * StyleCitationResource.
     */
    @Test
    public void testGetStylesHttp() {
        System.out.println("getStyles through a HTTP server");
        String expResult = "academy-of-management-review";
        String result = "";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + "/citation/style");
        client.setNext(cl);
        try {
            List<String> rep = client.get(List.class);
            result = rep.get(0);
        } catch (Exception ex) {
        } finally {
            client.release();
            assertEquals("Test if the server returns the right response", expResult, result);
        }
    }

    /**
     * Test of getStyles method as a JSON response, of class
     * StyleCitationResource.
     */
    @Test
    public void testGetStylesHttpsAsJSON() {
        System.out.println("getStyles as a JSON response");
        String expResult = "academy-of-management-review";
        String result = "";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/citation/style");
        client.setNext(cl);
        try {
            Representation rep = client.get(MediaType.APPLICATION_JSON);
            result = rep.getText();
        } catch (ResourceException | IOException ex) {
        } finally {
            client.release();
            Assert.assertTrue("Test is the server returns a JSON response", result.contains("["));
        }
    }

    /**
     * Test of getStyles method as a XML response, of class
     * StyleCitationResource.
     */
    @Test
    public void testGetStylesHttpsAsXML() {
        System.out.println("getStyles as a XML response");
        String expResult = "academy-of-management-review";
        String result = "";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/citation/style");
        client.setNext(cl);
        try {
            Representation rep = client.get(MediaType.APPLICATION_XML);
            result = rep.getText();
        } catch (ResourceException | IOException ex) {
        } finally {
            client.release();
            Assert.assertTrue("Test is the server returns a XML response", result.contains("<"));
        }
    }

}
