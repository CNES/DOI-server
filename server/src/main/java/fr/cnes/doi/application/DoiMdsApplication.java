/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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

import static fr.cnes.doi.client.ClientMDS.SCHEMA_DATACITE;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.GrammarsInfo;
import org.restlet.ext.wadl.IncludeInfo;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MethodAuthorizer;

import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.resource.mds.DoiResource;
import fr.cnes.doi.resource.mds.DoisResource;
import fr.cnes.doi.resource.mds.InistResource;
import fr.cnes.doi.resource.mds.MediaResource;
import fr.cnes.doi.resource.mds.MetadataResource;
import fr.cnes.doi.resource.mds.MetadatasResource;
import fr.cnes.doi.resource.mds.MetadatasValidatorResource;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.utils.spec.Requirement;
import org.apache.logging.log4j.ThreadContext;
import org.restlet.data.ClientInfo;

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
 * <code>
 * --------------<br>
 * The authentication is done by the following pipeline:<br>
 * |Method authorization|--&gt;|Authentication login/pwd|--&gt;|Authentication
 * token|<br>
 * Method authorization : Only GET method does not need an authorization
 * </code>
 *
 * <p>
 * <b>Routing</b><br>
 * <code>
 * --------------<br>
 * <br>
 * root<br>
 * |<br>
 * |__ dois (authorization)<br>
 * |__ dois/{doiName} (authorization)<br>
 * |__ metadata (authorization)<br>
 * |__ metadata/{doiName} (authorization)<br>
 * |__ media/{doiName} (authorization)<br>
 * </code>
 *
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
@Requirement(reqId = Requirement.DOI_SRV_010, reqName = Requirement.DOI_SRV_010_NAME)
@Requirement(reqId = Requirement.DOI_SRV_020, reqName = Requirement.DOI_SRV_020_NAME)
@Requirement(reqId = Requirement.DOI_SRV_030, reqName = Requirement.DOI_SRV_030_NAME)
@Requirement(reqId = Requirement.DOI_SRV_040, reqName = Requirement.DOI_SRV_040_NAME)
@Requirement(reqId = Requirement.DOI_SRV_050, reqName = Requirement.DOI_SRV_050_NAME)
@Requirement(reqId = Requirement.DOI_SRV_060, reqName = Requirement.DOI_SRV_060_NAME)
@Requirement(reqId = Requirement.DOI_SRV_070, reqName = Requirement.DOI_SRV_070_NAME)
@Requirement(reqId = Requirement.DOI_SRV_080, reqName = Requirement.DOI_SRV_080_NAME)
@Requirement(reqId = Requirement.DOI_SRV_090, reqName = Requirement.DOI_SRV_090_NAME)
@Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
public final class DoiMdsApplication extends AbstractApplication {

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
     * URI to handle metadata validation : {@value #XSD}.
     */
    public static final String XSD = "/xsd";
    
    /**
     * URI to retrieve the INIST code : {@value #INIST}.
     */
    public static final String INIST = "/inist";
    
    /**
     * URI to handle json to xml : {@value #XML}.
     */
    public static final String XML = "/xml";

    /**
     * Application name : {@value #NAME}
     */
    public static final String NAME = "Metadata Store Application";

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
     * Creates the Digital Object Identifier server application.
     *
     * @param client ClientMDS
     * @throws DoiRuntimeException When the DataCite schema is not available
     */
    public DoiMdsApplication(final ClientMDS client) {
        super();
        setName(NAME);
        setDescription(
                "Provides an application for handling Data Object Identifier at CNES<br/>"
                + "This application provides 3 API:" + "<ul>" + "<li>dois : DOI minting</li>"
                + "<li>metadata : Registration of the associated metadata</li>"
                + "<li>media : Possbility to obtain metadata in various formats and/or get "
                + "automatic, direct access to an object rather than via the \"landing page\"</li>"
                + "</ul>");
        this.client = client;
        this.tokenDB = TokenSecurity.getInstance().getTokenDB();
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

        final Filter logContext = new Filter() {
            /**
             * Get the IP client from proxy (if there is one) and set it in
             * the header.
             * 
             * @param request request
             * @param response response
             * @return 0
             */            
            @Override
            protected int beforeHandle(final Request request, final Response response) {
                final ClientInfo clientInfo = request.getClientInfo();
                final String ipAddress = request.getHeaders().getFirstValue(
                        Consts.PROXIFIED_IP, clientInfo.getUpstreamAddress()
                );
                ThreadContext.put(Consts.LOG_IP_ADDRESS, ipAddress);
                return Filter.CONTINUE;
            }
        };

        // Defines the strategy of authentication (authentication is not required)
        //   - authentication with login/pwd
        final ChallengeAuthenticator challAuth = createAuthenticatorLoginBased();
        challAuth.setOptional(true);

        //   - authentication with token
        final ChallengeAuthenticator challTokenAuth = createTokenAuthenticator();
        challTokenAuth.setOptional(true);

        // Set specific authorization on method after checking authentication
        final MethodAuthorizer methodAuth = createMethodAuthorizer();

        // set information available for Log4j
        logContext.setNext(challAuth);

        //  create a pipeline of authentication
        challAuth.setNext(challTokenAuth);
        challTokenAuth.setNext(methodAuth);

        // Router
        methodAuth.setNext(createRouter());

        final Filter filter = new SecurityPostProcessingFilter(getContext(), logContext);
        return LOG.traceExit(filter);
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
        router.attach(METADATAS_URI + XSD, MetadatasValidatorResource.class);
        router.attach(METADATAS_URI + DOI_NAME_URI, MetadataResource.class)
                .getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
        router.attach(MEDIA_URI + DOI_NAME_URI, MediaResource.class)
                .getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
        router.attach(INIST, InistResource.class);

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
        methodAuth.getAnonymousMethods().add(Method.OPTIONS);
        methodAuth.getAuthenticatedMethods().add(Method.GET);
        methodAuth.getAuthenticatedMethods().add(Method.POST);
        methodAuth.getAuthenticatedMethods().add(Method.PUT);
        methodAuth.getAuthenticatedMethods().add(Method.DELETE);

        return LOG.traceExit(methodAuth);
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
    @Override
    public AbstractTokenDBHelper getTokenDB() {
        LOG.traceEntry();
        return LOG.traceExit(this.tokenDB);
    }

    /**
     * Returns the logger.
     *
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
    public final ApplicationInfo getApplicationInfo(final Request request,
            final Response response) {
        final ApplicationInfo result = super.getApplicationInfo(request, response);
        final DocumentationInfo docInfo = new DocumentationInfo(
                "DOI server application provides is central service that registers DOI at DataCite"
        );
        docInfo.setTitle(this.getName());
        docInfo.setTextContent(this.getDescription());
        result.setDocumentation(docInfo);
        result.getNamespaces().put(SCHEMA_DATACITE, "default");
        result.getNamespaces().put("http://www.w3.org/2001/XMLSchema", "xsi");
        final GrammarsInfo grammar = new GrammarsInfo();
        final IncludeInfo include = new IncludeInfo();
        include.setTargetRef(new Reference(SCHEMA_DATACITE));
        grammar.getIncludes().add(include);
        result.setGrammars(grammar);
        return result;
    }

    /**
     * Post processing for specific authorization. Specific class to handle the
     * case where the user is authorized by oauth but non authorized by the
     * service because the user's role is not related to any projects
     */
    public static class SecurityPostProcessingFilter extends Filter {

        /**
         * Constructor
         *
         * @param context context
         * @param next gard
         */
        public SecurityPostProcessingFilter(final Context context, final Restlet next) {
            super(context, next);
        }

        /**
         * Fixed problem with Jetty response.
         *
         * @param request request
         * @param response response
         */
        @Override
        protected void afterHandle(final Request request, final Response response) {
            final Status status = response.getStatus();
            final String reason = status.getReasonPhrase();
            if (status.getCode() == Status.CLIENT_ERROR_UNAUTHORIZED.getCode()
                    && API_MDS.SECURITY_USER_NO_ROLE.getShortMessage().equals(reason)) {
                response.getHeaders().add("WWW-Authenticate",
                        "Basic realm=\"DOI Server access\", charset=\"UTF-8\"");
            }
        }
    }

    /**
     * API related only to the code in this webservice. Other codes can be
     * returned by the {@link ClientMDS}
     */
    public enum API_MDS {
        /**
         * Create metadata. SUCCESS_CREATED is used as status meaning "Operation
         * successful"
         */
        CREATE_METADATA(Status.SUCCESS_CREATED, "Operation successful"),
        /**
         * Validate metadata. SUCCESS_OK is used as status meaning "Operation
         * successful"
         */
        VALIDATE_METADATA(Status.SUCCESS_OK, "Metadata valid"),
        /**
         * Forbidden to use this role. It happens when a user provides a role to
         * the server whereas he is unknown in this role. CLIENT_ERROR_FORBIDDEN
         * is used as status meaning "Forbidden to use this role"
         */
        SECURITY_USER_NOT_IN_SELECTED_ROLE(Status.CLIENT_ERROR_FORBIDDEN,
                "Forbidden to use this role"),
        /**
         * Fail to authorize the user. It happens when a client is authentified
         * but unauthorized to use the resource. CLIENT_ERROR_UNAUTHORIZED is
         * used as status meaning "Fail to authorize the user"
         */
        SECURITY_USER_NO_ROLE(Status.CLIENT_ERROR_UNAUTHORIZED, "Fail to authorize the user"),
        /**
         * Fail to know privileges of a user. It happens when an user is
         * associated to several roles without selecting one.
         * CLIENT_ERROR_CONFLICT is used as status meaning "Error when an user
         * is associated to more than one role without setting selectedRole
         * parameter"
         */
        SECURITY_USER_CONFLICT(Status.CLIENT_ERROR_CONFLICT,
                "Error when an user is associated to more than one role without setting selectedRole "
                + "parameter"),
        /**
         * Fail to create a DOI. It happens when a user try to create a DOI with
         * the wrong role. Actually, the role is contained in the DOI name. So
         * if a user is authentified with a right role and try to create a DOI
         * for another role, an exception is raised. SECURITY_USER_PERMISSION is
         * used as status meaning "User is not allowed to make this operation"
         */
        SECURITY_USER_PERMISSION(Status.CLIENT_ERROR_FORBIDDEN,
                "User is not allowed to make this operation"),
        /**
         * Cannot access to Datacite. It happens when a network problem may
         * happen with Datacite. SERVER_ERROR_GATEWAY_TIMEOUT is used as status
         * meaning "Cannot access to Datacite"
         */
        NETWORK_PROBLEM(Status.CONNECTOR_ERROR_COMMUNICATION, "Cannot access to Datacite"),
        /**
         * Fail to validate user input parameter for creating the DOI. It
         * happens in the following cases:
         * <ul>
         * <li>the DOI or metadata are not provided</li>
         * <li>the prefix is not allowed</li>
         * <li>some characters are not allowed in the DOI name</li>
         * <li>an error occur on the schema instanciation</li>
         * </ul>
         * CLIENT_ERROR_BAD_REQUEST is used as status meaning "Failed to
         * validate the user inputs parameters"
         */
        METADATA_VALIDATION(Status.CLIENT_ERROR_BAD_REQUEST,
                "Failed to validate the user inputs parameters"),
        /**
         * Fail to create the media related to the DOI. CLIENT_ERROR_BAD_REQUEST
         * is used as status meaning "DOI not provided or one or more of the
         * specified mime-types or urls are invalid (e.g. non supported
         * mime-type, not allowed url domain, etc.)"
         */
        MEDIA_VALIDATION(Status.CLIENT_ERROR_BAD_REQUEST,
                "DOI not provided or one or more of the specified mime-types or urls are invalid "
                + "(e.g. non supported mime-type, not allowed url domain, etc.)"),
        /**
         * Fail to create the landing page related to the DOI.
         * CLIENT_ERROR_BAD_REQUEST is used as status meaning "Validation error
         * when defining the DOI and its landing page"
         */
        LANGING_PAGE_VALIDATION(Status.CLIENT_ERROR_BAD_REQUEST,
                "Validation error when defining the DOI and its landing page"),
        /**
         * DOI validation error. CLIENT_ERROR_BAD_REQUEST is used as status
         * meaning "Character or prefix not allowed in the DOI"
         */
        DOI_VALIDATION(Status.CLIENT_ERROR_BAD_REQUEST, "Character or prefix not allowed in the "
                + "DOI"),
        /**
         * Internal server error. Fail to communicate with DataCite using the
         * interface specification meaning "Interface problem between Datacite
         * and DOI-Server"
         */
        DATACITE_PROBLEM(Status.SERVER_ERROR_INTERNAL,
                "Interface problem between Datacite and DOI-Server");

        /**
         * Message status.
         */
        private final Status status;
        /**
         * Short message.
         */
        private final String shortMessage;

        /**
         * Constructor.
         *
         * @param status status
         * @param shortMessage short message related to the status
         */
        API_MDS(final Status status, final String shortMessage) {
            this.status = status;
            this.shortMessage = shortMessage;
        }

        /**
         * Returns the status
         *
         * @return the status
         */
        public Status getStatus() {
            return this.status;
        }

        /**
         * Returns the short message.
         *
         * @return the short message
         */
        public String getShortMessage() {
            return this.shortMessage;
        }

    }
}
