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
package fr.cnes.doi.services;

import fr.cnes.doi.client.ClientLandingPage;
import fr.cnes.doi.client.ClientSearchDataCite;
import fr.cnes.doi.db.AbstractProjectSuffixDBHelper;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Status;

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
            final Map<String, Map<String, Status>> doiErrors = new HashMap<>();
            final ClientSearchDataCite client = new ClientSearchDataCite();
            final List<String> response = client.getDois();
            final ClientLandingPage clientLandingPage = new ClientLandingPage(response);

            if (clientLandingPage.isSuccess()) {
                subject = "Landing pages checked with success";
                msg.append("All landing pages (").append(response.size()).append(") are on-line");
            } else {
                subject = "Landing pages checked with errors";

                final StringBuffer header = new StringBuffer();
                header.append("List of off-line landing pages:\n");
                header.append("-------------------------------\n");

                final Map<String, Status> errors = clientLandingPage.getErrors();
                msg.append(errors.size()).append(" are off-line !!!\n");
                msg.append(header);

                // build error index by projectID and a global message
                for (final Entry<String, Status> entry : errors.entrySet()) {
                    msg.append(buildDetailMessage(entry));

                    final String projectID = this.extractProjIdFrom(entry.getKey());
                    if (doiErrors.containsKey(projectID)) {
                        doiErrors.get(projectID).put(entry.getKey(), entry.getValue());
                    } else {
                        doiErrors.put(projectID, new HashMap<>());
                        doiErrors.get(projectID).put(entry.getKey(), entry.getValue());
                    }
                }
                // build message by project
                for (final Entry<String, Map<String, Status>> project : doiErrors.entrySet()) {
                    final StringBuffer body = new StringBuffer();
                    body.append(header);
                    final Map<String, Status> errorsDoi = project.getValue();
                    for (final Entry<String, Status> errorDoi : errorsDoi.entrySet()) {
                        body.append(buildDetailMessage(errorDoi));
                    }
                    sendMessageToMembers(project.getKey(), subject, body.toString(), email);
                }
            }
            email.sendMessage(subject, msg.toString());
        } catch (Exception ex) {
            email.sendMessage("Unrecoverable errors when checking landing pages", ex.toString(),
                    null);
        }
    }

    /**
     * Build detail message for a record
     * @param record
     * @return the message
     */
    private StringBuffer buildDetailMessage(final Entry<String, Status> record) {
        final StringBuffer msg = new StringBuffer();
        msg.append("- ").append(record.getKey())
                .append(" (").append(record.getValue().getCode())
                .append(" - ").append(record.getValue().getDescription()).append(" )")
                .append("\n");
        return msg;
    }

    /**
     * Extract projectID from the DOI
     *
     * @param doi doi
     * @return project ID
     */
    private String extractProjIdFrom(final String doi) {
        // parse project suffix from doi
        final String doiRegex = "^(.+)\\/(.+)\\/(.+)$";
        final Pattern doiPattern = Pattern.compile(doiRegex);
        final Matcher doiMatcher = doiPattern.matcher(doi);
        doiMatcher.matches();
        return doiMatcher.group(2);
    }

    /**
     * Send message by email to project members.
     *
     * @param doiSuffix project ID
     * @param subject email subject
     * @param body email body
     * @param email email settings
     */
    private void sendMessageToMembers(final String doiSuffix, final String subject,
            final String body, final EmailSettings email) {
        final AbstractProjectSuffixDBHelper manageProjects = PluginFactory.getProjectSuffix();
        List<DOIUser> members;
        try {
            members = manageProjects.getAllDOIUsersForProject(Integer.parseInt(doiSuffix));
        } catch (DOIDbException ex) {
            members = new ArrayList<>();
        }
        for (final DOIUser member : members) {
            email.sendMessage(subject, body, member.getEmail());
        }
    }
}
