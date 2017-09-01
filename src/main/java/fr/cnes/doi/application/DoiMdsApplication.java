/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

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
import org.restlet.security.MethodAuthorizer;

import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.resource.mds.DoiResource;
import fr.cnes.doi.resource.mds.DoisResource;
import fr.cnes.doi.resource.mds.MediaResource;
import fr.cnes.doi.resource.mds.MetadataResource;
import fr.cnes.doi.resource.mds.MetadatasResource;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.Requirement;
import fr.cnes.doi.security.TokenBasedVerifier;
import fr.cnes.doi.db.TokenDB;
import fr.cnes.doi.security.TokenSecurity;
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
@Requirement(
        reqId = "DOI_SRV_010",
        reqName = "Création de métadonnées"
)
@Requirement(
        reqId = "DOI_SRV_020",
        reqName = "Enregistrement d'un DOI"
)
@Requirement(
        reqId = "DOI_SRV_030",
        reqName = "Mise à jour de l'URL d'un DOI"
)
@Requirement(
        reqId = "DOI_SRV_040",
        reqName = "Mise à jour des métadonnées d'un DOI"
)
@Requirement(
        reqId = "DOI_SRV_050",
        reqName = "Désactivation d'un DOI"
)
@Requirement(
        reqId = "DOI_SRV_060",
        reqName = "Récupération des métadonnées"
)
@Requirement(
        reqId = "DOI_SRV_070",
        reqName = "Récupération de l'URL"
)
@Requirement(
        reqId = "DOI_SRV_080",
        reqName = "Création d'un média"
)
@Requirement(
        reqId = "DOI_SRV_090",
        reqName = "Récupération des médias"
)
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
    
    public static final String NAME = "Metadata Store Application";        

    /**
     * Client to query Mds Datacite.
     */
    private final ClientMDS client;
    
    /**
     * Token DB that contains the set of generated token.
     */
    private final TokenDB tokenDB;

    /**
     * Creates the Digital Object Identifier server application.
     */
    @Requirement(
            reqId = "DOI_DOC_010",
            reqName = "Documentation des interfaces"
    )
    public DoiMdsApplication() {
        super();
        LOGGER.entering(DoiMdsApplication.class.getName(), "Constructor");

        setName(NAME);
        setDescription("Provides an application for handling Data Object Identifier at CNES<br/>"
                + "This application provides 3 API:" + "<ul>" + "<li>dois : DOI minting</li>"
                + "<li>metadata : Registration of the associated metadata</li>"
                + "<li>media : Possbility to obtain metadata in various formats and/or get automatic, direct access to an object rather than via the \"landing page\"</li>"
                + "</ul>");
        setOwner("Centre National d'Etudes Spatiales (CNES)");
        setAuthor("Jean-Christophe Malapert (DNO/ISA/VIP)");
        setStatusService(new CnesStatusService());
        getServices().add(this.createCoreService(DEFAULT_CORS_ORIGIN, DEFAULT_CORS_CREDENTIALS));        
        String contextUse = DoiSettings.getInstance().getString(Consts.CONTEXT_MODE);
        client = new ClientMDS(ClientMDS.Context.valueOf(contextUse), getLoginMds(), getPwdMds());
        
        this.tokenDB = TokenSecurity.getInstance().getTokenDB();

        LOGGER.exiting(DoiMdsApplication.class.getName(), "Constructor");
    }

    /**
     * Assigns routes for Mds application securizes the access.
     *
     * @return Router
     */
    @Requirement(
            reqId = "DOI_AUTH_050",
            reqName = "Vérification du projet"
    )
    @Override
    public Restlet createInboundRoot() {
        LOGGER.entering(DoiMdsApplication.class.getName(), "createInboundRoot");

        // Defines the strategy of authentication (authentication is not required)
        //   - authentication with login/pwd
        ChallengeAuthenticator ca = createAuthenticator();
        ca.setOptional(true);
        
        //   - authentication with token
        ChallengeAuthenticator ct = createTokenAuthenticator();
        ct.setOptional(true);
        
        //  create a pipeline of authentication
        ca.setNext(ct);

        // Set specific authroization on method after checking authentication
        MethodAuthorizer ma = createMethodAuthorizer();
        ct.setNext(ma);

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
    @Requirement(
            reqId = "DOI_AUTH_030",
            reqName = "Authentification par login/mot de passe"
    )
    @Requirement(
            reqId = "DOI_AUTH_050",
            reqName = "Vérification du projet"
    )
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
    
    private ChallengeAuthenticator createTokenAuthenticator() {
        ChallengeAuthenticator guard = new ChallengeAuthenticator(
                getContext(), ChallengeScheme.HTTP_OAUTH_BEARER, "testRealm");
        TokenBasedVerifier verifier = new TokenBasedVerifier(getTokenDB());
        guard.setVerifier(verifier);   
        return guard;
    }

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
    
    public TokenDB getTokenDB() {
        return this.tokenDB;
    }

    /**
     * Method to describe application in the WADL.
     *
     * @param request Request
     * @param response Response
     * @return the application description for WADL
     */
    @Requirement(
            reqId = "DOI_DOC_010",
            reqName = "Documentation des interfaces"
    )
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
     * @param applicationInfo Application description
     * @return the HTML representation of the WADL
     */
    @Requirement(
            reqId = "DOI_DOC_010",
            reqName = "Documentation des interfaces"
    )
    @Override
    protected Representation createHtmlRepresentation(ApplicationInfo applicationInfo) {
        WadlCnesRepresentation wadl = new WadlCnesRepresentation(applicationInfo);
        return wadl.getHtmlRepresentation();
    }
        

}
