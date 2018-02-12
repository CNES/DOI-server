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
import javax.xml.bind.JAXBException;
import org.datacite.schema.kernel_4.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import org.mockserver.model.HttpRequest;
import static org.mockserver.model.HttpRequest.request;
import org.mockserver.model.HttpResponse;
import static org.mockserver.model.HttpResponse.response;
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
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.xml.sax.SAXException;

/**
 * Tests the metadataResource.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadataResourceTest {

    public static final String DOI = "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b";
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<resource xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://datacite.org/schema/kernel-4\" xsi:schemaLocation=\"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\">\n"
            + "    <identifier identifierType=\"DOI\">" + DOI + "</identifier>\n"
            + "    <creators>\n"
            + "        <creator>\n"
            + "            <creatorName>CNES</creatorName>\n"
            + "        </creator>\n"
            + "    </creators>\n"
            + "    <titles>\n"
            + "        <title>Le portail Éduthèque</title>\n"
            + "    </titles>\n"
            + "    <publisher>CNES</publisher>\n"
            + "    <publicationYear>2015</publicationYear>\n"
            + "    <resourceType resourceTypeGeneral=\"Other\">Portail Éduthèque</resourceType>\n"
            + "</resource>";
    private ClientAndServer mockServer;
    private static Client cl;

    public MetadataResourceTest() {
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
     * Test of getMetadata method throw a HTTPS server, of class
     * MetadataResource. A Status.SUCCESS_OK is expected when the metadata is
     * available whereas a Status.CLIENT_ERROR_GONE is expected when the
     * metadata is deleted.
     *
     * @throws java.io.IOException - if OutOfMemoryErrors
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problme occurs
     */
    @Test
    public void testGetMetadata() throws IOException, JAXBException, SAXException {
        System.out.println("getMetadata");

        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + DOI)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody(XML, StandardCharsets.UTF_8));

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/metadata/" + DOI);
        client.setNext(cl);
        int code;
        String doi;
        try {
            Resource resource = client.get(Resource.class);
            code = client.getStatus().getCode();
            doi = resource.getIdentifier().getValue();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
            doi = "";
        }
        client.release();
        assertTrue(Status.SUCCESS_OK.getCode() == code || Status.CLIENT_ERROR_GONE.getCode() == code);
        assertTrue(DOI.equals(doi) || doi.isEmpty());

        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + DOI)
                .withMethod("GET"), VerificationTimes.once());
    }

    /**
     * Test of getMetadata method throw a HTTPS server with a Json response, of
     * class MetadataResource. Status.SUCCESS_OK is expected when the metadata
     * is available whereas a Status.CLIENT_ERROR_GONE is expected when the
     * metadata is deleted.
     *
     * @throws java.io.IOException - if OutOfMemoryErrors
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problem occurs
     */
    @Test
    public void testGetMetadataAsJson() throws IOException, JAXBException, SAXException {
        System.out.println("getMetadata as Json");

        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + DOI)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody(XML, StandardCharsets.UTF_8));

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/metadata/" + DOI);
        client.setNext(cl);
        int code;
        String result;
        try {
            Representation rep = client.get(MediaType.APPLICATION_JSON);
            result = rep.getText();
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
            result = "";
        }
        client.release();
        assertTrue(Status.SUCCESS_OK.getCode() == code || Status.CLIENT_ERROR_GONE.getCode() == code);
        assertTrue(result.contains("{") || result.isEmpty());
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + DOI)
                .withMethod("GET"), VerificationTimes.once());        
    }

    /**
     * Test of getMetadata method with a wrong DOI through a HTTPS server, of
     * class MetadataResource. A Status.CLIENT_ERROR_NOT_FOUND is expected
     *
     * @throws java.io.IOException - if OutOfMemoryErrors
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problem occurs
     */
    @Test
    public void testGetMetadataFromWrongDOI() throws IOException, JAXBException, SAXException {
        System.out.println("getMetadata");
        
        mockServer
                .when(
                        request()
                                .withPath("/" + ClientMDS.METADATA_RESOURCE + "/" + DOI)
                                .withMethod("GET")
                )
                .respond(
                        response()
                                .withStatusCode(404)
                                .withBody("DOI not found")
                );
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/metadata/"+DOI);
        client.setNext(cl);
        int code;
        String doi;
        try {
            Resource resource = client.get(Resource.class);
            code = client.getStatus().getCode();
            doi = resource.getIdentifier().getValue();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
            doi = "";
        }
        client.release();
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), code);
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + DOI)
                .withMethod("GET"), VerificationTimes.once());        

    }

    /**
     * Test of getMetadata method, of class MetadataResource.
     */
    @Test
    public void testGetMetadataFromWrongPrefix() throws IOException, JAXBException, SAXException {
        System.out.println("getMetadata");
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + DOI)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(404).withBody(XML, StandardCharsets.UTF_8));
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/metadata/"+DOI);
        client.setNext(cl);
        int code;
        String doi;
        try {
            Resource resource = client.get(Resource.class);
            code = client.getStatus().getCode();
            doi = resource.getIdentifier().getValue();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
            doi = "";
        }
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), code);
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + DOI)
                .withMethod("GET"), VerificationTimes.once());          

    }      
    /**
     * Test of deleteMetadata method, of class MetadataResource. A
     * Status.SUCCESS_OK is expected
     *
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problem occurs
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testDeleteMetadata() throws JAXBException, SAXException, IOException {
        System.out.println("deleteMetadata");
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE+"/"+DOI)
                .withMethod("DELETE")).respond(HttpResponse.response().withStatusCode(200).withBody(XML, StandardCharsets.UTF_8));
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/metadata/" + DOI);
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
            Representation rep = client.delete();
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(Status.SUCCESS_OK.getCode(), code);
        
        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE+"/"+DOI)
                .withMethod("DELETE"), VerificationTimes.once());         
    }
}
