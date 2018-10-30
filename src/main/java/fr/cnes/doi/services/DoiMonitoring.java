/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
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

import fr.cnes.doi.utils.Utils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.restlet.data.Method;

/**
 * Speed monitoring (average time to answer requests).
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DoiMonitoring {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(Utils.APP_LOGGER_NAME);

    /**
     * Hash map of records to compute average time to answer requests.
     */
    private final Map<String, DoiMonitoringRecord> applications = new ConcurrentHashMap<>();

    /**
     * Registers the features to monitor.
     *
     * @param name Method
     * @param path path URI
     * @param description Feature's description
     */
    public void register(final Method name, final String path, final String description) {
        LOG.traceEntry("Parameters : {}, {}, {}",name, path, description);
        this.applications.put(name.getName() + path, new DoiMonitoringRecord(description, 0.0f, 0));
        LOG.traceExit();
    }

    /**
     * Add Measurement.
     *
     * @param name method
     * @param path path URI
     * @param duration duration in ms
     */
    public void addMeasurement(final Method name, final String path, final float duration) {
        LOG.traceEntry("Parameters : {} {} {}", name.getName(), path, duration);
        final String identifier = name.getName() + path;
        if (this.applications.containsKey(identifier)) {
            final DoiMonitoringRecord record = this.applications.get(name.getName() + path);
            final float previousSpeedAverage = (float) record.getAverage();
            LOG.info("current speed average = {} ms", previousSpeedAverage);
            final int previousNbAccess = (int) record.getNbAccess();
            final float newSpeedAverage = (previousSpeedAverage + duration) / (previousNbAccess + 1);
            LOG.info("new speed average = {} ms", newSpeedAverage);
            record.setAverage(newSpeedAverage);
            record.setNbAccess(previousNbAccess + 1);
        } else {
            LOG.info("Unable to add the measurement : Unknown feature");
        }
        LOG.traceExit();
    }

    /**
     * Checks if the features is registered.
     *
     * @param name method
     * @param path Path URI
     * @return True when the feature is registered
     */
    public boolean isRegistered(final Method name, final String path) {
        LOG.traceEntry("Parameters : {} and {}", name.getName(), path);
        final String identifier = name.getName() + path;
        final boolean isRegistered = this.applications.containsKey(identifier);
        LOG.debug(name+" "+path+" is registered : " + isRegistered);       
        return LOG.traceExit(isRegistered);
    }

    /**
     * Returns the average speed of the measurement.
     *
     * @param name method name
     * @param path path URI
     * @return the average speed
     */
    public float getCurrentAverage(final Method name, final String path) {    
        LOG.traceEntry("Parameters : {} and {}", name.getName(), path);
        final float average;
        final String identifier = name.getName() + path;
        if (isRegistered(name, path)) {
            average = this.applications.get(identifier).getAverage();
        } else {
            throw LOG.throwing(new IllegalArgumentException(identifier + " is not registered"));
        }
        return LOG.traceExit(average);
    }

    /**
     * Returns the description.
     *
     * @param name method name
     * @param path path URI
     * @return the description
     */
    public String getDescription(final Method name, final String path) {    
        LOG.traceEntry("Parameters : {} and {}", name.getName(), path);        
        final String identifier = name.getName() + path;
        final String description = this.applications.get(identifier).getDescription();
        return LOG.traceExit(description);
    }

}
