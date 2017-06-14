/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import fr.cnes.doi.server.DoiServer;
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
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class StyleCitationResourceTest {
    
    private static InputStream inputStream = StyleCitationResourceTest.class.getResourceAsStream("/config.properties");
    private static DoiServer doiServer;    
    
    public StyleCitationResourceTest() throws InterruptedException, Exception {
      
    }
    
    @BeforeClass
    public static void setUpClass()  {
        try {
            DoiSettings instance = DoiSettings.getInstance();
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
    public void tearDown() throws Exception {
                
    }

    /**
     * Test of getStyles method, of class StyleCitationResource.
     */
    @Test
    public void testGetStyles() {
        System.out.println("getStyles");        
        String expResult = "academy-of-management-review";
        String result = "";       
        try {
            doiServer.start();
            ClientResource client = new ClientResource("http://localhost:8182/citation/style");
            List<String> rep = client.get(List.class);
            result = rep.get(0);
        } catch (Exception ex) {
            Logger.getLogger(StyleCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                doiServer.stop();
            } catch (Exception ex) {
                Logger.getLogger(StyleCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            assertEquals(expResult, result);            
        }
    }
    
}
