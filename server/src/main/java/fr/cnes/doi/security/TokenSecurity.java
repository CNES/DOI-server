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
package fr.cnes.doi.security;

import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.exception.TokenSecurityException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.UniqueProjectName;
import fr.cnes.doi.utils.Utils;
import fr.cnes.doi.utils.spec.Requirement;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.TextCodec;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Status;

/**
 * Security class for token generation.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_AUTH_020, reqName = Requirement.DOI_AUTH_020_NAME)
public final class TokenSecurity {

    /**
     * Project ID name in token.
     */
    public static final String PROJECT_ID = "projectID";

    /**
     * Project name in token.
     */
    public static final String PROJECT_NAME = "projectName";

    /**
     * Default token key.
     */
    public static final String DEFAULT_TOKEN_KEY = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";

    /**
     * Default date format for the token {@value #DATE_FORMAT}
     */
    public static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy";

    /**
     * Plugin for token database.
     */
    private static final AbstractTokenDBHelper TOKEN_DB = PluginFactory.getToken();

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(TokenSecurity.class.getName());

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static TokenSecurity getInstance() {
        LOG.traceEntry();
        return LOG.traceExit(TokenSecurityHolder.INSTANCE);
    }

    /**
     * Creates a key for token signature encoded with the algorithm HS256
     *
     * @return key encoded in Base64
     * @see <a href="https://fr.wikipedia.org/wiki/Base64">Base64</a>
     */
    public static String createKeySignatureHS256() {
        LOG.traceEntry();
        final Key key = MacProvider.generateKey(SignatureAlgorithm.HS256);
        return LOG.traceExit(TextCodec.BASE64.encode(key.getEncoded()));
    }
    /**
     * token key.
     */
    private String tokenKey;

    /**
     * Private constructor.
     */
    private TokenSecurity() {
        LOG.traceEntry();
        init();
        LOG.traceExit();
    }

    /**
     * Init.
     */
    private void init() {
        LOG.traceEntry();
        final String token = DoiSettings.getInstance().getString(Consts.TOKEN_KEY);
        this.tokenKey = (token == null) ? DEFAULT_TOKEN_KEY : token;
        TokenSecurity.TOKEN_DB.setConfiguration(null);
        LOG.traceExit();
    }

    /**
     * Creates a token.
     *
     * @param userID The user that creates the token
     * @param projectID The project ID
     * @param timeUnit The time unit for the date expiration
     * @param amount the amount of timeUnit for the date expiration
     * @return JWT token
     * @throws fr.cnes.doi.exception.TokenSecurityException if the projectID is not first registered
     */
    public String generate(final String userID,
            final int projectID,
            final TokenSecurity.TimeUnit timeUnit,
            final int amount) throws TokenSecurityException {
        LOG.traceEntry("Parameters : {}, {}, {} and {}", userID, projectID, timeUnit, amount);
        final Map<String, Integer> projects = UniqueProjectName.getInstance().getProjects();
        final Set<String> projectNameColl = Utils.getKeysByValue(projects, projectID);
        if (projectNameColl.isEmpty()) {
            throw LOG.throwing(new TokenSecurityException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "No register " + PROJECT_ID + ", please create one")
            );
        }
        final String projectName = projectNameColl.iterator().next();

        final Date now = Date.from(Instant.now());
        final Date expirationTime = computeExpirationDate(now, timeUnit.getTimeUnit(), amount);

        final String token = Jwts.builder()
                .setIssuer(DoiSettings.getInstance().getString(Consts.APP_NAME))
                .setIssuedAt(Date.from(Instant.now()))
                .setSubject(userID)
                .claim(PROJECT_ID, projectID)
                .claim(PROJECT_NAME, projectName)
                .setExpiration(expirationTime)
                .signWith(
                        SignatureAlgorithm.HS256,
                        TextCodec.BASE64.decode(getTokenKey())
                )
                .compact();
        LOG.debug(String.format("token generated : %s", token));
        return LOG.traceExit(token);
    }

    //TODO createToken surcharged
    /**
     * Creates a token. (no project ID required)
     *
     * @param userID The user that creates the token
     * @param timeUnit The time unit for the date expiration
     * @param amount the amount of timeUnit for the date expiration
     * @return JWT token
     */
    public String generate(final String userID,
            final TokenSecurity.TimeUnit timeUnit,
            final int amount) {
        LOG.traceEntry("Parameters : {}, {} and {}", userID, timeUnit, amount);

        final Date now = Date.from(Instant.now());
        final Date expirationTime = computeExpirationDate(now, timeUnit.getTimeUnit(), amount);

        final String token = Jwts.builder()
                .setIssuer(DoiSettings.getInstance().getString(Consts.APP_NAME))
                .setIssuedAt(Date.from(Instant.now()))
                .setSubject(userID)
                .setExpiration(expirationTime)
                .signWith(
                        SignatureAlgorithm.HS256,
                        TextCodec.BASE64.decode(getTokenKey())
                )
                .compact();
        LOG.debug(String.format("token generated : %s", token));
        return LOG.traceExit(token);
    }

    /**
     * Returns the token key computed by the algorithm HS256.
     *
     * @return the token key encoded in base64
     */
    public String getTokenKey() {
        LOG.traceEntry();
        return LOG.traceExit(this.tokenKey);
    }

    /**
     * Sets a custom token key.
     *
     * @param tokenKey token key
     */
    public void setTokenKey(final String tokenKey) {
        LOG.traceEntry("Parameter : {}", tokenKey);
        this.tokenKey = tokenKey;
        LOG.debug(String.format("Set tokenKey to %s", tokenKey));
        LOG.traceExit();
    }

    /**
     * Returns the token information.
     *
     * @param jwtToken token JWT
     * @return the information
     * @throws DoiRuntimeException - if an error happens getting information from the token
     */
    public Jws<Claims> getTokenInformation(final String jwtToken) throws DoiRuntimeException {
        LOG.traceEntry("Parameter : {}", jwtToken);
        Jws<Claims> token;
        try {
            token = Jwts.parser()
                    .requireIssuer(DoiSettings.getInstance().getString(Consts.APP_NAME))
                    .setSigningKey(TextCodec.BASE64.decode(getTokenKey()))
                    .parseClaimsJws(jwtToken);
        } catch (UnsupportedJwtException
                | MalformedJwtException | SignatureException
                | IllegalArgumentException ex) {
            throw LOG.throwing(new DoiRuntimeException("Unable to get the token information", ex));
        } catch (ExpiredJwtException e) {
            LOG.info("Cannot get the token information because : " + e.getMessage());
            token = null;
        }
        return LOG.traceExit(token);
    }

    /**
     * Returns the token DB.
     *
     * @return the token DB
     */
    public AbstractTokenDBHelper getTOKEN_DB() {
        LOG.traceEntry();
        return LOG.traceExit(TokenSecurity.TOKEN_DB);
    }

    /**
     * Computes the expiration date.
     *
     * @param now the current date
     * @param calendarTime the unit time associated to the amount
     * @param amount amount
     * @return the expiration date
     */
    private Date computeExpirationDate(final Date now,
            final int calendarTime,
            final int amount) {
        LOG.traceEntry("Parameters : {}, {} and {}", now, calendarTime, amount);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(calendarTime, amount);
        return LOG.traceExit(calendar.getTime());
    }

    /**
     * Class to handle the instance
     *
     */
    private static class TokenSecurityHolder {

        /**
         * Unique Instance unique
         */
        private static final TokenSecurity INSTANCE = new TokenSecurity();
    }

    /**
     * Time unit.
     */
    public enum TimeUnit {
        /**
         * Hour.
         */
        HOUR(Calendar.HOUR),
        /**
         * Day.
         */
        DAY(Calendar.DATE),
        /**
         * Year.
         */
        YEAR(Calendar.YEAR);

        /**
         * time unit
         */
        private final int timeUnit;

        /**
         * Constructor.
         *
         * @param timeUnit time unit
         */
        TimeUnit(final int timeUnit) {
            this.timeUnit = timeUnit;
        }

        /**
         * Returns the time unit.
         *
         * @return the time unit
         */
        public int getTimeUnit() {
            return this.timeUnit;
        }

        /**
         * Returns the time unit from a value.
         *
         * @param value vale
         * @return time unit
         */
        public static TimeUnit getTimeUnitFrom(final int value) {
            TimeUnit result = null;
            final TimeUnit[] units = TimeUnit.values();
            for (final TimeUnit unit : units) {
                if (unit.getTimeUnit() == value) {
                    result = unit;
                    break;
                }
            }
            return result;
        }
    }
}
