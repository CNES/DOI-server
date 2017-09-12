/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.db.TokenDBHelper;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.exception.TokenSecurityException;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.UniqueProjectName;
import fr.cnes.doi.utils.Utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.Status;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class TokenSecurity {

    /**
     * logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TokenSecurity.class.getName());

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

    public static final String DATE_FORMAT = "E MMM dd HH:mm:ss z yyyy";

    private String tokenKey;

    private TokenDBHelper tokenDB;

    /**
     * Class to handle the instance
     *
     */
    private static class TokenSecurityHolder {

        /**
         * Unique Instance unique
         */
        private final static TokenSecurity INSTANCE = new TokenSecurity();
    }

    /**
     * Access to unique INSTANCE of Settings
     *
     * @return the configuration instance.
     */
    public static TokenSecurity getInstance() {
        return TokenSecurityHolder.INSTANCE;
    }

    /**
     * Creates a key for token signature encoded with the algorithm HS256
     *
     * @return key encoded in Base64
     * @see <a href="https://fr.wikipedia.org/wiki/Base64">Base64</a>
     */
    public static String createKeySignatureHS256() {
        Key key = MacProvider.generateKey(SignatureAlgorithm.HS256);
        return TextCodec.BASE64.encode(key.getEncoded());
    }

    private TokenSecurity() {
        String token = DoiSettings.getInstance().getString(Consts.TOKEN_KEY);
        this.tokenKey = (token == null) ? DEFAULT_TOKEN_KEY : token;
        this.tokenDB = PluginFactory.getToken();
    }

    public enum TimeUnit {
        HOUR(Calendar.HOUR),
        DAY(Calendar.DATE),
        YEAR(Calendar.YEAR);

        private final int timeUnit;

        TimeUnit(int timeUnit) {
            this.timeUnit = timeUnit;
        }

        public int getTimeUnit() {
            return this.timeUnit;
        }

        public static TimeUnit getTimeUnitFrom(final int value) {
            TimeUnit result = null;
            TimeUnit[] units = TimeUnit.values();
            for (TimeUnit unit : units) {
                if (unit.getTimeUnit() == value) {
                    result = unit;
                    break;
                }
            }
            return result;
        }
    }

    /**
     * Creates a token.
     *
     * @param userID The user that creates the token
     * @param projectID The project ID
     * @param timeUnit The time unit for the date expiration
     * @param amount the amount of timeUnit for the date expiration
     * @return JWT token
     * @throws fr.cnes.doi.exception.TokenSecurityException if the projectID is
     * not first registered
     */
    public String generate(final String userID, final int projectID, final TokenSecurity.TimeUnit timeUnit, final int amount) throws TokenSecurityException {
        Map<String, Integer> projects = UniqueProjectName.getInstance().getProjects();
        Set<String> projectNameColl = Utils.getKeysByValue(projects, projectID);
        if (projectNameColl.isEmpty()) {
            throw new TokenSecurityException(Status.CLIENT_ERROR_BAD_REQUEST, "No register " + PROJECT_ID + ", please create one");
        }
        String projectName = projectNameColl.iterator().next();

        Date now = Date.from(Instant.now());
        Date expirationTime = computeExpirationDate(now, timeUnit.getTimeUnit(), amount);

        String token = Jwts.builder()
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
        LOGGER.log(Level.FINE, "token generated : {0}", token);

        return token;
    }

    /**
     * Returns the token key computed by the algorithm HS256.
     *
     * @return the token key encoded in base64
     */
    public String getTokenKey() {
        return this.tokenKey;
    }

    /**
     * Sets a custom token key.
     *
     * @param tokenKey token key
     */
    public void setTokenKey(final String tokenKey) {
        this.tokenKey = tokenKey;
    }

    /**
     * Returns the token information.
     *
     * @param jwtToken token JWT
     * @return the information
     * @throws DoiRuntimeException - if an error happens getting information from the token
     */
    public Jws<Claims> getTokenInformation(final String jwtToken) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .requireIssuer(DoiSettings.getInstance().getString(Consts.APP_NAME))
                    .setSigningKey(TextCodec.BASE64.decode(getTokenKey()))
                    .parseClaimsJws(jwtToken);
            return jws;
        } catch (RuntimeException ex) {
            throw new DoiRuntimeException(ex.getMessage(), ex);
        }
    }

    public TokenDBHelper getTokenDB() {
        return this.tokenDB;
    }

    /**
     * Computes the expiration date.
     *
     * @param now the current date
     * @param calendarTime the unit time associated to the amount
     * @param amount amount
     * @return the expiration date
     */
    private Date computeExpirationDate(final Date now, int calendarTime, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(calendarTime, amount);
        return c.getTime();
    }
}
