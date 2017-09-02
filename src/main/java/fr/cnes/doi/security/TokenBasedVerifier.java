/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.db.TokenDB;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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

    private final TokenDB tokenDB;

    public TokenBasedVerifier(final TokenDB tokenDB) {
        this.tokenDB = tokenDB;
    }

    @Override
    public int verify(Request request, Response response) {
        int result;
        ChallengeResponse cr = request.getChallengeResponse();
        String token = cr.getRawValue();
        if (token == null) {
            result =  Verifier.RESULT_MISSING;
        } else if (this.tokenDB.isExist(token)) {
            if (this.tokenDB.isExpirated(token)) {
                result = Verifier.RESULT_INVALID;
            } else {
                result = Verifier.RESULT_VALID;                
                Jws<Claims> tokenInfo = TokenSecurity.getInstance().getTokenInformation(token);
                Claims body = tokenInfo.getBody();
                String userID = body.getSubject();
                Integer projectID = (Integer) body.get(TokenSecurity.PROJECT_ID);                
                request.getClientInfo().setUser(new User(userID));
                request.getHeaders().set(UtilsHeader.SELECTED_ROLE_PARAMETER, String.valueOf(projectID));                
            }
        } else {
            result = Verifier.RESULT_INVALID;
        }
        return result;
    }
}
