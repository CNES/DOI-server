package fr.cnes.doi.plugin.impl.db.persistence.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

public class JDBCConnector {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(JDBCConnector.class.getName());
    //private BasicDataSource ds = new BasicDataSource(); 
    private BasicDataSource ds = new BasicDataSource();
    private DoiSettings conf = DoiSettings.getInstance();

    public JDBCConnector(String customDbConfigFile) {
        try {
            conf.setPropertiesFile(customDbConfigFile);
        } catch (IOException e) {
            logger.error("JDBCConnector: cannot retrieve the configuration file");
        };
        init();
    }

    public JDBCConnector() {
        init();
    }

    // Data source initialization
    private void init() {
        ds.setUrl(conf.getString(Consts.DB_URL));
        ds.setUsername(conf.getString(Consts.DB_USER));
        try {
            ds.setPassword(UtilsCryptography.decrypt(conf.getString(Consts.DB_PWD)));
        } catch (Exception e) {
            logger.error("Failure occored in JDBCConnector init()", e);
        }
        ds.setMaxIdle(conf.getInt(Consts.DB_MAX_IDLE_CONNECTIONS));
        ds.setMaxActive(conf.getInt(Consts.DB_MAX_ACTIVE_CONNECTIONS));
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

}
