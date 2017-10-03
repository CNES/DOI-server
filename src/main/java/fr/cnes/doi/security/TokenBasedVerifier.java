/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.logging.business.JsonMessage;
import fr.cnes.doi.utils.spec.Requirement;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.security.User;
import org.restlet.security.Verifier;

/**
 * Security class for checking token.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_AUTH_020,
        reqName = Requirement.DOI_AUTH_020_NAME
)
public class TokenBasedVerifier implements Verifier {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(TokenBasedVerifier.class.getName());
    
    /**
     * Token DB.
     */
    private final AbstractTokenDBHelper tokenDB;    

    /**
     * Constructor.
     * @param tokenDB token DB
     */
    public TokenBasedVerifier(final AbstractTokenDBHelper tokenDB) {
        LOG.traceEntry(new JsonMessage(tokenDB));
        this.tokenDB = tokenDB;
    }

    /**
     * Verifies the token.
     * @param request request
     * @param response response
     * @return the result
     */
    @Override
    public int verify(final Request request, final Response response) {
        LOG.traceEntry(new JsonMessage(request));
        final int result;
        final ChallengeResponse challResponse = request.getChallengeResponse();
        if (challResponse == null) {
            result = Verifier.RESULT_MISSING;
        } else {
            result = processAuthentication(request, challResponse);
        }
        return LOG.traceExit(result);
    }

    /**
     * Process Authentication.     
     * @param request request
     * @param challResponse authentication object
     * @return the authentication status
     */
    private int processAuthentication(final Request request, final ChallengeResponse challResponse) {
        LOG.traceEntry(new JsonMessage(request));        
        final int result;
        final String token = challResponse.getRawValue();
        LOG.debug("Token from challenge response : "+token);
        if (token == null) {
            result = Verifier.RESULT_MISSING;
        } else if (this.tokenDB.isExist(token)) {
            result = processToken(request, token);            
        } else {
            result = Verifier.RESULT_INVALID;
        }
        return LOG.traceExit(result);
    }

    /**
     * Process token.
     * @param request request
     * @param token token
     * @return the status given by {@link Verifier}
     */
    private int processToken(final Request request, final String token) {
        LOG.traceEntry(new JsonMessage(request));
        LOG.traceEntry(token);
        final int result;
        if (this.tokenDB.isExpirated(token)) {
            LOG.info("token {} is expirated", token);
            result = Verifier.RESULT_INVALID;
        } else {
            result = Verifier.RESULT_VALID;
            final Jws<Claims> tokenInfo = TokenSecurity.getInstance().getTokenInformation(token);
            final Claims body = tokenInfo.getBody();
            final String userID = body.getSubject();
            final Integer projectID = (Integer) body.get(TokenSecurity.PROJECT_ID);
            LOG.info("token {} is valid, {} for {} are authenticated", token, userID, projectID);
            request.getClientInfo().setUser(new User(userID));
            request.getHeaders().set(UtilsHeader.SELECTED_ROLE_PARAMETER, String.valueOf(projectID));
        }
        return LOG.traceExit(result);
    }
}
