/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.server.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Method;

/**
 * Speed monitoring (average time to answer requests).
 * @author Jean-Christophe Malapert
 */
public class DoiMonitoring {
    
	/** Logger **/
    private static final Logger LOGGER = Logger.getLogger(DoiMonitoring.class.getName());
        
    /** Hash map of records to compute average time to answer requests **/
    private final Map<String, DoiMonitoringRecord> applications = new HashMap<>();
    
    /**
     * Constructor
     */
    public DoiMonitoring() {
        
    }
    
    /**
     * Registers the features to monitor.
     * @param name Method
     * @param path path URI
     * @param description Feature's description
     */
    public void register(final Method name, final String path, final String description) {
        LOGGER.entering(getClass().getName(), "register", new Object[]{name.getName(), path, description});
        this.applications.put(name.getName()+path, new DoiMonitoringRecord(description, 0.0f, 0));
        LOGGER.exiting(getClass().getName(), "register");
    }
    
    /**
     * Add Measurement.
     * @param name method
     * @param path path URI
     * @param duration duration in ms
     */
    public void addMeasurement(final Method name, final String path, float duration) {
        LOGGER.entering(getClass().getName(), "addMeasurement", new Object[]{name.getName(), path, duration});        
        String id = name.getName()+path;
        if(this.applications.containsKey(id)) {
            DoiMonitoringRecord record = this.applications.get(name.getName()+path);
            float previousSpeedAverage = (float) record.getAverage();
            LOGGER.log(Level.FINE, "current speed average = {0}", previousSpeedAverage);
            int previousNbAccess = (int) record.getNbAccess();
            float newSpeedAverage = (previousSpeedAverage+duration) / (previousNbAccess+1);
            LOGGER.log(Level.FINE, "new speed average = {0}", newSpeedAverage);            
            record.setAverage(newSpeedAverage);
            record.setNbAccess(previousNbAccess+1);    
        } else {
            LOGGER.warning("Unable to add the measurement : Unknown feature");
        }
        LOGGER.exiting(getClass().getName(), "addMEasurement");        
    }
    
    /**
     * Checks if the features is registered.
     * @param name method
     * @param path Path URI
     * @return True when the feature is registered
     */
    public boolean isRegistered(final Method name, final String path) {
        String id = name.getName()+path;
        return this.applications.containsKey(id);
    }
    
    /**
     * Returns the average speed of the measurement.
     * @param name method name 
     * @param path path URI
     * @return the average speed
     */
    public float getCurrentAverage(final Method name, final String path) {
        String id = name.getName()+path;
        if(isRegistered(name, path)) {
            return this.applications.get(id).getAverage();            
        } else {
            throw new IllegalArgumentException(id + " is not registered");
        }
    }
    
    /**
     * Returns the description.
     * @param name method name
     * @param path path URI
     * @return the description
     */
    public String getDescription(final Method name, final String path) {
        String id = name.getName()+path;
        return this.applications.get(id).getDescription();    
    }
       
}
