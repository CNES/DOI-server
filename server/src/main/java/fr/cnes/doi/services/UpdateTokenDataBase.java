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
package fr.cnes.doi.services;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.security.TokenSecurity;
import java.util.ArrayList;

/**
 * Updates token database. The service checks if the token is expired. When it
 * is expired, the token is removed from the database.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class UpdateTokenDataBase implements Runnable {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(UpdateTokenDataBase.class.getName());

    /**
     * Token database.
     */
    private final AbstractTokenDBHelper tokenDB = TokenSecurity.getInstance().getTokenDB();

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        LOG.info("Executing task that remove expired token from database.");
        List<String> tokenList;
        try {
            tokenList = tokenDB.getTokens();
        } catch (DOIDbException ex) {
            tokenList = new ArrayList<>();
        }
        for (final String token : tokenList) {
            if (TokenSecurity.getInstance().isExpired(token)) {
                LOG.info("Token {} is expired", token);
            }
        }
    }

}
