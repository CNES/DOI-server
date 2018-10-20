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
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Tests the suffixProjects resource of the AdminisrationApplication.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class SuffixProjectsResourceTest {

    @Rule
    public ExpectedException exceptions = ExpectedException.none(); 
    
    /**
     * Cache file for tests.
     */
    private static final String cacheFile = "src"+File.separatorChar+"test"+File.separatorChar+"resources"+File.separatorChar+"projects.conf";
    
    private static Client cl;        

    public SuffixProjectsResourceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        InitServerForTest.init();
        cl = new Client(new Context(), Protocol.HTTPS);
        Series<Parameter> parameters = cl.getContext().getParameters();
        parameters.add("truststorePath", JKS_DIRECTORY+File.separatorChar+JKS_FILE);
        parameters.add("truststorePassword", DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
        parameters.add("truststoreType", "JKS"); 
        System.out.println("------ TEST SuffixProjectsResource ------");        
    }

    @AfterClass
    public static void tearDownClass() {
        InitServerForTest.close();
    }

    @Before
    public void setUp() {
        // Save the projects.conf file
        try {
            Files.copy(new File(SuffixProjectsResourceTest.cacheFile).toPath(),
                    new File(SuffixProjectsResourceTest.cacheFile + ".bak").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        // restore the cache file
        try {
            Files.copy(new File(SuffixProjectsResourceTest.cacheFile + ".bak").toPath(),
                    new File(SuffixProjectsResourceTest.cacheFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.delete(new File(SuffixProjectsResourceTest.cacheFile + ".bak").toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test of getProjectsNameAsJson method, of class SuffixProjectsResource.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetProjectsNameAsJson() throws IOException {
        System.out.println("TEST: getProjectsNameAsJson throw a HTTPS server");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/admin/suffixProject");
        client.setNext(cl);
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        Representation response = client.get(MediaType.APPLICATION_JSON); 
        String projects = response.getText();
        client.release();
        assertNotNull("Test if the response is not null",projects);
        assertTrue("Test if the response is a JSON format",projects.contains("{"));
    }

    /**
     * Test of getProjectsNameAsXml method, of class SuffixProjectsResource.
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testGetProjectsNameAsXml() throws IOException {
        System.out.println("TEST: getProjectsNameAsXml through a HTTPS server");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/admin/suffixProject");
        client.setNext(cl);
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        Representation response = client.get(MediaType.APPLICATION_XML); 
        String projects = response.getText();
        client.release();
        assertNotNull("Test if the response is not null",projects);
        assertTrue("Test is the response is in XML format",projects.contains("<ConcurrentHashMap>"));
    }

    /**
     * Test of createProject method, of class SuffixProjectsResource.
     * This method is used to create a short DOI suffix given for a specific project.     
     * @throws java.io.IOException - if OutOfMemoryErrors
     */
    @Test
    public void testCreateProject() throws IOException {
        System.out.println("TEST: createProject");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/admin/suffixProject");
        client.setNext(cl);
        Form form = new Form();
        form.add("projectName", "Myphhfffcvdscsdfvdffff");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        Representation response = client.post(form); 
        String projectID = response.getText();
        client.release();
        assertNotNull("Test is the server returns the DOI suffix", projectID);
    }
    
    /**
     * Test of createProject method, of class SuffixProjectsResource.
     * A ResourceException is thrown because the submitted parameters are wrongs.
     * @throws ResourceException
     */
    @Test
    public void testCreateProjectWithWrongParameter() {
        System.out.println("TEST: createProject with wrong parameters");
        
        exceptions.expect(ResourceException.class);
        
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/admin/suffixProject");
        client.setNext(cl);
        Form form = new Form();
        form.add("project", "Myphhfffcvdscsdfvdffff");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        try {
            Representation response = client.post(form); 
        } finally {
            client.release();            
        }
    }    

}
