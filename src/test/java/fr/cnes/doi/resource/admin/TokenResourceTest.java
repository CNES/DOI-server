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
package fr.cnes.doi.resource.admin;

import fr.cnes.doi.InitServerForTest;
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
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Tests Token resource of the Administation application.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class TokenResourceTest {

    private static Client cl;
    public static final String DOI = "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b";
    
    @Rule
    public ExpectedException exceptions = ExpectedException.none();    

    public TokenResourceTest() {
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
        System.out.println("------ TEST TokenResource ------");        
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
     * Test of createToken method, of class TokenResource.
     *
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateToken() throws IOException {
        System.out.println("TEST: CreateToken");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/admin/token");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        client.setNext(cl);
        Form form = new Form();
        form.add("identifier", "jcm");
        form.add("projectID", "828606");
        Representation response = client.post(form);
        String token = response.getText();
        client.release();
        assertNotNull("Test if a token is returned", token);
    }
    
    /**
     * Test of createToken method with wrong parameters, of class TokenResource.
     * A ResourceException is thrown because the send parameters are wrong.
     *
     * @throws ResourceException - 
     */
    @Test
    public void testCreateTokenWithWrongParameters() {
        System.out.println("TEST: CreateToken with wrong parameters");
        exceptions.expect(ResourceException.class);
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/admin/token");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        client.setNext(cl);
        Form form = new Form();
        form.add("identif", "jcm");
        form.add("projec", "828606");
        try {
            Representation response = client.post(form);
        } finally {
            client.release();  
        }            
    }    
    
    /**
     * Test of createToken method with wrong credentials, of class TokenResource.
     * A Status.CLIENT_ERROR_UNAUTHORIZED is expected
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateTokenWithWrongCredentials() throws IOException {
        System.out.println("TEST: CreateToken");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/admin/token");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "test");
        client.setNext(cl);
        Form form = new Form();
        form.add("identifier", "jcm");
        form.add("projectID", "828606");
        Status status;
        try {
            Representation response = client.post(form);
            status = client.getStatus();
        } catch(ResourceException ex) {
            status = ex.getStatus();
        }
        client.release();
        assertEquals("Test if a token is returned with wrong credentials", Status.CLIENT_ERROR_UNAUTHORIZED.getCode(), status.getCode());
    }    

    /**
     * Test of getTokenInformation method with wrong credentials, of class TokenResource.
     * A Status.CLIENT_ERROR_UNAUTHORIZED is expected
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetTokenInformationWithWrongCredentials() throws IOException {
        System.out.println("TEST: GetTokenInformation with wrong credentials");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + "/admin/token");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        client.setNext(cl);
        Form form = new Form();
        form.add("identifier", "jcm");
        form.add("projectID", "828606");
        Representation response = client.post(form);
        String token = response.getText();
        client.release();

        client = new ClientResource("https://localhost:" + port + "/admin/token/" + token);
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "test");
        Status status;
        try {
            Representation rep = client.get();
            status = client.getStatus();
        } catch (ResourceException ex) {
            status=ex.getStatus();
        }
        client.release();
        assertEquals("Test is the information is returned with wrong credentials",Status.CLIENT_ERROR_UNAUTHORIZED.getCode(), status.getCode());

    }

}
