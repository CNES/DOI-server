/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import static org.junit.Assert.assertEquals;

import java.util.List;
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
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.server.DoiServer;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class LanguageCitationResourceTest {

	private static DoiServer doiServer;
	private static DoiSettings instance;
	private Client cl;

	public LanguageCitationResourceTest() {
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
		} catch (Exception ex) {
			Logger.getLogger(StyleCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of getLanguages method, of class LanguageCitationResource.
	 */
	@Test
	public void testGetLanguages() {
		System.out.println("getLanguages");
		String expResult = "af-ZA";
		String result = "";
		try {
			doiServer.start();
			ClientResource client = new ClientResource("https://localhost:8183/citation/language");
			client.setNext(cl);
			List<String> rep = client.get(List.class);
			result = rep.get(0);
		} catch (Exception ex) {
			Logger.getLogger(LanguageCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				doiServer.stop();
			} catch (Exception ex) {
				Logger.getLogger(LanguageCitationResourceTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			assertEquals(expResult, result);
		}
	}

}
