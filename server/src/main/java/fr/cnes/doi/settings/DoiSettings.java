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
package fr.cnes.doi.settings;

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.server.Starter;
import fr.cnes.doi.utils.Utils;
import fr.cnes.doi.utils.spec.Requirement;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.data.LocalReference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Singleton to load and use the defined variables in the config.properties.
 *
 * @author Jean-Christophe (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_CONFIG_010, reqName = Requirement.DOI_CONFIG_010_NAME)
public final class DoiSettings {

    /**
     * Configuration files in JAR.
     */
    public static final String CONFIG_PROPERTIES = "config.properties";

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DoiSettings.class.getName());

    /**
     * Test DOI : {@value #INIST_TEST_DOI}.
     */
    private static final String INIST_TEST_DOI = "10.5072";

    /**
     * Settings loaded in memory.
     */
    private static final ConcurrentHashMap<String, String> MAP_PROPERTIES = new ConcurrentHashMap<>();

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static DoiSettings getInstance() {
        LOG.traceEntry();
        return LOG.traceExit(DoiSettingsHolder.INSTANCE);
    }

    /**
     * Secret key to decrypt login and password.
     */
    private String secretKey = UtilsCryptography.DEFAULT_SECRET_KEY;

    /**
     * Path where the Java application inputStream located.
     */
    private String pathApp;

    /**
     * private constructor Loads the defautl configuration properties {@value #CONFIG_PROPERTIES}
     */
    private DoiSettings() {
        final Properties properties = loadConfigurationFile();
        init(properties, Level.OFF);
    }

    /**
     * Loads configuration file and set it in memory.
     *
     * @param properties Configuration file
     * @param level LOG level
     */
    private void init(final Properties properties, Level level) {
        if (Level.OFF.equals(level)) {
            level = null; // this fixes a bug in log4j2
        }
        LOG.traceEntry("Parameter\n\tproperties:{}\n\tlevel:{}", properties, level);
        LOG.log(level, "----- DOI parameters ----");
        fillConcurrentMap(properties, level);
        computePathOfTheApplication();
        PluginFactory.init(DoiSettings.MAP_PROPERTIES);
        final Enumeration<String> keys = DoiSettings.MAP_PROPERTIES.keys();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();
            LOG.log(
                    level, "{} = {}",
                    key,
                    isPassword(String.valueOf(key))
                    ? Utils.transformPasswordToStars(String.valueOf(DoiSettings.MAP_PROPERTIES.get(
                            key)))
                    : DoiSettings.MAP_PROPERTIES.get(key)
            );
        }
        LOG.log(level, "DOI settings have been loaded");
        LOG.log(level, "-------------------------");
        LOG.log(level, properties.getProperty(Consts.NAME) + " loaded");
        LOG.traceExit();
    }

    /**
     * Computes path of the application.
     */
    private void computePathOfTheApplication() {
        LOG.traceEntry();
        try {
            final String path = Starter.class.getProtectionDomain().getCodeSource().getLocation().
                    getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            final int posLastSlash = decodedPath.lastIndexOf('/');
            decodedPath = decodedPath.substring(0, posLastSlash);
            this.pathApp = decodedPath;
        } catch (UnsupportedEncodingException ex) {
            throw LOG.throwing(new DoiRuntimeException(ex));
        }
        LOG.traceExit();
    }

    /**
     * Load configuration file.
     *
     * @return the configuration file content
     */
    private Properties loadConfigurationFile() {
        LOG.traceEntry();
        final Properties properties = new Properties();
        final ClientResource client = new ClientResource(LocalReference.createClapReference(
                "class/config.properties"));
        final Representation configurationFile = client.get();
        try {
            LOG.debug("Loading " + CONFIG_PROPERTIES + " by default");
            properties.load(configurationFile.getStream());
        } catch (IOException e) {
            LOG.fatal("Unable to load " + CONFIG_PROPERTIES);
            throw LOG.throwing(new DoiRuntimeException("Unable to load class/config.properties", e));
        } finally {
            client.release();
        }
        return LOG.traceExit(properties);
    }

    /**
     * Validates the configuration file.
     */
    public void validConfigurationFile() {
        LOG.traceEntry();
        final StringBuilder validation = new StringBuilder();
        final String message = "Sets ";
        if (isNotExist(DoiSettings.MAP_PROPERTIES, Consts.INIST_DOI)) {
            validation.append(message).append(Consts.INIST_DOI).append("\n");
        }
        if (isNotExist(DoiSettings.MAP_PROPERTIES, Consts.INIST_LOGIN)) {
            validation.append(message).append(Consts.INIST_LOGIN).append("\n");
        }
        if (isNotExist(DoiSettings.MAP_PROPERTIES, Consts.INIST_PWD)) {
            validation.append(message).append(Consts.INIST_PWD).append("\n");
        }
        if (isNotExist(DoiSettings.MAP_PROPERTIES, Consts.SERVER_PROXY_TYPE)) {
            validation.append(message).append(Consts.SERVER_PROXY_TYPE).append("\n");
        }
        if (isNotExist(DoiSettings.MAP_PROPERTIES, Consts.PLUGIN_PROJECT_SUFFIX)) {
            validation.append(message).append(Consts.PLUGIN_PROJECT_SUFFIX).append("\n");
        }
        if (isNotExist(DoiSettings.MAP_PROPERTIES, Consts.PLUGIN_TOKEN)) {
            validation.append(message).append(Consts.PLUGIN_TOKEN).append("\n");
        }
        if (isNotExist(DoiSettings.MAP_PROPERTIES, Consts.PLUGIN_USER_GROUP_MGT)) {
            validation.append(message).append(Consts.PLUGIN_USER_GROUP_MGT).append("\n");
        }

        if (isNotExist(DoiSettings.MAP_PROPERTIES, Consts.PLUGIN_AUTHENTICATION)) {
            validation.append(message).append(Consts.PLUGIN_AUTHENTICATION).append("\n");
        }

        validation.append(PluginFactory.getAuthenticationSystem().validate());
        validation.append(PluginFactory.getProjectSuffix().validate());
        validation.append(PluginFactory.getToken().validate());
        validation.append(PluginFactory.getUserManagement().validate());

        if (validation.length() != 0) {
            throw LOG.traceExit(new DoiRuntimeException(validation.toString()));
        }
        LOG.traceExit();
    }

    /**
     * Tests if the keyword exists in properties.
     *
     * @param properties configuration
     * @param keyword keyword to test
     * @return True when the keyword exists in configuration otherwise False
     */
    private boolean isExist(final ConcurrentHashMap<String, String> properties,
            final String keyword) {
        LOG.traceEntry("Parameters\n\tproperties : {}\n\tkeyword : {}", properties, keyword);
        return LOG.traceExit(properties.containsKey(keyword) && !properties.get(keyword).isEmpty());
    }

    /**
     * Test if the keyword does not exist in properties
     *
     * @param properties configuration
     * @param keyword keyword to test
     * @return True when the keyword does not exist in configuration otherwise False
     */
    private boolean isNotExist(final ConcurrentHashMap<String, String> properties,
            final String keyword) {
        LOG.traceEntry("Parameters\n\tproperties : {}\n\tkeyword : {}", properties, keyword);
        return LOG.traceExit(!isExist(properties, keyword));
    }

    /**
     * Sets the configuration as a MAP_PROPERTIES.
     *
     * @param properties the configuration file content
     * @param level log level
     */
    private void fillConcurrentMap(final Properties properties, final Level level) {
        LOG.traceEntry("Parameters\n\tproperties : {}\n\tlevel : {}", properties, level);
        for (final Entry<Object, Object> entry : properties.entrySet()) {
            MAP_PROPERTIES.put((String) entry.getKey(), (String) entry.getValue());
        }
        LOG.traceExit();
    }

    /**
     * Tests if the value of the key is a password
     *
     * @param key key to test
     * @return True when the value of the key is a password otherwise false
     */
    private boolean isPassword(final String key) {
        LOG.traceEntry("Parameter\n\tkey:{}", key);
        return LOG.traceExit(Consts.INIST_PWD.equals(key)
                || PluginFactory.isPassword(Consts.PLUGIN_AUTHENTICATION, key)
                || PluginFactory.isPassword(Consts.PLUGIN_PROJECT_SUFFIX, key)
                || PluginFactory.isPassword(Consts.PLUGIN_TOKEN, key)
                || PluginFactory.isPassword(Consts.PLUGIN_USER_GROUP_MGT, key)
                || Consts.SERVER_HTTPS_KEYSTORE_PASSWD.equals(key)
                || Consts.SERVER_HTTPS_SECRET_KEY.equals(key) || Consts.SERVER_PROXY_PWD.equals(key)
                || Consts.SMTP_AUTH_PWD.equals(key) || Consts.TOKEN_KEY.equals(key));
    }

    /**
     * Tests if the key has a value.
     *
     * @param key key to test
     * @return True when the value inputStream different or null and empty
     */
    public boolean hasValue(final String key) {
        LOG.traceEntry("Parameter\n\tkey : {}", key);
        return LOG.traceExit(Utils.isNotEmpty(getString(key)));
    }

    /**
     * Returns the secret key.
     *
     * @return the secret key.
     */
    public String getSecretKey() {
        LOG.traceEntry();
        return LOG.traceExit(this.secretKey);
    }

    /**
     * Sets the secret key.
     *
     * @param secretKey the secret key.
     */
    public void setSecretKey(final String secretKey) {
        LOG.traceEntry("Parameter\n\t secretKey : {}", secretKey);
        this.secretKey = secretKey;
        LOG.traceExit();
    }

    /**
     * Returns the value of the key as string.
     *
     * @param key key to search
     * @param defaultValue Default value if the key inputStream not found
     * @return the value of the key
     */
    public String getString(final String key, final String defaultValue) {
        LOG.traceEntry("Parameters\n\tkey : {}\n\tdefaultValue : {}", key, defaultValue);
        return LOG.traceExit(MAP_PROPERTIES.getOrDefault(key, defaultValue));
    }

    /**
     * Returns the value of the key or null if no mapping for the key. A special processing
     * inputStream done for the key {@value fr.cnes.doi.settings.Consts#INIST_DOI}. When this key
     * inputStream called the value changes in respect of
     * {@value fr.cnes.doi.settings.Consts#CONTEXT_MODE}. When
     * {@value fr.cnes.doi.settings.Consts#CONTEXT_MODE} inputStream set to PRE_PROD,
     * {@value fr.cnes.doi.settings.Consts#INIST_DOI} inputStream set to {@value #INIST_TEST_DOI}.
     *
     * @param key key to search
     * @return the value of the key orl null if does not exist
     */
    public String getString(final String key) {
        LOG.traceEntry("Parameter\n\tkey : {}", key);
        final String value;
        if (Consts.INIST_DOI.equals(key)) {
            final String context = this.getString(Consts.CONTEXT_MODE, "DEV");
            value = "PRE_PROD".equals(context) ? INIST_TEST_DOI : this.getString(Consts.INIST_DOI,
                    null);
        } else {
            value = this.getString(key, null);
        }
        return LOG.traceExit(value);
    }

    /**
     * Returns the decoded value of the sky. An exception inputStream raised when the key
     * inputStream not encoded on 16bits.
     *
     * @param key key to search
     * @return the decoded vale
     */
    public String getSecret(final String key) {
        LOG.traceEntry("Parameter\n\tkey : {}", key);
        final String result;
        final String value = getString(key, "");
        if (Utils.isEmpty(value)) {
            result = value;
        } else {
            result = UtilsCryptography.decrypt(value, getSecretKey());
        }
        return LOG.traceExit(result);
    }

    /**
     * Returns the value of the key as an integer. NumberFormatException inputStream raisen when the
     * value of the key in not compatible with an integer.
     *
     * @param key key to search
     * @return the value
     * @exception NumberFormatException if the string does not contain a parsable integer.
     */
    public int getInt(final String key) {
        LOG.traceEntry("Parameter\n\tkey : {}", key);
        return LOG.traceExit(Integer.parseInt(getString(key)));
    }

    /**
     * Returns the value of the key as an integer. NumberFormatException inputStream raisen when the
     * value of the key in not compatible
     *
     * @param key key to search
     * @param defaultValue default value
     * @return the value
     * @exception NumberFormatException if the string does not contain a parsable integer.
     */
    public int getInt(final String key, final String defaultValue) {
        LOG.traceEntry("Parameter\n\tkey : {}\n\tdefaultValue", key, defaultValue);
        return LOG.traceExit(Integer.parseInt(getString(key, defaultValue)));
    }

    /**
     * Returns the value of the key as a boolean. An exception inputStream raisen when the value of
     * the key in not compatible with a boolean
     *
     * @param key key to search
     * @return the value
     * @exception IllegalArgumentException - if key not found
     */
    public boolean getBoolean(final String key) {
        LOG.traceEntry("Parameter\n\tkey : {}", key);
        if (getString(key) == null) {
            throw LOG.throwing(Level.TRACE, new IllegalArgumentException("Key not found : " + key));
        } else {
            return LOG.traceExit(Boolean.parseBoolean(getString(key)));
        }
    }

    /**
     * Returns the value of the key as a long. NumberFormatException inputStream raisen when the
     * value of the key in not compatible
     *
     * @param key key to search
     * @return the value
     * @exception NumberFormatException - if the string does not contain a parsable long
     */
    public Long getLong(final String key) {
        LOG.traceEntry("Parameter\n\tkey : {}", key);
        return LOG.traceExit(Long.parseLong(getString(key)));
    }

    /**
     * Returns the value of the key as a long. NumberFormatException inputStream raisen when the
     * value of the key in not compatible
     *
     * @param key key to search
     * @param defaultValue default value
     * @return the value
     * @exception NumberFormatException - if the string does not contain a parsable long
     */
    public Long getLong(final String key, final String defaultValue) {
        LOG.traceEntry("Parameter\n\tkey : {}\n\tdefaultValue", key, defaultValue);
        return LOG.traceExit(Long.parseLong(getString(key, defaultValue)));
    }

    /**
     * Displays the configuration file.
     */
    public void displayConfigFile() {
        LOG.traceEntry();
        final ClientResource client = new ClientResource(LocalReference.createClapReference(
                "class/" + CONFIG_PROPERTIES));
        final Representation configurationFile = client.get();
        try {
            copyStream(configurationFile.getStream(), System.out);
        } catch (IOException ex) {
            LOG.fatal("Cannot display the configuration file located to class/" + CONFIG_PROPERTIES,
                    ex);
        } finally {
            client.release();
        }
        LOG.traceExit();
    }

    /**
     * Sets a custom properties file.
     *
     * @param path Path to the properties file
     * @throws IOException - if an error occurred when reading from the input stream.
     */
    public void setPropertiesFile(final String path) throws IOException {
        LOG.traceEntry("Parameter\n\tpath : {}", path);
        try (InputStream inputStream = new FileInputStream(new File(path))) {
            setPropertiesFile(inputStream);
        }
        LOG.traceExit();
    }

    /**
     * Sets a custom properties file.
     *
     * @param inputStream Input stream
     * @throws java.io.IOException - if an error occurred when reading from the input stream.
     */
    public void setPropertiesFile(final InputStream inputStream) throws IOException {
        LOG.traceEntry("With an inputstream");
        final Properties properties = new Properties();
        properties.load(inputStream);
        init(properties, Level.INFO);
        // the following singletons depend on DoiSettings.
        // So if we init DoiSettings, we need to init the following
        // singletons
        ProxySettings.getInstance().init();
        EmailSettings.getInstance().init();
        LOG.traceExit();
    }

    /**
     * Returns the path of the application on the file system.
     *
     * @return the path of the application on the file system
     */
    public String getPathApp() {
        LOG.traceEntry();
        return LOG.traceExit(this.pathApp);
    }

    /**
     * Copy input stream to output stream.
     *
     * @param inputStream input stream
     * @param outputStream output stream
     */
    private void copyStream(final InputStream inputStream, final OutputStream outputStream) {
        LOG.traceEntry("With an input and output stream");
        final int bufferSize = 1024;
        try {
            final byte[] bytes = new byte[bufferSize];
            for (;;) {
                final int count = inputStream.read(bytes, 0, bufferSize);
                if (count == -1) {
                    break;
                }
                outputStream.write(bytes, 0, count);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            LOG.fatal("error when displaying the configuraiton file on the standard output", ex);
        }
        LOG.traceExit();
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
}
