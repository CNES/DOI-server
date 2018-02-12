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
import fr.cnes.doi.client.ClientProxyTest;
import fr.cnes.doi.security.UtilsHeader;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Tests MetadatasResource.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadatasResourceTest {
    
    @Rule
    public ExpectedException exceptions = ExpectedException.none();     

    private static Client cl;
    private String result;
    private InputStream inputStream;
    private InputStream inputStreamFileError; 
    private ClientAndServer mockServer;    

    public MetadatasResourceTest() {
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
    public void setUp() throws IOException {
        this.inputStream = ClientProxyTest.class.getResourceAsStream("/test.xml");
        this.inputStreamFileError = ClientProxyTest.class.getResourceAsStream("/wrongFileTest.xml");
        mockServer = startClientAndServer(1081);
    }

    @After
    public void tearDown() throws IOException {
        this.inputStream.close();
        this.inputStreamFileError.close();
        mockServer.stop();
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A Status.SUCCESS_CREATED is expected.
     */
    @Test
    public void testCreateMetadataHttps() {
        System.out.println("createMetadata");
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE)
                .withMethod("POST")).respond(HttpResponse.response().withStatusCode(201).withBody("CREATED"));
        
        result = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .collect(Collectors.joining("\n"));
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/metadata");
        client.setNext(cl);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
        int code;
        try {
            Representation rep = client.post(new StringRepresentation(result, MediaType.APPLICATION_XML));
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(Status.SUCCESS_CREATED.getCode(), code);
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE)
                .withMethod("POST"), VerificationTimes.once());        
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A Status.CLIENT_ERROR_BAD_REQUEST is thrown because the metadata file is not valid.
     */
    @Test
    public void testCreateMetadataHttpsWithWrongFile() {
        System.out.println("createMetadata with Wrong File");
        exceptions.expect(ResourceException.class);
        result = new BufferedReader(new InputStreamReader(inputStreamFileError)).lines()
                .collect(Collectors.joining("\n"));
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/metadata");
        client.setNext(cl);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
        try {
            Representation rep = client.post(new StringRepresentation(result, MediaType.APPLICATION_XML));
        } finally {
            client.release();
        }
    }    
    
    /**
     * Test of createMetadata method through HTTPS server with no role, of class MetadatasResource.
     * A ResourceException CLIENT_ERROR_UNAUTHORIZED is thrown.
     */
    @Test
    public void testCreateMetadataHttpsWithNoRole() {
        System.out.println("createMetadata with not role");
        exceptions.expect(ResourceException.class);        
        
        result = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .collect(Collectors.joining("\n"));
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/metadata");
        client.setNext(cl);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "norole", "norole"));
        int code;
        try {
            Representation rep = client.post(new StringRepresentation(result, MediaType.APPLICATION_XML));
        } finally {
            client.release();
        }
    }    

}
