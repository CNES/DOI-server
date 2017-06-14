/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import fr.cnes.doi.server.DoiServer;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.Utils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class LanguageCitationResourceTest {
    
    private static InputStream inputStream = StyleCitationResourceTest.class.getResourceAsStream("/config.properties");
    private static DoiServer doiServer;     
    private static DoiSettings instance;
    private Client cl;
    
    public LanguageCitationResourceTest() {
        cl = new Client(new Context(), Protocol.HTTPS);
        Series<Parameter> parameters = cl.getContext().getParameters();
        parameters.add("truststorePath", "jks/doiServerKey.jks");
        parameters.add("truststorePassword", instance.getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
        parameters.add("truststoreType", "JKS");         
    }
    
    @BeforeClass
    public static void setUpClass() {
        try {
            instance = DoiSettings.getInstance();
            String result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
            String secretKey = System.getProperty("private.key");
            result = Utils.decrypt(result, secretKey);
            InputStream stream = new ByteArrayInputStream(result.getBytes(Charset.forName("UTF-8")));
            try {
                instance.setPropertiesFile(stream);
            } catch (IOException ex) {
                Logger.getLogger(CitationSuite.class.getName()).log(Level.SEVERE, null, ex);
            }
            doiServer = new DoiServer(instance);
        } catch (Exception ex) {
            Logger.getLogger(StyleCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
        }        
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

    /**
     * Test of getLanguages method, of class LanguageCitationResource.
     */
    @Test
    public void testGetLanguages() {
        System.out.println("getLanguages");        
        String expResult = "af-ZA";
        String result = "";
        try {
            doiServer.start();
            ClientResource client = new ClientResource("https://localhost:8183/citation/language");
            client.setNext(cl);
            List<String> rep =  client.get(List.class);
            result = rep.get(0);
        } catch (Exception ex) {
            Logger.getLogger(LanguageCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                doiServer.stop();
            } catch (Exception ex) {
                Logger.getLogger(LanguageCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            assertEquals(expResult, result);            
        }
    }
    
}
