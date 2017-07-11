/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.server.DoiServer;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class FormatCitationResourceTest {

	private static InputStream inputStream = StyleCitationResourceTest.class.getResourceAsStream("/config.properties");
	private static DoiServer doiServer;
	private static DoiSettings instance;
	private Client cl;

	public FormatCitationResourceTest() {
		cl = new Client(new Context(), Protocol.HTTPS);
		Series<Parameter> parameters = cl.getContext().getParameters();
		parameters.add("truststorePath", "jks/doiServerKey.jks");
		parameters.add("truststorePassword", instance.getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
		parameters.add("truststoreType", "JKS");
	}

	@BeforeClass
	public static void setUpClass() {
		try {
			InitSettingsForTest.init();
			instance = DoiSettings.getInstance();
			doiServer = new DoiServer(instance);
			doiServer.start();
		} catch (Exception ex) {
			Logger.getLogger(FormatCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@AfterClass
	public static void tearDownClass() {
		try {
			doiServer.stop();
		} catch (Exception ex) {
			Logger.getLogger(FormatCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of getFormat method, of class FormatCitationResource.
	 */
	@Test
	public void testGetFormat() {
		System.out.println("getFormat");
		String expResult = "Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI ’15. ACM Press. https://doi.org/10.1145/2783446.2783605.\n";
		String result = "";
		String doiName = "10.1145/2783446.2783605";
		String style = "academy-of-management-review";
		String language = "af-ZA";
		ClientResource client = new ClientResource("https://localhost:8183/citation/format");
		client.addQueryParameter("doi", doiName);
		client.addQueryParameter("lang", language);
		client.addQueryParameter("style", style);
		client.setNext(cl);
		Representation rep = client.get();
		try {
			result = rep.getText();
		} catch (IOException ex) {
			Logger.getLogger(FormatCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		assertEquals(expResult, result);
	}

	/**
	 * Test of getFormat method with a bad DOI, of class FormatCitationResource.
	 */
	@Test
	public void testGetFormatWithBadDOI() {
		System.out.println("getFormat");
		int expResult = Status.CLIENT_ERROR_NOT_FOUND.getCode();
		int result = -1;

		ClientResource client;
		String doiName = "xxxx";
		String style = "academy-of-management-review";
		String language = "af-ZA";
		client = new ClientResource("https://localhost:8183/citation/format");
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
		assertEquals(expResult, result);
	}

	/**
	 * Test of getFormat method with a bad style, of class
	 * FormatCitationResource.
	 */
	@Test
	public void testGetFormatWithBadStyle() {
		System.out.println("getFormat");
		int expResult = Status.CLIENT_ERROR_BAD_REQUEST.getCode();
		int result = -1;

		ClientResource client;
		String doiName = "10.1145/2783446.2783605";
		String style = "academy-of-management-rew";
		String language = "af-ZA";
		client = new ClientResource("https://localhost:8183/citation/format");
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
		assertEquals(expResult, result);
	}

	/**
	 * Test of getFormat method with a bad style, of class
	 * FormatCitationResource.
	 */
	@Test
	public void testGetFormatWithBadLang() {
		System.out.println("getFormat");
		int expResult = Status.CLIENT_ERROR_BAD_REQUEST.getCode();
		int result = -1;

		ClientResource client;
		String doiName = "10.1145/2783446.2783605";
		String style = "academy-of-management-review";
		String language = "af-Z";
		client = new ClientResource("https://localhost:8183/citation/format");
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
		assertEquals(expResult, result);
	}

	/**
	 * Test of getFormat method with a bad style, of class
	 * FormatCitationResource.
	 */
	@Test
	public void testGetFormatWithBadLangAndBadDoi() {
		System.out.println("getFormat");
		int expResult = Status.CLIENT_ERROR_NOT_FOUND.getCode();
		int result = -1;

		ClientResource client;
		String doiName = "10.1145/276.27";
		String style = "academy-of-management-review";
		String language = "af-Z";
		client = new ClientResource("https://localhost:8183/citation/format");
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
		assertEquals(expResult, result);
	}
}
