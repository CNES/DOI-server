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
package fr.cnes.doi.services;

import fr.cnes.doi.client.ClientLandingPage;
import fr.cnes.doi.client.ClientSearchDataCite;
import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.impl.DOIDbDataAccessServiceImpl;
import fr.cnes.doi.persistence.model.DOIUser;
import fr.cnes.doi.persistence.service.DOIDbDataAccessService;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides a check on the availability of each published landing page publisher
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_DISPO_020, reqName = Requirement.DOI_DISPO_020_NAME)
public class LandingPageMonitoring implements Runnable {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(LandingPageMonitoring.class.getName());

    private DOIDbDataAccessService dbAccessService = new DOIDbDataAccessServiceImpl();
    
    
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
                    // send message to all doi member
                    String body = "";
                    sendMessageToMembers(error, subject, body, email);
                    
                }
            }
            email.sendMessage(subject, msg.toString(), null);
            
            
            LOG.info("message to send : {}", msg.toString());
        } catch (Exception ex) {
            email.sendMessage("Unrecoverable errors when checking landing pages", ex.toString(), null);
        }
    }

	private void sendMessageToMembers(String error, String subject, String body, EmailSettings email) {
		try {
			// parse project suffix from doi
			String doiRegex = "^(.+)\\/(.+)\\/(.+)$";
			Pattern doiPattern = Pattern.compile(doiRegex);
			Matcher doiMatcher = doiPattern.matcher(error);
			int doiSuffix = Integer.parseInt(doiMatcher.group(3));
			List<DOIUser> members = dbAccessService.getAllDOIUsersForProject(doiSuffix);
			for (DOIUser member: members) {
				 email.sendMessage(subject, body, member.getEmail());
			}
		} catch (DOIDbException e) {
			LOG.error("[LandingPageMonitoring] Failed to get members list of doi project (" + error + ") from doi database", e);
		}
		
	}

}
