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
package fr.cnes.doi.resource.admin;

import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
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

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.UnitTest;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

/**
 * Tests the suffixProjects resource of the AdminisrationApplication.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(UnitTest.class)
public class SuffixProjectsResourceTest {

    /**
     * Client
     */
    private static Client cl;

    /**
     * Is database configured
     */
    private static boolean isDatabaseConfigured;

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() {
	try {
	    isDatabaseConfigured = true;
	    InitServerForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES);
	    cl = new Client(new Context(), Protocol.HTTPS);
	    Series<Parameter> parameters = cl.getContext().getParameters();
	    parameters.set(RESTLET_MAX_TOTAL_CONNECTIONS,
		    DoiSettings.getInstance().getString(
			    fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS,
			    DEFAULT_MAX_TOTAL_CONNECTIONS));
	    parameters.set(RESTLET_MAX_CONNECTIONS_PER_HOST,
		    DoiSettings.getInstance().getString(
			    fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST,
			    DEFAULT_MAX_CONNECTIONS_PER_HOST));
	    parameters.add("truststorePath", JKS_DIRECTORY + File.separatorChar + JKS_FILE);
	    parameters.add("truststorePassword",
		    DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
	    parameters.add("truststoreType", "JKS");
	} catch (Error ex) {
	    isDatabaseConfigured = false;
	}
    }

    @AfterClass
    public static void tearDownClass() {
	try {
	    InitServerForTest.close();
	} catch (Error ex) {
	}
    }

    @Before
    public void setUp() {
	Assume.assumeTrue("Database is not configured, please configure it and rerun the tests",
		isDatabaseConfigured);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getProjectsNameAsJson method, of class SuffixProjectsResource.
     *
     * @throws java.io.IOException
     *             - if OutOfMemoryErrors
     */
    @Test
    public void testGetProjectsNameAsJson() throws IOException {
	String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
	ClientResource client = new ClientResource("https://localhost:" + port + "/admin/projects");
	client.setNext(cl);
	client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
	Representation response = client.get(MediaType.APPLICATION_JSON);
	String projects = response.getText();
	client.release();
	assertNotNull("Test if the response is not null", projects);
	assertTrue("Test if the response is a JSON format", projects.contains("{"));
    }

    /**
     * Test of createProject method, of class SuffixProjectsResource. This method is
     * used to create a short DOI suffix given for a specific project.
     *
     * @throws java.io.IOException
     *             - if OutOfMemoryErrors
     */
    @Test
    public void testCreateProject() throws IOException {
	String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
	ClientResource client = new ClientResource("https://localhost:" + port + "/admin/projects");
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
     * Test of createProject method, of class SuffixProjectsResource. A
     * ResourceException is thrown because the submitted parameters are wrongs.
     *
     * @throws ResourceException
     */
    @Test
    public void testCreateProjectWithWrongParameter() {
	exceptions.expect(ResourceException.class);
	String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
	ClientResource client = new ClientResource("https://localhost:" + port + "/admin/projects");
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
