/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.services;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Method;

/**
 * Speed monitoring (average time to answer requests).
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DoiMonitoring {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(DoiMonitoring.class.getName());

    /**
     * Hash map of records to compute average time to answer requests.
     */
    private final Map<String, DoiMonitoringRecord> applications = new HashMap<>();

    /**
     * Constructor.
     */
    public DoiMonitoring() {

    }

    /**
     * Registers the features to monitor.
     *
     * @param name Method
     * @param path path URI
     * @param description Feature's description
     */
    public void register(final Method name, final String path, final String description) {
        LOGGER.entering(getClass().getName(), "register", new Object[]{name.getName(), path, description});
        this.applications.put(name.getName() + path, new DoiMonitoringRecord(description, 0.0f, 0));
        LOGGER.exiting(getClass().getName(), "register");
    }

    /**
     * Add Measurement.
     *
     * @param name method
     * @param path path URI
     * @param duration duration in ms
     */
    public void addMeasurement(final Method name, final String path, float duration) {
        LOGGER.entering(getClass().getName(), "addMeasurement", new Object[]{name.getName(), path, duration});
        String id = name.getName() + path;
        if (this.applications.containsKey(id)) {
            DoiMonitoringRecord record = this.applications.get(name.getName() + path);
            float previousSpeedAverage = (float) record.getAverage();
            LOGGER.log(Level.CONFIG, "current speed average = {0}", previousSpeedAverage);
            int previousNbAccess = (int) record.getNbAccess();
            float newSpeedAverage = (previousSpeedAverage + duration) / (previousNbAccess + 1);
            LOGGER.log(Level.CONFIG, "new speed average = {0}", newSpeedAverage);
            record.setAverage(newSpeedAverage);
            record.setNbAccess(previousNbAccess + 1);
        } else {
            LOGGER.warning("Unable to add the measurement : Unknown feature");
        }
        LOGGER.exiting(getClass().getName(), "addMEasurement");
    }

    /**
     * Checks if the features is registered.
     *
     * @param name method
     * @param path Path URI
     * @return True when the feature is registered
     */
    public boolean isRegistered(final Method name, final String path) {
        String id = name.getName() + path;
        boolean isRegistered = this.applications.containsKey(id);
        LOGGER.log(Level.FINER, "{0} {1} is registered : {2}", new Object[]{name, path, isRegistered});       
        return isRegistered;
    }

    /**
     * Returns the average speed of the measurement.
     *
     * @param name method name
     * @param path path URI
     * @return the average speed
     */
    public float getCurrentAverage(final Method name, final String path) {        
        String id = name.getName() + path;
        if (isRegistered(name, path)) {
            float average = this.applications.get(id).getAverage();
            LOGGER.finer(String.format("getCurrentAverage for %s %s : %s", name, path, average));
            return average;
        } else {
            IllegalArgumentException ex = new IllegalArgumentException(id + " is not registered");
            LOGGER.throwing(this.getClass().getName(), "getCurrentAverage", ex);
            throw ex;
        }
    }

    /**
     * Returns the description.
     *
     * @param name method name
     * @param path path URI
     * @return the description
     */
    public String getDescription(final Method name, final String path) {        
        String id = name.getName() + path;
        String description = this.applications.get(id).getDescription();
        LOGGER.log(Level.FINER, "getDescription : {0}", description);
        return description;
    }

}
