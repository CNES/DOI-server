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

import static fr.cnes.doi.AbstractSpec.classTitle;
import static fr.cnes.doi.AbstractSpec.testTitle;
import fr.cnes.doi.InitServerForTest;
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
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Tests the mediaResource.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(UnitTest.class)
public class MediaResourceTest {
    
    private MdsSpec mdsServerStub;      
    private static Client cl;
    
    private static final String MEDIA_SERVICE = "/mds/media/";
    
    public MediaResourceTest() {
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
        classTitle("MediaResource");
    }
    
    @AfterClass
    public static void tearDownClass() {
        InitServerForTest.close();
    }
    
    @Before
    public void setUp() {
        this.mdsServerStub = new MdsSpec(DATACITE_MOCKSERVER_PORT);
    }
    
    @After
    public void tearDown() {
        this.mdsServerStub.finish();
    }
      
    /**
     * Test of getMedias method, of class MediaResource.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetMediasHttps() throws IOException {
        testTitle("testGetMediasHttps");

        this.mdsServerStub.createSpec(MdsSpec.Spec.GET_MEDIA_200);
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + MEDIA_SERVICE +MdsSpec.Spec.GET_MEDIA_200.getTemplatePath());
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(MdsSpec.Spec.GET_MEDIA_200.getStatus(), code);
        
        this.mdsServerStub.verifySpec(MdsSpec.Spec.GET_MEDIA_200);        
    }
    
    /**
     * Test of getMedias method, of class MediaResource.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */    
    @Test
    public void testGetMediasHttp() throws IOException {
        testTitle("testGetMediasHttp");
        
        this.mdsServerStub.createSpec(MdsSpec.Spec.GET_MEDIA_200);
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + MEDIA_SERVICE+MdsSpec.Spec.GET_MEDIA_200.getTemplatePath());        
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(MdsSpec.Spec.GET_MEDIA_200.getStatus(), code);
        
        this.mdsServerStub.verifySpec(MdsSpec.Spec.GET_MEDIA_200);                
    }        

    /**
     * Test of getMedias method when the DOI does not exist through a HTTPS server, of class MediaResource.
     * A Status.CLIENT_ERROR_NOT_FOUND is expected because the DOI
     * is not found in the DataCite database.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetMediasWithWrongDOIHttps() throws IOException {
        testTitle("testGetMediasWithWrongDOIHttps");
        
        this.mdsServerStub.createSpec(MdsSpec.Spec.GET_MEDIA_404);
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + MEDIA_SERVICE+MdsSpec.Spec.GET_MEDIA_404.getTemplatePath());
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(MdsSpec.Spec.GET_MEDIA_404.getStatus(), code);
        
        this.mdsServerStub.verifySpec(MdsSpec.Spec.GET_MEDIA_404);                          
    }
    
    /**
     * Test of getMedias method when the DOI does not exist through a HTTP server, of class MediaResource.
     * A Status.CLIENT_ERROR_NOT_FOUND is expected because the DOI
     * is not found in the DataCite database.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */    
    @Test
    public void testGetMediasWithWrongDOIHttp() throws IOException {
        testTitle("testGetMediasWithWrongDOIHttp");
        
        this.mdsServerStub.createSpec(MdsSpec.Spec.GET_MEDIA_404);
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:" + port + MEDIA_SERVICE+MdsSpec.Spec.GET_MEDIA_404.getTemplatePath());        
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(MdsSpec.Spec.GET_MEDIA_404.getStatus(), code);
        
        this.mdsServerStub.verifySpec(MdsSpec.Spec.GET_MEDIA_404);                                  
    }    

    /**
     * Test of createMedia method, of class MediaResource.
     * A Status.SUCCESS_OK is expected
     */
    @Test    
    public void testCreateMediaForbidden() {
        testTitle("testCreateMediaForbidden");
                
        Form mediaForm = new Form();
        mediaForm.add("image/fits", "https://cnes.fr/sites/default/files/drupal/201508/default/is_cnesmag65-interactif-fr.pdf");
        mediaForm.add("image/jpeg", "https://cnes.fr/sites/default/files/drupal/201508/default/is_cnesmag65-interactif-fr.pdf");
        mediaForm.add("image/png", "https://cnes.fr/sites/default/files/drupal/201508/default/is_cnesmag65-interactif-fr.pdf");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + MEDIA_SERVICE+MdsSpec.Spec.POST_MEDIA_403.getTemplatePath());
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
            Representation rep = client.post(mediaForm);
            code = client.getStatus().getCode();
        } catch(ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(MdsSpec.Spec.POST_MEDIA_403.getStatus(), code);     
    }
    
}
