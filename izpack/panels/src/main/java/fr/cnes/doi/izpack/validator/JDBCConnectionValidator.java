/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.izpack.validator;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.installer.DataValidator.Status;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_HOST;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_NAME;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_PASSWORD;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_PORT;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_SCHEMA;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_USER;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malapert
 */
public class JDBCConnectionValidator implements DataValidator {

    private static final Logger LOG = Logger.getLogger(JDBCConnectionValidator.class.getName());
    private static final String STR_DEFAULT_ERROR_MESSAGE = "Cannot connect to the specified database.";

    protected InstallData installData;

    //Error and Warning Messages
    protected String str_errorMsg;
    protected String str_warningMsg = "";

    @Override
    public boolean getDefaultAnswer() {
        return true;
    }

    @Override
    public String getErrorMessageId() {
        if (str_errorMsg != null) {
            return str_errorMsg;
        } else {
            return STR_DEFAULT_ERROR_MESSAGE;
        }
    }

    @Override
    public String getWarningMessageId() {
        return str_warningMsg;
    }

    @Override
    public Status validateData(InstallData installData) {
        LOG.info("Validating database connection parameters");
        this.installData = installData;

        try {
            Class.forName(getDriver());
            LOG.log(Level.INFO, "url: {0}", getUrl());
            LOG.log(Level.INFO, "user: {0}", getUser());
            LOG.log(Level.INFO, "pwd: {0}", getPassword());
            Connection conn = DriverManager.getConnection(getUrl(), getUser(), getPassword());
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(getQuery());
            rs.close();
            conn.close();            
        } catch (ClassNotFoundException | SQLException ex) {
            str_errorMsg = "Problem during loading db-driver.";
            LOG.severe(str_errorMsg);            
            return Status.ERROR; 
        }

        LOG.info("validated database connection parameters");
        return Status.OK;
    }

    public String getDriver() {
        return "org.postgresql.Driver";
    }

    public String getUrl() {
        String url = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDbName();
        if (getSchema() != null && !"".equals(getSchema())) {
            url += "?currentSchema=" + getSchema();
        }
        return url;
    }

    public String getHost() {
        return installData.getVariable(ID_DB_HOST);
    }

    public String getPort() {
        return installData.getVariable(ID_DB_PORT);
    }

    public String getDbName() {
        return installData.getVariable(ID_DB_NAME);
    }

    public String getSchema() {
        return installData.getVariable(ID_DB_SCHEMA);
    }

    public String getQuery() {
        return "SELECT 1";
    }

    public String getUser() {
        return installData.getVariable(ID_DB_USER);
    }

    public String getPassword() {
        return installData.getVariable(ID_DB_PASSWORD);
    }
}
