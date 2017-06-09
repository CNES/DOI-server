/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.Utils;
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
    private static final Logger LOGGER = Utils.getAppLogger();    
    
    /**
     * Creates a specific error page.
     */
    public CnesStatusService() {
        this.settings = DoiSettings.getInstance();
    }

    /**
     * Returns the representation of the status page.
     * @param status Status
     * @param request request
     * @param response response
     * @return the representation of the status page
     */
    @Override
    public Representation getRepresentation(final Status status, final Request request, final Response response) {
        Map<String, String> dataModel = new TreeMap<>();       
        dataModel.put("applicationName", Application.getCurrent().getName());
        dataModel.put("statusCode", String.valueOf(response.getStatus().getCode()));
        dataModel.put("statusName", response.getStatus().getReasonPhrase());
        dataModel.put("statusDescription", response.getStatus().getDescription());
        dataModel.put("logo", "/resources/images/Cnes-logo.png");
        dataModel.put("contactAdmin", settings.getString(Consts.SERVER_CONTACT_ADMIN, ""));
        LOGGER.log(Level.FINER, "Data model for CNES status page", dataModel);
        Representation mailFtl = new ClientResource(LocalReference.createClapReference("class/CnesStatus.ftl")).get();
        return new TemplateRepresentation(mailFtl, dataModel, MediaType.TEXT_HTML);
    }
}
