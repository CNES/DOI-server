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
import javax.xml.bind.JAXBException;
import org.datacite.schema.kernel_4.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
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
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadataResourceTest {

    public static final String DOI = "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b";
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
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getMetadata method throw a HTTPS server, of class MetadataResource.
     * A Status.SUCCESS_OK is expected when the metadata is
     * available whereas a Status.CLIENT_ERROR_GONE is expected
     * when the metadata is deleted.
     * @throws java.io.IOException - if OutOfMemoryErrors
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problme occurs
     */
    @Test
    public void testGetMetadata() throws IOException, JAXBException, SAXException {
        System.out.println("getMetadata");
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
    }
    
    /**
     * Test of getMetadata method throw a HTTPS server with a Json response, of class MetadataResource.
     * Status.SUCCESS_OK is expected when the metadata is
     * available whereas a Status.CLIENT_ERROR_GONE is expected
     * when the metadata is deleted.
     * @throws java.io.IOException - if OutOfMemoryErrors
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problem occurs
     */
    @Test
    public void testGetMetadataAsJson() throws IOException, JAXBException, SAXException {
        System.out.println("getMetadata as Json");
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
        assertTrue(result.contains("{"));
    }    
    
    /**
     * Test of getMetadata method with a wrong DOI through a HTTPS server, of class MetadataResource.
     * A Status.CLIENT_ERROR_NOT_FOUND is expected
     * @throws java.io.IOException - if OutOfMemoryErrors
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problem occurs    
     */
    @Test
    public void testGetMetadataFromWrongDOI() throws IOException, JAXBException, SAXException {
        System.out.println("getMetadata");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/metadata/10.5072/828606");
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

    }   
    
//    /**
//     * Test of getMetadata method, of class MetadataResource.
//     */
//    @Test
//    @Ignore
//    public void testGetMetadataFromWrongPrefix() throws IOException, JAXBException, SAXException {
//        System.out.println("getMetadata");
//        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
//        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/metadata/10.1145/2783446.2783605");
//        client.setNext(cl);
//        int code;
//        String doi;
//        try {
//            Resource resource = client.get(Resource.class);
//            code = client.getStatus().getCode();
//            doi = resource.getIdentifier().getValue();
//        } catch (ResourceException ex) {
//            code = ex.getStatus().getCode();
//            doi = "";
//        }
//        assertEquals(Status.CLIENT_ERROR_FORBIDDEN.getCode(), code);
//
//    }      

    /**
     * Test of deleteMetadata method, of class MetadataResource.
     * A Status.SUCCESS_OK is expected
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problem occurs
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testDeleteMetadata() throws JAXBException, SAXException, IOException {
        System.out.println("deleteMetadata");
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
    }
}
