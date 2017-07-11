/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.ext.httpclient.HttpClientHelper;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class ClientProxyTest {

	/**
	 * Init the settings
	 */
	@BeforeClass
	public static void setUpClass() {
		InitSettingsForTest.init();
	}

	/**
	 * Executed after the test class
	 */
	@AfterClass
	public static void tearDownClass() {
	}

	/**
	 * Executed before each test
	 */
	@Before
	public void setUp() {
	}

	/**
	 * Executed after each test
	 */
	@After
	public void tearDown() {
	}

	/**
	 * Test the connection through the proxy. Works only if the test is executed
	 * behind a proxy
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProxy() throws Exception {

		// Execute only if proxy is enabled
		if (DoiSettings.getInstance().getBoolean(Consts.SERVER_PROXY_USED)) {
			Engine.getInstance().getRegisteredClients().clear();
			Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));

			Client proxy = new Client(new Context(), Protocol.HTTP);
			proxy.getContext().getParameters().add("proxyHost",
					DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST));
			proxy.getContext().getParameters().add("proxyPort",
					DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT));

			ClientResource client = new ClientResource("http://www.google.fr");
			client.setProxyChallengeResponse(ChallengeScheme.HTTP_BASIC,
					DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN),
					DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD));
			client.setNext(proxy);

			Representation rep = client.get();
			Status status = client.getStatus();
			Assert.assertTrue("Test si la requete est OK", status.isSuccess());

			System.out.println(rep.getText());
		} else {
			System.out.println("Proxy not enabled, no test");
			Assert.assertTrue("No test executed", true);
		}

	}

	/**
	 * Test the connection through the proxy with BaseClient. Works only if the
	 * test is executed behind a proxy
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBaseClient() throws Exception {
		BaseClient baseClient = new BaseClient("http://www.google.fr");
		if (DoiSettings.getInstance().getBoolean(Consts.SERVER_PROXY_USED)) {
			baseClient.setProxyAuthentication(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST),
					DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT),
					DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN),
					DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD));
		}

		Representation rep = baseClient.client.get();
		Status status = baseClient.client.getStatus();
		Assert.assertTrue("Test si la requete est OK", status.isSuccess());

		System.out.println(rep.getText());

	}

	/**
	 * Test the connection through the proxy with BaseClient and crossCite URL.
	 * Works only if the test is executed behind a proxy
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBaseClientCrossCite() throws Exception {
		BaseClient baseClient = new BaseClient("http://citation.crosscite.org/styles");
		if (DoiSettings.getInstance().getBoolean(Consts.SERVER_PROXY_USED)) {
			baseClient.setProxyAuthentication(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST),
					DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT),
					DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN),
					DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD));
		}

		Representation rep = baseClient.client.get();
		Status status = baseClient.client.getStatus();
		Assert.assertTrue("Test si la requete est OK", status.isSuccess());

	}

}
