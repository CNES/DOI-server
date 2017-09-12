/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.db.TokenDBHelper;
import fr.cnes.doi.utils.Utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.security.User;
import org.restlet.security.Verifier;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class TokenBasedVerifier implements Verifier {

    private final TokenDBHelper tokenDB;
    /**
     * Logger.
     */
    public static final Logger LOGGER = Utils.getAppLogger();

    public TokenBasedVerifier(final TokenDBHelper tokenDB) {
        this.tokenDB = tokenDB;
    }

    @Override
    public int verify(Request request, Response response) {
        int result;
        ChallengeResponse cr = request.getChallengeResponse();
        if (cr == null) {
            result = Verifier.RESULT_MISSING;
        } else {
            result = processAuthentication(request, cr);
        }

        return result;
    }

    /**
     * Process Authentication.     
     * @param request request
     * @param cr authentication object
     * @return the authentication status
     */
    private int processAuthentication(Request request, ChallengeResponse cr) {
        int result;
        String token = cr.getRawValue();
        if (token == null) {
            result = Verifier.RESULT_MISSING;
        } else if (this.tokenDB.isExist(token)) {
            result = processToken(request, token);            
        } else {
            result = Verifier.RESULT_INVALID;
        }
        return result;
    }

    /**
     * Process token.
     * @param request request
     * @param token token
     * @return 
     */
    private int processToken(Request request, String token) {
        int result;
        if (this.tokenDB.isExpirated(token)) {
            LOGGER.log(Level.INFO, "token {0} is expirated", token);
            result = Verifier.RESULT_INVALID;
        } else {
            result = Verifier.RESULT_VALID;
            Jws<Claims> tokenInfo = TokenSecurity.getInstance().getTokenInformation(token);
            Claims body = tokenInfo.getBody();
            String userID = body.getSubject();
            Integer projectID = (Integer) body.get(TokenSecurity.PROJECT_ID);
            LOGGER.log(Level.INFO, "token {0} is valid, {1} for {2} are authenticated", new Object[]{token, userID, projectID});
            request.getClientInfo().setUser(new User(userID));
            request.getHeaders().set(UtilsHeader.SELECTED_ROLE_PARAMETER, String.valueOf(projectID));
        }
        return result;
    }
}
