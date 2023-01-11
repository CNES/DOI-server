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
package fr.cnes.doi.resource.mds;

import static fr.cnes.doi.client.BaseClient.DATACITE_MOCKSERVER_PORT;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.MdsSpec;
import fr.cnes.doi.UnitTest;
import fr.cnes.doi.client.ClientProxyTest;
import fr.cnes.doi.security.UtilsHeader;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import org.restlet.data.Form;
import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link fr.cnes.doi.resource.mds.MetadatasResource}
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(UnitTest.class)
public class MetadatasResourceTest {
    
    /**
     * Client
     */
    private static Client cl;
    
    /**
     * Is databse configured
     */
    private static boolean isDatabaseConfigured;
    
    /**
     * Specification Metadata Store
     */
    private static MdsSpec mdsServerStub;
    
    /**
     * URI metadata
     */
    private static final String METADATA_SERVICE = "/mds/metadata";

    private String result;
    private InputStream inputStream;
    private InputStream inputStreamFileError; 
    
    @Rule
    public ExpectedException exceptions = ExpectedException.none();     

    @BeforeClass
    public static void setUpClass() {
        try {
            isDatabaseConfigured = true;
            InitServerForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES);

            cl = new Client(new Context(), Protocol.HTTPS);
            Series<Parameter> parameters = cl.getContext().getParameters();
            parameters.set(RESTLET_MAX_TOTAL_CONNECTIONS, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS));        
            parameters.set(RESTLET_MAX_CONNECTIONS_PER_HOST, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST, DEFAULT_MAX_CONNECTIONS_PER_HOST));
            parameters.add("truststorePath", JKS_DIRECTORY+File.separatorChar+JKS_FILE);
            parameters.add("truststorePassword", DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
            parameters.add("truststoreType", "JKS");
        }catch(Error ex) {
            isDatabaseConfigured = false;
        }
        mdsServerStub = new MdsSpec(DATACITE_MOCKSERVER_PORT);
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            InitServerForTest.close();
        } catch(Error ex) {
            
        }
        mdsServerStub.finish();        
    }

    @Before
    public void setUp() throws IOException {
        mdsServerStub.reset();
        this.inputStream = ClientProxyTest.class.getResourceAsStream("/test.xml");
        this.inputStreamFileError = ClientProxyTest.class.getResourceAsStream("/wrongFileTest.xml");    
        Assume.assumeTrue("Database is not configured, please configure it and rerun the tests", isDatabaseConfigured);                        
    }

    @After
    public void tearDown() throws IOException {
        this.inputStream.close();
        this.inputStreamFileError.close();
        
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttpsAsNonAdminWithWrongProjectDOIWithToken() throws IOException {
        testSpecCreateMetadataAsObjWithToken(MdsSpec.Spec.PUT_METADATA_403, inputStream, "testMe", "testMe", "100378", 0);        
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttpsAsNonAdminWithWrongUserWithToken() throws IOException {
        testSpecCreateMetadataAsObjWithToken(MdsSpec.Spec.PUT_METADATA_403, inputStream, "doi_kerberos", "doi_kerberos", "828606", 0);        
    }   
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttpsAsNonAdminWithToken() throws IOException {
        testSpecCreateMetadataAsObjWithToken(MdsSpec.Spec.PUT_METADATA_201, inputStream, "nonadmin", "nonadmin", "828606", 1);        
    }       
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttpsWithToken() throws IOException {
        testSpecCreateMetadataAsObjWithToken(MdsSpec.Spec.PUT_METADATA_201, inputStream, "malapert", "pwd", "828606", 1);        
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A CLIENT_ERROR_BAD_REQUEST status is expected because the metadata file is not valid.
     * The file is validated at the DOIServer level then DataCite is not requested.
     * @throws java.io.IOException
     */    
    @Test
    public void testCreateMetadataHttpsWithWrongFileWithToken() throws IOException {
        testSpecCreateMetadataAsObjWithToken(MdsSpec.Spec.PUT_METADATA_400, inputStreamFileError, "malapert", "pwd", "828606", 0);
    }  
    
    
    /**
     * Test of createMetadata method through HTTPS server with no role, of class MetadatasResource.
     * A CLIENT_ERROR_UNAUTHORIZED is expected because the user is not related to a project then he
     * is not allowed to create a metadata.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttpsWithNoRoleWithToken() throws IOException {
        testSpecCreateMetadataAsObjWithToken(MdsSpec.Spec.PUT_METADATA_401, inputStream, "norole", "norole", null, 0);        
    }
   
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttpsAsNonAdminWithNonAllowProject() throws IOException {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.PUT_METADATA_403, inputStream, "doi_kerberos", "doi_kerberos", "828606", 0);        
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttpsAsNonAdminWithWrongProjectDOI() throws IOException {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.PUT_METADATA_403, inputStream, "testMe", "testMe", "100378", 0);        
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttpsAsNonAdminWithWrongUser() throws IOException {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.PUT_METADATA_403, inputStream, "doi_kerberos", "doi_kerberos", "828606", 0);        
    }   
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttpsAsNonAdmin() throws IOException {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.PUT_METADATA_201, inputStream, "nonadmin", "nonadmin", "828606", 1);        
    }       
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttps() throws IOException {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.PUT_METADATA_201, inputStream, "malapert", "pwd", "828606", 1);        
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A CLIENT_ERROR_BAD_REQUEST status is expected because the metadata file is not valid.
     * The file is validated at the DOIServer level then DataCite is not requested.
     * @throws java.io.IOException
     */    
    @Test
    public void testCreateMetadataHttpsWithWrongFile() throws IOException {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.PUT_METADATA_400, inputStreamFileError, "malapert", "pwd", "828606", 0);
    }  
    
    
    /**
     * Test of createMetadata method through HTTPS server with no role, of class MetadatasResource.
     * A CLIENT_ERROR_UNAUTHORIZED is expected because the user is not related to a project then he
     * is not allowed to create a metadata.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMetadataHttpsWithNoRole() throws IOException {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.PUT_METADATA_401, inputStream, "norole", "norole", null, 0);        
    }
    
    /**
     * Test of createMetadata method through HTTPS server with a user related to two projets with no
     * role provided, of class MetadatasResource.
     * A CLIENT_ERROR_CONFLICT is expected because the user is related to several project and no 
     * role is provided then the DOIServer does not know which role must be applied. No request is 
     * done to DataCite
     * @throws java.io.IOException
     */    
    @Test
    public void testCreateMetadataHttpsWithConflict() throws IOException {
        testSpecCreateMetadataAsObjWithConflict(MdsSpec.Spec.PUT_METADATA_401, inputStream, "malapert", "pwd", null, 0);        
    }
    
    /**
     * The test
     * @param spec the spec
     * @param is the file to register
     * @param login login
     * @param pwd password
     * @param role the role to set (when no role, set to null)
     * @param exactly the number of expected requests to Datacite (-1 when at least 1 request is done)
     */
    private void testSpecCreateMetadataAsObj(MdsSpec.Spec spec, InputStream is, String login, String pwd, String role, int exactly) throws IOException {        
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);

        result = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + METADATA_SERVICE);
        client.setNext(cl);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, login, pwd));
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        if (role != null) {
            headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, role);
        }
        int code;
        try {
            Representation rep = client.post(new StringRepresentation(result, MediaType.APPLICATION_XML));
            code = client.getStatus().getCode();
            rep.exhaust();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        } finally {
            client.release();    
        }        
        assertEquals(spec.getStatus(), code);        
        
        // Checks the stub.        
        if (exactly == -1) {
            mdsServerStub.verifySpec(spec);       
        } else {
            mdsServerStub.verifySpec(spec, exactly);   
        }
             
    }  

    /**
     * The test
     * @param spec the spec
     * @param is the file to register
     * @param login login
     * @param pwd password
     * @param role the role to set (when no role, set to null)
     * @param exactly the number of expected requests to Datacite (-1 when at least 1 request is done)
     */
    private void testSpecCreateMetadataAsObjWithToken(MdsSpec.Spec spec, InputStream is, String login, String pwd, String role, int exactly) throws IOException {        
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/admin/token");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, login, pwd);
        client.setNext(cl);
        Form form = new Form();
        form.add("identifier", login);
        form.add("projectID", role);
        Representation response = client.post(form);
        String token = response.getText();
        client.release();        
        
        result = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
        client = new ClientResource("https://localhost:" + port + METADATA_SERVICE);
        client.setNext(cl);
        ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH_BEARER);
        cr.setRawValue(token.substring(1, token.length()-1));
        client.setChallengeResponse(cr);
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        if (role != null) {
            headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, role);
        }
        int code;
        try {
            Representation rep = client.post(new StringRepresentation(result, MediaType.APPLICATION_XML));
            code = client.getStatus().getCode();
            rep.exhaust();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        } finally {
            client.release();    
        }        
        assertEquals(spec.getStatus(), code);        
        
        // Checks the stub.        
        if (exactly == -1) {
            mdsServerStub.verifySpec(spec);       
        } else {
            mdsServerStub.verifySpec(spec, exactly);   
        }
             
    }       

    /**
     * The test for which a status of 40 is expected.
     * @param spec Ignore it
     * @param is the file to register
     * @param login login
     * @param pwd password
     * @param role the role to set (when no role, set to null)
     * @param exactly the number of expected requests to Datacite (-1 when at least 1 request is done)
     */    
    private void testSpecCreateMetadataAsObjWithConflict(MdsSpec.Spec spec, InputStream is, String login, String pwd, String role, int exactly) throws IOException {        
        // Creates the MetadataStoreService stub        
        mdsServerStub.createSpec(spec);

        result = new BufferedReader(new InputStreamReader(is)).lines()
                .collect(Collectors.joining("\n"));
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + METADATA_SERVICE);
        client.setNext(cl);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, login, pwd));
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        if (role != null) {
            headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
        }
        int code;
        try {
            Representation rep = client.post(new StringRepresentation(result, MediaType.APPLICATION_XML));
            code = client.getStatus().getCode();
            rep.exhaust();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();            
        }
        client.release();
        assertEquals(409, code);        
        
        // Checks the stub.        
        if (exactly == -1) {
            mdsServerStub.verifySpec(spec);       
        } else {
            mdsServerStub.verifySpec(spec, exactly);   
        }                       
    }      
}
