/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging.api;

import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;
import org.restlet.service.LogService;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.resource.mds.DoiResource;
import fr.cnes.doi.resource.mds.DoisResource;
import fr.cnes.doi.resource.mds.MetadataResource;
import fr.cnes.doi.resource.mds.MetadatasResource;
import fr.cnes.doi.server.DoiServer;
import fr.cnes.doi.services.DoiMonitoring;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.CoverageAnnotation;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Creates a LOG service to monitor the speed of applications.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_ARCHI_020,
        reqName = Requirement.DOI_ARCHI_020_NAME
)
public class DoiLogDataServer extends LogService {

    //private Logger logger = Engine.getLogger(Utils.HTTP_LOGGER_NAME);
    /**
     * Constructs a new logger.
     *
     * @param logName logger name
     * @param isEnabled true when logger is enabled otherwise false
     */
    public DoiLogDataServer(final String logName, final boolean isEnabled) {
        super(isEnabled);
        this.setResponseLogFormat(DoiSettings.getInstance().getString(Consts.LOG_FORMAT));
        try {
            this.setLoggerName(logName);

            if (logName != null && !"".equals(logName)) {
                Engine.getLogger(logName);
            }
        } catch (SecurityException ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex);
        }
    }

    /**
     * Create the filter that should be invoked for incoming calls
     * @param context context
     * @return filter
     */
    @Override
    public Filter createInboundFilter(final Context context) {
        return new MonitoringLogFilter(context, initMonitoring(), this);
    }

    /**
     * Init Monitoring
     *
     * @return monitoring object
     */
    private DoiMonitoring initMonitoring() {

        final DoiMonitoring monitoring = new DoiMonitoring();
        monitoring.register(Method.GET, 
                DoiServer.MDS_URI + DoiMdsApplication.DOI_URI, 
                DoisResource.LIST_ALL_DOIS
        );
        monitoring.register(Method.POST, 
                DoiServer.MDS_URI + DoiMdsApplication.DOI_URI, 
                DoisResource.CREATE_DOI
        );
        monitoring.register(Method.GET, 
                DoiServer.MDS_URI + DoiMdsApplication.DOI_URI + DoiMdsApplication.DOI_NAME_URI, 
                DoiResource.GET_DOI
        );
        monitoring.register(Method.POST, 
                DoiServer.MDS_URI + DoiMdsApplication.METADATAS_URI, 
                MetadatasResource.CREATE_METADATA
        );
        monitoring.register(Method.GET, 
                DoiServer.MDS_URI + DoiMdsApplication.METADATAS_URI 
                        + DoiMdsApplication.DOI_NAME_URI, 
                MetadataResource.GET_METADATA
        );
        monitoring.register(Method.DELETE, 
                DoiServer.MDS_URI + DoiMdsApplication.METADATAS_URI 
                        + DoiMdsApplication.DOI_NAME_URI, 
                MetadataResource.DELETE_METADATA
        );

        return monitoring;
    }

}
