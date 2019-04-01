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
package fr.cnes.doi.settings;

import fr.cnes.doi.utils.spec.Requirement;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.javamail.JavaMailClientHelper;
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
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static EmailSettings getInstance() {
        return EmailSettingsHolder.INSTANCE;
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

        this.authUser = settings.getString(Consts.SMTP_AUTH_USER, "");
        LOG.info(String.format("auth user : %s", this.authUser));

        this.authPwd = settings.getSecret(Consts.SMTP_AUTH_PWD);
        LOG.info(String.format("auth pwd : %s", this.authPwd));

        this.tlsEnable = settings.getString(Consts.SMTP_STARTTLS_ENABLE, "false");
        LOG.info(String.format("TLS enable : %s", this.tlsEnable));

        this.contactAdmin = settings.getString(Consts.SERVER_CONTACT_ADMIN, "L-doi-support@cnes.fr");
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
    public boolean sendMessage(final String subject, final String msg) {
        final String email = EmailSettings.getInstance().getContactAdmin();
        return this.sendMessage(subject, msg, email);
    }

    /**
     * Sends a message by email to receiver.
     *
     * @param subject Email's subject
     * @param msg Email's message
     * @param receiverEmail receiver
     * @return True when the message is sent
     */
    public boolean sendMessage(final String subject, final String msg, final String receiverEmail) {
        LOG.traceEntry("Parameters : {}, {} and {}", subject, msg, receiverEmail);
        boolean result;
        try {
            if (isConfigureForSendingEmail()) {
                LOG.info("Try to send this message {} to {}", msg, receiverEmail);
                result = processMessage(subject, msg, receiverEmail);
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
     * @param receiverEmail receiver
     * @return true when the message has been send otherwise false
     * @throws Exception when an error happens
     */
    private boolean processMessage(final String subject,
            final String message, final String receiverEmail) throws Exception {
        LOG.debug("Enough information to send the email.");
        final Request request = new Request(Method.POST, getSmtpURL());
        setSmtpCredentials(request);
        return sendMail(Protocol.valueOf(getSmtpProtocol()), request, Boolean.getBoolean(
                getTlsEnable()), subject, message, receiverEmail);
    }

    /**
     * Sets the SMTP credential when the SMTP needs an authentication.
     *
     * @param request request
     */
    private void setSmtpCredentials(final Request request) {
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
     * Creates the email representation.
     *
     * @param subject Email's subject
     * @param msg Email's message
     * @param to receiver
     * @return the email representation
     */
    private Representation createMailRepresentation(final String subject, final String msg,
            final String to) {
        final Map<String, String> dataModel = new ConcurrentHashMap<>();
        dataModel.put("subject", subject);
        dataModel.put("message", msg);
        dataModel.put("from", this.getContactAdmin());
        dataModel.put("to", to);
        final Representation mailFtl = new ClientResource(LocalReference.createClapReference(
                "class/email.ftl")).get();
        return new TemplateRepresentation(mailFtl, dataModel, MediaType.TEXT_XML);
    }

    /**
     * Sends an email only when {@value fr.cnes.doi.settings.Consts#CONTEXT_MODE} is set to PROD.
     *
     * @param protocol Protocol (SMTP or SMTPS)
     * @param request request
     * @param startTls startTls
     * @param subject Email's subject
     * @param msg Email's message
     * @param to receiver
     * @return True when the message is sent
     * @throws Exception - if an error happens when stopping the request
     */
    private boolean sendMail(final Protocol protocol,
            final Request request,
            final boolean startTls,
            final String subject,
            final String msg,
            final String to) throws Exception {
        LOG.traceEntry("Parameters\n  protocol:{}\n  startTls:{}, request:{}\n  subject:{}\n  "
                + "msg:{}\n  to:{}",
                protocol, request, startTls, subject, msg, to);
        final String contextMode = DoiSettings.getInstance().getString(Consts.CONTEXT_MODE);
        final Representation mail = this.createMailRepresentation(subject, msg, to);
        request.setEntity(mail);
        final boolean result;
        if ("PROD".equals(contextMode)) {
            final Client client = new Client(new Context(), protocol);
            final Series<Parameter> parameters = client.getContext().getParameters();
            parameters.add("debug", String.valueOf(isDebug()));
            parameters.add("startTls", Boolean.toString(startTls).toLowerCase(Locale.ENGLISH));
            final JavaMailClientHelper smtpClient = new JavaMailClientHelper(client);
            final Response response = new Response(request);
            smtpClient.handle(request, response);
            final Status status = response.getStatus();
            if (status.isSuccess()) {
                result = true;
                LOG.info("Message sent to {}", to);
            } else {
                result = false;
                LOG.error("Cannot connect to SMTP server", status.getThrowable());
            }
        } else {
            result = true;
            LOG.warn("The configuration context {} is not PROD, do not send the email : {}",
                    contextMode, mail.getText());
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
