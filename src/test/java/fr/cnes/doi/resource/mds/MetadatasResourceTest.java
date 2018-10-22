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
import fr.cnes.doi.MdsSpec;
import fr.cnes.doi.client.ClientProxyTest;
import fr.cnes.doi.security.UtilsHeader;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.BufferedReader;
import java.io.File;
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
import org.junit.Rule;
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
import static fr.cnes.doi.client.BaseClient.DATACITE_MOCKSERVER_PORT;

/**
 * Test class for {@link fr.cnes.doi.resource.mds.MetadatasResource}
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MetadatasResourceTest {
    
    @Rule
    public ExpectedException exceptions = ExpectedException.none();     

    private static Client cl;
    private String result;
    private InputStream inputStream;
    private InputStream inputStreamFileError; 
    private MdsSpec mdsServerStub;
    
    private static final String METADATA_SERVICE = "/mds/metadata";

    public MetadatasResourceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        InitServerForTest.init();
        cl = new Client(new Context(), Protocol.HTTPS);
        Series<Parameter> parameters = cl.getContext().getParameters();
        parameters.set(RESTLET_MAX_TOTAL_CONNECTIONS, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS));        
        parameters.set(RESTLET_MAX_CONNECTIONS_PER_HOST, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST, DEFAULT_MAX_CONNECTIONS_PER_HOST));
        parameters.add("truststorePath", JKS_DIRECTORY+File.separatorChar+JKS_FILE);
        parameters.add("truststorePassword", DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
        parameters.add("truststoreType", "JKS");
        System.out.println("------ TEST MetadatasResource ------");        
    }

    @AfterClass
    public static void tearDownClass() {
        InitServerForTest.close();
    }

    @Before
    public void setUp() throws IOException {
        this.inputStream = ClientProxyTest.class.getResourceAsStream("/test.xml");
        this.inputStreamFileError = ClientProxyTest.class.getResourceAsStream("/wrongFileTest.xml");
        mdsServerStub = new MdsSpec(DATACITE_MOCKSERVER_PORT);
    }

    @After
    public void tearDown() throws IOException {
        this.inputStream.close();
        this.inputStreamFileError.close();
        mdsServerStub.finish();
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     */
    @Test
    public void testCreateMetadataHttps() {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.POST_METADATA_201, inputStream, "malapert", "pwd", "828606", 1);        
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A CLIENT_ERROR_BAD_REQUEST status is expected because the metadata file is not valid.
     * The file is validated at the DOIServer level then DataCite is not requested.
     */    
    @Test
    public void testCreateMetadataHttpsWithWrongFile() {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.POST_METADATA_400, inputStreamFileError, "malapert", "pwd", "828606", 0);
    }  
    
    
    /**
     * Test of createMetadata method through HTTPS server with no role, of class MetadatasResource.
     * A CLIENT_ERROR_UNAUTHORIZED is expected because the user is not related to a project then he
     * is not allowed to create a metadata.
     */
    @Test
    public void testCreateMetadataHttpsWithNoRole() {
        testSpecCreateMetadataAsObj(MdsSpec.Spec.POST_METADATA_401, inputStream, "norole", "norole", null, 0);        
    }
    
    /**
     * Test of createMetadata method through HTTPS server with a user related to two projets with no
     * role provided, of class MetadatasResource.
     * A CLIENT_ERROR_CONFLICT is expected because the user is related to several project and no 
     * role is provided then the DOIServer does not know which role must be applied. No request is 
     * done to DataCite
     */    
    @Test
    public void testCreateMetadataHttpsWithConflict() {
        testSpecCreateMetadataAsObjWithConflict(MdsSpec.Spec.POST_METADATA_401, inputStream, "malapert", "pwd", null, 0);        
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
    private void testSpecCreateMetadataAsObj(MdsSpec.Spec spec, InputStream is, String login, String pwd, String role, int exactly) {
        System.out.println("TEST: "+spec.getDescription());
        
        // Creates the MetadataStoreService stub        
        this.mdsServerStub.createSpec(spec);

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
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(spec.getStatus(), code);        
        
        // Checks the stub.        
        if (exactly == -1) {
            this.mdsServerStub.verifySpec(spec);       
        } else {
            this.mdsServerStub.verifySpec(spec, exactly);   
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
    private void testSpecCreateMetadataAsObjWithConflict(MdsSpec.Spec spec, InputStream is, String login, String pwd, String role, int exactly) {
        System.out.println("TEST: Failed to create metadata due to a conflit - Do not know which role to apply");
        
        // Creates the MetadataStoreService stub        
        this.mdsServerStub.createSpec(spec);

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
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(409, code);        
        
        // Checks the stub.        
        if (exactly == -1) {
            this.mdsServerStub.verifySpec(spec);       
        } else {
            this.mdsServerStub.verifySpec(spec, exactly);   
        }                       
    }      
}
