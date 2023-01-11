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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.datacite.schema.kernel_4.Resource;
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
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.xml.sax.SAXException;

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.MdsSpec;
import fr.cnes.doi.UnitTest;
import fr.cnes.doi.security.UtilsHeader;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the metadataResource.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(UnitTest.class)
public class MetadataResourceTest {

    private static Client cl;
    private static boolean isDatabaseConfigured;
    
    private static MdsSpec mdsServerStub;
    
    private static final String METADATA_SERVICE = "/mds/metadata/";

    public MetadataResourceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        try{
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
    public void setUp() {
        Assume.assumeTrue("Database is not configured, please configure it and rerun the tests", isDatabaseConfigured);        
        mdsServerStub.reset();        
    }

    @After
    public void tearDown() {        
    }
    
    /**
     * Test of getMetadata method throw a HTTPS server, of class
     * MetadataResource. A Status.SUCCESS_OK is expected when the metadata is
     * available whereas a Status.CLIENT_ERROR_GONE is expected when the
     * metadata is deleted.
     *
     * @throws java.io.IOException - if OutOfMemoryErrors
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problme occurs
     */
    @Test
    public void testGetMetadata() throws IOException, JAXBException, SAXException {
        mdsServerStub.createSpec(MdsSpec.Spec.GET_METADATA_200);

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + METADATA_SERVICE + MdsSpec.Spec.GET_METADATA_200.getTemplatePath());
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
        assertTrue(MdsSpec.Spec.GET_METADATA_200.getStatus() == code);
        assertTrue(MdsSpec.Spec.GET_METADATA_200.getTemplatePath().equals(doi) || doi.isEmpty());

        mdsServerStub.verifySpec(MdsSpec.Spec.GET_METADATA_200);
    }

    /**
     * Test of getMetadata method with a wrong DOI through a HTTPS server, of
     * class MetadataResource. A Status.CLIENT_ERROR_NOT_FOUND is expected
     *
     * @throws java.io.IOException - if OutOfMemoryErrors
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problem occurs
     */
    @Test
    public void testGetMetadataFromWrongDOI() throws IOException, JAXBException, SAXException {        
        mdsServerStub.createSpec(MdsSpec.Spec.GET_METADATA_400);

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + METADATA_SERVICE + MdsSpec.Spec.GET_METADATA_400.getTemplatePath());
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
        assertEquals(MdsSpec.Spec.GET_METADATA_400.getStatus(), code);
        
        mdsServerStub.verifySpec(MdsSpec.Spec.GET_METADATA_400);

    }

    /**
     * Test of getMetadata method, of class MetadataResource.
     * @throws java.io.IOException
     * @throws javax.xml.bind.JAXBException
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testGetMetadataFromWrongPrefix() throws IOException, JAXBException, SAXException {        
        mdsServerStub.createSpec(MdsSpec.Spec.GET_METADATA_400);

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + METADATA_SERVICE + MdsSpec.Spec.GET_METADATA_400.getTemplatePath());
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
        assertEquals(MdsSpec.Spec.GET_METADATA_400.getStatus(), code);
        
        mdsServerStub.verifySpec(MdsSpec.Spec.GET_METADATA_400);

    }      
    /**
     * Test of deleteMetadata method, of class MetadataResource. A
     * Status.SUCCESS_OK is expected
     *
     * @throws javax.xml.bind.JAXBException - if a parsing problem occurs
     * @throws org.xml.sax.SAXException - if a parsing problem occurs
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testDeleteMetadata() throws JAXBException, SAXException, IOException {        
        mdsServerStub.createSpec(MdsSpec.Spec.DELETE_METADATA_200);

        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:" + port + METADATA_SERVICE +MdsSpec.Spec.DELETE_METADATA_200.getTemplatePath());
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
            rep.exhaust();
        } catch (ResourceException ex) {
            code = ex.getStatus().getCode();
        }
        client.release();
        assertEquals(MdsSpec.Spec.DELETE_METADATA_200.getStatus(), code);
        
        mdsServerStub.createSpec(MdsSpec.Spec.DELETE_METADATA_200);       
    }
}
