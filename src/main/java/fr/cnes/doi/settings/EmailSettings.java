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
 * Email settings and method do send an email.
 * @author Jean-Christophe Malapert
 */
public class EmailSettings {
    
    private static final boolean DEFAULT_DEBUG = false;
    
    private static final Logger LOGGER = Logger.getLogger(EmailSettings.class.getName());

    private String smtpUrl;
    private String smtpProtocol;
    private String tlsEnable;
    private String authUser;
    private String authPwd;
    private String contactAdmin;
    private boolean debug = DEFAULT_DEBUG;
    

    private EmailSettings() {
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

    public void init(final DoiSettings settings) {
        this.smtpProtocol = settings.getString(Consts.SMTP_PROTOCOL);
        this.smtpUrl = settings.getString(Consts.SMTP_URL);
        this.authUser = settings.getSecret(Consts.SMTP_AUTH_USER);
        this.authPwd = settings.getSecret(Consts.SMTP_AUTH_PWD);
        this.tlsEnable = settings.getString(Consts.SMTP_STARTTLS_ENABLE);
    }
    
    public void setDebug(boolean isEnabled) {
        this.debug = isEnabled;
    }
    
    public boolean getDebug() {
        return this.debug;
    }
    
    /**
     * Sends a message by email.
     * @param subject Email's subject
     * @param msg  Email's message
     */
    public void sendMessage(final String subject, final String msg) {
        try {
            final Request request = new Request(Method.POST, getSmtpURL());
            request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.SMTP_PLAIN, getAuthUser(), getAuthPwd()));               
            sendMail(Protocol.valueOf(getSmtpProtocol()), request, Boolean.getBoolean(getTlsEnable()), subject, msg);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }    
            
    /**
     * Sends email.
     * @param protocol Protocol (SMTP or SMTPS)
     * @param request request
     * @param startTls startTls
     * @param subject Email's subject
     * @param msg Email's message
     * @throws Exception 
     */
    private void sendMail(final Protocol protocol, final Request request, boolean startTls, final String subject, final String msg) throws Exception {
        final Client client = new Client(protocol);
        client.getContext().getParameters().add("debug", String.valueOf(getDebug()));
        client.getContext().getParameters().add("startTls", Boolean.toString(startTls).toLowerCase());
        Map<String, String> dataModel = new TreeMap<>();
        dataModel.put("subject", subject);
        dataModel.put("message", msg);
        dataModel.put("from", DoiSettings.getInstance().getString(Consts.SERVER_CONTACT_ADMIN, "L-doi-support@cnes.fr"));
        dataModel.put("to", DoiSettings.getInstance().getString(Consts.SERVER_CONTACT_ADMIN, "L-doi-support@cnes.fr"));
        Representation mailFtl = new ClientResource(LocalReference.createClapReference("class/resources/email.ftl")).get();
        Representation mail = new TemplateRepresentation(mailFtl, dataModel, MediaType.TEXT_XML);
        request.setEntity(mail);
        final Response response = client.handle(request);
        Status status = response.getStatus();
        if(status.isError()) {
            LOGGER.severe("Cannot send the email!");
        } 
        client.stop();
    }       

    /**
     * @return the hostName
     */
    public String getSmtpURL() {
        return smtpUrl;
    }

    /**
     * @return the port
     */
    public String getSmtpProtocol() {
        return smtpProtocol;
    }

    /**
     * @return the tlsEnable
     */
    public String getTlsEnable() {
        return tlsEnable;
    }

    /**
     * @return the authUser
     */
    public String getAuthUser() {
        return authUser;
    }

    /**
     * @return the authPwd
     */
    public String getAuthPwd() {
        return authPwd;
    }

    /**
     * @return the contactAdmin
     */
    public String getContactAdmin() {
        return contactAdmin;
    }

}
