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

import fr.cnes.doi.services.DoiMonitoring;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.utils.Utils;
import fr.cnes.doi.utils.spec.Requirement;
import org.apache.logging.log4j.LogManager;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.engine.log.LogFilter;
import org.restlet.service.LogService;

/**
 * Filter to monitor the applications speed performance.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_MONIT_010, reqName = Requirement.DOI_MONIT_010_NAME)
@Requirement(reqId = Requirement.DOI_ARCHI_020, reqName = Requirement.DOI_ARCHI_020_NAME)
public class MonitoringLogFilter extends LogFilter {

    /**
     * Threshold from which an alarm is send.
     */
    private static final float THRESHOLD_SPEED_PERCENT = Float
            .valueOf(DoiSettings.getInstance().getString(Consts.THRESHOLD_SPEED_PERCENT));

    /**
     * The monitoring object
     */
    private final DoiMonitoring monitoring;

    /**
     * Constructs a filter that filters applications to monitor
     *
     * @param context the Context
     * @param doiMonitoring DOI monitoring
     * @param logService the {@link LogService}
     */
    public MonitoringLogFilter(final Context context, final DoiMonitoring doiMonitoring,
            final LogService logService) {
        super(context, logService);
        this.monitoring = doiMonitoring;
        this.logService
                .setResponseLogFormat(DoiSettings.getInstance().getString(Consts.LOG_FORMAT));
    }

    /**
     * Allows filtering after processing by the next Restlet. Does nothing by
     * default.
     *
     * @param request request
     * @param response response
     */
    @Override
    protected void afterHandle(final Request request, final Response response) {
        if (this.logService.isLoggable(request)) {

            LogManager.getLogger(Utils.HTTP_LOGGER_NAME)
                    .info(this.logService.getResponseLogMessage(response, 1));
        }
        if (response.getStatus().isSuccess()) {
            final String path = request.getResourceRef().getPath();
            final Method method = request.getMethod();
            final long startTime = (Long) request.getAttributes().get("org.restlet.startTime");
            final int duration = (int) (System.currentTimeMillis() - startTime);
            if (monitoring.isRegistered(method, path)) {
                monitoring.addMeasurement(method, path, duration);
                final String description = monitoring.getDescription(method, path);
                final String methodName = method.getName();
                final float average = monitoring.getCurrentAverage(method, path);
                LogManager.getLogger(Utils.APP_LOGGER_NAME).info(
                        "{}({} {}) - current speed average : {} ms - "
                        + "current measure: {} ms",
                        description, methodName, path, average, duration);
                sendAlertIfNeeded(monitoring.getCurrentAverage(method, path), duration, path,
                        method);
            }
        }
    }

    /**
     * Send an email alert if the time to answer request it too long
     *
     * @param average time average
     * @param currentDuration current duration
     * @param path resource name
     * @param method method name
     */
    private void sendAlertIfNeeded(final double average, final double currentDuration,
            final String path, final Method method) {
        final double elevation = (currentDuration - average * 1000) * 100.0 / average;
        if (elevation > THRESHOLD_SPEED_PERCENT) {
            final EmailSettings email = EmailSettings.getInstance();
            final String subject = "Speed performance alert for " + path;
            final String msg = "Dear administrator,\nthe speed performance of the " + "application "
                    + path + "" + " has been reduced of " + String.format("%.02f", elevation)
                    + "% using the method " + method.getName() + ".\n\n" + "Details:\n"
                    + "--------\n" + " * Average : " + average + "\n" + " * current : "
                    + currentDuration + "\n";
            email.sendMessage(subject, msg);
        }
    }

}
