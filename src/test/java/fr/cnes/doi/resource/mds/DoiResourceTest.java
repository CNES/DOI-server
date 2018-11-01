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
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.MdsSpec;
import fr.cnes.doi.UnitTest;
import static fr.cnes.doi.client.BaseClient.DATACITE_MOCKSERVER_PORT;
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
import org.junit.experimental.categories.Category;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(UnitTest.class)
public class DoiResourceTest {
    
    private static Client cl;
    private MdsSpec mdsServerStub;     

    private static final String DOIS_SERVICE = "/mds/dois/";
    
    public DoiResourceTest() {
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
        classTitle("DoiResource");
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
     * Test of getDoi method through a HTTP server, of class DoiResource.
     * A SUCCESS_OK status is expected and the exptected response is <i>https://cfosat.cnes.fr/</i>
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetDoiHttp() throws IOException {
        testTitle("testGetDoiHttp");
        
        this.mdsServerStub.createSpec(MdsSpec.Spec.GET_DOI_200);
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
        ClientResource client = new ClientResource("http://localhost:"+port+DOIS_SERVICE+MdsSpec.Spec.GET_DOI_200.getTemplatePath());
        Representation rep = client.get();
        Status status = client.getStatus();
        
        assertEquals("Test the status code is the right one", MdsSpec.Spec.GET_DOI_200.getStatus(), status.getCode());
        assertEquals("Test the landing page is the right one", MdsSpec.Spec.GET_DOI_200.getBody(), rep.getText());
        
        this.mdsServerStub.verifySpec(MdsSpec.Spec.GET_DOI_200);
    }
    
    /**
     * Test of getDoi method through a HTTP server, of class DoiResource.
     * A SUCCESS_OK status is expected and the expected response is <i>https://cfosat.cnes.fr/</i>
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetDoiHttps() throws IOException {
        testTitle("testGetDoiHttps");
        
        this.mdsServerStub.createSpec(MdsSpec.Spec.GET_DOI_200);
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+DOIS_SERVICE+MdsSpec.Spec.GET_DOI_200.getTemplatePath());
        client.setNext(cl);
        Representation rep = client.get();
        Status status = client.getStatus();
        
        assertEquals("Test the status code is the right one", MdsSpec.Spec.GET_DOI_200.getStatus(), status.getCode());
        assertEquals("Test the landing page is the right one",MdsSpec.Spec.GET_DOI_200.getBody(), rep.getText());
        
        this.mdsServerStub.verifySpec(MdsSpec.Spec.GET_DOI_200);
         
    }    
    
    /**
     * Test of getDoi method when the DOI is not found through a HTTPS server, of class DoiResource.
     * A CLIENT_ERROR_NOT_FOUND status is expected because the DOI does not exist.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetDoiNotFoundHttps() throws IOException {    
        testTitle("testGetDoiNotFoundHttps");
        
        this.mdsServerStub.createSpec(MdsSpec.Spec.GET_DOI_404);
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+DOIS_SERVICE+MdsSpec.Spec.GET_DOI_404.getTemplatePath());
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        } 
        client.release();
        assertEquals(MdsSpec.Spec.GET_DOI_404.getStatus(), code);
        
        this.mdsServerStub.verifySpec(MdsSpec.Spec.GET_DOI_404);
       
    }        
    
    /**
     * Test of getDoi method when the DOI prefix is not the right one through a HTTPS server, of class DoiResource.
     * @throws java.io.IOException
     */
    @Test
    public void testGetDoiNotAllowedHttps() throws IOException {
        testTitle("testGetDoiNotAllowedHttps");        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+DOIS_SERVICE+"10.xxxx/828606");
        client.setNext(cl);
        int code;
        try {
            Representation rep = client.get();
            code = client.getStatus().getCode();
        } catch (ResourceException ex) {   
            code = ex.getStatus().getCode();
        }
        
        assertEquals("Test the status code is the right one", code, Status.CLIENT_ERROR_BAD_REQUEST.getCode());
    }     
    
}
