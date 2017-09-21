/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.security.UtilsHeader;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.IOException;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class MediaResourceTest {
    
    public static final String DOI = "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b";
    
    private static Client cl;
    
    public MediaResourceTest() {
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
     * Test of getMedias method, of class MediaResource.
     */
    @Test
    public void testGetMediasHttps() throws IOException {
        System.out.println("getMedias");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/media/"+DOI);
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        assertEquals(Status.SUCCESS_OK.getCode(), code);
    }
    
    /**
     * Test of getMedias method, of class MediaResource.
     */    
    @Test
    public void testGetMediasHttp() throws IOException {
        System.out.println("getMedias");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + "/mds/media/"+DOI);        
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        assertEquals(Status.SUCCESS_OK.getCode(), code);
    }        

    /**
     * Test of getMedias method, of class MediaResource.
     */
    @Test
    @Ignore
    public void testGetMediasWithNoMediaHttps() throws IOException {
        System.out.println("getMedias");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/media/"+DOI);
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), code);
    }
    
    /**
     * Test of getMedias method, of class MediaResource.
     */    
    @Test
    @Ignore
    public void testGetMediasWithNoMediaHttp() throws IOException {
        System.out.println("getMedias");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + "/mds/media/"+DOI);        
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), code);
    }    

    /**
     * Test of createMedia method, of class MediaResource.
     */
    @Test    
    public void testCreateMedia() {
        System.out.println("createMedia");
        Form mediaForm = new Form();
        mediaForm.add("image/fits", "https://cnes.fr/sites/default/files/drupal/201508/default/is_cnesmag65-interactif-fr.pdf");
        mediaForm.add("image/jpeg", "https://cnes.fr/sites/default/files/drupal/201508/default/is_cnesmag65-interactif-fr.pdf");
        mediaForm.add("image/png", "https://cnes.fr/sites/default/files/drupal/201508/default/is_cnesmag65-interactif-fr.pdf");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/media/"+DOI);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
        
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.post(mediaForm);
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        assertEquals(Status.SUCCESS_OK.getCode(), code);
    }
    
}
