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
package fr.cnes.doi.plugin.impl.db;

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
import fr.cnes.doi.plugin.impl.db.persistence.impl.DOIDbDataAccessServiceImpl;
import fr.cnes.doi.plugin.impl.db.persistence.service.DOIDbDataAccessService;
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

    private static final String DESCRIPTION = "Provides a pre-defined list of users and groups";
    private static final String VERSION = "1.0.0";
    private static final String OWNER = "CNES";
    private static final String AUTHOR = "Jean-Christophe Malapert";
    private static final String LICENSE = "LGPLV3";
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DefaultTokenImpl.class.getName());

    private final String NAME = this.getClass().getName();

    private final DOIDbDataAccessService das = new DOIDbDataAccessServiceImpl();

    /**
     * Default Constructor of the token database
     */
    public DefaultTokenImpl() {
        super();
    }

    @Override
    public void init(Object configuration) {
    }

    @Override
    public boolean addToken(String jwt) {

        boolean isAdded = false;
        try {
//            Jws<Claims> jws = TokenSecurity.getInstance().getTokenInformation(jwt);
//
//            String projectSuffix = String.valueOf(jws.getBody()
//                    .get(TokenSecurity.PROJECT_ID, Integer.class));
//            String expirationDate = jws.getBody().getExpiration().toString();
//
//            // should be fine, the JWT representation does not contain ;
//            String line = jwt + ";" + projectSuffix + ";" + expirationDate + "\n";
//            LOG.info("token inserted : " + line);
//            Files.write(
//                    new File(this.tokenConf).toPath(),
//                    line.getBytes(StandardCharsets.UTF_8),
//                    StandardOpenOption.APPEND
//            );
            das.addToken(jwt);
//            this.db.put(jwt, new ConcurrentHashMap<String, Object>() {
//                private static final long serialVersionUID = 3109256773218160485L;
//
//                {
//                    put("projectSuffix", projectSuffix);
//                    put("expirationDate", expirationDate);
//                }
//            });
            isAdded = true;
        } catch (DOIDbException e) {
            LOG.fatal("The token " + jwt + "cannot be saved in database", e);
        }
        return isAdded;
    }

    @Override
    public void deleteToken(String jwt) {
        try {
            das.deleteToken(jwt);
        } catch (DOIDbException e) {
            LOG.fatal("The token " + jwt + "cannot be deleted in database", e);
        }
    }

    @Override
    public boolean isExist(String jwt) {
        boolean isTokenExist = false;
        try {
            List<String> tokenList = das.getTokens();
            if (!tokenList.isEmpty()) {
                for (String token : tokenList) {
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

    @Override
    public boolean isExpired(String jwt) {
        boolean isExpirated = true;
        Jws<Claims> jws = TokenSecurity.getInstance().getTokenInformation(jwt);

        // TODO Cannot get token information of an expired token...
        if (jws == null) {
            return isExpirated;
        }

//        String projectSuffix = String.valueOf(jws.getBody()
//                .get(TokenSecurity.PROJECT_ID, Integer.class));
        String expirationDate = jws.getBody().getExpiration().toString();
        try {
            // Precise "Locale.ENGLISH" otherwise unparsable exception occur for day in week and month
            DateFormat dateFormat = new SimpleDateFormat(TokenSecurity.DATE_FORMAT, Locale.ENGLISH);
            Date expDate = dateFormat.parse(expirationDate);
            isExpirated = new Date().after(expDate);
        } catch (ParseException ex) {
            LOG.fatal(ex);
        }
        return isExpirated;
    }

    public List<String> getTokens() {
        try {
            return das.getTokens();
        } catch (DOIDbException e) {
            LOG.fatal("Cannot retrieve the token list from database", e);
            return new ArrayList<>();
        }
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
