/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
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
    private final Properties config;
    
    /**
     * Creates a specific error page.
     */
    public CnesStatusService() {
        this.config = null;
    }
    
    /**
     * Creates a specific error page with a data model taking account of the
     * configuration file.
     * @param config configuration file
     */
    public CnesStatusService(final Properties config) {
        this.config = config;
    }

    @Override
    public Representation getRepresentation(Status status, Request request, Response response) {
        Map<String, String> dataModel = new TreeMap<>();       
        dataModel.put("applicationName", Application.getCurrent().getName());
        dataModel.put("statusCode", String.valueOf(response.getStatus().getCode()));
        dataModel.put("statusName", response.getStatus().getReasonPhrase());
        dataModel.put("statusDescription", response.getStatus().getDescription());
        dataModel.put("logo", "/resources/images/Cnes-logo.png");
        dataModel.put("contactAdmin", (this.config == null) ? "":this.config.getProperty("CONTACT_ADMIN"));
        Representation mailFtl = new ClientResource(LocalReference.createClapReference(getClass().getPackage()) + "/CnesStatus.ftl").get();
        return new TemplateRepresentation(mailFtl, dataModel, MediaType.TEXT_HTML);
    }
}
