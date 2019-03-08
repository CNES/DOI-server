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
package fr.cnes.doi.resource.citation;

import fr.cnes.doi.CrossCiteSpec;
import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.UnitTest;
import static fr.cnes.doi.client.BaseClient.DATACITE_MOCKSERVER_PORT;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.File;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

/**
 * Test the citation format resource.
 * 
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(UnitTest.class)
public class FormatCitationResourceTest {

    /**
     * Client
     */
    private static Client cl;

    /**
     * CrossCite spécification
     */
    private static CrossCiteSpec spec;

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() {
	InitServerForTest.init(InitSettingsForTest.CONFIG_TEST_PROPERTIES);
	cl = new Client(new Context(), Protocol.HTTPS);
	final Series<Parameter> parameters = cl.getContext().getParameters();
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
	spec = new CrossCiteSpec(DATACITE_MOCKSERVER_PORT);
    }

    @AfterClass
    public static void tearDownClass() {
	spec.finish();
	InitServerForTest.close();
    }

    @Before
    public void setUp() {
	spec.reset();
    }

    @After
    public void tearDown() {

    }

    /**
     * Test of getFormat method, of class FormatCitationResource.
     */
    @Test
    public void testGetFormatHttps() {
	spec.createSpec(CrossCiteSpec.Spec.GET_FORMAT_200);

	String expResult = CrossCiteSpec.Spec.GET_FORMAT_200.getBody();
	String result = "";
	String doiName = "10.1145/2783446.2783605";
	String style = "academy-of-management-review";
	String language = "af-ZA";
	String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
	ClientResource client = new ClientResource(
		"https://localhost:" + port + "/citation/format");
	client.addQueryParameter("doi", doiName);
	client.addQueryParameter("lang", language);
	client.addQueryParameter("style", style);
	client.setNext(cl);
	Representation rep = client.get();
	try {
	    result = rep.getText();
	} catch (IOException ex) {
	}
	client.release();
	assertEquals("Test the citation format through a HTTPS server", expResult, result);

	spec.verifySpec(CrossCiteSpec.Spec.GET_FORMAT_200);

    }

    /**
     * Test of getFormat method with wrong send parameters, of class
     * FormatCitationResource. A ResourceException is thrown
     */
    @Test
    public void testGetFormatHttpsWithWrongParameters() {
	exceptions.expect(ResourceException.class);

	spec.createSpec(CrossCiteSpec.Spec.GET_FORMAT_400);

	String expResult = "Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI ’15. Presented at the the 2015 British HCI Conference, ACM Press. https://doi.org/10.1145/2783446.2783605.\n";
	String result = "";
	String doiName = "10.1145/2783446.2783605";
	String style = "academy-of-management-review";
	String language = "af-ZA";
	String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
	ClientResource client = new ClientResource(
		"https://localhost:" + port + "/citation/format");
	client.addQueryParameter("do", doiName);
	client.addQueryParameter("lan", language);
	client.addQueryParameter("styl", style);
	client.setNext(cl);
	Representation rep = client.get();
	try {
	    result = rep.getText();
	} catch (IOException ex) {
	}
	client.release();
	assertEquals("Test the citation format through a HTTPS server", expResult, result);

	spec.verifySpec(CrossCiteSpec.Spec.GET_FORMAT_400);
    }

    /**
     * Test of getFormat method, of class FormatCitationResource.
     */
    @Test
    public void testGetFormatHttp() {
	spec.createSpec(CrossCiteSpec.Spec.GET_FORMAT_200);

	String expResult = CrossCiteSpec.Spec.GET_FORMAT_200.getBody();
	String result = "";
	String doiName = "10.1145/2783446.2783605";
	String style = "academy-of-management-review";
	String language = "af-ZA";
	String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
	ClientResource client = new ClientResource("http://localhost:" + port + "/citation/format");
	client.addQueryParameter("doi", doiName);
	client.addQueryParameter("lang", language);
	client.addQueryParameter("style", style);
	client.setNext(cl);
	Representation rep = client.get();
	try {
	    result = rep.getText();
	} catch (IOException ex) {
	}
	client.release();
	assertEquals("Test the citation format through a HTTP server", expResult, result);

	spec.verifySpec(CrossCiteSpec.Spec.GET_FORMAT_200);

    }

    /**
     * Test of getFormat method with a wrong DOI, of class FormatCitationResource. A
     * Status.CLIENT_ERROR_NOT_FOUND is expected because the DOI does not exist.
     */
    @Test
    public void testGetFormatWithBadDOI() {
	spec.createSpec(CrossCiteSpec.Spec.GET_FORMAT_404);

	int expResult = Status.CLIENT_ERROR_NOT_FOUND.getCode();
	int result;

	ClientResource client;
	String doiName = "xxxx";
	String style = "academy-of-management-review";
	String language = "af-ZA";
	String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
	client = new ClientResource("https://localhost:" + port + "/citation/format");
	client.addQueryParameter("doi", doiName);
	client.addQueryParameter("lang", language);
	client.addQueryParameter("style", style);
	client.setNext(cl);
	try {
	    client.get();
	    result = client.getStatus().getCode();
	} catch (ResourceException ex) {
	    result = ex.getStatus().getCode();
	}
	client.release();
	assertEquals("Test the response with a given wrong DOI", expResult, result);

	spec.verifySpec(CrossCiteSpec.Spec.GET_FORMAT_404);

    }

    /**
     * Test of getFormat method with a wrong style, of class FormatCitationResource.
     * A Status.CLIENT_ERROR_BAD_REQUEST is expected because the style does not
     * exist among the enumerated list.
     */
    @Test
    public void testGetFormatWithBadStyle() {
	spec.createSpec(CrossCiteSpec.Spec.GET_FORMAT_400);

	int expResult = Status.CLIENT_ERROR_BAD_REQUEST.getCode();
	int result = -1;

	ClientResource client;
	String doiName = "10.1145/2783446.2783605";
	String style = "academy-of-management-rew";
	String language = "af-ZA";
	String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
	client = new ClientResource("https://localhost:" + port + "/citation/format");
	client.addQueryParameter("doi", doiName);
	client.addQueryParameter("lang", language);
	client.addQueryParameter("style", style);
	client.setNext(cl);
	try {
	    client.get();
	    result = client.getStatus().getCode();
	} catch (ResourceException ex) {
	    result = ex.getStatus().getCode();
	}
	client.release();
	assertEquals("Test the response with a given wrong style", expResult, result);

	spec.verifySpec(CrossCiteSpec.Spec.GET_FORMAT_400);

    }

    /**
     * Test of getFormat method with a bad style, of class FormatCitationResource. A
     * Status.CLIENT_ERROR_BAD_REQUEST is expected because the language does not
     * exist among the enumerated list.
     */
    @Test
    public void testGetFormatWithBadLang() {
	spec.createSpec(CrossCiteSpec.Spec.GET_FORMAT_400);

	int expResult = Status.CLIENT_ERROR_BAD_REQUEST.getCode();
	int result = -1;

	ClientResource client;
	String doiName = "10.1145/2783446.2783605";
	String style = "academy-of-management-review";
	String language = "af-Z";
	String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
	client = new ClientResource("https://localhost:" + port + "/citation/format");
	client.addQueryParameter("doi", doiName);
	client.addQueryParameter("lang", language);
	client.addQueryParameter("style", style);
	client.setNext(cl);

	try {
	    client.get();
	    result = client.getStatus().getCode();
	} catch (ResourceException ex) {
	    result = ex.getStatus().getCode();
	}
	client.release();
	assertEquals("Test the response with a given wrong style", expResult, result);

	spec.verifySpec(CrossCiteSpec.Spec.GET_FORMAT_400);

    }

    /**
     * Test of getFormat method with a wrong style and a wrong DOI, of class
     * FormatCitationResource. A Status.CLIENT_ERROR_NOT_FOUND is expected because
     * the DOI does not exist.
     */
    @Test
    public void testGetFormatWithBadLangAndBadDoi() {
	spec.createSpec(CrossCiteSpec.Spec.GET_FORMAT_404);

	int expResult = Status.CLIENT_ERROR_NOT_FOUND.getCode();
	int result = -1;

	ClientResource client;
	String doiName = "10.1145/276.27";
	String style = "academy-of-management-review";
	String language = "af-Z";
	String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
	client = new ClientResource("https://localhost:" + port + "/citation/format");
	client.addQueryParameter("doi", doiName);
	client.addQueryParameter("lang", language);
	client.addQueryParameter("style", style);
	client.setNext(cl);

	try {
	    client.get();
	    result = client.getStatus().getCode();
	} catch (ResourceException ex) {
	    result = ex.getStatus().getCode();
	}
	client.release();
	assertEquals(expResult, result);

	spec.verifySpec(CrossCiteSpec.Spec.GET_FORMAT_404);

    }
}
