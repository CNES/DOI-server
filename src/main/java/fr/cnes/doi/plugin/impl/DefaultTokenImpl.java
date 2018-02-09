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
package fr.cnes.doi.plugin.impl;

import fr.cnes.doi.plugin.AbstractTokenDBPluginHelper;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.settings.DoiSettings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Default implementation of the token database.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DefaultTokenImpl extends AbstractTokenDBPluginHelper {

    private final String NAME = this.getClass().getName();
    private static final String DESCRIPTION = "Provides a pre-defined list of users and groups";
    private static final String VERSION = "1.0.0";
    private static final String OWNER = "CNES";
    private static final String AUTHOR = "Jean-Christophe Malapert";
    private static final String LICENSE = "LGPLV3";
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DefaultTokenImpl.class.getName());      

    /**
     * Default file if the path is not defined in the configuration file
     */
    private static final String DEFAULT_CACHE_FILE = "data"+File.separator+"token.conf";

    private String tokenConf;

    private Map<String, Map<String, Object>> db = new ConcurrentHashMap<>();

    public DefaultTokenImpl() {
        super();
    }

    @Override
    public void init(Object configuration) {        
        this.tokenConf = (configuration == null) 
                ? DoiSettings.getInstance().getPathApp()+File.separatorChar+DEFAULT_CACHE_FILE
                : String.valueOf(configuration);
        File tokenConfFile = new File(tokenConf);
        try {
            // If the file exists, load it
            if (tokenConfFile.exists()) {
                loadProjectConf(tokenConfFile);
            } else {
                createProjectConf(tokenConfFile);
            }

        } catch (IOException e) {
            LOG.fatal("Cannot access the cache file for retrieving Token ", e);
        }
    }

    /**
     * Loads the database that contains the encoded project name.
     *
     * @param projConfFile File that contains the project configuration
     * @throws IOException Exception when trying to load the file
     */
    private void loadProjectConf(File projConfFile) throws IOException {
        LOG.debug("Cache file exists : "+ projConfFile.getAbsolutePath());

        List<String> lines = Files.readAllLines(projConfFile.toPath());
        // Si le fichier contient autre chose que la ligne d'entete
        if (lines.size() > 1) {
            boolean firstLine = true;
            // lit chaque ligne et ajoute les infos dans la map
            for (String line : lines) {
                if (firstLine) {
                    firstLine = false;
                } else {
                    String[] split = line.split(";");
                    if (split.length != 3) {
                        LOG.fatal(String.format("The line %s is not formatted in the expected way", line));
                    } else {
                        this.db.put(split[0], new ConcurrentHashMap<String, Object>() {
                            private static final long serialVersionUID = 3109256773218160485L;
                            {
                                put("projectSuffix", split[1]);
                                put("expirationDate", split[2]);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Creates the project configuration with the header Project Name;Id
     *
     * @param tokenConfFile File to create
     * @throws IOException Exception when trying to create the file
     */
    private void createProjectConf(File tokenConfFile) throws IOException {
        // Init the config file
        LOG.info("Cache file does not exist, create it : "+ tokenConfFile.getAbsolutePath());
        File directory = new File(tokenConfFile.getParent());
        Files.createDirectories(directory.toPath());
        Files.createFile(tokenConfFile.toPath());
        Files.write(tokenConfFile.toPath(), "#Token;Project suffix;Expiration date\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    }

    @Override
    public boolean addToken(String jwt) {

        boolean isAdded = false;
        try {
            Jws<Claims> jws = TokenSecurity.getInstance().getTokenInformation(jwt);

            String projectSuffix = String.valueOf(jws.getBody().get(TokenSecurity.PROJECT_ID, Integer.class));
            String expirationDate = jws.getBody().getExpiration().toString();

            // should be fine, the JWT representation does not contain ;
            String line = jwt + ";" + projectSuffix + ";" + expirationDate + "\n";
            LOG.info("token inserted : "+ line);
            Files.write(new File(this.tokenConf).toPath(), line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            this.db.put(jwt, new ConcurrentHashMap<String, Object>() {
                private static final long serialVersionUID = 3109256773218160485L;

                {
                    put("projectSuffix", projectSuffix);
                    put("expirationDate", expirationDate);
                }
            });
            isAdded = true;
        } catch (IOException | RuntimeException e) {
            LOG.fatal("The token " + jwt + "cannot be saved in the file", e);
        }
        return isAdded;
    }

    @Override
    public void deleteToken(String jwt) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isExist(String jwt) {
        return this.db.containsKey(jwt);
    }

    @Override
    public boolean isExpirated(String jwt) {
        boolean isExpirated = true;
        String dateStr = (String) this.db.get(jwt).get("expirationDate");
        try {
            DateFormat dateFormat = new SimpleDateFormat(TokenSecurity.DATE_FORMAT);
            Date expDate = dateFormat.parse(dateStr);
            isExpirated = new Date().after(expDate);
        } catch (ParseException ex) {
            LOG.fatal(ex);
        }
        return isExpirated;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getOwner() {
        return OWNER;
    }

    @Override
    public String getLicense() {
        return LICENSE;
    }

}
