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
import org.restlet.engine.Engine;
import org.restlet.ext.httpclient.HttpClientHelper;
import org.restlet.resource.ClientResource;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class ClientProxyTest {

	public ClientProxyTest() {

	}

	/**
	 * Init the settings
	 */
	@BeforeClass
	public static void setUpClass() {
		InitSettingsForTest.init();
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

			System.out.println(client.get().getText());
		} else {
			Assert.assertTrue("No test executed", true);
		}

	}

}
