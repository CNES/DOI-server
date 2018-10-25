/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.application;

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
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MethodAuthorizer;
import org.restlet.routing.Router;

import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.resource.mds.DoiResource;
import fr.cnes.doi.resource.mds.DoisResource;
import fr.cnes.doi.resource.mds.MediaResource;
import fr.cnes.doi.resource.mds.MetadataResource;
import fr.cnes.doi.resource.mds.MetadatasResource;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.security.TokenBasedVerifier;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.utils.spec.Requirement;
import javax.xml.validation.Schema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.routing.Template;

/**
 * Provides an application to handle Data Object Identifier within an
 * organization. A Digital Object Identifier (DOI) is a persistent identifier or
 * handle used to uniquely identify objects, standardized by the International
 * Organization. A DOI aims to be "resolvable", usually to some form of access
 * to the information object to which the DOI refers. This is achieved by
 * binding the DOI to metadata about the object, such as a URL, where all the
 * details about the object are accessible. Everytime a URL changes, the
 * publisher has to update the metadata for the DOI to link to the new URL. It
 * is the publisher's responsibility to update the DOI database. If he fails to
 * do so, the DOI resolves to a dead link leaving the DOI useless.
 * <p>
 * Two methods of authentication are defined in this application and both is
 * optional:
 * <ul>
 * <li>{@link #createAuthenticator authenticator} by login/password</li>
 * <li>{@link #createTokenAuthenticator authenticator} by
 * {@link fr.cnes.doi.resource.admin.TokenResource token}
 * </ul>
 * Only the GET can be anonymous whereas POST, PUT, DELETE
 * {@link #createMethodAuthorizer methods need to be authenticated}.
 * 
 * <p>
 * <b>Security</b><br>
 * --------------<br>
 * The authentication is done by the following pipeline:<br>
 * |Method authorization|-->|Authentication login/pwd|-->|Authentication token|<br>
 * Method authorization : Only GET method does not need an authorization 
 * 
 * <p>
 * <b>Routing</b><br>
 *  --------------<br>
 * <br>
 * root<br>
 *  |<br>
 *  |__ dois (authorization)<br>
 *  |__ dois/{doiName} (authorization)<br>
 *  |__ metadata (authorization)<br>
 *  |__ metadata/{doiName} (authorization)<br>
 *  |__ media/{doiName} (authorization)<br> 
 * @see #createInboundRoot the resources related to this application
 * @see <a href="http://www.doi.org/hb.html">DOI Handbook</a>
 * @see <a href="https://mds.datacite.org/static/apidoc">API Documentation</a>
 * @see DoisResource List and create DOI
 * @see DoiResource Handle a DOI
 * @see MetadatasResource Create metadata
 * @see MetadataResource Handle DOI metadata
 * @see MediaResource Handle media related to metadata
 * 
 * @author Jean-Christophe Malapert (jean-Christophe Malapert@cnes.fr) 
 */
@Requirement(reqId = Requirement.DOI_SRV_010,reqName = Requirement.DOI_SRV_010_NAME)
@Requirement(reqId = Requirement.DOI_SRV_020,reqName = Requirement.DOI_SRV_020_NAME)
@Requirement(reqId = Requirement.DOI_SRV_030,reqName = Requirement.DOI_SRV_030_NAME)
@Requirement(reqId = Requirement.DOI_SRV_040,reqName = Requirement.DOI_SRV_040_NAME)
@Requirement(reqId = Requirement.DOI_SRV_050,reqName = Requirement.DOI_SRV_050_NAME)
@Requirement(reqId = Requirement.DOI_SRV_060,reqName = Requirement.DOI_SRV_060_NAME)
@Requirement(reqId = Requirement.DOI_SRV_070,reqName = Requirement.DOI_SRV_070_NAME)
@Requirement(reqId = Requirement.DOI_SRV_080,reqName = Requirement.DOI_SRV_080_NAME)
@Requirement(reqId = Requirement.DOI_SRV_090,reqName = Requirement.DOI_SRV_090_NAME)
@Requirement(reqId = Requirement.DOI_MONIT_020,reqName = Requirement.DOI_MONIT_020_NAME)
public class DoiMdsApplication extends AbstractApplication {
  
    /**
     * Template Query for DOI : {@value #DOI_TEMPLATE}.
     */
    public static final String DOI_TEMPLATE = "doiName";

    /**
     * URI to handle the collection of DOIs : {@value #DOI_URI}.
     */
    public static final String DOI_URI = "/dois";

    /**
     * URI to handle a DOI : {@value #DOI_NAME_URI}.
     */
    public static final String DOI_NAME_URI = "/{" + DOI_TEMPLATE + "}";

    /**
     * URI to handle metadata : {@value #METADATAS_URI}.
     */
    public static final String METADATAS_URI = "/metadata";

    /**
     * URI to handle media : {@value #MEDIA_URI}.
     */
    public static final String MEDIA_URI = "/media";

    /**
     * Application name : {@value #NAME}
     */
    public static final String NAME = "Metadata Store Application";
    
    /**
     * Schema.
     */
    private final SchemaFactory schemaFactory = SchemaFactory.newInstance(
            XMLConstants.W3C_XML_SCHEMA_NS_URI
    );
    
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DoiMdsApplication.class.getName());      

    /**
     * Client to query Mds Datacite.
     */
    private final ClientMDS client;

    /**
     * Token DB that contains the set of generated token.
     */
    private final AbstractTokenDBHelper tokenDB;
    
    /**
     * Cache for Datacite schema
     */
    private final CacheSchema schemaCache = new CacheSchema();    

    /**
     * Creates the Digital Object Identifier server application.
     */
    public DoiMdsApplication() {
        super();
        setName(NAME);
        setDescription("Provides an application for handling Data Object Identifier at CNES<br/>"
                + "This application provides 3 API:" + "<ul>" + "<li>dois : DOI minting</li>"
                + "<li>metadata : Registration of the associated metadata</li>"
                + "<li>media : Possbility to obtain metadata in various formats and/or get automatic, direct access to an object rather than via the \"landing page\"</li>"
                + "</ul>");
        final String contextMode = this.getConfig().getString(Consts.CONTEXT_MODE);
        client = new ClientMDS(ClientMDS.Context.valueOf(contextMode), getLoginMds(), getPwdMds());
        this.tokenDB = TokenSecurity.getInstance().getTOKEN_DB();
    }

    /**
     * Creates a router for the DoiMdsApplication.
     *
     * This router routes the resources for the Mds application, which is
     * protected by two authentication mechanisms (optional mechanisms) and an
     * authorization by method.
     *
     * @see DoiMdsApplication#createRouter the router that contains the Mds
     * resources
     * @see DoiMdsApplication#createAuthenticator the authentication mechanism
     * by login/password
     * @see DoiMdsApplication#createTokenAuthenticator the authentication
     * mechanism by token
     * @see DoiMdsApplication#createMethodAuthorizer the method authorization
     * mechanism
     * @return Router
     */
    @Override
    public Restlet createInboundRoot() {
        LOG.traceEntry();
        
        // Defines the strategy of authentication (authentication is not required)
        //   - authentication with login/pwd
        final ChallengeAuthenticator challAuth = createAuthenticator();
        challAuth.setOptional(true);

        //   - authentication with token
        final ChallengeAuthenticator challTokenAuth = createTokenAuthenticator();
        challTokenAuth.setOptional(true);

        //  create a pipeline of authentication
        challAuth.setNext(challTokenAuth);

        // Set specific authorization on method after checking authentication
        final MethodAuthorizer methodAuth = createMethodAuthorizer();
        challTokenAuth.setNext(methodAuth);

        // Router
        methodAuth.setNext(createRouter());

        return LOG.traceExit(challAuth);
    }

    /**
     * Creates the router. The router routes the following resources:
     * <ul>
     * <li>{@link DoiMdsApplication#DOI_URI} to create/update a DOI and its
     * landing page</li>
     * <li>{@link DoiMdsApplication#DOI_URI} {@link DoiMdsApplication#DOI_NAME_URI}
     * to get the URL of the landing page related to a given DOI</li>
     * <li>{@link DoiMdsApplication#METADATAS_URI} to create/update DOI
     * metadata</li>
     * <li>{@link DoiMdsApplication#METADATAS_URI} {@link DoiMdsApplication#DOI_NAME_URI}
     * to get DOI's metadata or delete a given DOI</li>
     * <li>{@link DoiMdsApplication#MEDIA_URI} {@link DoiMdsApplication#DOI_NAME_URI}
     * to handle media related to a DOI
     * </ul>
     *
     * @see DoiResource Handles a DOI and its landing page
     * @see MetadatasResource Handles DOI metadata
     * @see MetadataResource Handles DOI metadata
     * @see MediaResource Handles media related to a DOI
     * @return the router
     */    
    private Router createRouter() {
        LOG.traceEntry();
        
        final Router router = new Router(getContext());        
        router.attach(DOI_URI, DoisResource.class);
        router.attach(DOI_URI + DOI_NAME_URI, DoiResource.class)
                .getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
        router.attach(METADATAS_URI, MetadatasResource.class);
        router.attach(METADATAS_URI + DOI_NAME_URI, MetadataResource.class)
                .getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
        router.attach(MEDIA_URI + DOI_NAME_URI, MediaResource.class)
                .getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);

        return LOG.traceExit(router);
    }

    /**
     * Creates the method authorizer. GET method can be anonymous. The verbs
     * (POST, PUT, DELETE) need to be authenticated.
     *
     * @return Authorizer based on authorized methods
     */
    private MethodAuthorizer createMethodAuthorizer() {
        LOG.traceEntry();
        
        final MethodAuthorizer methodAuth = new MethodAuthorizer();
        methodAuth.getAnonymousMethods().add(Method.GET);
        methodAuth.getAuthenticatedMethods().add(Method.GET);
        methodAuth.getAuthenticatedMethods().add(Method.POST);
        methodAuth.getAuthenticatedMethods().add(Method.PUT);
        methodAuth.getAuthenticatedMethods().add(Method.DELETE);

        return LOG.traceExit(methodAuth);
    }

    /**
     * Creates an authentication by token.
     *
     * @return the object that contains the business to check
     * @see TokenBasedVerifier the token verification
     */
    @Requirement(reqId = Requirement.DOI_AUTH_020,reqName = Requirement.DOI_AUTH_020_NAME)
    private ChallengeAuthenticator createTokenAuthenticator() {
        LOG.traceEntry();
        
        final ChallengeAuthenticator guard = new ChallengeAuthenticator(
                getContext(), ChallengeScheme.HTTP_OAUTH_BEARER, "testRealm");
        final TokenBasedVerifier verifier = new TokenBasedVerifier(getTokenDB());
        guard.setVerifier(verifier);

        return LOG.traceExit(guard);
    }

    /**
     * Returns the object to valid the datacite schema.
     *
     * @return the schema factory
     */
    public SchemaFactory getSchemaFactory() {
        LOG.traceEntry();
        return LOG.traceExit(this.schemaFactory);
    }

    /**
     * Returns the decrypted login for DataCite.
     *
     * @return the DataCite's login
     */
    private String getLoginMds() {
        LOG.traceEntry();
        return LOG.traceExit(this.getConfig().getSecret(Consts.INIST_LOGIN));
    }

    /**
     * Returns the decrypted password for DataCite.
     *
     * @return the DataCite's pwd
     */
    private String getPwdMds() {
        LOG.traceEntry();
        return LOG.traceExit(this.getConfig().getSecret(Consts.INIST_PWD));
    }

    /**
     * Returns the DOI prefix.
     *
     * @return the DOI prefix
     */
    public String getDataCentrePrefix() {
        LOG.traceEntry();
        return LOG.traceExit(this.getConfig().getString(Consts.INIST_DOI));
    }

    /**
     * Returns the client.
     *
     * @return the client
     */
    public ClientMDS getClient() {
        LOG.traceEntry();
        return LOG.traceExit(this.client);
    }

    /**
     * Returns the token database.
     *
     * @return the token database
     */
    public AbstractTokenDBHelper getTokenDB() {
        LOG.traceEntry();
        return LOG.traceExit(this.tokenDB);
    }
    
    /**
     * Returns the cache that may contain the schema.
     * @return the cache
     */
    public CacheSchema getCache() {
        LOG.traceEntry();
        return LOG.traceExit(this.schemaCache);
    }    
    
    /**
     * Returns the logger.
     * @return the logger
     */
    @Override
    public Logger getLog() {
        return LOG;
    }

    /**
     * Method to describe application in the WADL.
     *
     * @param request Request
     * @param response Response
     * @return the application description for WADL
     */
    @Override
    public final ApplicationInfo getApplicationInfo(final Request request, final Response response){
        final ApplicationInfo result = super.getApplicationInfo(request, response);
        final DocumentationInfo docInfo = new DocumentationInfo(
                "DOI server application provides is central service that registers DOI at DataCite"
        );
        docInfo.setTitle(this.getName());
        docInfo.setTextContent(this.getDescription());
        result.setDocumentation(docInfo);
        result.getNamespaces().put(
                "https://schema.datacite.org/meta/kernel-4.0/metadata.xsd", "default"
        );
        result.getNamespaces().put(
                "http://www.w3.org/2001/XMLSchema", "xsi"
        );
        final GrammarsInfo grammar = new GrammarsInfo();
        final IncludeInfo include = new IncludeInfo();
        include.setTargetRef(
                new Reference("https://schema.datacite.org/meta/kernel-4.0/metadata.xsd")
        );
        grammar.getIncludes().add(include);
        result.setGrammars(grammar);
        return result;
    }
    
    /**
     * Handles cache for DataCite schema.
     */
    public class CacheSchema {
        
        /**
         * Conversion hour to ms.
         */
        private static final int H_TO_MS = 3600000;
        
        /**
         * Max time before to refresh the cache when the cache is requested.
         */
        private static final int MAX_TIME_CACHE = 24 * H_TO_MS;
        
        /**
         * Cached schema
         */
        private Schema schema;
        /**
         * Time in ms when the schema was cached.
         */
        private long cachedTime = 0;
        
        /**
         * Create a cache.
         */
        public CacheSchema() {            
        }
        
        /**
         * Store the schema.
         * @param schema the Datacite schema
         */
        public synchronized void store(final Schema schema) {
            LOG.traceEntry("Parameter : {}", schema);
            this.schema = schema;
            this.cachedTime = System.currentTimeMillis();
            LOG.traceExit();
        }
        
        /**
         * Returns the cache.
         * @return the schema or null
         */
        public synchronized Schema getCache() {  
            LOG.traceEntry();
            return LOG.traceExit(isStored() ? this.schema : null);
        }
        
        /**
         * Checks whether the cache is stored.
         * @return true when the schema is stored otherwise false.
         */
        public synchronized boolean isStored() {
            LOG.traceEntry();
            long newTime = System.currentTimeMillis();
            long elapsedTime = newTime - cachedTime;
            return LOG.traceExit(elapsedTime <= MAX_TIME_CACHE && elapsedTime != 0);
        }
        
    }     
}
