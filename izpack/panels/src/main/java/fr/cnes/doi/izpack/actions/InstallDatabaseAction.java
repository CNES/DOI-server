/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.izpack.actions;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.AbstractInstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.event.AbstractProgressInstallerListener;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_HOST;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_NAME;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_PASSWORD;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_PORT;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_SCHEMA;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_URL;
import static fr.cnes.doi.izpack.utils.Constants.ID_DB_USER;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malapert
 */
public class InstallDatabaseAction extends AbstractProgressInstallerListener {

    private static final Logger LOG = Logger.getLogger(InstallDatabaseAction.class.getName());

    /**
     * List of Postgresql files for user database
     */
    private final List<String> listPostgreSQLFiles;
    
    private AutomatedInstallData installData;


    public InstallDatabaseAction(AutomatedInstallData installData) {
        super(installData);
        LOG.info("db/doidb.sql");
        this.installData = installData;
        listPostgreSQLFiles = new ArrayList<>();
        listPostgreSQLFiles.add("db/doidb.sql");
       
    }
    

    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener) {
        super.afterPacks(packs, listener);
        //boolean installDBSelected = Boolean.parseBoolean(getInstalldata().getVariable(
        //        "dbInstallSelected"));
        try {
            //if (!installDBSelected) {
            //    return;
            //    boolean installDBSelected = Boolean.parseBoolean(getInstalldata().getVariable(
            //            "dbInstallSelected"));
            installDatabase();
        } catch (Exception ex) {
            Logger.getLogger(InstallDatabaseAction.class.getName()).log(Level.SEVERE, null, ex);
            throw new IzPackException(ex);
        }
    }

    public void installDatabase() throws Exception {
        LOG.info(this.getDbName());
        LOG.log(Level.INFO, "Creating tables in {0}", this.getDbName());
        //boolean installDBSelected = Boolean.parseBoolean(this.installData.getVariable(
        //        "dbInstallSelected"));

        //if (!installDBSelected) {
        //    return;
        //}        
        LOG.info(this.installData.getDefaultInstallPath());
        String installPath = this.installData.getInstallPath();
        String request;
        String ligne;
        Connection cnx = null;
        Statement stat = null;
        try {
            LOG.info(getDriver());
            Class.forName(getDriver());
            cnx = DriverManager.getConnection(getUrl(), getUser(), getPassword());
            for (Iterator<String> iterator = listPostgreSQLFiles.iterator(); iterator.hasNext();) {
                String fileName = installPath + "/" + iterator.next();
                InputStream ips = new FileInputStream(fileName);
                InputStreamReader ipsr = new InputStreamReader(ips);
                BufferedReader br = new BufferedReader(ipsr);
                StringBuilder stringBuilder = new StringBuilder();
                String ls = System.getProperty("line.separator");
                while ((ligne = br.readLine()) != null) {
                    stringBuilder.append(ligne);
                    stringBuilder.append(ls);
                }
                request = stringBuilder.toString();
                br.close();

                cnx.setAutoCommit(false);
                stat = cnx.createStatement();
                stat.execute(request);
                cnx.commit();
                stat.close();
                LOG.info("Created tables");
            }
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            LOG.severe("there was an error while installing the database");
            throw new InstallerException(
                    "Warning there was an error while installing the database :\n "
                    + ex.getLocalizedMessage(), ex);
        } finally {
            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                }
            }
            if (cnx != null) {
                try {
                    cnx.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public String getDriver() {
        return "org.postgresql.Driver";
    }

    public String getUrl() {
        return installData.getVariable(ID_DB_URL);
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

    public String getUser() {
        return installData.getVariable(ID_DB_USER);
    }

    public String getPassword() {
        return installData.getVariable(ID_DB_PASSWORD);
    }
}
