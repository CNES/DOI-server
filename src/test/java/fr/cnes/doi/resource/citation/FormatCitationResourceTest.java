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

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.client.ClientCrossCiteCitation;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;

/**
 * Test the citation format resource.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class FormatCitationResourceTest {
    
    @Rule
    public ExpectedException exceptions = ExpectedException.none();     

    private static Client cl;
    private ClientAndServer mockServer;    

    public FormatCitationResourceTest() {
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
        mockServer = startClientAndServer(1080);        
    }

    @After
    public void tearDown() {
        mockServer.stop();
    }
    
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);     

    /**
     * Test of getFormat method, of class FormatCitationResource.
     */
    @Test
    public void testGetFormatHttps() {
        System.out.println("getFormat through a HTTPS server");
        
        mockServer.when(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI).withMethod("GET")).respond(HttpResponse.response().withBody("Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI '15. Presented at the the 2015 British HCI Conference, ACM Press. https://doi.org/10.1145/2783446.2783605.\n"));                
        
        String expResult = "Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI '15. Presented at the the 2015 British HCI Conference, ACM Press. https://doi.org/10.1145/2783446.2783605.\n";
        String result = "";
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/citation/format");
        client.addQueryParameter("doi", doiName);
        client.addQueryParameter("lang", language);
        client.addQueryParameter("style", style);
        client.setNext(cl);
        Representation rep = client.get();
        try {
            result = rep.getText();
        } catch (IOException ex) {
        }
        client.release();
        assertEquals("Test the citation format through a HTTPS server",expResult, result);
        
        mockServer.verify(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI), VerificationTimes.once());          
        
    }
    
    /**
     * Test of getFormat method with wrong send parameters, of class FormatCitationResource.
     * A ResourceException is thrown
     */
    @Test
    public void testGetFormatHttpsWithWrongParameters() {
        System.out.println("getFormat through a HTTPS server with wrong parameters");
        exceptions.expect(ResourceException.class);
        
        mockServer.when(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI).withMethod("GET")).respond(HttpResponse.response().withStatusCode(400));                
        
        String expResult = "Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI ’15. Presented at the the 2015 British HCI Conference, ACM Press. https://doi.org/10.1145/2783446.2783605.\n";
        String result = "";
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/citation/format");
        client.addQueryParameter("do", doiName);
        client.addQueryParameter("lan", language);
        client.addQueryParameter("styl", style);
        client.setNext(cl);
        Representation rep = client.get();
        try {
            result = rep.getText();
        } catch (IOException ex) {
        }
        client.release();
        assertEquals("Test the citation format through a HTTPS server",expResult, result);
        
        mockServer.verify(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI), VerificationTimes.once());          
        
    }    
    
    /**
     * Test of getFormat method, of class FormatCitationResource.
     */
    @Test
    public void testGetFormatHttp() {
        System.out.println("getFormat through a HTTP server");
        
        mockServer.when(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI).withMethod("GET")).respond(HttpResponse.response().withBody("Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI '15. Presented at the the 2015 British HCI Conference, ACM Press. https://doi.org/10.1145/2783446.2783605.\n"));                

        String expResult = "Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI '15. Presented at the the 2015 British HCI Conference, ACM Press. https://doi.org/10.1145/2783446.2783605.\n";
        String result = "";
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:"+port+"/citation/format");
        client.addQueryParameter("doi", doiName);
        client.addQueryParameter("lang", language);
        client.addQueryParameter("style", style);
        client.setNext(cl);
        Representation rep = client.get();
        try {
            result = rep.getText();
        } catch (IOException ex) {
        }
        client.release();
        assertEquals("Test the citation format through a HTTP server",expResult, result);
        
        mockServer.verify(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI), VerificationTimes.once());          
        
    }    

    /**
     * Test of getFormat method with a wrong DOI, of class FormatCitationResource.
     * A Status.CLIENT_ERROR_NOT_FOUND is expected because the DOI does
     * not exist.
     */
    @Test
    public void testGetFormatWithBadDOI() {
        System.out.println("getFormat with a wrong DOI");
        
        mockServer.when(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI).withMethod("GET")).respond(HttpResponse.response().withStatusCode(404));                

        int expResult = Status.CLIENT_ERROR_NOT_FOUND.getCode();
        int result;

        ClientResource client;
        String doiName = "xxxx";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);        
        client = new ClientResource("https://localhost:"+port+"/citation/format");
        client.addQueryParameter("doi", doiName);
        client.addQueryParameter("lang", language);
        client.addQueryParameter("style", style);
        client.setNext(cl);
        try {
            client.get();
            result = client.getStatus().getCode();
        } catch (ResourceException ex) {
            result = ex.getStatus().getCode();
        }
        client.release();
        assertEquals("Test the response with a given wrong DOI",expResult, result);
        
        mockServer.verify(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI), VerificationTimes.once());          
        
    }

    /**
     * Test of getFormat method with a wrong style, of class FormatCitationResource.
     * A Status.CLIENT_ERROR_BAD_REQUEST is expected because the style does
     * not exist among the enumerated list.     
     */
    @Test
    public void testGetFormatWithBadStyle() {
        System.out.println("getFormat with a wrong style");
        
        mockServer.when(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI).withMethod("GET")).respond(HttpResponse.response().withStatusCode(400));                

        int expResult = Status.CLIENT_ERROR_BAD_REQUEST.getCode();
        int result = -1;

        ClientResource client;
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-rew";
        String language = "af-ZA";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);        
        client = new ClientResource("https://localhost:"+port+"/citation/format");
        client.addQueryParameter("doi", doiName);
        client.addQueryParameter("lang", language);
        client.addQueryParameter("style", style);
        client.setNext(cl);
        try {
            client.get();
            result = client.getStatus().getCode();
        } catch (ResourceException ex) {
            result = ex.getStatus().getCode();
        }
        client.release();
        assertEquals("Test the response with a given wrong style", expResult, result);
        
        mockServer.verify(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI), VerificationTimes.once());          
        
    }

    /**
     * Test of getFormat method with a bad style, of class FormatCitationResource.
     * A Status.CLIENT_ERROR_BAD_REQUEST is expected because the language does
     * not exist among the enumerated list.      
     */
    @Test
    public void testGetFormatWithBadLang() {
        System.out.println("getFormat with a wrong language");
        
        mockServer.when(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI).withMethod("GET")).respond(HttpResponse.response().withStatusCode(400));                

        int expResult = Status.CLIENT_ERROR_BAD_REQUEST.getCode();
        int result = -1;

        ClientResource client;
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-review";
        String language = "af-Z";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        client = new ClientResource("https://localhost:"+port+"/citation/format");
        client.addQueryParameter("doi", doiName);
        client.addQueryParameter("lang", language);
        client.addQueryParameter("style", style);
        client.setNext(cl);

        try {
            client.get();
            result = client.getStatus().getCode();
        } catch (ResourceException ex) {
            result = ex.getStatus().getCode();
        }
        client.release();
        assertEquals("Test the response with a given wrong style",expResult, result);
        
        mockServer.verify(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI), VerificationTimes.once());          
        
    }

    /**
     * Test of getFormat method with a wrong style and a wrong DOI, of class FormatCitationResource.
     * A Status.CLIENT_ERROR_NOT_FOUND is expected because the DOI does
     * not exist.      
     */
    @Test
    public void testGetFormatWithBadLangAndBadDoi() {
        System.out.println("getFormat with a wrong DOI and language");
        
        mockServer.when(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI).withMethod("GET")).respond(HttpResponse.response().withStatusCode(404));                
        
        int expResult = Status.CLIENT_ERROR_NOT_FOUND.getCode();
        int result = -1;

        ClientResource client;
        String doiName = "10.1145/276.27";
        String style = "academy-of-management-review";
        String language = "af-Z";
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);        
        client = new ClientResource("https://localhost:"+port+"/citation/format");
        client.addQueryParameter("doi", doiName);
        client.addQueryParameter("lang", language);
        client.addQueryParameter("style", style);
        client.setNext(cl);

        try {
            client.get();
            result = client.getStatus().getCode();
        } catch (ResourceException ex) {
            result = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(expResult, result);
        
        mockServer.verify(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI), VerificationTimes.once());          
        
    }
}
