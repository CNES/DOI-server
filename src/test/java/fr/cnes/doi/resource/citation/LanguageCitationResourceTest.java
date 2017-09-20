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

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.server.DoiServer;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class LanguageCitationResourceTest {

    private static Client cl;

    public LanguageCitationResourceTest() {
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
     * Test of getLanguages method, of class LanguageCitationResource.
     */
    @Test
    public void testGetLanguagesHttps() {
        System.out.println("getLanguages");
        String expResult = "af-ZA";
        String result = "";
        try {
            String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);            
            ClientResource client = new ClientResource("https://localhost:"+port+"/citation/language");
            client.setNext(cl);
            List<String> rep = client.get(List.class);
            result = rep.get(0);
        } catch (Exception ex) {
            Logger.getLogger(LanguageCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            assertEquals(expResult, result);
        }
    }
    @Test
    public void testGetLanguagesHttp() {
        System.out.println("getLanguages");
        String expResult = "af-ZA";
        String result = "";
        try {
            String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);            
            ClientResource client = new ClientResource("http://localhost:"+port+"/citation/language");
            client.setNext(cl);
            List<String> rep = client.get(List.class);
            result = rep.get(0);
        } catch (Exception ex) {
            Logger.getLogger(LanguageCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            assertEquals(expResult, result);
        }
    }    
    

}
