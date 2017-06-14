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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class FormatCitationResourceTest {
    
    private static InputStream inputStream = StyleCitationResourceTest.class.getResourceAsStream("/config.properties");
    private static DoiServer doiServer;      
    
    public FormatCitationResourceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
    public void tearDown() {     
    }

    /**
     * Test of getFormat method, of class FormatCitationResource.
     */
    @Test
    public void testGetFormat() {
        System.out.println("getFormat");
        String expResult = "Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI ’15. ACM Press. https://doi.org/10.1145/2783446.2783605.\n";
        String result="";
        
        try {
            doiServer.start();            
            String doiName = "10.1145/2783446.2783605";
            String style = "academy-of-management-review";
            String language = "af-ZA";
            ClientResource client = new ClientResource("http://localhost:8182/citation/format");
            client.addQueryParameter("doi", doiName);
            client.addQueryParameter("lang", language);
            client.addQueryParameter("style", style);
            Representation rep = client.get();            
            try {
                result = rep.getText();
            } catch (IOException ex) {
                Logger.getLogger(FormatCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception ex) {
            Logger.getLogger(FormatCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);            
        } finally {
            try {
                doiServer.stop();
            } catch (Exception ex) {
                Logger.getLogger(FormatCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            assertEquals(expResult, result);
        }
    }                
    
    /**
     * Test of getFormat method, of class FormatCitationResource.
     */
    @Test
    public void testGetFormatWithBadDOI() {
        System.out.println("getFormat");
        int expResult = Status.CLIENT_ERROR_NOT_FOUND.getCode();
        int result=-1;
        
        try {
            doiServer.start();            
            String doiName = "xxxx";
            String style = "academy-of-management-review";
            String language = "af-ZA";
            ClientResource client = new ClientResource("http://localhost:8182/citation/format");
            client.addQueryParameter("doi", doiName);
            client.addQueryParameter("lang", language);
            client.addQueryParameter("style", style);
            client.get();
            result = client.getStatus().getCode();
        } catch (Exception ex) {
            Logger.getLogger(FormatCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);                        
        } finally {
            try {
                doiServer.stop();
            } catch (Exception ex) {
                Logger.getLogger(FormatCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            assertEquals(expResult, result);
        }
    }     
    
}
