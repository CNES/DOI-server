/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging.api;

import fr.cnes.doi.server.monitoring.DoiMonitoring;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.utils.Requirement;
import fr.cnes.doi.utils.Utils;

import java.text.MessageFormat;
import java.util.Date;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.engine.Engine;
import org.restlet.engine.log.LogFilter;
import org.restlet.engine.log.LogUtils;
import org.restlet.service.LogService;

/**
 * Filter to monitor the applications speed performance.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = "DOI_SRV_130",
        reqName = "Monitoring des temps de réponse"
)
@Requirement(
        reqId = "DOI_ARCHI_040",
        reqName = "Logs"
)
public class MonitoringLogFilter extends LogFilter {

    private static final Date START_DATE_MONITORING = new Date();

    private static final float THRESHOLD_SPEED_PERCENT = 130;

    private static final Logger APP_LOGGER = Utils.getAppLogger();

    /**
     * The monitoring object
     */
    private final DoiMonitoring monitoring;

    /**
     * Constructs a filter that filters applications to monitor
     *
     * @param context the Context
     * @param doiMonitoring
     * @param logService the {@link LogService}
     */
    public MonitoringLogFilter(final Context context, final DoiMonitoring doiMonitoring, final LogService logService) {
        super(context, logService);
        this.monitoring = doiMonitoring;

        if (logService != null) {
            if (logService.getLoggerName() != null) {
                Engine.getLogger(logService.getLoggerName());
            } else if ((context != null) && (context.getLogger().getParent() != null)) {
                Engine.getLogger(context.getLogger().getParent().getName() + "."
                        + LogUtils.getBestClassName(logService.getClass()));
            } else {
                Engine.getLogger(LogUtils.getBestClassName(logService.getClass()));
            }
        }
    }

    /* (non-Javadoc)
	 * @see org.restlet.engine.log.LogFilter#afterHandle(org.restlet.Request, org.restlet.Response)
     */
    /**
     *
     * @param request
     * @param response
     */
    @Override
    protected void afterHandle(Request request, Response response) {
        if (APP_LOGGER.isLoggable(Level.INFO) && response.getStatus().isSuccess()) {
            String path = request.getResourceRef().getPath();
            Method method = request.getMethod();
            long startTime = (Long) request.getAttributes().get("org.restlet.startTime");
            int duration = (int) (System.currentTimeMillis() - startTime);
            if (monitoring.isRegistered(method, path)) {
                monitoring.addMeasurement(method, path, duration);
                APP_LOGGER.log(Level.INFO, MessageFormat.format("{0}({1} {2}) - current speed average : {3} ms / current measure: {4} ms", monitoring.getDescription(method, path), method.getName(), path, monitoring.getCurrentAverage(method, path), duration));
                sendAlertIfNeeded(monitoring.getCurrentAverage(method, path), duration, path, method);
            }

        }

    }

    /**
     * Send an email alert if the time to answer request it too long
     *
     * @param average
     * @param currentDuration
     * @param path
     * @param method
     */
    private void sendAlertIfNeeded(double average, double currentDuration, final String path, final Method method) {
        double elevation = currentDuration * 100.0 / average;
        if (elevation > THRESHOLD_SPEED_PERCENT) {
            EmailSettings email = EmailSettings.getInstance();
            String subject = "Speed performance alert for " + path;
            String msg = "Dear administrator,\nthe speed performance of the application " + path + ""
                    + " has been reduced than more 30% using the method " + method.getName() + ".\n\n"
                    + "Details:\n"
                    + "--------\n"
                    + " * Average : " + average + "\n"
                    + " * current : " + currentDuration + "\n";
            email.sendMessage(subject, msg);
        }
    }

}
