/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import fr.cnes.doi.client.ClientProxyTest;
import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.settings.DoiSettings;
import java.util.Properties;
import java.util.logging.LogManager;
import org.restlet.engine.Engine;

/**
 * Class to read the settings from the crypted config file and to enable the
 * proxy if the system property has been sets
 *
 * @author Claire
 *
 */
public class InitSettingsForTest {

    /**
     * Init loggers.
     */
    private static final Logger LOGGER = Engine.getLogger(InitSettingsForTest.class);

    /**
     * Reads the settings.
     *
     */
    public static void init() {
        initLogging();
        try {
            String secretKey = System.getProperty("private.key");
            String result;
            try (InputStream inputStream = ClientProxyTest.class.getResourceAsStream("/config.properties")) {
                result = new BufferedReader(new InputStreamReader(inputStream)).lines()
                        .collect(Collectors.joining("\n"));
            }
            if (secretKey != null) {
                result = UtilsCryptography.decrypt(result, secretKey);
            } else {
                LOGGER.log(Level.WARNING, "No private.key provided, the configuration is not crypted");
            }
            // Replace the value to use the proxy by the system property
            String useProxy = System.getProperty("proxy.use");
            if (useProxy != null) {
                result = result.replace("Starter.Proxy.used = false", "Starter.Proxy.used=" + useProxy);
            } else {
                LOGGER.log(Level.INFO, "The key proxy.use is not set, default param applied");
            }
            InputStream stream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
            DoiSettings.getInstance().setPropertiesFile(stream);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during initialisation of the settings", e);
        }

    }

    private static void initLogging() {
        LogManager.getLogManager().reset();
        Properties p = new Properties();
        p.setProperty("org.eclipse.jetty.LEVEL", "OFF");
        org.eclipse.jetty.util.log.StdErrLog.setProperties(p);
        Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
        globalLogger.setLevel(java.util.logging.Level.OFF); 
        Engine.setRestletLogLevel(Level.OFF);
        Engine.setLogLevel(Level.OFF);
    }

}
