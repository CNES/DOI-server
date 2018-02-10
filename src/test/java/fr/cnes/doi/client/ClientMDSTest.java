/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package fr.cnes.doi.client;

import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.datacite.schema.kernel_4.Resource;
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
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class ClientMDSTest {
    
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<resource xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://datacite.org/schema/kernel-4\" xsi:schemaLocation=\"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\">\n"
                + "    <identifier identifierType=\"DOI\">10.5072/EDU/TESTID</identifier>\n"
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
    private final String login;
    private final String pwd;

    public ClientMDSTest() {
        this.login = DoiSettings.getInstance().getSecret(Consts.INIST_LOGIN);
        this.pwd = DoiSettings.getInstance().getSecret(Consts.INIST_PWD);        
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        mockServer = startClientAndServer(1081);
    }

    @After
    public void tearDown() {
        mockServer.stop();
    }

    @Rule
    public ExpectedException exceptions = ExpectedException.none();
    public MockServerRule mockServerRule = new MockServerRule(this);

    /**
     * Test of checkIfAllCharsAreValid method, of class ClientMDS.
     */
    @Test
    public void testCheckIfOneCharIsNotValid() {
        System.out.println("checkIfOneCharIsNotValid");
        exceptions.expect(IllegalArgumentException.class);
        String test = "10.5072/éabscd";
        ClientMDS.checkIfAllCharsAreValid(test);
    }

    @Test
    public void testCheckIfAllCharsAreValid() {
        System.out.println("checkIfAllCharsAreValid");
        String test = "10.5072/eabscd";
        ClientMDS.checkIfAllCharsAreValid(test);
        assertTrue("Test the DOI chars are valid", true);
    }

    /**
     * Test of getDoi method, of class ClientMDS.
     */
    @Test
    public void testGetDoiWithWrongDoi() throws Exception {
        System.out.println("getDoi");
        
        String doiName = "10.5072/2783446.2783605";
        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(404));
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expResult = 404;
        int result;
        try {
            String doi = instance.getDoi(doiName);
            result = 200;
        } catch (ClientMdsException ex) {
            result = ex.getStatus().getCode();
        }
        assertEquals("Test the response with a given wrong DOI", expResult, result);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
                .withMethod("GET"), VerificationTimes.once());

    }

    /**
     * Test of getDoi method, of class ClientMDS.
     */
    @Test
    public void testGetDoiWithWrongAuthentication() throws Exception {
        System.out.println("getDoi");

        String doiName = "10.5072/2783446.2783605";
        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(403));

        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expResult = 403;
        int result;
        try {
            String doi = instance.getDoi(doiName);
            result = 200;
        } catch (ClientMdsException ex) {
            result = ex.getStatus().getCode();
        }
        assertEquals("Test the response with a given wrong DOI", expResult, result);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
                .withMethod("GET"), VerificationTimes.once());

    }

//    /**
//     * Test of getDoi method, of class ClientMDS.
//     */
// Bug in Mocker Server => canoot test this case
//    @Test
//    public void testGetDoiWithNoContent() throws Exception {
//        System.out.println("getDoi");
//
//        String doiName = "10.5072/2783446.2783605";
//        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
//                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(204).withBody(""));
//
//        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
//        int expResult = 204;
//        int result;
//        try {
//            String doi = instance.getDoi(doiName);
//            result = 200;
//        } catch (ClientMdsException ex) {
//            result = ex.getStatus().getCode();
//        }
//        assertEquals("Test the response with no content", expResult, result);
//
//        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
//                .withMethod("GET"), VerificationTimes.once());
//
//    } 
    
    
    /**
     * Test of getDoi method, of class ClientMDS.
     */
    @Test
    public void testGetDoiWithUnautorized() throws Exception {
        System.out.println("getDoi");

        String doiName = "10.5072/2783446.2783605";
        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(401));

        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expResult = 401;
        int result;
        try {
            String doi = instance.getDoi(doiName);
            result = 200;
        } catch (ClientMdsException ex) {
            result = ex.getStatus().getCode();
        }
        assertEquals("Test the response with unauthorized", expResult, result);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
                .withMethod("GET"), VerificationTimes.once());

    }

    /**
     * Test of getDoi method, of class ClientMDS.
     */
    @Test
    public void testGetDoiWithInternalError() throws Exception {
        System.out.println("getDoi");

        String doiName = "10.5072/2783446.2783605";
        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(500));

        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expResult = 500;
        int result;
        try {
            String doi = instance.getDoi(doiName);
            result = 200;
        } catch (ClientMdsException ex) {
            result = ex.getStatus().getCode();
        }
        assertEquals("Test the response with internal error server", expResult, result);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
                .withMethod("GET"), VerificationTimes.once());

    }

    /**
     * Test of getDoi method, of class ClientMDS.
     */
    @Test
    public void testGetDoi() throws Exception {
        System.out.println("getDoi");

        String doiName = "10.5072/2783446.2783605";
        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody("https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php"));

        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expResult = 200;
        String expResultOutput = "https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php";
        int result;
        String doi;
        try {
            doi = instance.getDoi(doiName);
            result = 200;
        } catch (ClientMdsException ex) {
            result = ex.getStatus().getCode();
            doi = "";
        }
        assertEquals("Test the response with a specfic DOI", expResult, result);
        assertEquals("Test the response with a secific DOI", expResultOutput, doi);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE + "/" + doiName)
                .withMethod("GET"), VerificationTimes.once());

    }

    /**
     * Test of getDoiCollection method, of class ClientMDS.
     */
    @Test
    public void testGetDoiCollection() throws Exception {
        System.out.println("getDoiCollection");

        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody("10.5072/EDU/TESTID"));

        String expResult = "10.5072/EDU/TESTID";
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        String result = instance.getDoiCollection();

        assertEquals("Test the collection", expResult, result);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE)
                .withMethod("GET"), VerificationTimes.once());

    }

//Bug in MockerServer => cannot test 204
//    @Test
//    public void testGetDoiCollectionNoDoiFound() throws Exception {
//        System.out.println("getDoiCollection");
//        
//        String login = DoiSettings.getInstance().getSecret(Consts.INIST_LOGIN);
//        String pwd = DoiSettings.getInstance().getSecret(Consts.INIST_PWD);
//        mockServer.when(HttpRequest.request(ClientMDS.DOI_RESOURCE)
//                .withPath("/" + ClientMDS.DOI_RESOURCE)
//                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(204).withBody(""));
//        
//        String expResult = "";
//        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);        
//        String result = instance.getDoiCollection();
//        
//        assertEquals(expResult, result);
//
//        mockServer.verify(HttpRequest.request(ClientMDS.DOI_RESOURCE)
//                .withPath("/" + ClientMDS.DOI_RESOURCE)
//                .withMethod("GET"), VerificationTimes.once());        
//        
//    }    
    /**
     * Test of createDoi method, of class ClientMDS.
     */
    @Test
    public void testCreateDoi() throws Exception {
        System.out.println("createDoi");

        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE)
                .withMethod("POST")).respond(HttpResponse.response().withStatusCode(201).withBody("CREATED"));

        Form form = new Form();
        form.add("doi", "10.5072/EDU/TESTID");
        form.add("url", "https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php");
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expResult = 201;
        int resultCode;
        try {
            String result = instance.createDoi(form);
            resultCode = 201;
        } catch (ClientMdsException ex) {
            resultCode = ex.getStatus().getCode();
        }
        assertEquals("Test the creation of a DOI", expResult, resultCode);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE)
                .withMethod("POST"), VerificationTimes.once());
    }
    
    /**
     * Test of createDoi method, of class ClientMDS.
     */
    @Test
    public void testCreateDoiWithBadRequest() throws Exception {
        System.out.println("createDoi");

        mockServer.when(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE)
                .withMethod("POST")).respond(HttpResponse.response().withStatusCode(400).withBody("request body must be exactly two lines: DOI and URL; wrong domain, wrong prefix"));

        Form form = new Form();
        form.add("doi", "10.5072/EDU/TESTID");
        form.add("url", "https://edutheque.toto.fr/fr/web/CNES-fr/10884-edutheque.php");
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expResult = 400;
        int resultCode;
        try {
            String result = instance.createDoi(form);
            resultCode = 201;
        } catch (ClientMdsException ex) {
            resultCode = ex.getStatus().getCode();
        }
        assertEquals("Test the creation of a DOI with a bad Request", expResult, resultCode);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.DOI_RESOURCE)
                .withMethod("POST"), VerificationTimes.once());
    }    
    
    

    /**
     * Test of getMetadataAsObject method, of class ClientMDS.
     */
    @Test
    public void testGetMetadataAsObject() throws Exception {
        System.out.println("getMetadataAsObject");

        String doiName = "10.5072/EDU/TESTID";
        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + doiName)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody(XML, StandardCharsets.UTF_8));

        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);

        final JAXBContext ctx = JAXBContext.newInstance(new Class[]{Resource.class});
        final Unmarshaller unMarshaller = ctx.createUnmarshaller();
        final Resource expResult = (Resource) unMarshaller.unmarshal(new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8)));
        
        Resource result = instance.getMetadataAsObject(doiName);
        assertEquals(expResult.getIdentifier().getValue(), result.getIdentifier().getValue());

        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + doiName)
                .withMethod("GET"), VerificationTimes.once());
    }

    /**
     * Test of getMetadata method, of class ClientMDS.
     */
    @Test
    public void testGetMetadata() throws Exception {
        System.out.println("getMetadata");

        String doiName = "10.5072/EDU/TESTID";
        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + doiName)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody(XML, StandardCharsets.UTF_8));
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        String expResult = XML;
        Representation result = instance.getMetadata(doiName);

        assertEquals(expResult, result.getText());

        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE + "/" + doiName)
                .withMethod("GET"), VerificationTimes.once());
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     */
    @Test
    public void testCreateMetadata_Representation() throws Exception {
        System.out.println("createMetadata");

        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE)
                .withMethod("POST")).respond(HttpResponse.response().withStatusCode(201).withBody("CREATED"));

        Representation entity = new StringRepresentation(XML, org.restlet.data.MediaType.TEXT_XML, Language.ALL, CharacterSet.UTF_8);
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        String expResult = "CREATED";
        String result = instance.createMetadata(entity);
        assertEquals(expResult, result);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE)
                .withMethod("POST"), VerificationTimes.once());
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     */
    @Test
    public void testCreateMetadata_Resource() throws Exception {
        System.out.println("createMetadata");

        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE)
                .withMethod("POST")).respond(HttpResponse.response().withStatusCode(201).withBody("CREATED"));

        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        String expResult = "CREATED";

        final JAXBContext ctx = JAXBContext.newInstance(new Class[]{Resource.class});
        final Unmarshaller unMarshaller = ctx.createUnmarshaller();
        final Resource entity = (Resource) unMarshaller.unmarshal(new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8)));
        String result = instance.createMetadata(entity);
        assertEquals(expResult, result);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE)
                .withMethod("POST"), VerificationTimes.once());
    }

    /**
     * Test of deleteMetadataDoiAsObject method, of class ClientMDS.
     */
    @Test
    public void testDeleteMetadataDoiAsObject() throws Exception {
        System.out.println("deleteMetadataDoiAsObject");
        String doiName = "10.5072/EDU/TESTID";
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE+"/"+doiName)
                .withMethod("DELETE")).respond(HttpResponse.response().withStatusCode(200).withBody(XML, StandardCharsets.UTF_8));
        
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        final JAXBContext ctx = JAXBContext.newInstance(new Class[]{Resource.class});
        final Unmarshaller unMarshaller = ctx.createUnmarshaller();
        final Resource expResult = (Resource) unMarshaller.unmarshal(new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8)));
        
        Resource result = instance.deleteMetadataDoiAsObject(doiName);
        assertEquals(expResult.getIdentifier().getValue(), result.getIdentifier().getValue());

        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE+"/"+doiName)
                .withMethod("DELETE"), VerificationTimes.once());        
    }

    /**
     * Test of deleteMetadata method, of class ClientMDS.
     */
    @Test
    public void testDeleteMetadata() throws Exception {
        System.out.println("deleteMetadata");
        String doiName = "10.5072/EDU/TESTID";
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE+"/"+doiName)
                .withMethod("DELETE")).respond(HttpResponse.response().withStatusCode(200).withBody(XML, StandardCharsets.UTF_8));
        
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        Representation expResult = new StringRepresentation(XML, org.restlet.data.MediaType.TEXT_XML, Language.ALL, CharacterSet.UTF_8);
        Representation result = instance.deleteMetadata(doiName);
        assertEquals(expResult.getText(), result.getText());

        mockServer.verify(HttpRequest.request("/" + ClientMDS.METADATA_RESOURCE+"/"+doiName)
                .withMethod("DELETE"), VerificationTimes.once());        
    }

    /**
     * Test of getMedia method, of class ClientMDS.
     */
    @Test
    public void testGetMedia() throws Exception {
        System.out.println("getMedia");
        String doiName = "10.5072/EDU/TESTID";
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+doiName)
                .withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).withBody("application/fits=http://cnes.fr/test-data", StandardCharsets.UTF_8));
        
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        String expResult = "application/fits=http://cnes.fr/test-data";
        String result = instance.getMedia(doiName);
        assertEquals(expResult, result);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+doiName)
                .withMethod("GET"), VerificationTimes.once());         
    }

    /**
     * Test of createMedia method, of class ClientMDS.
     */
    @Test
    public void testCreateMedia() throws Exception {
        System.out.println("createMedia");
        String doiName = "10.5072/EDU/TESTID";
        
        mockServer.when(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+doiName)
                .withMethod("POST")).respond(HttpResponse.response().withStatusCode(200).withBody("operation successful"));
        
        Form form = new Form();
        form.add("application/fits","http://cnes.fr/test-data");
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        String expResult = "operation successful";
        String result = instance.createMedia(doiName, form);
        assertEquals(expResult, result);

        mockServer.verify(HttpRequest.request("/" + ClientMDS.MEDIA_RESOURCE+"/"+doiName)
                .withMethod("POST"), VerificationTimes.once());          
    }
}
