/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.Method;

/**
 * Speed monitoring.
 * @author Jean-Christophe Malapert
 */
public class DoiMonitoring {
    
    private static final Logger LOGGER = Logger.getLogger(DoiMonitoring.class.getName());
        
    private final Map<String, List> applications = new HashMap<>();
    
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
        this.applications.put(name.getName()+path, Arrays.asList(description, 0.0f, 0));
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
            List properties = this.applications.get(name.getName()+path);
            float currentSpeedMean = (float) properties.get(1);
            LOGGER.log(Level.FINE, "current speed mean = {0}", currentSpeedMean);
            int currentRecord = (int) properties.get(2);
            float newSpeedMean = (currentSpeedMean+duration) / (currentRecord+1);
            LOGGER.log(Level.FINE, "new speed mean = {0}", newSpeedMean);            
            properties.set(1, newSpeedMean);
            properties.set(2, currentRecord+1);    
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
     * Returns the mean of the measurement.
     * @param name method name 
     * @param path path URI
     * @return the mean speed
     */
    public float getCurrentMean(final Method name, final String path) {
        String id = name.getName()+path;
        if(isRegistered(name, path)) {
            return (float) this.applications.get(id).get(1);            
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
        return (String) this.applications.get(id).get(0);    
    }
       
}
