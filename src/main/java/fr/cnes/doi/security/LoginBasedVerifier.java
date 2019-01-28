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
package fr.cnes.doi.security;

import java.util.Base64;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.restlet.security.Verifier;

import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.logging.business.JsonMessage;

/**
 * Security class for checking login/password.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class LoginBasedVerifier implements Verifier {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(LoginBasedVerifier.class.getName());

    /**
     * User DB.
     */
    private final AbstractUserRoleDBHelper userDB;

    /**
     * Constructor.
     *
     * @param userDB user DB
     */
    public LoginBasedVerifier(final AbstractUserRoleDBHelper userDB) {
        LOG.traceEntry(new JsonMessage(userDB));
        this.userDB = userDB;
    }

    /**
     * Verifies the user name and his password.
     *
     * @param request request
     * @param response response
     * @return the result
     */
    @Override
    public int verify(final Request request,
            final Response response) {
        LOG.traceEntry(new JsonMessage(request));
        final int result;
        final ChallengeResponse challResponse = request.getChallengeResponse();
        
        if (challResponse == null) {
            result = Verifier.RESULT_MISSING;
        } else if (challResponse.getScheme().equals(ChallengeScheme.HTTP_OAUTH_BEARER)){
        	result = Verifier.RESULT_MISSING;
        } else {
            result = processAuthentication(request, challResponse);
        }
        return LOG.traceExit(result);
    }

    /**
     * Process Authentication.
     *
     * @param request request
     * @param challResponse authentication object
     * @return the authentication status
     */
    private int processAuthentication(final Request request,
            final ChallengeResponse challResponse) {
        LOG.traceEntry(new JsonMessage(request));
        final int result;
        final String login = challResponse.getRawValue();
        LOG.debug("User from challenge response : " + login);
        
        if (login == null) {
            return LOG.traceExit(Verifier.RESULT_MISSING);
        }
        
        //TODO test password via LDAP
        final String decodedLogin = new String(Base64.getDecoder().decode(login));
        final String[] userLogin = decodedLogin.split(":");
        
        if (this.userDB.isUserExist(userLogin[0])) {
//            result = processToken(request, login);
        	result = Verifier.RESULT_VALID;
        	request.getClientInfo().setUser(new User(userLogin[0]));
            request.getHeaders().set(UtilsHeader.SELECTED_ROLE_PARAMETER, String.valueOf(""));
        } else {
            result = Verifier.RESULT_INVALID;
        }
        return LOG.traceExit(result);
    }
    
    /**
     * Check the user password via LDAP connection  
     * @param userName 
     * @param userPwd
     * @return true if user authenticated, false otherwise
     */
    private boolean verifPwdLDAP(String userName, String userPwd) {
    	
    	return true;
    }

//    /**
//     * Process token.
//     *
//     * @param request request
//     * @param token token
//     * @return the status given by {@link Verifier}
//     */
//    private int processToken(final Request request,
//            final String token) {
//        LOG.traceEntry(new JsonMessage(request));
//        LOG.traceEntry(token);
//        final int result;
//        if (this.userDB.isExpired(token)) {
//            LOG.info("token {} is expirated", token);
//            result = Verifier.RESULT_INVALID;
//        } else {
//            result = Verifier.RESULT_VALID;
//            final Jws<Claims> tokenInfo = TokenSecurity.getInstance().getTokenInformation(token);
//            final Claims body = tokenInfo.getBody();
//            final String userID = body.getSubject();
//            final Integer projectID = (Integer) body.get(TokenSecurity.PROJECT_ID);
//            LOG.info("token {} is valid, {} for {} are authenticated", token, userID, projectID);
//            request.getClientInfo().setUser(new User(userID));
//            request.getHeaders().set(UtilsHeader.SELECTED_ROLE_PARAMETER, String.valueOf(projectID));
//        }
//        return LOG.traceExit(result);
//    }
}
