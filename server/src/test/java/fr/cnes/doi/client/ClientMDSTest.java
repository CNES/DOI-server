/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.client;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.MdsSpec;
import fr.cnes.doi.UnitTest;
import static fr.cnes.doi.client.BaseClient.DATACITE_MOCKSERVER_PORT;
import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.datacite.schema.kernel_4.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Test of the ClientMDS
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 * @see fr.cnes.doi.resource.mds
 */
@Category(UnitTest.class)
public class ClientMDSTest {

    private static MdsSpec mdsServerStub;
    private final String login;
    private final String pwd;

    public ClientMDSTest() {
        this.login = DoiSettings.getInstance().getSecret(Consts.INIST_LOGIN);
        this.pwd = DoiSettings.getInstance().getSecret(Consts.INIST_PWD);
    }

    @BeforeClass
    public static void setUpClass() {
        InitSettingsForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES);
        mdsServerStub = new MdsSpec(DATACITE_MOCKSERVER_PORT);
    }

    @AfterClass
    public static void tearDownClass() {
        mdsServerStub.finish();
    }

    @Before
    public void setUp() {
        mdsServerStub.reset();      
    }

    @After
    public void tearDown() {
        
    }

    @Rule
    public ExpectedException exceptions = ExpectedException.none();
    
    /**
     * Test of checkIfAllCharsAreValid method, of class ClientMDS.
     */
    @Test
    public void testCheckIfOneCharIsNotValid() {  
        exceptions.expect(IllegalArgumentException.class);
        String test = "10.5072/éabscd";
        ClientMDS.checkIfAllCharsAreValid(test);
    }

    @Test
    public void testCheckIfAllCharsAreValid() {
        String test = "10.5072/eabscd";
        ClientMDS.checkIfAllCharsAreValid(test);
        assertTrue("Test the DOI chars are valid", true);
    }
    
    @Test
    public void testGetDoisPrefix() throws ClientMdsException {
        String key = "329360";
        List expMessage = Arrays.asList("10.24400/329360/F7Q52MNK");
        
        // Creates the MetadataStoreService stub
        mdsServerStub.createSpec(MdsSpec.Spec.GET_DOIS);        

        // Requests the DOIServer using the stub (mode DEV)
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expResult = MdsSpec.Spec.GET_DOIS.getStatus();
        int result;
        List<String> message = new ArrayList<>();
        try {
            message = instance.getDois(key);
            result = MdsSpec.Spec.GET_DOIS.getStatus();
        } catch (ClientMdsException ex) {
            result = ex.getStatus().getCode();
        }
        assertEquals("Test the status code", expResult, result);
        if(!message.isEmpty()) {
            assertEquals("Test the response", expMessage, message);
        }

        // Checks the stub.
        mdsServerStub.verifySpec(MdsSpec.Spec.GET_DOIS);        
    }

    /**
     * Test the Get DOI
     * @param spec spec
     */
    private void testSpecGetDois(MdsSpec.Spec spec) throws ClientMdsException {
        // Creates the MetadataStoreService stub
        mdsServerStub.createSpec(spec);

        // Requests the DOIServer using the stub (mode DEV)
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expResult = spec.getStatus();
        List<String> expMessage = Arrays.asList(spec.getBody().split("\n"));
        int result;
        List<String> message = new ArrayList<>();
        try {
            message = instance.getDois();
            result = spec.getStatus();
        } catch (ClientMdsException ex) {
            result = ex.getStatus().getCode();
        }
        assertEquals("Test the status code", expResult, result);
        if(!message.isEmpty()) {
            assertEquals("Test the response", expMessage, message);
        }

        // Checks the stub.
        mdsServerStub.verifySpec(spec);
    }  
    
    
    /**
     * Test the Get DOI
     * @param spec spec
     */
    private void testSpecGetDoi(MdsSpec.Spec spec) throws ClientMdsException {
        // Creates the MetadataStoreService stub
        mdsServerStub.createSpec(spec);

        // Requests the DOIServer using the stub (mode DEV)
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expResult = spec.getStatus();
        String expMessage = spec.getBody();
        int result;
        String message;
        try {
            message = instance.getDoi(spec.getTemplatePath());
            result = spec.getStatus();
        } catch (ClientMdsException ex) {
            result = ex.getStatus().getCode();
            message = ex.getDetailMessage();
        }
        assertEquals("Test the status code", expResult, result);
        if(message != null) {
            assertEquals("Test the response", expMessage, message);
        }

        // Checks the stub.
        mdsServerStub.verifySpec(spec);
    }     

    /**
     * Test of creating DOI.
     * @param spec spec
     */
    private void testSpecCreateDoi(MdsSpec.Spec spec) throws ClientMdsException {
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);

        Form form = new Form();
        form.add("doi", "10.5072/EDU/TESTID");
        form.add("url", "https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php");

        // Requests the DOIServer using the stub (mode DEV)        
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        int expCode = spec.getStatus();
        String expMessage = spec.getBody();

        int code;
        String message;
        try {
            message = instance.createDoi(form);
            code = spec.getStatus();
        } catch (ClientMdsException ex) {
            code = ex.getStatus().getCode();
            message = ex.getDetailMessage();
        }
        assertEquals("Test the status code", expCode, code);
        if(message != null) {        
            assertEquals("Test the response", expMessage, message);
        }
        
        // Checks the stub.
        mdsServerStub.verifySpec(spec);
    }

    private void testSpectGetMetadataAsObj(MdsSpec.Spec spec) throws JAXBException, ClientMdsException {
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);

        // Requests the DOIServer using the stub (mode DEV)        
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);

        final JAXBContext ctx = JAXBContext.newInstance(new Class[]{Resource.class});
        final Unmarshaller unMarshaller = ctx.createUnmarshaller();
        final Resource expResult = (Resource) unMarshaller.unmarshal(new ByteArrayInputStream(spec.getBody().getBytes(StandardCharsets.UTF_8)));

        Resource result = instance.getMetadataAsObject(spec.getTemplatePath());
        assertEquals("Test the response", expResult.getIdentifier().getValue(), result.getIdentifier().getValue());

        // Checks the stub.        
        mdsServerStub.verifySpec(spec);
    }

    private void testSpectGetMetadata(MdsSpec.Spec spec) throws Exception {
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);

        // Requests the DOIServer using the stub (mode DEV)        
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        String expMessage = spec.getBody();
        int expCode = spec.getStatus();
        String message;
        int code;
        try {
            Representation result = instance.getMetadata(spec.getTemplatePath());
            message = result.getText();
            code = spec.getStatus();
        } catch (ClientMdsException ex) {
            message = ex.getDetailMessage();
            code = ex.getStatus().getCode();
        }

        assertEquals("Test the status code", expCode, code);
        if(message != null) {        
            assertEquals("Test the response", expMessage, message);
        }

        // Checks the stub.        
        mdsServerStub.verifySpec(spec);
    }
    
    
    @Test
    public void testGetDois() throws Exception {
        testSpecGetDois(MdsSpec.Spec.GET_DOIS);
    }    

    /**
     * Test of getDoi method, of class ClientMDS with a wrong DOI. A mock server is set for this
     * test in order to emulate the response of the server on a path:
     * <ul>
     * <li>GET /doi/10.5072/2783446.2783605</li>
     * <li>404 : DOI not found</li>
     * </ul>
     * @throws java.lang.Exception
     */
    @Test
    public void testGetDoi404() throws Exception {
        testSpecGetDoi(MdsSpec.Spec.GET_DOI_404);
    }

    /**
     * Test of getDoi method, of class ClientMDS with a wrong authentication. A mock server is set
     * for this test in order to emulate the response of the server on a path:
     * <ul>
     * <li>GET /doi/10.5072/2783446.2783605</li>
     * <li>401 : Bad credentials</li>
     * </ul>
     * @throws java.lang.Exception
     */
    @Test
    public void testGetDoi401() throws Exception {
        testSpecGetDoi(MdsSpec.Spec.GET_DOI_401);
    }

    /**
     * Test of getDoi method, of class ClientMDS with no content as result. A mock server is set for
     * this test in order to emulate the response of the server on a path:
     * <ul>
     * <li>GET /doi/10.5072/2783446.2783605</li>
     * <li>204 : ""</li>
     * </ul>
     * @throws java.lang.Exception
     */
    @Test
    public void testGetDoi204() throws Exception {
        testSpecGetDoi(MdsSpec.Spec.GET_DOI_204);
    }

    /**
     * Test of getDoi method, of class ClientMDS with a login problem. A mock server is set for this
     * test in order to emulate the response of the server on a path:
     * <ul>
     * <li>GET /doi/10.5072/2783446.2783605</li>
     * <li>403 : ""</li>
     * </ul>
     * @throws java.lang.Exception
     */
    @Test
    public void testGetDoi403() throws Exception {
        testSpecGetDoi(MdsSpec.Spec.GET_DOI_403);
    }

    /**
     * Test of getDoi method, of class ClientMDS with an internal error. A mock server is set for
     * this test in order to emulate the response of the server on a path:
     * <ul>
     * <li>GET /doi/10.5072/2783446.2783605</li>
     * <li>500 : "server internal error, try later and if problem persists please contact us"</li>
     * </ul>
     * @throws java.lang.Exception
     */
    @Test
    public void testGetDoi500() throws Exception {
        testSpecGetDoi(MdsSpec.Spec.GET_DOI_500);
    }

    /**
     * Test of getDoi method, of class ClientMDS, with a right DOI. A mock server is set for this
     * test in order to emulate the response of the server on a path:
     * <ul>
     * <li>GET /doi/10.5072/2783446.2783605</li>
     * <li>200 : "https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php"</li>
     * </ul>
     * @throws java.lang.Exception
     */
    @Test
    public void testGetDoi200() throws Exception {
        testSpecGetDoi(MdsSpec.Spec.GET_DOI_200);
    }

    /**
     * Test of createDoi method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateDoi201() throws Exception {
        testSpecCreateDoi(MdsSpec.Spec.POST_DOI_201);
    }

    /**
     * Test of createDoi method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateDoi400() throws Exception {
        testSpecCreateDoi(MdsSpec.Spec.POST_DOI_400);
    }

    /**
     * Test of createDoi method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateDoi401() throws Exception {
        testSpecCreateDoi(MdsSpec.Spec.POST_DOI_401);
    }

    /**
     * Test of createDoi method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateDoi403() throws Exception {
        testSpecCreateDoi(MdsSpec.Spec.POST_DOI_403);
    }

    /**
     * Test of createDoi method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateDoi412() throws Exception {
        testSpecCreateDoi(MdsSpec.Spec.POST_DOI_412);
    }

    /**
     * Test of createDoi method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateDoi500() throws Exception {
        testSpecCreateDoi(MdsSpec.Spec.POST_DOI_500);
    }

    /**
     * Test of getMetadataAsObject method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMetadataAsObject() throws Exception {
        testSpectGetMetadataAsObj(MdsSpec.Spec.GET_METADATA_200);
    }

    /**
     * Test of getMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMetadata200() throws Exception {
        testSpectGetMetadata(MdsSpec.Spec.GET_METADATA_200);
    }

    /**
     * Test of getMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMetadata401() throws Exception {
        testSpectGetMetadata(MdsSpec.Spec.GET_METADATA_401);
    }

    /**
     * Test of getMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMetadata403() throws Exception {
        testSpectGetMetadata(MdsSpec.Spec.GET_METADATA_403);
    }

    /**
     * Test of getMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMetadata404() throws Exception {
        testSpectGetMetadata(MdsSpec.Spec.GET_METADATA_400);
    }

    /**
     * Test of getMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMetadata410() throws Exception {
        testSpectGetMetadata(MdsSpec.Spec.GET_METADATA_410);
    }

    /**
     * Test of getMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMetadata500() throws Exception {
        testSpectGetMetadata(MdsSpec.Spec.GET_METADATA_500);
    }

    private void testSpecCreateMetadata(MdsSpec.Spec spec) throws ClientMdsException {
        mdsServerStub.createSpec(spec);
        Representation entity = new StringRepresentation(
                MdsSpec.XML, org.restlet.data.MediaType.TEXT_XML, Language.ALL, CharacterSet.UTF_8
        );
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        String expResult = spec.getBody();
        int expCode = spec.getStatus();
        String result;
        int code;
        try {
            // Requests the DOIServer using the stub (mode DEV)            
            result = instance.createMetadata(entity);
            code = spec.getStatus();
        } catch (ClientMdsException ex) {
            result = ex.getDetailMessage();
            code = ex.getStatus().getCode();
        }
        assertEquals("Test the status code", expCode, code);
        if(result != null) {
            assertEquals("Test the response", expResult, result);
        }

        // Checks the stub.        
        mdsServerStub.verifySpec(spec);
    }

    private void testSpecCreateMetadataAsObj(MdsSpec.Spec spec) throws Exception {
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);
        Representation entity = new StringRepresentation(
                MdsSpec.XML, org.restlet.data.MediaType.TEXT_XML, Language.ALL, CharacterSet.UTF_8
        );
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        String expResult = spec.getBody();
        int expCode = spec.getStatus();
        String result;
        int code;

        try {
            result = instance.createMetadata(entity);
            code = spec.getStatus();
        } catch (ClientMdsException ex) {
            result = ex.getDetailMessage();
            code = ex.getStatus().getCode();
        }        
        assertEquals("Test the status code", expCode, code);
        if(result != null) {        
            assertEquals("Test the response", expResult, result);
        }

        // Checks the stub.        
        mdsServerStub.verifySpec(spec);
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMetadata201() throws Exception {
        testSpecCreateMetadata(MdsSpec.Spec.POST_METADATA_201);
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMetadata400() throws Exception {
        testSpecCreateMetadata(MdsSpec.Spec.POST_METADATA_400);
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMetadata401() throws Exception {
        testSpecCreateMetadata(MdsSpec.Spec.POST_METADATA_401);
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMetadata403() throws Exception {
        testSpecCreateMetadata(MdsSpec.Spec.POST_METADATA_403);
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMetadata500() throws Exception {
        testSpecCreateMetadata(MdsSpec.Spec.POST_METADATA_500);
    }
    
    /**
     * Test of createMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMetadataObj201() throws Exception {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.POST_METADATA_201);
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMetadataObj400() throws Exception {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.POST_METADATA_400);
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMetadataObj401() throws Exception {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.POST_METADATA_401);
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMetadataObj403() throws Exception {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.POST_METADATA_403);
    }

    /**
     * Test of createMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMetadataObj500() throws Exception {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.POST_METADATA_500);
    }    


    private void testSpecDeleteMetadata(MdsSpec.Spec spec) throws Exception {
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);

        // Requests the DOIServer using the stub (mode DEV)        
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);
        
        final String expResult = spec.getBody();
        final int expCode = spec.getStatus();
        
        String result;
        int code;
        try {
            Representation rep = instance.deleteMetadata(spec.getTemplatePath());
            result = rep.getText();
            code = spec.getStatus();
        } catch (ClientMdsException ex) {
            code = ex.getStatus().getCode();
            result = ex.getDetailMessage();
        }        
        
        assertEquals("Test the status code", expCode, code);
        if(result != null) {
            assertEquals("Test the response", expResult, result);
        }

        mdsServerStub.verifySpec(spec);
    }
    
    private void testSpecDeleteMetadataAsObj(MdsSpec.Spec spec) throws Exception{
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);

        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);

        final JAXBContext ctx = JAXBContext.newInstance(new Class[]{Resource.class});
        final Unmarshaller unMarshaller = ctx.createUnmarshaller();
        final Resource entity = (Resource) unMarshaller.unmarshal(
                new ByteArrayInputStream(MdsSpec.XML.getBytes(StandardCharsets.UTF_8))
        );
        
        String expResult;
        final int expCode = spec.getStatus();
        String result;
        int code;
        try {
            expResult = entity.getIdentifier().getValue();
            Resource rep = instance.deleteMetadataDoiAsObject(spec.getTemplatePath());
            result = rep.getIdentifier().getValue();
            code = spec.getStatus();
        } catch (ClientMdsException ex) {
            expResult = spec.getBody();
            code = ex.getStatus().getCode();
            result = ex.getDetailMessage();
        }        
        
        assertEquals("Test the status code", expCode, code);
        if(result != null) {        
            assertEquals("Test the response", expResult, result);
        }

        // Checks the stub.        
       mdsServerStub.verifySpec(spec);
    }

    /**
     * Test of deleteMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMetadata200() throws Exception {
        testSpecDeleteMetadata(MdsSpec.Spec.DELETE_METADATA_200);
    }
    
    /**
     * Test of deleteMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMetadata401() throws Exception {
        testSpecDeleteMetadata(MdsSpec.Spec.DELETE_METADATA_401);
    }
    
    /**
     * Test of deleteMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMetadata403() throws Exception {
        testSpecDeleteMetadata(MdsSpec.Spec.DELETE_METADATA_403);
    }   
    
    /**
     * Test of deleteMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMetadata404() throws Exception {
        testSpecDeleteMetadata(MdsSpec.Spec.DELETE_METADATA_404);
    }   
    
    /**
     * Test of deleteMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMetadata500() throws Exception {
        testSpecDeleteMetadata(MdsSpec.Spec.DELETE_METADATA_500);
    }
    
    /**
     * Test of deleteMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMetadataObj200() throws Exception {
        testSpecDeleteMetadataAsObj(MdsSpec.Spec.DELETE_METADATA_200);
    }
    
    /**
     * Test of deleteMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMetadataObj401() throws Exception {
        testSpecDeleteMetadataAsObj(MdsSpec.Spec.DELETE_METADATA_401);
    }
    
    /**
     * Test of deleteMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMetadataObj403() throws Exception {
        testSpecDeleteMetadataAsObj(MdsSpec.Spec.DELETE_METADATA_403);
    }   
    
    /**
     * Test of deleteMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMetadataObj404() throws Exception {
        testSpecDeleteMetadataAsObj(MdsSpec.Spec.DELETE_METADATA_404);
    }   
    
    /**
     * Test of deleteMetadata method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMetadataObj500() throws Exception {
        testSpecDeleteMetadataAsObj(MdsSpec.Spec.DELETE_METADATA_500);
    }   
    
    private void testSpecGetMedia(MdsSpec.Spec spec) throws ClientMdsException {
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);

        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);

        final int expCode = spec.getStatus();
        final String expMessage = spec.getBody();
        int code;
        String message;
        try {
            message = instance.getMedia(spec.getTemplatePath());
            code = spec.getStatus();
        } catch (ClientMdsException ex) {
            code = ex.getStatus().getCode();
            message = ex.getDetailMessage();
        }
        
        assertEquals("Test the status code", expCode, code);
        if(message != null) {
            assertEquals("Test the response", expMessage, message);        
        }

        // Checks the stub.                
        mdsServerStub.verifySpec(spec);
    }

    /**
     * Test of getMedia method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMedia200() throws Exception {
        testSpecGetMedia(MdsSpec.Spec.GET_MEDIA_200);
    }
    
    /**
     * Test of getMedia method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMedia401() throws Exception {
        testSpecGetMedia(MdsSpec.Spec.GET_MEDIA_401);
    } 
    
    /**
     * Test of getMedia method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMedia403() throws Exception {
        testSpecGetMedia(MdsSpec.Spec.GET_MEDIA_403);
    }    

    /**
     * Test of getMedia method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMedia404() throws Exception {
        testSpecGetMedia(MdsSpec.Spec.GET_MEDIA_404);
    }
    
    /**
     * Test of getMedia method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMedia500() throws Exception {
        testSpecGetMedia(MdsSpec.Spec.GET_MEDIA_500);
    }    
    
    private void testSpecCreateMedia(MdsSpec.Spec spec) throws ClientMdsException {     
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);

        Form form = new Form();
        form.add("application/fits", "http://cnes.fr/test-data");
        ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV, login, pwd);

        final String expMessage = spec.getBody();
        final int expCode = spec.getStatus();
        
        String message;
        int code;
        try {
            message = instance.createMedia(spec.getTemplatePath(), form);
            code = spec.getStatus();
        } catch (ClientMdsException ex) {
            message = ex.getDetailMessage();
            code = ex.getStatus().getCode();
        }

        assertEquals("Test the status code", expCode, code);
        if(message != null) {
            assertEquals("Test the response", expMessage, message); 
        }
       
        // Checks the stub.                
        mdsServerStub.verifySpec(spec);
    }
    
    
    /**
     * Test of createMedia method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMedia200() throws Exception {
        testSpecCreateMedia(MdsSpec.Spec.POST_MEDIA_200);
    }
    
    /**
     * Test of createMedia method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMedia400() throws Exception {
        testSpecCreateMedia(MdsSpec.Spec.POST_MEDIA_400);
    }  
    
    /**
     * Test of createMedia method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMedia401() throws Exception {
        testSpecCreateMedia(MdsSpec.Spec.POST_MEDIA_401);
    }  
    
    /**
     * Test of createMedia method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMedia403() throws Exception {
        testSpecCreateMedia(MdsSpec.Spec.POST_MEDIA_403);
    }
    
    /**
     * Test of createMedia method, of class ClientMDS.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateMedia500() throws Exception {
        testSpecCreateMedia(MdsSpec.Spec.POST_MEDIA_500);
    }    

}
