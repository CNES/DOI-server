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
package fr.cnes.doi.plugin.impl.db;

import fr.cnes.doi.plugin.impl.db.persistence.service.DatabaseSingleton;
import fr.cnes.doi.plugin.impl.db.persistence.service.DOIDbDataAccessService;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.plugin.AbstractTokenDBPluginHelper;
import fr.cnes.doi.security.TokenSecurity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * Default implementation of the token database.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DefaultTokenImpl extends AbstractTokenDBPluginHelper {

    /**
     * Plugin description.
     */
    private static final String DESCRIPTION = "Provides a pre-defined list of users and groups";
    /**
     * Plugin version.
     */
    private static final String VERSION = "1.0.0";
    /**
     * Plugin owner.
     */
    private static final String OWNER = "CNES";
    /**
     * Plugin author.
     */
    private static final String AUTHOR = "Jean-Christophe Malapert";
    /**
     * Plugin license.
     */
    private static final String LICENSE = "LGPLV3";
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DefaultTokenImpl.class.getName());

    /**
     * Name of the class.
     */
    private final String NAME = this.getClass().getName();

    /**
     * Database access.
     */
    private final DOIDbDataAccessService das = DatabaseSingleton.getInstance().getDatabaseAccess();

    /**
     * Default Constructor of the token database
     */
    public DefaultTokenImpl() {
        super();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setConfiguration(final Object configuration) {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean addToken(final String jwt) {

        boolean isAdded = false;
        try {
            das.addToken(jwt);
            LOG.info("token added : " + jwt);
            isAdded = true;
        } catch (DOIDbException e) {
            LOG.fatal("The token " + jwt + "cannot be saved in database", e);
        }
        return isAdded;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void deleteToken(final String jwt) {
        try {
            das.deleteToken(jwt);
            LOG.info("token deleted : " + jwt);
        } catch (DOIDbException e) {
            LOG.fatal("The token " + jwt + "cannot be deleted in database", e);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isExist(final String jwt) {
        boolean isTokenExist = false;
        try {
            final List<String> tokenList = das.getTokens();
            if (!tokenList.isEmpty()) {
                for (final String token : tokenList) {
                    if (token.equals(jwt)) {
                        isTokenExist = true;
                    }
                }
            }
        } catch (DOIDbException e) {
            LOG.fatal("The token " + jwt + "cannot access to token database", e);
        }
        return isTokenExist;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isExpired(final String jwt) {
        boolean isExpirated = true;
        final Jws<Claims> jws = TokenSecurity.getInstance().getTokenInformation(jwt);

        // Cannot get token information of an expired token...
        if (jws == null) {
            return isExpirated;
        }

        final String expirationDate = jws.getBody().getExpiration().toString();
        try {
            // Precise "Locale.ENGLISH" otherwise unparsable exception occur for day in week and month
            final DateFormat dateFormat = new SimpleDateFormat(TokenSecurity.DATE_FORMAT,
                    Locale.ENGLISH);
            final Date expDate = dateFormat.parse(expirationDate);
            isExpirated = new Date().after(expDate);
        } catch (ParseException ex) {
            LOG.fatal(ex);
        }
        //TODO delete an expired token?
        return isExpirated;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<String> getTokens() {
        final List<String> tokens = new ArrayList<>();
        try {
            tokens.addAll(das.getTokens());
        } catch (DOIDbException e) {
            LOG.fatal("Cannot retrieve the token list from database", e);
        }
        return tokens;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getOwner() {
        return OWNER;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getLicense() {
        return LICENSE;
    }

}
