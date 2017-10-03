/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.services;

import com.fasterxml.jackson.annotation.JsonFormat;
import fr.cnes.doi.logging.business.JsonMessage;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.service.StatusService;

import java.util.Map;

import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.settings.Consts;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides a specific error page, which is sent in the HTTP response.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class CnesStatusService extends StatusService {

    /**
     * Configuration file.
     */
    private final DoiSettings settings;

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(CnesStatusService.class.getName());
    
    /**
     * Creates a specific error page.
     */
    public CnesStatusService() {
        super();
        this.settings = DoiSettings.getInstance();
    }

    /**
     * Returns the representation of the status page.
     *
     * @param status Status to send
     * @param request request to server
     * @param response response from the server
     * @return the representation of the error page
     */
    @Override
    public Representation getRepresentation(final Status status, final Request request, final Response response) {
        final Map<String, String> dataModel = createDataModel(response);
        final Representation mailFtl = new ClientResource(LocalReference.createClapReference("class/CnesStatus.ftl")).get();
        return new TemplateRepresentation(mailFtl, dataModel, MediaType.TEXT_HTML);
    }

    /**
     * Creates a data model. The data model is used to replace values in the
     * template CnesStatus.ftl.
     *
     * @param response Response from server
     * @return the data model
     */
    private Map<String, String> createDataModel(final Response response) {
        LOG.traceEntry(new JsonMessage(response));
        final Map<String, String> dataModel = new ConcurrentHashMap<>();
        dataModel.put("applicationName", Application.getCurrent().getName());
        dataModel.put("statusCode", String.valueOf(response.getStatus().getCode()));
        dataModel.put("statusName", response.getStatus().getReasonPhrase());
        dataModel.put("statusDescription", response.getStatus().getDescription());
        dataModel.put("logo", "/resources/images/Cnes-logo.png");
        dataModel.put("contactAdmin", settings.getString(Consts.SERVER_CONTACT_ADMIN, ""));
        return LOG.traceExit(dataModel);
    }
}
