/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.ChallengeResponse;

import fr.cnes.doi.InitSettingsForTest;

/**
 *
 * @author malapert
 */
public class ProxySettingsTest {

	private static ProxySettings instance;

	/**
	 * Init the configuration file
	 */
	@BeforeClass
	public static void setUpClass() {
		InitSettingsForTest.init();
		instance = ProxySettings.getInstance();
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
	 * Test of isWithProxy method, of class ProxySettings.
	 */
	@Test
	public void testIsWithProxy() {
		System.out.println("isWithProxy");
		boolean expResult = false;
		boolean result = instance.isWithProxy();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getProxyHost method, of class ProxySettings.
	 */
	@Test
	public void testGetProxyHost() {
		System.out.println("getProxyHost");
		String expResult = "proxy-HTTP2.cnes.fr";
		String result = instance.getProxyHost();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getProxyPort method, of class ProxySettings.
	 */
	@Test
	public void testGetProxyPort() {
		System.out.println("getProxyPort");
		String expResult = "8050";
		String result = instance.getProxyPort();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getProxyUser method, of class ProxySettings.
	 */
	@Test
	public void testGetProxyUser() {
		System.out.println("getProxyUser");
		String result = instance.getProxyUser();
		assertNotNull(result);
	}

	/**
	 * Test of getProxyPassword method, of class ProxySettings.
	 */
	@Test
	public void testGetProxyPassword() {
		System.out.println("getProxyPassword");
		String result = instance.getProxyPassword();
		assertNotNull(result);
	}

	/**
	 * Test of getProxyAuthentication method, of class ProxySettings.
	 */
	@Test
	public void testGetProxyAuthentication() {
		System.out.println("getProxyAuthentication");
		ChallengeResponse result = instance.getProxyAuthentication();
		assertNull(result);
	}

	/**
	 * Test of getNonProxyHosts method, of class ProxySettings.
	 */
	@Test
	public void testGetNonProxyHosts() {
		System.out.println("getNonProxyHosts");
		String expResult = "";
		String result = instance.getNonProxyHosts();
		assertEquals(expResult, result);
	}

}
