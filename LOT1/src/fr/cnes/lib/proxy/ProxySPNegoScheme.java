package fr.cnes.lib.proxy;

/*
 * Copyright [2018] [S. ETCHEVERRY (CNES)]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.ContextAwareAuthScheme;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.impl.auth.AuthSchemeBase;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/**
 * SPNEGO (Simple and Protected GSSAPI Negotiation Mechanism) authentication
 * scheme adapted for Proxy Authentication
 */
public class ProxySPNegoScheme extends AuthSchemeBase {

    /** */
    enum State {
	/** */
	UNINITIATED, CHALLENGE_RECEIVED, TOKEN_GENERATED, FAILED,
    }

    /**
     * Mechanism OID assigned to the pseudo-mechanism SPNEGO to negotiate the best
     * common GSS-API mechanism between two communication peers.
     */
    private static final String SPNEGO_OID = "1.3.6.1.5.5.2";

    /** */
    private ProxyGSSClient gssClient;

    /** the SPN (proxy host) */
    private String servicePrincipalName;

    /** the oid representing the type of service name form */
    private Oid servicePrincipalOid;

    /** */
    private final Log log = LogFactory.getLog(getClass());

    /** */
    private final Base64 base64codec;

    /** Authentication process state */
    private State state;
    /** base64 decoded challenge **/
    private byte[] token;

    /**
     * Init a Proxy SPNego Scheme
     * 
     * @param gssClient
     *            the gss client with all the client configuration
     * @param servicePrincipalName
     *            the SPN (proxy host)
     * @param servicePrincipalOid
     *            the oid representing the type of service name form
     */
    ProxySPNegoScheme(ProxyGSSClient gssClient, String servicePrincipalName,
	    Oid servicePrincipalOid) {
	super();
	this.base64codec = new Base64();
	this.state = State.UNINITIATED;
	this.gssClient = gssClient;
	this.servicePrincipalName = servicePrincipalName;
	this.servicePrincipalOid = servicePrincipalOid;
    }

    /**
     * @return a GSSManager's instance
     */
    protected GSSManager getManager() {

	return GSSManager.getInstance();
    }

    /**
     * Generate a GSS Token
     * 
     * @param oid
     *            The universal object identifier
     * @return a Kerberos token as a byte array
     * @throws GSSException
     *             This exception is thrown whenever a GSS-API error occurs
     */
    protected byte[] generateGSSToken(final Oid oid) throws GSSException {

	final byte[] token = new byte[0];

	if (log.isDebugEnabled()) {
	    log.debug("Init token:" + servicePrincipalName);
	}

	final GSSManager manager = getManager();
	final GSSName serverName = manager.createName("HTTP@" + servicePrincipalName,
		servicePrincipalOid);
	final GSSContext gssContext = manager.createContext(serverName.canonicalize(oid), oid, null,
		GSSContext.DEFAULT_LIFETIME);

	// Get client to login if not already done
	return gssClient.negotiate(gssContext, token);
    }

    /**
     * (non-Javadoc)
     * 
     * @return true if authentication process state is failed or token generated
     * @see org.apache.http.auth.AuthScheme#isComplete()
     */
    public boolean isComplete() {

	return this.state == State.TOKEN_GENERATED || this.state == State.FAILED;
    }

    /**
     * @param credentials
     *            (not used)
     * @param request
     *            the request being authenticated (not used)
     * @throws AuthenticationException
     *             if authentication string cannot be generated due to an
     *             authentication failure
     * @return SPNEGO authentication Header
     * @deprecated (4.2) Use
     *             {@link ContextAwareAuthScheme#authenticate(Credentials, HttpRequest, org.apache.http.protocol.HttpContext)}
     */
    @Deprecated
    public Header authenticate(final Credentials credentials, final HttpRequest request)
	    throws AuthenticationException {

	return authenticate(credentials, request, null);
    }

    /**
     * Produces SPNEGO authorization Proxy Header based on token created by
     * processChallenge.
     *
     * @param credentials
     *            (not used by the Proxy SPNEGO scheme).
     * @param request
     *            The request being authenticated (not used by the Proxy SPNEGO
     *            scheme)
     * @param context
     *            The context used for authentication (not used by the Proxy SPNEGO
     *            scheme)
     * @throws AuthenticationException
     *             if authentication string cannot be generated due to an
     *             authentication failure
     *
     * @return SPNEGO authentication Header
     */
    @Override
    public Header authenticate(final Credentials credentials, final HttpRequest request,
	    final HttpContext context) throws AuthenticationException {

	if (request == null) {
	    throw new IllegalArgumentException("HTTP request may not be null");
	}
	switch (state) {
	    case UNINITIATED:
		throw new AuthenticationException(
			getSchemeName() + " authentication has not been initiated");

	    case FAILED:
		throw new AuthenticationException(getSchemeName() + " authentication has failed");

	    case CHALLENGE_RECEIVED:
		try {
		    token = generateToken();
		    state = State.TOKEN_GENERATED;
		} catch (GSSException gsse) {
		    state = State.FAILED;
		    if (gsse.getMajor() == GSSException.DEFECTIVE_CREDENTIAL
			    || gsse.getMajor() == GSSException.CREDENTIALS_EXPIRED
			    || gsse.getMajor() == GSSException.NO_CRED) {
			throw new InvalidCredentialsException(gsse.getMessage(), gsse);
		    }
		    // if (gsse.getMajor() == GSSException.DEFECTIVE_TOKEN
		    // || gsse.getMajor() == GSSException.DUPLICATE_TOKEN
		    // || gsse.getMajor() == GSSException.OLD_TOKEN) {
		    // throw new AuthenticationException(gsse.getMessage(),
		    // gsse);
		    // }
		    // other error
		    throw new AuthenticationException(gsse.getMessage(), gsse);
		}

		break;

	    case TOKEN_GENERATED:
		break;

	    default:
		throw new IllegalStateException("Illegal state: " + state);
	}

	return getBasicHeader();
    }

    /**
     * @return the basicHeader
     */
    private BasicHeader getBasicHeader() {
	final String tokenStr = new String(base64codec.encode(token));
	if (log.isDebugEnabled()) {
	    log.debug("Sending response '" + tokenStr + "' back to the auth server");
	}
	return new BasicHeader(AUTH.PROXY_AUTH_RESP, "Negotiate " + tokenStr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.http.impl.auth.AuthSchemeBase#parseChallenge(org.apache.http.
     * util. CharArrayBuffer, int, int)
     */
    @Override
    protected void parseChallenge(final CharArrayBuffer buffer, int beginIndex, int endIndex)
	    throws MalformedChallengeException {

	final String challenge = buffer.substringTrimmed(beginIndex, endIndex);
	if (log.isDebugEnabled()) {
	    log.debug("Received challenge '" + challenge + "' from the auth server");
	}
	if (state == State.UNINITIATED) {
	    state = State.CHALLENGE_RECEIVED;
	} else {
	    log.debug("Authentication already attempted");
	    state = State.FAILED;
	}
    }

    /**
     * Token generation
     * 
     * @return the token as a byte array
     * @throws GSSException
     *             This exception is thrown whenever a GSS-API error occurs
     */
    protected byte[] generateToken() throws GSSException {

	return generateGSSToken(new Oid(SPNEGO_OID));
    }

    /**
     * There are no valid parameters for SPNEGO authentication so this method always
     * returns {@code null}.
     *
     * @return {@code null}
     */
    @Override
    public String getParameter(final String name) {
	Args.notNull(name, "Parameter name");
	return null;
    }

    @Override
    public String getSchemeName() {
	return "Negotiate";
    }

    /**
     * Returns {@code true}. SPNEGO authentication scheme is connection based.
     *
     * @return {@code true}.
     */
    @Override
    public boolean isConnectionBased() {
	return true;
    }

    /**
     * The concept of an authentication realm is not supported by the Negotiate
     * authentication scheme. Always returns {@code null}.
     *
     * @return {@code null}
     */
    @Override
    public String getRealm() {
	return null;
    }
}