/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.admin;

import fr.cnes.doi.InitServerForTest;
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
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class SuffixProjectsResourceTest {

    /**
     * Cache file for tests
     */
    private static final String cacheFile = "src/test/resources/projects.conf";
    
    private static Client cl;

    public SuffixProjectsResourceTest() {
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
     */
    @Test
    public void testGetProjectsNameAsJson() throws IOException {
        System.out.println("getProjectsNameAsJson");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/admin/suffixProject");
        client.setNext(cl);
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        Representation response = client.get(MediaType.APPLICATION_JSON); 
        String projects = response.getText();
        assertNotNull(projects);
        assertTrue(projects.contains("{"));
    }

    /**
     * Test of getProjectsNameAsXml method, of class SuffixProjectsResource.
     */
    @Test
    public void testGetProjectsNameAsXml() throws IOException {
        System.out.println("getProjectsNameAsXml");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/admin/suffixProject");
        client.setNext(cl);
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        Representation response = client.get(MediaType.APPLICATION_XML); 
        String projects = response.getText();
        assertNotNull(projects);
        assertTrue(projects.contains("<ConcurrentHashMap>"));
    }

    /**
     * Test of createProject method, of class SuffixProjectsResource.
     */
    @Test
    public void testCreateProject() throws IOException {
        System.out.println("createProject");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/admin/suffixProject");
        client.setNext(cl);
        Form form = new Form();
        form.add("projectName", "Myphhfffcvdscsdfvdffff");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        Representation response = client.post(form); 
        String projectID = response.getText();
        assertNotNull(projectID);
    }

}
