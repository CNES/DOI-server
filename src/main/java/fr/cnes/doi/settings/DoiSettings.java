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
import fr.cnes.doi.utils.spec.Requirement;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Singleton to load and use the defined variables in the config.properties.
 *
 * @author Jean-Christophe (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_CONFIG_010,
        reqName = Requirement.DOI_CONFIG_010_NAME
)
public final class DoiSettings {
    
    /**
     * Class name.
     */
    private static final String CLASS_NAME = DoiSettings.class.getName();

    /**
     * Configuration files in JAR.
     */
    public static final String CONFIG_PROPERTIES = "config.properties";

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DoiSettings.class.getName());
    
    /**
     * Test DOI.
     */
    private static final String INIST_TEST_DOI = "10.5072";

    /**
     * Settings loaded in memory.
     */
    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    /**
     * Secret key to decrypt login and password.
     */
    private String secretKey = UtilsCryptography.DEFAULT_SECRET_KEY;

    /**
     * Path where the Java application inputStream located.
     */
    private String pathApp;

    /**
     * private constructor
     */
    private DoiSettings() {
        final Properties properties = loadConfigurationFile(CONFIG_PROPERTIES);
        init(properties);
    }

    /**
     * Loads configuration file and set it in memory.
     * @param properties Configuration file
     */
    private void init(final Properties properties) {
        LOGGER.entering(CLASS_NAME, "init");        
        fillConcurrentMap(properties);
        computePathOfTheApplication();
        PluginFactory.init(this.map);
        LOGGER.exiting(CLASS_NAME, "init");        
    }

    private void computePathOfTheApplication() {
        LOGGER.entering(CLASS_NAME, "computePathOfTheApplication");
        try {
            final String path = Starter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            final int posLastSlash = decodedPath.lastIndexOf("/");
            decodedPath = decodedPath.substring(0, posLastSlash);
            this.pathApp = decodedPath;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DoiSettings.class.getName()).log(Level.SEVERE, null, ex);
            final DoiRuntimeException doiEx = new DoiRuntimeException(ex);
            LOGGER.throwing(CLASS_NAME, "computePathOfTheApplication", doiEx);
            throw doiEx;
        }
        LOGGER.exiting(CLASS_NAME, "computePathOfTheApplication");
    }

    /**
     * Load configuration file.
     *
     * @param path path to the configuration file.
     * @return the configuration file content
     */
    private Properties loadConfigurationFile(final String path) {
        LOGGER.entering(CLASS_NAME, "loadConfigurationFile", path);
        final Properties properties = new Properties();
        final ClientResource client = new ClientResource(LocalReference.createClapReference("class/config.properties"));
        final Representation configurationFile = client.get();
        try {
            properties.load(configurationFile.getStream());
        } catch (IOException e) {
            final DoiRuntimeException doiEx = new DoiRuntimeException("Unable to load " + path, e);
            LOGGER.throwing(CLASS_NAME, "loadConfigurationFile", doiEx);
            throw doiEx;
        } finally {
            client.release();
        }
        LOGGER.exiting(CLASS_NAME, "loadConfigurationFile", properties);
        return properties;
    }
    
    /**
     * Validates the configuration file.
     */
    public void validConfigurationFile() {
        StringBuilder validation = new StringBuilder();
        final String message = "Sets ";
        if(isNotExist(this.map, Consts.INIST_DOI)) {
            validation = validation.append(message).append(Consts.INIST_DOI).append("\n");
        }
        if(isNotExist(this.map, Consts.INIST_LOGIN)) {
            validation = validation.append(message).append(Consts.INIST_LOGIN).append("\n");
        }
        if(isNotExist(this.map, Consts.INIST_PWD)) {
            validation = validation.append(message).append(Consts.INIST_PWD).append("\n");
        }    
        if(isNotExist(this.map, Consts.SERVER_PROXY_USED)) {
            validation = validation.append(message).append(Consts.SERVER_PROXY_USED).append("\n");
        }
        if(isNotExist(this.map, Consts.PLUGIN_PROJECT_SUFFIX)){
            validation = validation.append(message).append(Consts.PLUGIN_PROJECT_SUFFIX).append("\n");
        }
        if(isNotExist(this.map, Consts.PLUGIN_TOKEN)){
            validation = validation.append(message).append(Consts.PLUGIN_TOKEN).append("\n");
        } 
        if(isNotExist(this.map, Consts.PLUGIN_USER_GROUP_MGT)){
            validation = validation.append(message).append(Consts.PLUGIN_USER_GROUP_MGT).append("\n");
        }         
        if(validation.length()!=0) {
            throw new DoiRuntimeException(validation.toString());
        }
    }
    
    private boolean isExist(final  ConcurrentHashMap<String,String> properties, final String keyword) {
        return properties.containsKey(keyword) && !properties.get(keyword).isEmpty();
    }
    
    private boolean isNotExist(final  ConcurrentHashMap<String,String> properties, final String keyword) {
        return !isExist(properties, keyword);
    }

    /**
     * Sets the configuration as a map.
     *
     * @param properties the configuration file content
     */
    private void fillConcurrentMap(final Properties properties) {
        LOGGER.entering(CLASS_NAME, "fillConcurrentMap", properties);
        for (final Entry<Object, Object> entry : properties.entrySet()) {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        LOGGER.exiting(CLASS_NAME, "fillConcurrentMap");
    }

    /**
     * Holder
     */
    private static class DoiSettingsHolder {

        /**
         * Unique Instance unique not pre-initiliaze
         */
        private static final DoiSettings INSTANCE = new DoiSettings();
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
     * @return True when the value inputStream different or null and empty
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
     * @param defaultValue Default value if the key inputStream not found
     * @return the value of the key
     */
    public String getString(final String key, final String defaultValue) {
        LOGGER.log(Level.CONFIG, "GetString({0},{1}) = {2}", new Object[]{key, defaultValue, map.getOrDefault(key, defaultValue)});
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the value of the key or null if no mapping for the key.
     * A special processing inputStream done for the key {@value fr.cnes.doi.settings.Consts#INIST_DOI}. 
 When this key inputStream called the value changes in respect of {@value fr.cnes.doi.settings.Consts#CONTEXT_MODE}.
     * When {@value fr.cnes.doi.settings.Consts#CONTEXT_MODE} inputStream set to PRE_PROD, {@value fr.cnes.doi.settings.Consts#INIST_DOI}
 inputStream set to {@value #INIST_TEST_DOI}.
     *
     * @param key key to search
     * @return the value of the key
     */
    public String getString(final String key) {
        final String value;
        if(Consts.INIST_DOI.equals(key)) {
            final String context = this.getString(Consts.CONTEXT_MODE, "DEV");
            value = "PRE_PROD".equals(context) ? INIST_TEST_DOI : this.getString(Consts.INIST_DOI, null);
        } else {
            value = this.getString(key, null);
        }
        return value;
    }

    /**
     * Returns the decoded value of the sky. An exception inputStream raised when the key
 inputStream not encoded on 16bits.
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
     * Returns the value of the key as an integer. NumberFormatException inputStream
 raisen when the value of the key in not compatible with an integer
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
     * Returns the value of the key as an integer. NumberFormatException inputStream
 raisen when the value of the key in not compatible
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
     * Returns the value of the key as a boolean. An exception inputStream raisen when
 the value of the key in not compatible with a boolean
     *
     * @param key key to search
     * @return the value
     * @exception IllegalArgumentException - if key not found
     */
    public boolean getBoolean(final String key) {
        if (getString(key) == null) {
            final IllegalArgumentException exception = new IllegalArgumentException("Key not found : " + key);
            LOGGER.throwing(CLASS_NAME, "getBoolean", exception);
            throw exception;
        } else {
            return Boolean.parseBoolean(getString(key));
        }
    }

    /**
     * Returns the value of the key as a long. NumberFormatException inputStream raisen
 when the value of the key in not compatible
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
     * Returns the value of the key as a long. NumberFormatException inputStream raisen
 when the value of the key in not compatible
     *
     * @param key key to search
     * @param defaultValue default value
     * @return the value
     * @exception NumberFormatException - if the string does not contain a
     * parsable long
     */
    public Long getLong(final String key, final String defaultValue) {
        return Long.parseLong(getString(key, defaultValue));
    }

    /**
     * Displays the configuration file.
     */
    public void displayConfigFile() {
        LOGGER.entering(CLASS_NAME, "displayConfigFile");
        final ClientResource client = new ClientResource(LocalReference.createClapReference("class/" + CONFIG_PROPERTIES));
        final Representation configurationFile = client.get();
        try {
            copyStream(configurationFile.getStream(), System.out);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot display the configuration file located to class/" + CONFIG_PROPERTIES, ex);
        } finally {
            client.release();
        }
        LOGGER.exiting(CLASS_NAME, "displayConfigFile");
    }

    /**
     * Copy input stream to output stream.
     *
     * @param inputStream input stream
     * @param outputStream output stream
     */
    private void copyStream(final InputStream inputStream, final OutputStream outputStream) {
        LOGGER.entering(CLASS_NAME, "copyStream");
        final int buffer_size = 1024;
        try {
            final byte[] bytes = new byte[buffer_size];
            for (;;) {
                final int count = inputStream.read(bytes, 0, buffer_size);
                if (count == -1) {
                    break;
                }
                outputStream.write(bytes, 0, count);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "error when displaying the configuraiton file on the standard output", ex);
        }
        LOGGER.exiting(CLASS_NAME, "copyStream");
    }

    /**
     * Sets a custom properties file.
     *
     * @param path Path to the properties file
     * @throws IOException - if an error occurred when reading from the input stream.
     */
    public void setPropertiesFile(final String path) throws IOException {
        final InputStream inputStream = new FileInputStream(new File(path));
        setPropertiesFile(inputStream);
    }

    /**
     * Sets a custom properties file.
     *
     * @param inputStream Input stream 
     * @throws java.io.IOException - if an error occurred when reading from the input stream.
     */
    public void setPropertiesFile(final InputStream inputStream) throws IOException {
        LOGGER.entering(CLASS_NAME, "setPropertiesFile");                
        final Properties properties = new Properties();
        properties.load(inputStream);
        init(properties);
        inputStream.close();
        LOGGER.exiting(CLASS_NAME, "setPropertiesFile");                        
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
