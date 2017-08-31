/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.db.TokenDB;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.security.Verifier;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class TokenBasedVerifier implements Verifier {
    
    private final TokenDB tokenDB;
    
    public TokenBasedVerifier(final TokenDB tokenDB) {
        this.tokenDB = tokenDB;
    }
    
    
    @Override
    public int verify(Request request, Response response) {
        ChallengeResponse cr = request.getChallengeResponse();
        String token = cr.getRawValue();
        if (token == null) {
            return Verifier.RESULT_MISSING;
        } else if(this.tokenDB.isExist(token)) {
            return Verifier.RESULT_VALID;    
        } else {
            return Verifier.RESULT_INVALID;
        }
        //checkToken(token);
        //return Verifier.RESULT_VALID;
    }
}
