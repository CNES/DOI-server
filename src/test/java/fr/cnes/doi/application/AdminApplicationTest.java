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
package fr.cnes.doi.application;

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.UnitTest;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.datacite.schema.kernel_4.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.experimental.categories.Category;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * Tests API description for the Administration application.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(UnitTest.class)
public class AdminApplicationTest {
    
    private static Client cl;
    
    public AdminApplicationTest() {
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
     * Test of the API description with a HTTP server, of class AdminApplication.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testApiWithHttp() throws IOException {
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);        
        ClientResource client = new ClientResource("http://localhost:"+port+"/");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        Representation repApi = client.options();
        String txt = repApi.getText();
        client.release();
        assertTrue("WADL API through HTTP", txt!=null && !txt.isEmpty());
    }

    /**
     * Test of the API description with a HTTPS server, of class AdminApplication.
     * @throws java.io.IOException
     */
    @Test
    public void testApiWithHttps() throws IOException {
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);        
        ClientResource client = new ClientResource("https://localhost:"+port+"/");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");        
        client.setNext(cl);
        Representation repApi = client.options();
        String txt = repApi.getText();
        client.release();
        assertTrue("WADL API through HTTPS",txt!=null && !txt.isEmpty() && txt.contains("wadl"));
    }    
    
    /**
     * Test of API generation in HTML.
     * @throws Exception 
     */
    @Test
    public void generateAPIWadl() throws Exception {
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);        
        ClientResource client = new ClientResource("http://localhost:"+port+"/?media=text/html"); 
	client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");              
	Representation repApi = client.options();
        String txt = repApi.getText();
        client.release();
        try (FileWriter writer = new FileWriter("admin_api.html")) {
            writer.write(txt);
            writer.flush();
        }
        assertTrue("HTML API through HTTPS",txt!=null && !txt.isEmpty() && txt.contains("html"));
    }    
    
    @Test
    public void jaxb() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<resource\n" +
"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
"    xmlns=\"http://datacite.org/schema/kernel-4\" xsi:schemaLocation=\"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4/metadata.xsd\">\n" +
"    <identifier identifierType=\"DOI\">10.24400/329360/F7Q52MNK</identifier>\n" +
"    <creators>\n" +
"        <creator>\n" +
"            <creatorName nameType=\"Personal\">Gascoin, Simon</creatorName>\n" +
"            <givenName>Simon</givenName>\n" +
"            <familyName>Gascoin</familyName>\n" +
"            <affiliation>CESBIO/CNRS</affiliation>\n" +
"        </creator>\n" +
"        <creator>\n" +
"            <creatorName nameType=\"Personal\">Grizonnet, Manuel</creatorName>\n" +
"            <givenName>Manuel</givenName>\n" +
"            <familyName>Grizonnet</familyName>\n" +
"            <affiliation>CNES</affiliation>\n" +
"        </creator>\n" +
"        <creator>\n" +
"            <creatorName nameType=\"Personal\">Hagolle, Olivier</creatorName>\n" +
"            <givenName>Oliver</givenName>\n" +
"            <familyName>Hagolle</familyName>\n" +
"            <affiliation>CESBIO/CNES</affiliation>\n" +
"        </creator>\n" +
"        <creator>\n" +
"            <creatorName nameType=\"Personal\">Salgues, Germain</creatorName>\n" +
"            <givenName>Germain</givenName>\n" +
"            <familyName>Salgues</familyName>\n" +
"            <affiliation>Magellium</affiliation>\n" +
"        </creator>\n" +
"    </creators>\n" +
"    <contributors>\n" +
"        <contributor contributorType=\"ContactPerson\">\n" +
"            <contributorName nameType=\"Organizational\">exploitation.theia-land@cnes.fr</contributorName>\n" +
"        </contributor>\n" +
"    </contributors>\n" +
"    <titles>\n" +
"        <title>Theia Snow collection</title>\n" +
"    </titles>\n" +
"    <publisher>CNES for THEIA Land data center</publisher>\n" +
"    <publicationYear>2018</publicationYear>\n" +
"    <resourceType resourceTypeGeneral=\"Dataset\">Dataset</resourceType>\n" +
"    <subjects>\n" +
"        <subject>Snow cover area from Sentinel-2 observations</subject>\n" +
"    </subjects>\n" +
"    <dates>\n" +
"        <date dateType=\"Issued\">2017/</date>\n" +
"    </dates>\n" +
"    <relatedIdentifiers>\n" +
"        <relatedIdentifier relatedIdentifierType=\"DOI\" relationType=\"IsDocumentedBy\">10.5281/zenodo.1414452</relatedIdentifier>\n" +
"        <relatedIdentifier relatedIdentifierType=\"URL\" relationType=\"IsDescribedBy\">https://umap.openstreetmap.fr/fr/map/theias-sentinel-2-snow-tiles_156646</relatedIdentifier>\n" +
"        <relatedIdentifier relatedIdentifierType=\"URL\" relationType=\"IsDescribedBy\">http://www.cesbio.ups-tlse.fr/multitemp/?page_id=10748#en</relatedIdentifier>\n" +
"    </relatedIdentifiers>\n" +
"    <rightsList>\n" +
"        <rights rightsURI=\"https://theia.cnes.fr/atdistrib/documents/TC_Sentinel_Data_31072014.pdf\">TERMS AND CONDITIONS FOR THE USE AND DISTRIBUTION OF SENTINEL DATA</rights>\n" +
"    </rightsList>\n" +
"    <descriptions>\n" +
"        <description descriptionType=\"Abstract\">The Theia Snow collection provides the snow presence or absence on the land surface over selected regions from Sentinel-2 observations (cloud-permitting).\n" +
"</description>\n" +
"    </descriptions>\n" +
"    <fundingReferences>\n" +
"        <fundingReference>\n" +
"            <funderName>CNES</funderName>\n" +
"        </fundingReference>\n" +
"    </fundingReferences>\n" +
"</resource>";
        JaxbRepresentation<Resource> resourceEntity = new JaxbRepresentation<>(new StringRepresentation(xml, MediaType.APPLICATION_XML), Resource.class);
        final Resource resource = resourceEntity.getObject();
        System.out.println("**********************************" +resource.getCreators().getCreator().get(0).getAffiliation());
       
        JaxbRepresentation<Resource> result = new JaxbRepresentation<Resource>(resource);
        result.setFormattedOutput(true);
        System.out.println(result.getText());
    }
}
