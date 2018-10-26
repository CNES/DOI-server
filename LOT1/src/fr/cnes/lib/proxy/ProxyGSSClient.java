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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.HexDump;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;

/**
 * This class is used for SPNEGO authentication with a web proxy This models the
 * Kerberos client and manages the logging in to the Kerberos KDC to acquire the
 * TGT. It also performs the client-server authentication.
 */

public class ProxyGSSClient {

    /**
     * The initiator subject. This object will hold the TGT and all service tickets
     * in its private credentials cache.
     */
    private Subject subject;

    /** User ID */
    private String clientPrincipalName;

    /** the path of the keytab file */
    private String clientKeytabFileName;

    /** the path of the ticket cache file */
    private String ticketCacheFileName;

    /** */
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Init a Proxy GSS Client
     * 
     * @param clientPrincipalName
     *            the user Id
     * @param clientKeytabFileName
     *            the path of the keytab file
     * @param ticketCacheFileName
     *            the path of the ticket cache file
     * @param krb5ConfFile
     *            the path of the krb conf file
     */
    public ProxyGSSClient(String clientPrincipalName, String clientKeytabFileName,
	    String ticketCacheFileName, File krb5ConfFile) {

	System.setProperty("java.security.krb5.conf", krb5ConfFile.toString());
	this.clientPrincipalName = clientPrincipalName;
	this.clientKeytabFileName = clientKeytabFileName;
	this.ticketCacheFileName = ticketCacheFileName;
    }

    /**
     * @return the user Id
     */
    public String getName() {

	return clientPrincipalName;
    }

    /**
     * @return the user Id between square bracket
     */
    private String getNameWithBracket() {
	return "[" + getName() + "]";
    }

    /**
     * Login to Kerberos KDC and accquire a TGT.
     * 
     * @throws GSSException
     *             This exception is thrown whenever a GSS-API error occurs
     */
    private void loginViaJAAS() throws GSSException {

	try {
	    final ProxyKerberosConfiguration config = createGssKerberosConfiguration();

	    if (clientKeytabFileName != null) {
		config.setKeytab(clientKeytabFileName);
	    }
	    if (ticketCacheFileName != null) {
		config.setTicketCache(ticketCacheFileName);
	    }
	    config.initialize();

	    final LoginContext loginContext = new LoginContext("other");
	    loginContext.login();

	    // Subject will be populated with the Kerberos Principal name and the TGT.
	    // Krb5LoginModule obtains a TGT (KerberosTicket) for the user either from the
	    // KDC
	    // or from an existing ticket cache, and stores this TGT in the private
	    // credentials
	    // set of a Subject
	    subject = loginContext.getSubject();

	    log.debug("Logged in successfully as subject=\n" + subject.getPrincipals().toString());

	} catch (LoginException e) {
	    log.error("Error", e);
	    throw new GSSException(GSSException.DEFECTIVE_CREDENTIAL, GSSException.BAD_STATUS,
		    "Kerberos client '" + clientPrincipalName + "' failed to login to KDC. Error: "
			    + e.getMessage());
	}
    }

    /**
     * @return a ProxyKerberosConfiguration object initiated with the user Id
     */
    public ProxyKerberosConfiguration createGssKerberosConfiguration() {

	return new ProxyKerberosConfiguration(clientPrincipalName);
    }

    /**
     * Called when SPNEGO client-service authentication is taking place.
     * 
     * @param context
     *            the current GSS context
     * @param negotiationToken
     *            a previous token
     * @return a kerberos token as a byte array
     * @throws GSSException
     *             This exception is thrown whenever a GSS-API error occurs
     */
    public byte[] negotiate(GSSContext context, byte[] negotiationToken) throws GSSException {

	byte[] token = negotiationToken;

	if (subject == null) {
	    loginViaJAAS(); // throw GSSException if fail to login
	}

	// If we do not have the service ticket it will be retrieved from the TGS on a
	// call to initSecContext().
	final NegotiateContextAction negotiationAction = new NegotiateContextAction(context,
		negotiationToken);

	// Run the negotiation as the initiator
	// The service ticket will then be cached in the Subject's private credentials,
	// as the subject.
	token = (byte[]) Subject.doAs(subject, negotiationAction);

	if (negotiationAction.getGSSException() != null) {
	    throw negotiationAction.getGSSException();
	}

	return token;
    }

    /**
     * Action to call initSecContext() for initiator side of context negotiation.
     * Run as the initiator Subject so that any service tickets are cached in the
     * subject's private credentials.
     */
    class NegotiateContextAction implements PrivilegedAction<Object> {

	/** */
	private GSSContext context;
	/** */
	private byte[] negotiationToken;
	/** */
	private GSSException exception;

	/**
	 * @param context
	 *            the current GSS context
	 * @param negotiationToken
	 *            a previous token
	 */
	public NegotiateContextAction(GSSContext context, byte[] negotiationToken) {

	    this.context = context;
	    this.negotiationToken = negotiationToken;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.security.PrivilegedAction#run()
	 * @return The negotiation token
	 */
	public Object run() {

	    try {
		// If we do not have the service ticket it will be retrieved from the TGS on the
		// first call to initSecContext().
		// The subject's private credentials are checked for the service ticket.
		// If we run this action as the initiator subject, the service ticket will be
		// stored in the subject's credentials and will not need
		// to be retrieved next time the client wishes to talk to the server (acceptor).

		final Subject subject = Subject.getSubject(AccessController.getContext());

		log.debug(subject.toString());

		negotiationToken = context.initSecContext(negotiationToken, 0,
			negotiationToken.length);

		traceAfterNegotiate(traceBeforeNegotiate());

	    } catch (GSSException e) {
		// Trace out some info
		traceServiceTickets();
		log.error(e.toString());
		exception = e;
	    }

	    return negotiationToken;
	}

	/**
	 * @return The exception
	 */
	public GSSException getGSSException() {
	    return exception;
	}

	/**
	 * @return int Number of credentials held by the subject
	 */
	private int traceBeforeNegotiate() {

	    int beforeNumSubjectCreds = 0;
	    // Traces all credentials too.
	    if (subject != null) {
		log.debug(getNameWithBracket() + " AUTH_NEGOTIATE as subject "
			+ subject.getPrincipals().toString());
		beforeNumSubjectCreds = subject.getPrivateCredentials().size();
	    }

	    if (negotiationToken != null && negotiationToken.length > 0) {
		try {
		    final OutputStream os = new ByteArrayOutputStream();
		    HexDump.dump(negotiationToken, 0, os, 0);
		    log.debug(getNameWithBracket()
			    + " AUTH_NEGOTIATE Process token from service==>\n");
		} catch (IOException e) {
		}
	    }

	    return beforeNumSubjectCreds;
	}

	/**
	 * @param beforeNumSubjectCreds
	 *            Number of credentials held by the subject
	 */
	private void traceAfterNegotiate(int beforeNumSubjectCreds) {

	    if (subject != null) {
		final int afterNumSubjectCreds = subject.getPrivateCredentials().size();
		if (afterNumSubjectCreds > beforeNumSubjectCreds) {
		    log.debug(getNameWithBracket() + " AUTH_NEGOTIATE have extra credentials.");
		    // Traces all credentials too.
		    log.debug(getNameWithBracket() + " AUTH_NEGOTIATE updated subject"
			    + subject.toString());
		}
	    }

	    traceServiceTickets();

	    if (negotiationToken != null && negotiationToken.length > 0) {
		try {
		    final OutputStream os = new ByteArrayOutputStream();
		    HexDump.dump(negotiationToken, 0, os, 0);
		    log.debug(getNameWithBracket() + " AUTH_NEGOTIATE Send token to service\n");
		} catch (IOException e) {
		}
	    }
	}

	/**
	 * Write logs for each Kerberos ticket held by the subject if there is any
	 */
	public void traceServiceTickets() {

	    if (subject == null) {
		log.debug("Subject null");
		return;
	    }
	    final Set<Object> creds = subject.getPrivateCredentials();
	    if (creds.size() == 0) {
		log.debug(getNameWithBracket() + " No service tickets");
	    }

	    synchronized (creds) {
		// The Subject's private credentials is a synchronizedSet
		// We must manually synchronize when iterating through the set.
		for (Object ecred : creds) {
		    if (ecred instanceof KerberosTicket) {
			final KerberosTicket ticket = (KerberosTicket) ecred;
			log.debug(getNameWithBracket() + " Service ticket "
				+ "belonging to client principal [" + ticket.getClient().getName()
				+ "] for server principal [" + ticket.getServer().getName()
				+ "] End time=[" + ticket.getEndTime() + "] isCurrent="
				+ ticket.isCurrent());
		    }
		}
	    }
	}
    }
}
