/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author malapert
 */
public class EmailSettings {

    public static final String SMTP_HOST_NAME = "Starter.mail.send.server";
    public static final String SMTP_PORT = "Starter.mail.send.port";
    public static final String SMTP_STARTTLS_ENABLE = "Starter.mail.send.tls";
    public static final String SMTP_AUTH_USER = "Starter.mail.send.identifier";
    public static final String SMTP_AUTH_PWD = "Starter.mail.send.secret";
    public static final String CONTACT_ADMIN = "Starter.Server.contactAdmin";
    
    private static final Logger LOGGER = Logger.getLogger(EmailSettings.class.getName());

    private String hostName;
    private String port;
    private String tlsEnable;
    private String authUser;
    private String authPwd;
    private String contactAdmin;
    

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
        this.hostName = settings.getString(SMTP_HOST_NAME);
        this.port = settings.getSecret(SMTP_PORT);
        this.authUser = settings.getString(SMTP_AUTH_USER);
        this.authPwd = settings.getSecret(SMTP_AUTH_PWD);
        this.tlsEnable = settings.getString(SMTP_STARTTLS_ENABLE);
    }

    public void sendEmail(final String subject, final String msg) {
        if(StringUtils.isEmpty(getHostName()) && StringUtils.isEmpty(getPort())
           && StringUtils.isEmpty(getAuthUser()) && StringUtils.isEmpty(getAuthPwd())
           && StringUtils.isEmpty(getTlsEnable())) {
            LOGGER.warning("Required parameters are not defined");
            LOGGER.info("Simulates the email to send ... ");
            LOGGER.log(Level.INFO, "Subject: {0}", subject);
            LOGGER.log(Level.INFO, "Message: {0}", msg);           
        } else {
            sendMessage(subject, msg);
        }

    }
    
    private void sendMessage(final String subject, final String msg) {
        final String username = getAuthUser();
        final String password = getAuthPwd();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", getTlsEnable());
        props.put("mail.smtp.host", getHostName());
        props.put("mail.smtp.port", getPort());

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(getContactAdmin()));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(getContactAdmin()));
            message.setSubject(subject);
            message.setText(msg);

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }        
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
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
