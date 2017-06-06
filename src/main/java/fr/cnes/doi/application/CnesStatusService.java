/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.settings.Consts;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 * Specific status page in case of error.
 * @author Jean-Christophe Malapert
 */
public class CnesStatusService extends StatusService {
    
    /**
     * Configuration file.
     */
    private final DoiSettings settings;    
    private static final Logger LOGGER = Logger.getLogger(CnesStatusService.class.getName());    
    
    /**
     * Creates a specific error page.
     */
    public CnesStatusService() {
        this.settings = DoiSettings.getInstance();
    }

    @Override
    public Representation getRepresentation(Status status, Request request, Response response) {
        Map<String, String> dataModel = new TreeMap<>();       
        dataModel.put("applicationName", Application.getCurrent().getName());
        dataModel.put("statusCode", String.valueOf(response.getStatus().getCode()));
        dataModel.put("statusName", response.getStatus().getReasonPhrase());
        dataModel.put("statusDescription", response.getStatus().getDescription());
        dataModel.put("logo", "/resources/images/Cnes-logo.png");
        dataModel.put("contactAdmin", settings.getString(Consts.SERVER_CONTACT_ADMIN, ""));
        LOGGER.log(Level.FINER, "Data model for CNES status page", dataModel);
        Representation mailFtl = new ClientResource(LocalReference.createClapReference(getClass().getPackage()) + "/CnesStatus.ftl").get();
        return new TemplateRepresentation(mailFtl, dataModel, MediaType.TEXT_HTML);
    }
}
