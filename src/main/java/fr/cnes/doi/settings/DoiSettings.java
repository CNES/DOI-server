/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.server.Starter;
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

import fr.cnes.doi.utils.Utils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Singleton to load and use the defined variables in the config.properties.
 *
 * @author Jean-Christophe (jean-christophe.malapert@cnes.fr)
 */
public final class DoiSettings {

    /**
     * Configuration files in JAR.
     */
    public static final String CONFIG_PROPERTIES = "config.properties";

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DoiSettings.class.getName());

    /**
     * Settings loaded in memory.
     */
    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    /**
     * Secret key to decrypt login and password.
     */
    private String secretKey = UtilsCryptography.DEFAULT_SECRET_KEY;

    /**
     * Path where the Java application is located.
     */
    private String pathApp;

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
        LOGGER.entering(this.getClass().getName(), "init");
        Properties properties = loadConfigurationFile(CONFIG_PROPERTIES);
        fillConcurrentMap(properties);
        computePathOfTheApplication();
        PluginFactory.init(this.map);
        LOGGER.info("Settings have been loaded");
        LOGGER.exiting(this.getClass().getName(), "init");        
    }

    private void computePathOfTheApplication() {
        LOGGER.entering(this.getClass().getName(), "computePathOfTheApplication");
        try {
            String path = Starter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            int posLastSlash = decodedPath.lastIndexOf("/");
            decodedPath = decodedPath.substring(0, posLastSlash);
            this.pathApp = decodedPath;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DoiSettings.class.getName()).log(Level.SEVERE, null, ex);
            DoiRuntimeException doiEx = new DoiRuntimeException(ex);
            LOGGER.throwing(this.getClass().getName(), "computePathOfTheApplication", doiEx);
            throw doiEx;
        }
        LOGGER.exiting(this.getClass().getName(), "computePathOfTheApplication");
    }

    /**
     * Load configuration file.
     *
     * @param path path to the configuration file.
     * @return the configuration file content
     */
    private Properties loadConfigurationFile(String path) {
        LOGGER.entering(this.getClass().getName(), "loadConfigurationFile", path);
        Properties properties = new Properties();
        ClientResource client = new ClientResource(LocalReference.createClapReference("class/config.properties"));
        Representation configurationFile = client.get();
        try {
            properties.load(configurationFile.getStream());
        } catch (IOException e) {
            DoiRuntimeException doiEx = new DoiRuntimeException("Unable to load " + path, e);
            LOGGER.throwing(this.getClass().getName(), "loadConfigurationFile", doiEx);
            throw doiEx;
        } finally {
            client.release();
        }
        LOGGER.exiting(this.getClass().getName(), "loadConfigurationFile", properties);
        return properties;
    }

    /**
     * Sets the configuration as a map.
     *
     * @param properties the configuration file content
     */
    private void fillConcurrentMap(final Properties properties) {
        LOGGER.entering(this.getClass().getName(), "fillConcurrentMap", properties);
        for (final Entry<Object, Object> entry : properties.entrySet()) {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        LOGGER.exiting(this.getClass().getName(), "fillConcurrentMap");
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
     *
     * @param key key to test
     * @return True when the value is different or null and empty
     */
    public boolean hasValue(final String key) {
        LOGGER.log(Level.CONFIG, "hasValue({0}) = {1}", new Object[]{key, Utils.isNotEmpty(getString(key))});
        return Utils.isNotEmpty(getString(key));
    }

    /**
     * Returns the secret key.
     *
     * @return the secret key.
     */
    public String getSecretKey() {
        LOGGER.log(Level.CONFIG, "Gets the secret : {0}", secretKey);
        return this.secretKey;
    }

    /**
     * Sets the secret key.
     *
     * @param secretKey the secret key.
     */
    public void setSecretKey(final String secretKey) {
        LOGGER.log(Level.CONFIG, "Sets the secret : {0}", secretKey);
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
        LOGGER.log(Level.CONFIG, "GetString({0},{1}) = {2}", new Object[]{key, defaultValue, map.getOrDefault(key, defaultValue)});
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the value of the key or null if no mapping for the key
     *
     * @param key key to search
     * @return the value of the key
     */
    public String getString(final String key) {
        return this.getString(key, null);
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
            result = UtilsCryptography.decrypt(value, getSecretKey());
        }
        LOGGER.log(Level.CONFIG, "Decrypt({0}) from {1} : {2}", new Object[]{value, key, result});
        return result;
    }

    /**
     * Returns the value of the key as an integer. NumberFormatException is
     * raisen when the value of the key in not compatible with an integer
     *
     * @param key key to search
     * @return the value
     * @exception NumberFormatException if the string does not contain a
     * parsable integer.
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
     * @exception NumberFormatException if the string does not contain a
     * parsable integer.
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
     * @exception IllegalArgumentException - if key not found
     */
    public boolean getBoolean(final String key) {
        if (getString(key) == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Key not found : " + key);
            LOGGER.throwing(this.getClass().getName(), "getBoolean", ex);
            throw ex;
        } else {
            return Boolean.parseBoolean(getString(key));
        }
    }

    /**
     * Returns the value of the key as a long. NumberFormatException is raisen
     * when the value of the key in not compatible
     *
     * @param key key to search
     * @return the value
     * @exception NumberFormatException - if the string does not contain a
     * parsable long
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
     * @exception NumberFormatException - if the string does not contain a
     * parsable long
     */
    public Long getLong(String key, String defaultValue) {
        return Long.parseLong(getString(key, defaultValue));
    }

    /**
     * Displays the configuration file.
     */
    public void displayConfigFile() {
        LOGGER.entering(this.getClass().getName(), "displayConfigFile");
        ClientResource client = new ClientResource(LocalReference.createClapReference("class/" + CONFIG_PROPERTIES));
        Representation configurationFile = client.get();
        try {
            copyStream(configurationFile.getStream(), System.out);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot display the configuration file located to class/" + CONFIG_PROPERTIES, ex);
        } finally {
            client.release();
        }
        LOGGER.exiting(this.getClass().getName(), "displayConfigFile");
    }

    /**
     * Copy input stream to output stream.
     *
     * @param is input stream
     * @param os output stream
     */
    private void copyStream(final InputStream is, final OutputStream os) {
        LOGGER.entering(this.getClass().getName(), "copyStream");
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
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "error when displaying the configuraiton file on the standard output", ex);
        }
        LOGGER.exiting(this.getClass().getName(), "copyStream");
    }

    /**
     * Sets a custom properties file.
     *
     * @param path Path to the properties file
     * @throws java.io.FileNotFoundException
     */
    public void setPropertiesFile(final String path) throws FileNotFoundException, IOException {
        LOGGER.entering(this.getClass().getName(), "setPropertiesFile", path);        
        Properties properties = new Properties();
        try (FileInputStream is = new FileInputStream(new File(path))) {
            properties.load(is);
            fillConcurrentMap(properties);
        }
        LOGGER.exiting(this.getClass().getName(), "setPropertiesFile");                
    }

    /**
     * Sets a custom properties file.
     *
     * @param is Input stream
     * @throws java.io.IOException
     */
    public void setPropertiesFile(final InputStream is) throws IOException {
        LOGGER.entering(this.getClass().getName(), "setPropertiesFile");                
        Properties properties = new Properties();
        properties.load(is);
        fillConcurrentMap(properties);
        LOGGER.exiting(this.getClass().getName(), "setPropertiesFile");                        
    }

    /**
     * Returns the path of the application on the file system.
     *
     * @return the path of the application on the file system
     */
    public String getPathApp() {
        LOGGER.log(Level.CONFIG, "getPath : {0}", this.pathApp);
        return this.pathApp;
    }
}
