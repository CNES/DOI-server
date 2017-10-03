/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.services;

import fr.cnes.doi.client.ClientLandingPage;
import fr.cnes.doi.client.ClientSearchDataCite;
import fr.cnes.doi.exception.MailingException;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides a check on the availability of each published landing page 
 * publisher
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_DISPO_020,
        reqName = Requirement.DOI_DISPO_020_NAME
)
public class LandingPageMonitoring implements Runnable {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(LandingPageMonitoring.class.getName());

    /**
     * run.
     */
    @Override
    public void run() {
        LOG.traceEntry();
        final EmailSettings email = EmailSettings.getInstance();        
        final StringBuffer msg = new StringBuffer();
        try {
            final String subject;
            final ClientSearchDataCite client = new ClientSearchDataCite();
            final List<String> response = client.getDois();
            final ClientLandingPage clientLandingPage = new ClientLandingPage(response);

            if (clientLandingPage.isSuccess()) {
                subject = "Landing pages checked with success";
                msg.append("All landing pages (").append(response.size()).append(") are on-line");
            } else {
                subject = "Landing pages checked with errors";
                final List<String> errors = clientLandingPage.getErrors();
                msg.append(errors.size()).append(" are off-line !!!\n");
                msg.append("List of off-line landing pages:\n");
                msg.append("-------------------------------\n");
                for (final String error : errors) {
                    msg.append("- ").append(error).append("\n");
                }
            }
            email.sendMessage(subject, msg.toString());
            LOG.info("message to send : {}", msg.toString());
        } catch (Exception ex) {
            try {
                email.sendMessage("Unrecoverable errors when checking landing pages", ex.toString());
            } catch (MailingException ex1) {
                LOG.fatal("Cannot send the email", ex1);
            }
            LOG.fatal("Cannot send the email", ex);
        }
    }

}
