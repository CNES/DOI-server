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
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.security.UtilsHeader;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;
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
 * Tests the mediaResource.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MediaResourceTest {
    
    public static final String DOI = "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b";
    private ClientAndServer mockServer;    
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
        mockServer = startClientAndServer(1081);
    }
    
    @After
    public void tearDown() {
        mockServer.stop();
    }
    
    /**
     * Test of getMedias method, of class MediaResource.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetMediasHttps() throws IOException {
        System.out.println("getMedias");
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+DOI)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody("application/fits=http://cnes.fr/test-data", StandardCharsets.UTF_8));
        
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
        client.release();
        assertEquals(Status.SUCCESS_OK.getCode(), code);
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+DOI)
                .withMethod("GET"), VerificationTimes.once());         
    }
    
    /**
     * Test of getMedias method, of class MediaResource.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */    
    @Test
    public void testGetMediasHttp() throws IOException {
        System.out.println("getMedias");
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+DOI)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody("application/fits=http://cnes.fr/test-data", StandardCharsets.UTF_8));
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + "/mds/media/"+DOI);        
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(Status.SUCCESS_OK.getCode(), code);
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+DOI)
                .withMethod("GET"), VerificationTimes.once());         
    }        

    /**
     * Test of getMedias method when the DOI does not exist through a HTTPS server, of class MediaResource.
     * A Status.CLIENT_ERROR_NOT_FOUND is expected because the DOI
     * is not found in the DataCite database.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetMediasWithWrongDOIHttps() throws IOException {
        System.out.println("getMedias with wrong DOI through a HTTPS server");
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+DOI)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(404).withBody("No media attached to the DOI or DOI does not exist in our database"));
        
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
        client.release();
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), code);
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+DOI)
                .withMethod("GET"), VerificationTimes.once());           
    }
    
    /**
     * Test of getMedias method when the DOI does not exist through a HTTP server, of class MediaResource.
     * A Status.CLIENT_ERROR_NOT_FOUND is expected because the DOI
     * is not found in the DataCite database.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */    
    @Test
    public void testGetMediasWithWrongDOIHttp() throws IOException {
        System.out.println("getMedias with wrong DOI through HTTP server");
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+DOI)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(404).withBody("No media attached to the DOI or DOI does not exist in our database"));
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + "/mds/media/"+DOI);        
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), code);
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+DOI)
                .withMethod("GET"), VerificationTimes.once());         
    }    

    /**
     * Test of createMedia method, of class MediaResource.
     * A Status.SUCCESS_OK is expected
     */
    @Test    
    public void testCreateMedia() {
        System.out.println("createMedia");
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+DOI)
                .withMethod("POST")).respond(HttpResponse.response().withStatusCode(200).withBody("operation successful"));
        
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
        client.release();
        assertEquals(Status.SUCCESS_OK.getCode(), code);
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+DOI)
                .withMethod("POST"), VerificationTimes.once());         
    }
    
}
