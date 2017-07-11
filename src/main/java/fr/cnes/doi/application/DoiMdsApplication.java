/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.GrammarsInfo;
import org.restlet.ext.wadl.IncludeInfo;
import org.restlet.ext.wadl.WadlCnesRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MemoryRealm;
import org.restlet.security.MethodAuthorizer;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.restlet.service.CorsService;

import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.resource.mds.DoiResource;
import fr.cnes.doi.resource.mds.DoisResource;
import fr.cnes.doi.resource.mds.MediaResource;
import fr.cnes.doi.resource.mds.MetadataResource;
import fr.cnes.doi.resource.mds.MetadatasResource;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.Utils;

/**
 * Provides an application for handling Data Object Identifier within an
 * organization. A Digital Object Identifier (DOI) is an alphanumeric string
 * assigned to uniquely identify an object. It is tied to a metadata description
 * of the object as well as to a digital location, such as a URL, where all the
 * details about the object are accessible.
 *
 * @author Jean-Christophe Malapert (jean-Christophe Malapert@cnes.fr)
 * @see <a href="http://www.doi.org/hb.html">DOI Handbook</a>
 * @see "https://mds.datacite.org/static/apidoc"
 */
public class DoiMdsApplication extends BaseApplication {

	/**
	 * Template Query for DOI.
	 */
	public static final String DOI_TEMPLATE = "doiName";

	/**
	 * URI to handle the collection of DOIs.
	 */
	public static final String DOI_URI = "/dois";

	/**
	 * URI to handle a DOI.
	 */
	public static final String DOI_NAME_URI = "/{" + DOI_TEMPLATE + "}";

	/**
	 * URI to handle metadata.
	 */
	public static final String METADATAS_URI = "/metadata";

	/**
	 * URI to handle media.
	 */
	public static final String MEDIA_URI = "/media";

	/**
	 * Schema.
	 */
	private final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	/**
	 * Instance of configuration settings.
	 */
	private final DoiSettings config = DoiSettings.getInstance();

	/**
	 * Application logger.
	 */
	private static final Logger LOGGER = Utils.getAppLogger();

	/**
	 * Client to query Mds Datacite.
	 */
	private final ClientMDS client;

	/**
	 * Creates the Digital Object Identifier server application.
	 */
	public DoiMdsApplication() {
		super();
		LOGGER.entering(DoiMdsApplication.class.getName(), "Constructor");

		setName("Digital Object Identifier server application");
		setDescription("Provides an application for handling Data Object Identifier at CNES<br/>"
				+ "This application provides 3 API:" + "<ul>" + "<li>dois : DOI minting</li>"
				+ "<li>metadata : Registration of the associated metadata</li>"
				+ "<li>media : Possbility to obtain metadata in various formats and/or get automatic, direct access to an object rather than via the \"landing page\"</li>"
				+ "</ul>");
		setOwner("Centre National d'Etudes Spatiales (CNES)");
		setAuthor("Jean-Christophe Malapert (DNO/ISA/VIP)");
		setStatusService(new CnesStatusService());
		getServices().add(createCoreService());
		String contextUse = DoiSettings.getInstance().getString(Consts.CONTEXT_MODE);
		client = new ClientMDS(ClientMDS.Context.valueOf(contextUse), getLoginMds(), getPwdMds());

		LOGGER.exiting(DoiMdsApplication.class.getName(), "Constructor");
	}

	private CorsService createCoreService() {
		LOGGER.entering(DoiMdsApplication.class.getName(), "createCoreService");

		CorsService corsService = new CorsService();
		corsService.setAllowedOrigins(new HashSet(Arrays.asList("*")));
		corsService.setAllowedCredentials(true);

		LOGGER.exiting(DoiMdsApplication.class.getName(), "createCoreService", corsService);
		return corsService;
	}

	/**
	 * Assigns routes for Mds application securizes the access.
	 * 
	 * @return Router
	 */
	@Override
	public Restlet createInboundRoot() {
		LOGGER.entering(DoiMdsApplication.class.getName(), "createInboundRoot");

		// ChallengeAuthenticator
		ChallengeAuthenticator ca = createAuthenticator();
		ca.setOptional(true);

		// MethodAuthorizer
		MethodAuthorizer ma = createMethodAuthorizer();
		ca.setNext(ma);

		// Router
		ma.setNext(createRouter());

		LOGGER.exiting(DoiMdsApplication.class.getName(), "createInboundRoot", ca);
		return ca;
	}

	/**
	 * Creates the router.
	 *
	 * @return the router
	 */
	private Router createRouter() {
		LOGGER.entering(DoiMdsApplication.class.getName(), "createRouter");

		Router router = new Router(getContext());
		router.attach(DOI_URI, DoisResource.class);
		router.attach(DOI_URI + DOI_NAME_URI, DoiResource.class);
		router.attach(METADATAS_URI, MetadatasResource.class);
		router.attach(METADATAS_URI + DOI_NAME_URI, MetadataResource.class);
		router.attach(MEDIA_URI + DOI_NAME_URI, MediaResource.class);

		LOGGER.exiting(DoiMdsApplication.class.getName(), "createRouter", router);

		return router;
	}

	/**
	 * Creates the method authorizer. GET method can be anonymous. The verbs
	 * (POST, PUT, DELETE) need to be authenticated.
	 *
	 * @return Authorizer based on authorized methods
	 */
	private MethodAuthorizer createMethodAuthorizer() {
		LOGGER.entering(DoiMdsApplication.class.getName(), "createMethodAuthorizer");

		MethodAuthorizer methodAuth = new MethodAuthorizer();
		methodAuth.getAnonymousMethods().add(Method.GET);
		methodAuth.getAuthenticatedMethods().add(Method.GET);
		methodAuth.getAuthenticatedMethods().add(Method.POST);
		methodAuth.getAuthenticatedMethods().add(Method.PUT);
		methodAuth.getAuthenticatedMethods().add(Method.DELETE);

		LOGGER.exiting(DoiMdsApplication.class.getName(), "createMethodAuthorizer", methodAuth);
		return methodAuth;
	}

	/**
	 * Creates the authenticator. Creates the user, role and mapping user/role.
	 *
	 * @return Authenticator based on a challenge scheme
	 */
	private ChallengeAuthenticator createAuthenticator() {
		LOGGER.entering(DoiMdsApplication.class.getName(), "createAuthenticator");

		ChallengeAuthenticator guard = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "realm");

		// Create in-memory users with roles
		MemoryRealm realm = new MemoryRealm();
		User user = new User("jcm", "user");
		realm.getUsers().add(user);
		realm.map(user, Role.get(this, "project1"));
		realm.map(user, Role.get(this, "project3"));
		user = new User("toto", "user");
		realm.getUsers().add(user);
		realm.map(user, Role.get(this, "project1"));
		user = new User("tata", "user");
		realm.getUsers().add(user);
		realm.map(user, Role.get(this, "project2"));
		user = new User("tutu", "user");
		realm.getUsers().add(user);
		realm.map(user, Role.get(this, "project2"));
		user = new User("titi", "user");
		realm.getUsers().add(user);
		realm.map(user, Role.get(this, "project2"));

		// Attach verifier to check authentication and enroler to determine
		// roles
		guard.setVerifier(realm.getVerifier());
		guard.setEnroler(realm.getEnroler());

		LOGGER.exiting(DoiMdsApplication.class.getName(), "createAuthenticator", guard);

		return guard;
	}

	// private RoleAuthorizer createRoleAuthorizer() {
	// //Authorize owners and forbid users on roleAuth's children
	// RoleAuthorizer roleAuth = new RoleAuthorizer();
	// roleAuth.getAuthorizedRoles().add(Role.get(this, ROLE_OWNER));
	// roleAuth.getForbiddenRoles().add(Role.get(this, ROLE_USER));
	// return roleAuth;
	// }
	/**
	 * Returns the object to valid the datacite schema.
	 *
	 * @return
	 */
	public SchemaFactory getSchemaFactory() {
		LOGGER.entering(DoiMdsApplication.class.getName(), "getSchemaFactory");
		LOGGER.exiting(DoiMdsApplication.class.getName(), "getSchemaFactory", this.schemaFactory);
		return this.schemaFactory;
	}

	/**
	 * Returns the decrypted login for DataCite.
	 *
	 * @return the DataCite's login
	 */
	private String getLoginMds() {
		LOGGER.entering(DoiMdsApplication.class.getName(), "getLoginMds");
		LOGGER.exiting(DoiMdsApplication.class.getName(), "getLoginMds", this.config.getSecret(Consts.INIST_LOGIN));
		return this.config.getSecret(Consts.INIST_LOGIN);
	}

	/**
	 * Returns the decrypted password for DataCite.
	 *
	 * @return the DataCite's pwd
	 */
	private String getPwdMds() {
		LOGGER.entering(DoiMdsApplication.class.getName(), "getPwdMds");
		LOGGER.exiting(DoiMdsApplication.class.getName(), "getPwdMds", this.config.getSecret(Consts.INIST_PWD));
		return this.config.getSecret(Consts.INIST_PWD);
	}

	/**
	 * Returns the DOI prefix.
	 *
	 * @return the DOI prefix
	 */
	public String getDataCentrePrefix() {
		LOGGER.entering(DoiMdsApplication.class.getName(), "getDataCentrePrefix");
		LOGGER.exiting(DoiMdsApplication.class.getName(), "getDataCentrePrefix",
				this.config.getString(Consts.INIST_DOI));
		return this.config.getString(Consts.INIST_DOI);
	}

	/**
	 * Returns the client.
	 *
	 * @return the client
	 */
	public ClientMDS getClient() {
		return this.client;
	}

	/**
	 * Method to describe application in the WADL.
	 * 
	 * @param request
	 *            Request
	 * @param response
	 *            Response
	 * @return the application description for WADL
	 */
	@Override
	public final ApplicationInfo getApplicationInfo(final Request request, final Response response) {
		final ApplicationInfo result = super.getApplicationInfo(request, response);
		final DocumentationInfo docInfo = new DocumentationInfo(
				"DOI server application provides is central service that registers DOI at DataCite");
		docInfo.setTitle(this.getName());
		docInfo.setTextContent(this.getDescription());
		result.setDocumentation(docInfo);
		result.getNamespaces().put("https://schema.datacite.org/meta/kernel-4.0/metadata.xsd", "default");
		result.getNamespaces().put("http://www.w3.org/2001/XMLSchema", "xsi");
		final GrammarsInfo grammar = new GrammarsInfo();
		final IncludeInfo include = new IncludeInfo();
		include.setTargetRef(new Reference("https://schema.datacite.org/meta/kernel-4.0/metadata.xsd"));
		grammar.getIncludes().add(include);
		result.setGrammars(grammar);
		return result;
	}

	/**
	 * Creates HTML representation of the WADL.
	 * 
	 * @param applicationInfo
	 *            Application description
	 * @return the HTML representation of the WADL
	 */
	@Override
	protected Representation createHtmlRepresentation(ApplicationInfo applicationInfo) {
		WadlCnesRepresentation wadl = new WadlCnesRepresentation(applicationInfo);
		return wadl.getHtmlRepresentation();
	}

}
