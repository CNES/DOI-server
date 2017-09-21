/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import fr.cnes.doi.InitServerForTest;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.junit.Assert;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
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
     * Test of getStyles method, of class StyleCitationResource.
     */
    @Test
    public void testGetStylesHttps() {
        System.out.println("getStyles");
        String expResult = "academy-of-management-review";
        String result = "";
        try {
            String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);                        
            ClientResource client = new ClientResource("https://localhost:"+port+"/citation/style");
            client.setNext(cl);
            List<String> rep = client.get(List.class);
            result = rep.get(0);
        } catch (Exception ex) {
            Logger.getLogger(StyleCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            assertEquals(expResult, result);
        }
    }
    
    /**
     * Test of getStyles method, of class StyleCitationResource.
     */
    @Test
    public void testGetStylesHttp() {
        System.out.println("getStyles");
        String expResult = "academy-of-management-review";
        String result = "";
        try {
            String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);                        
            ClientResource client = new ClientResource("http://localhost:"+port+"/citation/style");
            client.setNext(cl);
            List<String> rep = client.get(List.class);
            result = rep.get(0);
        } catch (Exception ex) {
            Logger.getLogger(StyleCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            assertEquals(expResult, result);
        }
    }    
    
    /**
     * Test of getStyles method, of class StyleCitationResource.
     */
    @Test
    public void testGetStylesHttpsAsJSON() {
        System.out.println("getStyles");
        String expResult = "academy-of-management-review";
        String result = "";
        try {
            String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);                        
            ClientResource client = new ClientResource("https://localhost:"+port+"/citation/style");
            client.setNext(cl);
            Representation rep = client.get(MediaType.APPLICATION_JSON);
            result = rep.getText();
        } catch (Exception ex) {
            Logger.getLogger(StyleCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Assert.assertTrue(result.contains("["));
        }
    }    
    
    /**
     * Test of getStyles method, of class StyleCitationResource.
     */
    @Test
    public void testGetStylesHttpsAsXML() {
        System.out.println("getStyles");
        String expResult = "academy-of-management-review";
        String result = "";
        try {
            String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);                        
            ClientResource client = new ClientResource("https://localhost:"+port+"/citation/style");
            client.setNext(cl);
            Representation rep = client.get(MediaType.APPLICATION_XML);
            result = rep.getText();
        } catch (Exception ex) {
            Logger.getLogger(StyleCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Assert.assertTrue(result.contains("<"));
        }
    }    

}
