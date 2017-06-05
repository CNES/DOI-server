/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging;

import com.sun.mail.handlers.message_rfc822;
import fr.cnes.doi.server.DoiMonitoring;
import fr.cnes.doi.settings.EmailSettings;

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
 * @author Jean-Christophe Malapert
 */
public class MonitoringLogFilter extends LogFilter {

    /**
     * The logger to log to
     */
    private Logger logger;

    /**
     * The monitoring object
     */
    private DoiMonitoring monitoring;

    /**
     * Constructs a filter that filters applications to monitor
     *
     * @param context the Context
     * @param doiMonitoring
     * @param logService the {@link LogService}
     */
    public MonitoringLogFilter(Context context, DoiMonitoring doiMonitoring, LogService logService) {
        super(context, logService);
        this.monitoring = doiMonitoring;

        if (logService != null) {
            if (logService.getLoggerName() != null) {
                this.logger = Engine.getLogger(logService.getLoggerName());
            } else if ((context != null) && (context.getLogger().getParent() != null)) {
                this.logger = Engine.getLogger(context.getLogger().getParent().getName() + "."
                        + LogUtils.getBestClassName(logService.getClass()));
            } else {
                this.logger = Engine.getLogger(LogUtils.getBestClassName(logService.getClass()));
            }
        }
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        //if(logger.isLoggable(Level.INFO)) {

        if (logger.isLoggable(Level.INFO) && response.getStatus().isSuccess()) {
            String path = request.getResourceRef().getPath();
            Method method = request.getMethod();
            long startTime = (Long) request.getAttributes().get("org.restlet.startTime");
            int duration = (int) (System.currentTimeMillis() - startTime);
            if (monitoring.isRegistered(method, path)) {
                monitoring.addMeasurement(method, path, duration);
                logger.log(Level.INFO, "{0}({1} {2}) - current Mean: {3} ms / current measure: {4} ms", new Object[]{monitoring.getDescription(method, path), method.getName(), path, monitoring.getCurrentMean(method, path), duration});
                sendAlertIfNeeded(monitoring.getCurrentMean(method, path), duration, path, method);
            }

        }

    }
    
    private void sendAlertIfNeeded(double mean, double currentDuration, final String path, final Method method) {
        double elevation = currentDuration * 100.0 / mean;
        if(elevation > 30) {
            EmailSettings email = EmailSettings.getInstance();
            String subject = "Speed performance alert for "+path;
            String msg = "Dear administrator,\nthe speed performance of the application "+path+""
                    + " has been reduced than more 30% using the method "+method.getName()+".\n\n"
                    + "Details:\n"
                    + "--------\n"
                    + " * Mean : "+mean+"\n"
                    + " * current : "+currentDuration+"\n";
            email.sendEmail(subject, msg);
        }
    }

}
