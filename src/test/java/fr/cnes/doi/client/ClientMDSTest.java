/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.settings.DoiSettings;

/**
 *
 * @author malapert
 */
public class ClientMDSTest {

	private static DoiSettings settings;

	@BeforeClass
	public static void setUpClass() {
		InitSettingsForTest.init();
		settings = DoiSettings.getInstance();
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
		// try {
		// testCreateMetadata_Resource();
		// //testCreateDoi();
		// } catch (Exception ex) {
		// Logger.getLogger(ClientMDSTest.class.getName()).log(Level.SEVERE,
		// null, ex);
		// }
	}

	@After
	public void tearDown() {
	}

	// /**
	// * Test of setProxyAuthentication method, of class ClientMDS.
	// */
	// @Test
	// public void testSetProxyAuthentication() {
	// System.out.println("setProxyAuthentication");
	// ChallengeResponse authentication = null;
	// ClientMDS instance = null;
	// instance.setProxyAuthentication(authentication);
	// // TODO review the generated test code and remove the default call to
	// fail.
	// fail("The test case is a prototype.");
	// }
	//
	// /**
	// * Test of getDoiCollection method, of class ClientMDS.
	// */
	// @Test
	// public void testGetDoiCollection() throws Exception {
	// System.out.println("getDoiCollection");
	// ClientMDS instance = new ClientMDS(ClientMDS.Context.DEV,
	// settings.getSecret(Consts.INIST_LOGIN),
	// settings.getSecret(Consts.INIST_PWD));
	// String expResult = "";
	// String result = instance.getDoiCollection();
	// assertEquals(expResult, result);
	// }
	//
	// /**
	// * Test of getDoi method, of class ClientMDS.
	// */
	// @Test
	// public void testGetDoi() throws Exception {
	// System.out.println("getDoi");
	// String doiName = "";
	// ClientMDS instance = null;
	// String expResult = "";
	// String result = instance.getDoi(doiName);
	// assertEquals(expResult, result);
	// // TODO review the generated test code and remove the default call to
	// fail.
	// fail("The test case is a prototype.");
	// }
	//
	//
	//
	// /**
	// * Test of createDoi method, of class ClientMDS.
	// */
	// @Test
	// public void testCreateDoi() throws Exception {
	// System.out.println("createDoi");
	// Form form = null;
	// ClientMDS instance = null;
	// String expResult = "";
	// String result = instance.createDoi(form);
	// assertEquals(expResult, result);
	// // TODO review the generated test code and remove the default call to
	// fail.
	// fail("The test case is a prototype.");
	// }
	//
	// /**
	// * Test of getMetadataAsObject method, of class ClientMDS.
	// */
	// @Test
	// public void testGetMetadataAsObject() throws Exception {
	// System.out.println("getMetadataAsObject");
	// String doiName = "";
	// ClientMDS instance = null;
	// Resource expResult = null;
	// Resource result = instance.getMetadataAsObject(doiName);
	// assertEquals(expResult, result);
	// // TODO review the generated test code and remove the default call to
	// fail.
	// fail("The test case is a prototype.");
	// }
	//
	// /**
	// * Test of getMetadata method, of class ClientMDS.
	// */
	// @Test
	// public void testGetMetadata() throws Exception {
	// System.out.println("getMetadata");
	// String doiName = "";
	// ClientMDS instance = null;
	// Representation expResult = null;
	// Representation result = instance.getMetadata(doiName);
	// assertEquals(expResult, result);
	// // TODO review the generated test code and remove the default call to
	// fail.
	// fail("The test case is a prototype.");
	// }
	//
	// /**
	// * Test of createMetadata method, of class ClientMDS.
	// */
	// @Test
	// public void testCreateMetadata_Representation() throws Exception {
	// System.out.println("createMetadata");
	// Representation entity = null;
	// ClientMDS instance = null;
	// String expResult = "";
	// String result = instance.createMetadata(entity);
	// assertEquals(expResult, result);
	// // TODO review the generated test code and remove the default call to
	// fail.
	// fail("The test case is a prototype.");
	// }

	/**
	 * Test of createMetadata method, of class ClientMDS.
	 */
	// @Test
	// public void testCreateMetadata_Resource() throws Exception {
	// System.out.println("createMetadata");
	// Resource entity = new Resource();
	// Identifier identifier = new Identifier();
	// identifier.setValue("10.24400/test");
	// entity.setIdentifier(identifier);
	//
	// Creator creator = new Creator();
	// creator.setCreatorName("Malapert, Jean-Christophe");
	// Creators creators = new Resource.Creators();
	// creators.getCreator().add(creator);
	// entity.setCreators(creators);
	//
	// Titles titles = new Titles();
	// Title title = new Title();
	// title.setValue("My title");
	// titles.getTitle().add(title);
	// entity.setTitles(titles);
	//
	// entity.setPublisher("Centre National d'Etudes Spatiales (CNES)");
	// entity.setPublicationYear("2017");
	//
	// Resource.ResourceType resType = new Resource.ResourceType();
	// resType.setResourceTypeGeneral(ResourceType.SOFTWARE);
	// entity.setResourceType(resType);
	//
	// ClientMDS instance = new ClientMDS(ClientMDS.Context.PRE_PROD,
	// settings.getSecret(Consts.INIST_LOGIN),
	// settings.getSecret(Consts.INIST_PWD));
	// String expResult = "";
	// String result = instance.createMetadata(entity);
	// FileOutputStream fos = new FileOutputStream(new File("/tmp/test.jc"));
	// fos.write(result.getBytes());
	// fos.flush();
	// fos.close();
	// assertEquals(expResult, result);
	// }

	// /**
	// * Test of deleteMetadataDoiAsObject method, of class ClientMDS.
	// */
	// @Test
	// public void testDeleteMetadataDoiAsObject() throws Exception {
	// System.out.println("deleteMetadataDoiAsObject");
	// String doiName = "";
	// ClientMDS instance = null;
	// Resource expResult = null;
	// Resource result = instance.deleteMetadataDoiAsObject(doiName);
	// assertEquals(expResult, result);
	// // TODO review the generated test code and remove the default call to
	// fail.
	// fail("The test case is a prototype.");
	// }
	//
	// /**
	// * Test of deleteMetadata method, of class ClientMDS.
	// */
	// @Test
	// public void testDeleteMetadata() throws Exception {
	// System.out.println("deleteMetadata");
	// String doiName = "";
	// ClientMDS instance = null;
	// Representation expResult = null;
	// Representation result = instance.deleteMetadata(doiName);
	// assertEquals(expResult, result);
	// // TODO review the generated test code and remove the default call to
	// fail.
	// fail("The test case is a prototype.");
	// }
	//
	// /**
	// * Test of getMedia method, of class ClientMDS.
	// */
	// @Test
	// public void testGetMedia() throws Exception {
	// System.out.println("getMedia");
	// String doiName = "";
	// ClientMDS instance = null;
	// String expResult = "";
	// String result = instance.getMedia(doiName);
	// assertEquals(expResult, result);
	// // TODO review the generated test code and remove the default call to
	// fail.
	// fail("The test case is a prototype.");
	// }
	//
	// /**
	// * Test of createMedia method, of class ClientMDS.
	// */
	// @Test
	// public void testCreateMedia() throws Exception {
	// System.out.println("createMedia");
	// Form form = null;
	// ClientMDS instance = null;
	// String expResult = "";
	// String result = instance.createMedia(form);
	// assertEquals(expResult, result);
	// // TODO review the generated test code and remove the default call to
	// fail.
	// fail("The test case is a prototype.");
	// }

}
