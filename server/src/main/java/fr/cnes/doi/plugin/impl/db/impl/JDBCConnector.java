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
package fr.cnes.doi.plugin.impl.db.impl;

import fr.cnes.doi.exception.DoiRuntimeException;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_MAX_ACTIVE_CONNECTIONS;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_MAX_IDLE_CONNECTIONS;
import static fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl.DB_MIN_IDLE_CONNECTIONS;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.Utils;
import java.util.Map;
import org.apache.logging.log4j.Level;

/**
 * Class that handles the connection to the DOI database. As below, the structure of the DOI
 * database.
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
 *
 * @author Jean-Christophe Malapert (jean-Christophe.malapert@cnes.fr)
 */
public class JDBCConnector {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(JDBCConnector.class.getName());

    /**
     * Default min IDL connection {@value #DEFAULT_MIN_IDLE_CONNECTION}.
     */
    public static final int DEFAULT_MIN_IDLE_CONNECTION = 10;
    /**
     * Default max IDL connection {@value #DEFAULT_MAX_IDLE_CONNECTION}.
     */
    public static final int DEFAULT_MAX_IDLE_CONNECTION = 50;
    /**
     * Default max active connection {@value #DEFAULT_MAX_ACTIVE_CONNECTION}.
     */
    public static final int DEFAULT_MAX_ACTIVE_CONNECTION = 50;
    /**
     * The data source.
     */
    private final BasicDataSource ds = new BasicDataSource();

    /**
     * Creates the JDBC connector based on a specific configuration file.
     *
     * @param dbUrl database URL
     * @param dbUser database user
     * @param dbPwd database password
     * @param options database options
     */
    public JDBCConnector(final String dbUrl, final String dbUser, final String dbPwd,
            final Map<String, Integer> options) {
        LOGGER.traceEntry("Parameter\n\tdbUrl : {}\n\tdbPwd : {}\n\toptions : {}", dbUrl, dbPwd,
                options);
        init(dbUrl, dbUser, dbPwd, options);
        LOGGER.traceExit();
    }

    /**
     * Data source initialization.
     *
     * @param dbUrl database URL
     * @param dbUser database user
     * @param dbPwd database password
     * @param options database options
     * @throws DoiRuntimeException Cannot decrypt the database pwd from the configuration file
     */
    private void init(final String dbUrl, final String dbUser, final String dbPwd,
            final Map<String, Integer> options) {
        LOGGER.traceEntry();
        final int minIdleConnection = options.getOrDefault(
                DB_MIN_IDLE_CONNECTIONS, DEFAULT_MIN_IDLE_CONNECTION
        );
        final int maxIdleConnection = options.getOrDefault(
                DB_MAX_IDLE_CONNECTIONS, DEFAULT_MAX_IDLE_CONNECTION
        );
        final int maxActiveConnection = options.getOrDefault(
                DB_MAX_ACTIVE_CONNECTIONS, DEFAULT_MAX_ACTIVE_CONNECTION
        );

        LOGGER.info("[CONF] Datasource database URL : {}", dbUrl);
        LOGGER.info("[CONF] Datasource database user : {}", dbUser);
        LOGGER.info("[CONF] Datasource database password : {}", Utils.
                transformPasswordToStars(dbPwd));
        LOGGER.info("[CONF] Datasource min IDLE connection : {}", minIdleConnection);
        LOGGER.info("[CONF] Datasource max IDLE connection : {}", maxIdleConnection);
        LOGGER.info("[CONF] Datasource max active connection : {}", maxActiveConnection);

        ds.setUrl(dbUrl);
        ds.setUsername(dbUser);
        try {
            final String decryptedPasswd = DoiSettings.getInstance().getSecretValue(dbPwd);
            ds.setPassword(decryptedPasswd);
        } catch (Exception e) {
            throw LOGGER.throwing(Level.ERROR, new DoiRuntimeException(
                    "Cannot decrypt the database "
                    + "pwd " + dbPwd + " from the configuration file"));
        }
        ds.setMinIdle(minIdleConnection);
        ds.setMaxIdle(maxIdleConnection);
        ds.setMaxActive(maxActiveConnection);
        LOGGER.traceExit();
    }

    /**
     * Creates (if necessary) and return a connection to the database.
     *
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
