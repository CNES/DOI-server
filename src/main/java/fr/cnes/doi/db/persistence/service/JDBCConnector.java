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
package fr.cnes.doi.db.persistence.service;

import fr.cnes.doi.exception.DoiRuntimeException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.Utils;

/**
 * Class that handles the connection to the DOI database.
 * As below, the structure of the DOI database.
 * <pre>
 * {@code
 * CREATE TABLE doi_schema.T_DOI_USERS (
 * username varchar(255) NOT NULL,
 * admin boolean NOT NULL,
 * email varchar(255),
 * PRIMARY KEY (username)
 * );
 *
 * CREATE TABLE doi_schema.T_DOI_PROJECT (
 * suffix int NOT NULL,
 * projectname varchar(1024) NOT NULL,
 * PRIMARY KEY (suffix) 
 * );
 * 
 * CREATE TABLE doi_schema.T_DOI_ASSIGNATIONS (
 * username varchar(255) NOT NULL,
 * suffix int NOT NULL,
 * PRIMARY KEY (username, suffix)
 * );
 *
 * CREATE TABLE doi_schema.T_DOI_TOKENS (
 * token varchar(255) NOT NULL,
 * PRIMARY KEY (token)
 * );
 * }
 * </pre>
 * @author Jean-Christophe Malapert (jean-Christophe.malapert@cnes.fr)
 */
public class JDBCConnector {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(JDBCConnector.class.getName());
    /**
     * The data source.
     */
    private final BasicDataSource ds = new BasicDataSource();
    /**
     * The configuration file.
     */
    private final DoiSettings conf = DoiSettings.getInstance();
    
    /**
     * Default min IDL connection {@value #DEFAULT_MIN_IDLE_CONNECTION}.
     */
    public static final String DEFAULT_MIN_IDLE_CONNECTION = "10";
    /**
     * Default max IDL connection {@value #DEFAULT_MAX_IDLE_CONNECTION}.
     */    
    public static final String DEFAULT_MAX_IDLE_CONNECTION = "50";
    /**
     * Default max active connection {@value #DEFAULT_MAX_ACTIVE_CONNECTION}.
     */    
    public static final String DEFAULT_MAX_ACTIVE_CONNECTION = "50";

    /**
     * Creates the JDBC connector based on a specific configuration file.
     * @param customDbConfigFile configuration file.
     * @throws RuntimeException Cannot retrieve the configuration file
     */
    public JDBCConnector(String customDbConfigFile) {
        LOGGER.traceEntry("Parameter\n  customDbConfigFile: {} ",customDbConfigFile);
	try {
	    conf.setPropertiesFile(customDbConfigFile);
	} catch (IOException e) {
	    LOGGER.error("Cannot retrieve the configuration file {}", customDbConfigFile);
            throw LOGGER.throwing(new RuntimeException("Cannot retrieve the configuration "
                    + "file "+customDbConfigFile));
	}
	init();
        LOGGER.traceExit();
    }

    /**
     * Creates the JDBC connector based on the default settings.
     */
    public JDBCConnector() {
        LOGGER.traceEntry();
	init();
        LOGGER.traceExit();
    }
    
    /**
     * Data source initialization.
     * @throws DoiRuntimeException Cannot decrypt the database pwd from the configuration file
     */
    private void init() {
        LOGGER.traceEntry();
        final String dbURL = conf.getString(Consts.DB_URL);
        final String dbUser = conf.getString(Consts.DB_USER);
        final String passwd = conf.getString(Consts.DB_PWD);
        final int minIdleConnection = conf.getInt(
                Consts.DB_MIN_IDLE_CONNECTIONS, DEFAULT_MIN_IDLE_CONNECTION
        );
        final int maxIdleConnection = conf.getInt(
                Consts.DB_MAX_IDLE_CONNECTIONS, DEFAULT_MAX_IDLE_CONNECTION
        );        
        final int maxActiveConnection = conf.getInt(
                Consts.DB_MAX_ACTIVE_CONNECTIONS, DEFAULT_MAX_ACTIVE_CONNECTION
        );
        
        LOGGER.debug("[CONF] Datasource database URL", dbURL);
        LOGGER.debug("[CONF] Datasource database user", dbUser);
        LOGGER.debug("[CONF] Datasource database password", Utils.transformPasswordToStars(passwd));
        LOGGER.debug("[CONF] Datasource min IDLE connection", minIdleConnection);
        LOGGER.debug("[CONF] Datasource max IDLE connection", maxIdleConnection);
        LOGGER.debug("[CONF] Datasource max active connection", maxActiveConnection);                                
        
	ds.setUrl(dbURL);
	ds.setUsername(dbUser);
	try {
            final String decryptedPasswd = UtilsCryptography.decrypt(passwd);
	    ds.setPassword(decryptedPasswd);
	} catch (Exception e) {
            LOGGER.error("Cannot decrypt the database pwd from the configuration file: {}",passwd);
	    throw LOGGER.throwing(new DoiRuntimeException("Cannot decrypt the database "
                    + "pwd "+passwd+" from the configuration file"));
	}
	ds.setMinIdle(minIdleConnection);
        ds.setMaxIdle(maxIdleConnection);
	ds.setMaxActive(maxActiveConnection);
        LOGGER.traceExit();
    }

    /**
     * Creates (if necessary) and return a connection to the database.
     * @return a database connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        LOGGER.traceEntry();
	return LOGGER.traceExit(ds.getConnection());
    }
    
    /**
     * Closes and releases all idle connections that are currently stored in the connection pool 
     * associated with this data source.
     */
    public void close() {
        LOGGER.traceEntry();
        try {
            this.ds.close();
        } catch (SQLException ex) {
            LOGGER.error("Cannot close the datasource connection", ex);
        }
        LOGGER.traceExit();
    }

}
