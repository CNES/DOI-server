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
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DoiResourceTest {
    
    public static final String DOI = "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b";
    private static Client cl;
    private ClientAndServer mockServer;
    
    public DoiResourceTest() {
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
     * Test of getDoi method through a HTTP server, of class DoiResource.
     * A Status.SUCCESS_OK is expected and the response <i>https://cfosat.cnes.fr/</i>
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetDoiHttp() throws IOException {
        System.out.println("getDoi http");
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + DOI)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody("https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php"));
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:"+port+"/mds/dois/"+DOI);
        Representation rep = client.get();
        Status status = client.getStatus();
        assertEquals("Test the status code is the right one", status.getCode(), Status.SUCCESS_OK.getCode());
        assertEquals("Test the landing page is the right one","https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php", rep.getText());
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + DOI)
                .withMethod("GET"), VerificationTimes.once());        
    }
    
    /**
     * Test of getDoi method through a HTTP server, of class DoiResource.
     * A Status.SUCCESS_OK is expected and the response <i>https://cfosat.cnes.fr/</i>
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetDoiHttps() throws IOException {
        System.out.println("getDoi https");
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + DOI)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody("https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php"));
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/mds/dois/"+DOI);
        client.setNext(cl);
        Representation rep = client.get();
        Status status = client.getStatus();
        assertEquals("Test the status code is the right one", status.getCode(), Status.SUCCESS_OK.getCode());
        assertEquals("Test the landing page is the right one","https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php", rep.getText());
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + DOI)
                .withMethod("GET"), VerificationTimes.once());         
    }    
    
    /**
     * Test of getDoi method when the DOI is not found through a HTTPS server, of class DoiResource.
     * A Status.CLIENT_ERROR_NOT_FOUND is expected because the 
     * DOI does not exist.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetDoiNotFoundHttps() throws IOException {        
        System.out.println("getDoi not found https");      
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + DOI)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(404));  
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/mds/dois/"+DOI);
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        } 
        client.release();
        assertEquals(code, Status.CLIENT_ERROR_NOT_FOUND.getCode());
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + DOI)
                .withMethod("GET"), VerificationTimes.once());        
    }        
    
    /**
     * Test of getDoi method when the DOI prefix is not the right one through a HTTPS server, of class DoiResource.
     * @throws java.io.IOException
     */
    @Test
    public void testGetDoiNotAllowedHttps() throws IOException {        
        System.out.println("getDoi not allowed https");      
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/mds/dois/10.xxxx/828606");
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {   
            code = ex.getStatus().getCode();
        }
        
        assertEquals("Test the status code is the right one", code, Status.CLIENT_ERROR_BAD_REQUEST.getCode());
    }     
    
}
