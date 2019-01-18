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
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.MdsSpec;
import fr.cnes.doi.UnitTest;
import static fr.cnes.doi.client.BaseClient.DATACITE_MOCKSERVER_PORT;
import fr.cnes.doi.security.UtilsHeader;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.experimental.categories.Category;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Tests the DoisResource
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(UnitTest.class)
public class DoisResourceTest {

    private static Client cl;
    private static MdsSpec mdsSpecStub;   
    
    private static final String DOIS_SERVICE = "/mds/dois";
    
    public DoisResourceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        InitServerForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES);
        cl = new Client(new Context(), Protocol.HTTPS);
        Series<Parameter> parameters = cl.getContext().getParameters();       
        parameters.set(RESTLET_MAX_TOTAL_CONNECTIONS, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS));        
        parameters.set(RESTLET_MAX_CONNECTIONS_PER_HOST, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST, DEFAULT_MAX_CONNECTIONS_PER_HOST));
        parameters.add("truststorePath", JKS_DIRECTORY+File.separatorChar+JKS_FILE);
        parameters.add("truststorePassword", DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
        parameters.add("truststoreType", "JKS");
        mdsSpecStub = new MdsSpec(DATACITE_MOCKSERVER_PORT);
    }

    @AfterClass
    public static void tearDownClass() {
        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "https://cfosat.cnes.fr/"));

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + DOIS_SERVICE);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.post(doiForm);
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        mdsSpecStub.finish();
        InitServerForTest.close();
    }

    @Before
    public void setUp() {    
        mdsSpecStub.reset();
    }

    @After
    public void tearDown() {        
    }    

    /**
     * Test of getDois method through a HTTPS server, of class DoisResource.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetDoisHttps() throws IOException {        
        mdsSpecStub.createSpec(MdsSpec.Spec.GET_COLLECTION_200);

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + DOIS_SERVICE);
        client.setNext(cl);
        Representation rep = client.get();
        assertNotNull("Test if the response is not null", rep.getText());
        client.release();
        mdsSpecStub.verifySpec(MdsSpec.Spec.GET_COLLECTION_200);
    }

    /**
     * Test of getDois method  though a HTTP server, of class DoisResource.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetDoisHttp() throws IOException {        
        mdsSpecStub.createSpec(MdsSpec.Spec.GET_COLLECTION_200);
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + DOIS_SERVICE);
        Representation rep = client.get();
        assertNotNull("Test if the response is not null", rep.getText());
        
        mdsSpecStub.verifySpec(MdsSpec.Spec.GET_COLLECTION_200);          
    }

    /**
     * Test of createDoi method when a user is contained in several roles without a selected role, of class DoisResource.
     * CLIENT_ERROR_CONFLICT status is expected because the user is 
     * associated to several roles. So, the user must selected one role so that
     * the system applies his rights.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateDoiConflictHttps() throws IOException {        
        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + DOIS_SERVICE);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.post(doiForm);
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals("Test if the DOI is related to several accounts", Status.CLIENT_ERROR_CONFLICT.getCode(), code);
    }

    /**
     * Test of createDoi method, of class DoisResource.
     * A Status.SUCCESS_CREATED is expected.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateDoiHttps() throws IOException {
        mdsSpecStub.createSpec(MdsSpec.Spec.POST_DOI_201);
        
        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + DOIS_SERVICE);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";

        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.post(doiForm);
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals("Test if the DOI is related to several accounts with a specific account", MdsSpec.Spec.POST_DOI_201.getStatus(), code);
        
        mdsSpecStub.verifySpec(MdsSpec.Spec.POST_DOI_201);       
    }
    
    /**
     * Test of createDoi method, of class DoisResource.
     * A Status.SUCCESS_CREATED is expected.     
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateDoiHttp() throws IOException {        
        mdsSpecStub.createSpec(MdsSpec.Spec.POST_DOI_201);        

        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + DOIS_SERVICE);
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
            Representation rep = client.post(doiForm);
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        assertEquals("Test the creation of a DOI, related to several accounts with a specific account", MdsSpec.Spec.POST_DOI_201.getStatus(), code);
        
        mdsSpecStub.verifySpec(MdsSpec.Spec.POST_DOI_201);              
    }  
    
    /**
     * Test of createDoi method with a not registered DOI, of class DoisResource.
     * A Status.CLIENT_ERROR_PRECONDITION_FAILED is expected
     * because the DOI metadata was not registered first.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateFalseDoiHttps() throws IOException {        
        mdsSpecStub.createSpec(MdsSpec.Spec.POST_DOI_412);                        

        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + DOIS_SERVICE);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";

        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.post(doiForm);
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals("Test an error of the creation of a DOI when the metadata is not uploaded first",MdsSpec.Spec.POST_DOI_412.getStatus(), code);
        
        mdsSpecStub.verifySpec(MdsSpec.Spec.POST_DOI_201);                     
    }    
    
    /**
     * Test of createDoi method, of class DoisResource.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateDoiWithWrongPrefixHttps() throws IOException {
        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.4072/828606/8c3e91ad45ca855b477126bc073ae"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + DOIS_SERVICE);
        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";

        Map<String, Object> reqAttribs = client.getRequestAttributes();
        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
        }
        headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.post(doiForm);
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals("Test an error of the creation of a DOI when the prefix is wrong", Status.CLIENT_ERROR_FORBIDDEN.getCode(), code);
    }        

}
