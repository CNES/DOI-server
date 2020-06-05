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
package fr.cnes.doi.integration;


import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.MdsSpec;
import fr.cnes.doi.UnitTest;
import fr.cnes.doi.client.ClientProxyTest;
import fr.cnes.doi.integration.IntegrationTest;
import fr.cnes.doi.resource.mds.DoisResource;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import static org.junit.Assert.assertEquals;

/**
 * Tests the DoisResource
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(IntegrationTest.class)
public class ITDoiCreationDatacite {

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
    private static final String DOIS_SERVICE = "/mds/dois";
    
    private String result;
    private InputStream inputStream;
    
    @Rule
    public ExpectedException exceptions = ExpectedException.none();     

    @BeforeClass
    public static void setUpClass() {
        try {
            isDatabaseConfigured = true;
            InitServerForTest.init(InitSettingsForTest.CONFIG_IT_DATACITE_PROPERTIES);

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
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            InitServerForTest.close();
        } catch(Error ex) {
            
        }
    }

    @Before
    public void setUp() throws IOException {
        this.inputStream = ClientProxyTest.class.getResourceAsStream("/flat.xml");
        Assume.assumeTrue("Database is not configured, please configure it and rerun the tests", isDatabaseConfigured);                        
    }

    @After
    public void tearDown() throws IOException {
        this.inputStream.close();
        
    }
    
    /**
     * Test of createMetadata method through HTTPS server, of class MetadatasResource.
     * A SUCCESS_CREATED status is expected.
     */
    @Test
    public void testCreateDOIToDatacite() throws IOException {
        // create token
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/admin/token");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "testMe", "testMe");
        client.setNext(cl);
        Form form = new Form();
        form.add("identifier", "100378/test");
        form.add("projectID", "100378");
        Representation response = client.post(form);
        String token = response.getText();
        client.release();        
        
        // upload metadata
        result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));                
        client = new ClientResource("https://localhost:" + port + METADATA_SERVICE);
        client.setNext(cl);
        ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH_BEARER);
        cr.setRawValue(token);
        client.setChallengeResponse(cr);
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "100378");
        
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
        assertEquals(201, code);  

        // set the landing page
        final Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER,
                "10.80163/100378/test"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));
        client = new ClientResource("https://localhost:" + port + DOIS_SERVICE);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "testMe",
                "testMe"));
        client.setNext(cl);
        cr = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH_BEARER);
        cr.setRawValue(token);
        client.setChallengeResponse(cr);
        reqAttribs = client.getRequestAttributes();
        headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "100378");       
        try {
            Representation rep = client.post(doiForm);
            code = client.getStatus().getCode();
            rep.exhaust();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();   
       assertEquals(201, code);  
        
    }
           
}
