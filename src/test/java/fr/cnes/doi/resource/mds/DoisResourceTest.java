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
import fr.cnes.doi.security.UtilsHeader;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.IOException;
import java.util.Map;
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
public class DoisResourceTest {

    private static Client cl;

    public DoisResourceTest() {
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
        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "https://cfosat.cnes.fr/"));

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/dois");
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
        InitServerForTest.close();
    }

    @Before
    public void setUp() {
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
        System.out.println("getDois though a HTTPS server");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/dois");
        client.setNext(cl);
        Representation rep = client.get();
        assertNotNull("Test if the response is not null", rep.getText());
    }

    /**
     * Test of getDois method  though a HTTP server, of class DoisResource.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetDoisHttp() throws IOException {
        System.out.println("getDois though a HTTP server");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + "/mds/dois");
        Representation rep = client.get();
        assertNotNull("Test if the response is not null", rep.getText());
    }

    /**
     * Test of createDoi method when a user is contained in several roles without a selected role, of class DoisResource.
     * Status.CLIENT_ERROR_CONFLICT is expected because the user is 
     * associated to several roles. So, the user must selected one role so that
     * the system applies his rights.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateDoiConflictHttps() throws IOException {
        System.out.println("createDoi without a selected role");
        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/dois");
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
        assertEquals(Status.CLIENT_ERROR_CONFLICT.getCode(), code);
    }

    /**
     * Test of createDoi method, of class DoisResource.
     * A Status.SUCCESS_CREATED is expected.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateDoiHttps() throws IOException {
        System.out.println("createDoi through HTTPS server");

        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/dois");
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
        assertEquals(Status.SUCCESS_CREATED.getCode(), code);
    }
    
    /**
     * Test of createDoi method, of class DoisResource.
     * A Status.SUCCESS_CREATED is expected.     
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateDoiHttp() throws IOException {
        System.out.println("createDoi through a HTTP server");

        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + "/mds/dois");
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
        assertEquals(Status.SUCCESS_CREATED.getCode(), code);
    }  
    
    /**
     * Test of createDoi method with a not registered DOI, of class DoisResource.
     * A Status.CLIENT_ERROR_PRECONDITION_FAILED is expected
     * because the DOI metadata was not registered first.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateFalseDoiHttps() throws IOException {
        System.out.println("createDoi with a not registered DOI");

        Form doiForm = new Form();
        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae"));
        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/dois");
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
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED.getCode(), code);
    }    
    
//    /**
//     * Test of createDoi method, of class DoisResource.
//     */
//    @Test
//    public void testCreateDoiWithWrongPrefixHttps() throws IOException {
//        System.out.println("createDoi");
//
//        Form doiForm = new Form();
//        doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.4072/828606/8c3e91ad45ca855b477126bc073ae"));
//        doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));
//
//        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
//        ClientResource client = new ClientResource("https://localhost:" + port + "/mds/dois");
//        client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
//        final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
//
//        Map<String, Object> reqAttribs = client.getRequestAttributes();
//        Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
//        if (headers == null) {
//            headers = new Series<>(Header.class);
//            reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
//        }
//        headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
//        client.setNext(cl);
//        int code;
//        try {
//            Representation rep = client.post(doiForm);
//            code = client.getStatus().getCode();
//        } catch (ResourceException ex) {
//            code = ex.getStatus().getCode();
//        }
//        assertEquals(Status.CLIENT_ERROR_FORBIDDEN.getCode(), code);
//    }        

}
