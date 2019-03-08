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
package fr.cnes.doi.logging.api;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.resource.mds.DoiResource;
import fr.cnes.doi.resource.mds.DoisResource;
import fr.cnes.doi.resource.mds.MetadataResource;
import fr.cnes.doi.resource.mds.MetadatasResource;
import fr.cnes.doi.server.DoiServer;
import fr.cnes.doi.services.DoiMonitoring;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.Requirement;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.routing.Filter;
import org.restlet.service.LogService;

/**
 * Creates a LOG service to monitor the speed of applications.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_ARCHI_020, reqName = Requirement.DOI_ARCHI_020_NAME)
public class DoiLogDataServer extends LogService {

    //private Logger logger = Engine.getLogger(Utils.HTTP_LOGGER_NAME);
    /**
     * Constructs a new logger.
     *
     * @param logName logger name
     * @param isEnabled true when logger is enabled otherwise false
     */
    public DoiLogDataServer(final String logName,
            final boolean isEnabled) {
        super(isEnabled);
        this.setLoggerName(logName);
        this.setResponseLogFormat(DoiSettings.getInstance().getString(Consts.LOG_FORMAT));
    }

    /**
     * Create the filter that should be invoked for incoming calls
     *
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
