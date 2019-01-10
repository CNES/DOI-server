/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
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

import fr.cnes.doi.plugin.AbstractDoiDBPluginHelper;
import fr.cnes.doi.security.RoleAuthorizer;
import fr.cnes.doi.settings.DoiSettings;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default implementation of the project suffix database.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DefaultDoiImpl extends AbstractDoiDBPluginHelper {

    /**
     * Default file if the path is not defined in the configuration file
     */
    private static final String DEFAULT_CACHE_FILE = "data" + File.separator + "doi.conf";

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DefaultDoiImpl.class.getName());

    private static final String DESCRIPTION = "Provides a DOI database";
    private static final String VERSION = "1.0.0";
    private static final String OWNER = "CNES";
    private static final String AUTHOR = "Jean-Christophe Malapert";
    private static final String LICENSE = "LGPLV3";
    private final String NAME = this.getClass().getName();

    /**
     * Configuration of the projects identifiers
     */
    private String dbConf;

    /**
     * Mapping between the DOI and the landing page
     */
    private final Map<String, URI> doiLandingPageMap = new ConcurrentHashMap<>();
    /**
     * Mapping between the landing page and the DOI
     */
    private final Map<URI, String> landingPageDoiMap = new ConcurrentHashMap<>();

    /**
     * Default constructor of the project suffix database
     */
    public DefaultDoiImpl() {
        super();
    }

    /**
     * Init the configuration with the configuration file. If the given file does not exist a new
     * file will be created. The file contains the mapping between the project name and the
     * identifiers
     *
     * @param configuration The file that contains the database
     */
    @Override
    public void init(Object configuration) {
        if (configuration == null) {
            this.dbConf = DoiSettings.getInstance().getPathApp() + File.separatorChar + DEFAULT_CACHE_FILE;
        } else {
            this.dbConf = String.valueOf(configuration);
        }
        File projConfFile = new File(dbConf);
        try {
            // If the file exists, load it
            if (projConfFile.exists()) {
                loadDBConf(projConfFile);
            } else {
                createDbConf(projConfFile);
            }
            this.addObserver(RoleAuthorizer.getInstance());
        } catch (IOException e) {
            LOG.fatal("Cannot access the cache file for the mapping lading page/DOI " + dbConf, e);
        } catch (URISyntaxException ex) {
            LOG.fatal(ex);
        }

    }

    /**
     * Loads the database that contains the encoded project name.
     *
     * @param dbConfFile File that contains the project configuration
     * @throws IOException Exception when trying to load the file
     * @throws URISyntaxException Exception when the syntax of the landing page URL is not correct
     */
    private void loadDBConf(File dbConfFile) throws IOException, URISyntaxException {
        LOG.debug("Cache file exists : {}", dbConfFile.getAbsolutePath());

        List<String> lines = Files.readAllLines(dbConfFile.toPath());
        // Si le fichier contient autre chose que la ligne d'entete
        if (lines.size() > 1) {
            boolean firstLine = true;
            // lit chaque ligne et ajoute les infos dans la map
            for (String line : lines) {
                if (firstLine) {
                    firstLine = false;
                } else {
                    String[] split = line.split(";");
                    if (split.length != 2) {
                        LOG.debug("The line {} is not formatted in the expected way", line);
                    } else {
                        URI landingPage = new URI(split[0]);
                        String doi = split[1];
                        this.doiLandingPageMap.put(doi, landingPage);
                        this.landingPageDoiMap.put(landingPage, doi);
                    }
                }
            }
        }
    }

    /**
     * Creates the project configuration with the header Project Name;Id
     *
     * @param dbConfFile File to create
     * @throws IOException Exception when trying to create the file
     */
    private void createDbConf(File dbConfFile) throws IOException {
        // Init the config file
        LOG.debug("Cache file does not exist, create it : {}", dbConfFile.getAbsolutePath());
        File directory = new File(dbConfFile.getParent());
        Files.createDirectories(directory.toPath());
        Files.createFile(dbConfFile.toPath());
        Files.write(dbConfFile.toPath(), "LandingPage;DOI\n".getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.APPEND);
    }

    @Override
    public synchronized boolean addDoi(String doi,
            URI landingPage) {
        boolean isAdded = false;
        try {
            LOG.info("Add doi in the database {} / {}", doi, landingPage);
            String line = landingPage.toString() + ";" + doi + "\n";
            Files.write(
                    new File(this.dbConf).toPath(),
                    line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND
            );
            this.landingPageDoiMap.put(landingPage, doi);
            this.doiLandingPageMap.put(doi, landingPage);
            isAdded = true;
            setChanged();
            notifyObservers(new String[]{ADD_RECORD, doi});
        } catch (IOException e) {
            LOG.fatal("The doi " + doi + " related to the landing page " + landingPage
                    + "cannot be saved in the file", e);
        }
        return isAdded;
    }

    @Override
    public boolean isExistDOI(String doi) {
        return this.doiLandingPageMap.containsKey(doi);
    }

    @Override
    public boolean isExistLandingPage(URI landingPage) {
        return this.landingPageDoiMap.containsKey(landingPage);
    }

    @Override
    public URI getLandingPageFrom(String doi) {
        return this.doiLandingPageMap.get(doi);
    }

    @Override
    public String getDOIFrom(URI landingPage) {
        return this.landingPageDoiMap.get(landingPage);
    }

    @Override
    public Map<URI, String> getRecords() {
        return Collections.unmodifiableMap(this.landingPageDoiMap);
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
