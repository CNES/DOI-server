/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import fr.cnes.doi.utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.LocalReference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import sun.font.StrikeCache;

/**
 * DOI Settings.
 *
 * @author Jean-Christophe
 */
public class DoiSettings {

    public static final String CONFIG_PROPERTIES = "config.properties";

    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    private String secretKey = Utils.DEFAULT_SECRET_KEY;

    /**
     * private constructor
     */
    private DoiSettings() {
        init();
    }

    /**
     * Loads configuration file and set it in memory.
     */
    private void init() {
        Properties properties = loadConfigurationFile(CONFIG_PROPERTIES);
        fillConcurrentMap(properties);
    }

    /**
     * Load configuration file.
     *
     * @param path path to the configuration file.
     * @return the configuration file content
     */
    private Properties loadConfigurationFile(String path) {
        Properties properties = new Properties();
        ClientResource client = new ClientResource(LocalReference.createClapReference("class/config.properties"));
        Representation configurationFile = client.get();
        try {
            properties.load(configurationFile.getStream());
        } catch (IOException e) {           
            throw new RuntimeException("Unable to load " + path);
        } finally {
            client.release();
        }
        return properties;
    }

    /**
     * Sets the configuration as a map.
     *
     * @param properties the configuration file content
     */
    private void fillConcurrentMap(final Properties properties) {
        for (final Entry<Object, Object> entry : properties.entrySet()) {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
    }

    /**
     * Holder
     */
    private static class DoiSettingsHolder {

        /**
         * Unique Instance unique not pre-initiliaze
         */
        private final static DoiSettings INSTANCE = new DoiSettings();
    }

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static DoiSettings getInstance() {
        return DoiSettingsHolder.INSTANCE;
    }
    
    /**
     * Tests if the key has a value.
     * @param key key to test
     * @return True when the value is different or null and empty
     */
    public boolean hasValue(final String key) {
        return Utils.isNotEmpty(getString(key));
    }
    
    /**
     * Returns the secret key.
     * @return the secret key.
     */
    public String getSecretKey() {
        return this.secretKey;
    }
    
    /**
     * Sets the secret key.
     * @param secretKey the secret key.
     */
    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Returns the value of the key as string.
     *
     * @param key key to search
     * @param defaultValue Default value if the key is not found
     * @return the value of the key
     */
    public String getString(final String key, final String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the value of the key or null if no mapping for the key
     *
     * @param key key to search
     * @return the value of the key
     */
    public String getString(final String key) {
        return map.get(key);
    }

    /**
     * Returns the decoded value of the sky. An exception is raised when the key
     * is not encoded on 16bits.
     *
     * @param key key to search
     * @return the decoded vale
     */
    public String getSecret(final String key) {
        final String result;
        final String value = getString(key);
        if (Utils.isEmpty(value)) {
            result = value;
        } else {
            result = Utils.decrypt(value, getSecretKey());
        }
        return result;
    }

    /**
     * Returns the value of the key as an integer. NumberFormatException is
     * raisen when the value of the key in not compatible with an integer
     *
     * @param key key to search
     * @return the value
     */
    public int getInt(final String key) {
        return Integer.parseInt(getString(key));
    }

    /**
     * Returns the value of the key as an integer. NumberFormatException is
     * raisen when the value of the key in not compatible
     *
     * @param key key to search
     * @param defaultValue default value
     * @return the value
     */
    public int getInt(final String key, final String defaultValue) {
        return Integer.parseInt(getString(key, defaultValue));
    }

    /**
     * Returns the value of the key as a boolean. An exception is raisen when
     * the value of the key in not compatible with a boolean
     *
     * @param key key to search
     * @return the value
     */
    public boolean getBoolean(final String key) {
        if(getString(key) == null) {
            throw new IllegalArgumentException("Key not found");
        } else {
            return Boolean.getBoolean(getString(key));   
        }        
    }

    /**
     * Returns the value of the key as a long. NumberFormatException is raisen
     * when the value of the key in not compatible
     *
     * @param key key to search
     * @return the value
     */
    public Long getLong(final String key) {
        return Long.parseLong(getString(key));
    }

    /**
     * Returns the value of the key as a long. NumberFormatException is raisen
     * when the value of the key in not compatible
     *
     * @param key key to search
     * @param defaultValue default value
     * @return the value
     */
    public Long getLong(String key, String defaultValue) {
        return Long.parseLong(getString(key, defaultValue));
    }

    /**
     * Displays the configuration file.
     */
    public void displayConfigFile() {
        ClientResource client = new ClientResource(LocalReference.createClapReference("class/"+CONFIG_PROPERTIES));
        Representation configurationFile = client.get();
        try {
            copyStream(configurationFile.getStream(), System.out);
        } catch (IOException ex) {
            Logger.getLogger(DoiSettings.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            client.release();
        }
    }

    /**
     * Copy input stream to output stream.
     * @param is input stream
     * @param os output stream
     */
    private void copyStream(final InputStream is, final OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (;;) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) {
                    break;
                }
                os.write(bytes, 0, count);
            }
            is.close();
            os.flush();
            os.close();
        } catch (Exception ex) {
        } 
    }

    /**
     * Sets a custom properties file.
     * @param path Path to the properties file
     * @throws java.io.FileNotFoundException
     */
    public void setPropertiesFile(final String path) throws FileNotFoundException, IOException {
        Properties properties = new Properties();
        FileInputStream is = new FileInputStream(new File(path));
        properties.load(is);
        fillConcurrentMap(properties);
    }
    
    /**
     * Sets a custom properties file.
     * @param is Input stream
     * @throws java.io.IOException
     */
    public void setPropertiesFile(final InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(is);
        fillConcurrentMap(properties);
    }    
}
