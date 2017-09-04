/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Singleton to load and use Email settings.
 *
 * @author Jean-Christophe Malapert
 */
public final class EmailSettings {

    private static final boolean DEFAULT_DEBUG = false;

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(EmailSettings.class.getName());

    private String smtpUrl;
    private String smtpProtocol;
    private String tlsEnable;
    private String authUser;
    private String authPwd;
    private String contactAdmin;
    private boolean debug = DEFAULT_DEBUG;

    /**
     * Constructor
     */
    private EmailSettings() {
        init();
    }

    private static class EmailSettingsHolder {

        /**
         * Unique Instance unique not pre-initiliaze
         */
        private final static EmailSettings INSTANCE = new EmailSettings();
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
        LOGGER.entering(this.getClass().getName(), "init");
        DoiSettings settings = DoiSettings.getInstance();
        this.smtpProtocol = settings.getString(Consts.SMTP_PROTOCOL);
        this.smtpUrl = settings.getString(Consts.SMTP_URL);
        this.authUser = settings.getSecret(Consts.SMTP_AUTH_USER);
        this.authPwd = settings.getSecret(Consts.SMTP_AUTH_PWD);
        this.tlsEnable = settings.getString(Consts.SMTP_STARTTLS_ENABLE);
        this.contactAdmin = settings.getString(Consts.SERVER_CONTACT_ADMIN);
        LOGGER.info("Email settings have been loaded");        
        LOGGER.exiting(this.getClass().getName(), "init");        
    }

    /**
     * @param isEnabled
     */
    public void setDebug(boolean isEnabled) {
        LOGGER.log(Level.CONFIG, "setDebug to {0}", isEnabled);
        this.debug = isEnabled;
    }

    /**
     * Debug
     *
     * @return debug
     */
    public boolean getDebug() {
        LOGGER.log(Level.CONFIG, "getDebug : {0}", this.debug);
        return this.debug;
    }

    /**
     * Sends a message by email.
     *
     * @param subject Email's subject
     * @param msg Email's message
     * @return True when the message is sent
     */
    public boolean sendMessage(final String subject, final String msg) {
        LOGGER.entering(this.getClass().getName(), "sendMessage", new Object[]{subject, msg});
        boolean result;
        try {
            final Request request = new Request(Method.POST, getSmtpURL());
            LOGGER.finer("Sets the login/passwd for the SMTP server");
            request.setChallengeResponse(
                    new ChallengeResponse(ChallengeScheme.SMTP_PLAIN, getAuthUser(), getAuthPwd()));
            result = sendMail(Protocol.valueOf(getSmtpProtocol()), request, Boolean.getBoolean(getTlsEnable()), subject,
                    msg);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, String.format("Cannot send the message with the subject %s : %s", subject, msg), ex);
            result = false;
        }
        LOGGER.exiting(this.getClass().getName(), "sendMessage", result);        
        return result;
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
     */
    private boolean sendMail(final Protocol protocol, final Request request, boolean startTls, final String subject,
            final String msg) throws Exception {
        boolean result;
        LOGGER.entering(this.getClass().getName(), "sendMail", new Object[]{protocol.getName(), startTls, subject, msg});
        final Client client = new Client(protocol);
        Context context = new Context();
        client.setContext(context);
        client.getContext().getParameters().add("debug", String.valueOf(getDebug()));
        client.getContext().getParameters().add("startTls", Boolean.toString(startTls).toLowerCase());
        Map<String, String> dataModel = new TreeMap<>();
        dataModel.put("subject", subject);
        dataModel.put("message", msg);
        dataModel.put("from",
                DoiSettings.getInstance().getString(Consts.SERVER_CONTACT_ADMIN, "L-doi-support@cnes.fr"));
        dataModel.put("to", DoiSettings.getInstance().getString(Consts.SERVER_CONTACT_ADMIN, "L-doi-support@cnes.fr"));
        Representation mailFtl = new ClientResource(LocalReference.createClapReference("class/email.ftl")).get();
        Representation mail = new TemplateRepresentation(mailFtl, dataModel, MediaType.TEXT_XML);
        request.setEntity(mail);
        final Response response = client.handle(request);
        Status status = response.getStatus();
        if (status.isError()) {
            LOGGER.log(Level.SEVERE, "Cannot send the email! : {0}", status.getDescription());
            result = false;
        } else {
            result = true;
        }
        client.stop();
        LOGGER.exiting(this.getClass().getName(), "sendMail", result);
        return result;
    }

    /**
     * Returns the protocol URL.
     *
     * @return the URL
     */
    public String getSmtpURL() {
        LOGGER.log(Level.CONFIG, "getSmtpUrl : {0}", this.smtpUrl);
        return smtpUrl;
    }

    /**
     * Returns the protocol.
     *
     * @return the port
     */
    public String getSmtpProtocol() {
        LOGGER.log(Level.CONFIG, "getSmtpProtocol : {0}", this.smtpProtocol);        
        return smtpProtocol;
    }

    /**
     * Returns True when TLS is enable otherwise False.
     *
     * @return the tlsEnable
     */
    public String getTlsEnable() {
        LOGGER.log(Level.CONFIG, "getTlsEnable : {0}", this.tlsEnable);        
        return tlsEnable;
    }

    /**
     * Returns the decrypted login.
     *
     * @return the authUser
     */
    public String getAuthUser() {
        LOGGER.log(Level.CONFIG, "getAuthUser : {0}", this.authUser);                
        return authUser;
    }

    /**
     * Returns the decrypted password.
     *
     * @return the authPwd
     */
    public String getAuthPwd() {
        LOGGER.log(Level.CONFIG, "getAuthPwd : {0}", this.authPwd);                        
        return authPwd;
    }

    /**
     * Returns the administrator's email.
     *
     * @return the contactAdmin
     */
    public String getContactAdmin() {
        LOGGER.log(Level.CONFIG, "getContactAdmin : {0}", this.contactAdmin);                                
        return contactAdmin;
    }

}
