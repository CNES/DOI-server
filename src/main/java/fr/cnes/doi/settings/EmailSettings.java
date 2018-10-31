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
package fr.cnes.doi.settings;

import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * Singleton to load and use Email settings.
 *
 * @author Jean-Christophe Malapert
 */
@Requirement(reqId = Requirement.DOI_CONFIG_010, reqName = Requirement.DOI_CONFIG_010_NAME)
public final class EmailSettings {

    /**
     * Default debug : {@value #DEFAULT_DEBUG}.
     */
    private static final boolean DEFAULT_DEBUG = false;

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(EmailSettings.class.getName());
    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static EmailSettings getInstance() {
        return EmailSettingsHolder.INSTANCE;
    }

    /**
     * SMTP URL.
     */
    private String smtpUrl;

    /**
     * SMTP protocol.
     */
    private String smtpProtocol;

    /**
     * TLS.
     */
    private String tlsEnable;

    /**
     * login.
     */
    private String authUser;

    /**
     * Password.
     */
    private String authPwd;

    /**
     * Contact admin uRL.
     */
    private String contactAdmin;

    /**
     * debug.
     */
    private boolean debug = DEFAULT_DEBUG;

    /**
     * Constructor
     */
    private EmailSettings() {
        init();
    }


    /**
     * Init singleton.
     */
    public void init() {
        LOG.traceEntry();
        LOG.info("----- Email parameters ----");

        final DoiSettings settings = DoiSettings.getInstance();

        this.smtpProtocol = settings.getString(Consts.SMTP_PROTOCOL, "");
        LOG.info(String.format("smtp protocol : %s", this.smtpProtocol));

        this.smtpUrl = settings.getString(Consts.SMTP_URL, "");
        LOG.info(String.format("smtp URL : %s", this.smtpUrl));

        this.authUser = settings.getSecret(Consts.SMTP_AUTH_USER);
        LOG.info(String.format("auth user : %s", this.authUser));

        this.authPwd = settings.getSecret(Consts.SMTP_AUTH_PWD);
        LOG.info(String.format("auth pwd : %s", this.authPwd));

        this.tlsEnable = settings.getString(Consts.SMTP_STARTTLS_ENABLE, "false");
        LOG.info(String.format("TLS enable : %s", this.tlsEnable));

        this.contactAdmin = settings.getString(Consts.SERVER_CONTACT_ADMIN, "");
        LOG.info(String.format("Contact admin : %s", this.contactAdmin));

        LOG.info("Email settings have been loaded");
        LOG.info("---------------------------");

        LOG.traceExit();
    }

    /**
     * Sets the debug.
     *
     * @param isEnabled True if debug is enabled otherwise False
     */
    public void setDebug(final boolean isEnabled) {
        LOG.traceEntry("Parameter : {}", isEnabled);
        this.debug = isEnabled;
    }

    /**
     * Returns the debug.
     *
     * @return debug
     */
    public boolean isDebug() {
        return LOG.traceExit(this.debug);
    }

    /**
     * Sends a message by email.
     *
     * @param subject Email's subject
     * @param msg Email's message
     * @return True when the message is sent
     */
    public boolean sendMessage(final String subject,
            final String msg) {
        LOG.traceEntry("Parameters : {} and {}", subject, msg);
        boolean result;
        try {
            if (isConfigureForSendingEmail()) {
                result = processMessage(subject, msg);
            } else {
                LOG.warn("Cannot send the email, please fill the configuration file");
                result = false;
            }
        } catch (RuntimeException ex) {
            LOG.catching(Level.DEBUG, ex);
            LOG.error("Cannot send the message with the subject {} : {}", subject, msg);
            result = false;
        } catch (Exception ex) {
            LOG.catching(Level.DEBUG, ex);
            LOG.error("Cannot send the message with the subject {} : {}", subject, msg);
            result = false;
        }
        return LOG.traceExit(result);
    }

    /**
     * Process the message. Sends to SMTP server
     *
     * @param subject subject of the email
     * @param message message
     * @return true when the message has been send otherwise false
     * @throws Exception when an error happens
     */
    private boolean processMessage(final String subject,
            final String message) throws Exception {
        LOG.debug("Enough information to send the email.");
        final Request request = new Request(Method.POST, getSmtpURL());
        setSmtpPassword(request);
        return sendMail(Protocol.valueOf(getSmtpProtocol()), request, Boolean.getBoolean(
                getTlsEnable()), subject, message);
    }

    /**
     * Sets the SMTP password
     *
     * @param request request
     */
    private void setSmtpPassword(final Request request) {
        if (isAuthenticate()) {
            LOG.debug("Sets the login/passwd for the SMTP server");
            request.setChallengeResponse(
                    new ChallengeResponse(ChallengeScheme.SMTP_PLAIN, getAuthUser(), getAuthPwd()));
        } else {
            LOG.debug("No required login/pwd for the SMTP server");
            request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.SMTP_PLAIN));
        }
    }

    /**
     * Returns true when configuration may be enough to send an email. Checks the SMTP URL and the
     * SMTP protocol.
     *
     * @return true when configuration may be enough to send an email
     */
    private boolean isConfigureForSendingEmail() {
        return !(this.getSmtpURL().isEmpty() && this.getSmtpProtocol().isEmpty());
    }

    /**
     * Returns true when sendmail neeed to be authenticated otherwide false.
     *
     * @return true when sendmail neeed to be authenticated otherwide false
     */
    private boolean isAuthenticate() {
        return !(this.getAuthUser().isEmpty() && this.getAuthPwd().isEmpty());
    }

    /**
     * Sends email.
     *
     * @param protocol Protocol (SMTP or SMTPS)
     * @param request request
     * @param startTls startTls
     * @param subject Email's subject
     * @param msg Email's message
     * @return True when the message is sent
     * @throws Exception - if an error happens when stopping the request
     */
    private boolean sendMail(final Protocol protocol,
            final Request request,
            final boolean startTls,
            final String subject,
            final String msg) throws Exception {
        LOG.traceEntry("With parameters");
        final DoiSettings settings = DoiSettings.getInstance();
        boolean result;
        final Map<String, String> dataModel = new ConcurrentHashMap<>();
        dataModel.put("subject", subject);
        dataModel.put("message", msg);
        dataModel.put("from", settings.getString(Consts.SERVER_CONTACT_ADMIN,
                "L-doi-support@cnes.fr"));
        dataModel.
                put("to", settings.getString(Consts.SERVER_CONTACT_ADMIN, "L-doi-support@cnes.fr"));

        final Representation mailFtl = new ClientResource(LocalReference.createClapReference(
                "class/email.ftl")).get();
        final Representation mail = new TemplateRepresentation(mailFtl, dataModel,
                MediaType.TEXT_XML);
        request.setEntity(mail);
        try {
            final ClientResource client = new ClientResource(new Context(), request);
            client.setProtocol(protocol);
            final Series<Parameter> params = client.getContext().getParameters();
            params.set(RESTLET_MAX_TOTAL_CONNECTIONS, DoiSettings.getInstance().getString(
                    fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS,
                    DEFAULT_MAX_TOTAL_CONNECTIONS));
            params.set(RESTLET_MAX_CONNECTIONS_PER_HOST, DoiSettings.getInstance().getString(
                    fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST,
                    DEFAULT_MAX_CONNECTIONS_PER_HOST));
            params.add("debug", String.valueOf(isDebug()));
            params.add("startTls", Boolean.toString(startTls).toLowerCase(Locale.ENGLISH));
            client.get();
            final Status status = client.getStatus();
            if (status.isError()) {
                LOG.error("Cannot send the email! : {}", status.getDescription());
                result = false;
            } else {
                result = true;
            }
        } catch (NullPointerException ex) {
            LOG.error("Cannot connect to SMTP server", ex);
            result = false;
        }
        return LOG.traceExit(result);
    }

    /**
     * Returns the protocol URL.
     *
     * @return the URL
     */
    public String getSmtpURL() {
        LOG.traceEntry();
        return LOG.traceExit(smtpUrl);
    }

    /**
     * Returns the protocol.
     *
     * @return the port
     */
    public String getSmtpProtocol() {
        LOG.traceEntry();
        return LOG.traceExit(smtpProtocol);
    }

    /**
     * Returns True when TLS is enable otherwise False.
     *
     * @return the tlsEnable
     */
    public String getTlsEnable() {
        LOG.traceEntry();
        return LOG.traceExit(tlsEnable);
    }

    /**
     * Returns the decrypted login.
     *
     * @return the authUser
     */
    public String getAuthUser() {
        LOG.traceEntry();
        return LOG.traceExit(authUser);
    }

    /**
     * Returns the decrypted password.
     *
     * @return the authPwd
     */
    public String getAuthPwd() {
        LOG.traceEntry();
        return LOG.traceExit(authPwd);
    }

    /**
     * Returns the administrator's email.
     *
     * @return the contactAdmin
     */
    public String getContactAdmin() {
        LOG.traceEntry();
        return LOG.traceExit(contactAdmin);
    }
    /**
     *
     */
    private static class EmailSettingsHolder {
        
        /**
         * Unique Instance unique not pre-initiliaze
         */
        private static final EmailSettings INSTANCE = new EmailSettings();
    }

}
