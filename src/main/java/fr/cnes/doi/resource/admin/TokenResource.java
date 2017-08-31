/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.admin;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.resource.BaseResource;
import fr.cnes.doi.db.TokenDB;
import fr.cnes.doi.exception.TokenSecurityException;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.security.TokenSecurity.TimeUnit;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class TokenResource extends BaseResource {

    private String tokenParam;
    private TokenDB tokenDB;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.tokenParam = getAttribute("tokenID");
        this.tokenDB = ((AdminApplication)this.getApplication()).getTokenDB();
    }

    @Post
    public String createToken(Form info) {
        try {
            String userID = info.getFirstValue("identifier", null);
            String projectID = info.getFirstValue("projectID", null);
            String timeParam = info.getFirstValue("typeOfTime","0");
            
            int timeUnit = timeParam.equals("0") ? 1 : Integer.valueOf(timeParam);
            TimeUnit unit = TokenSecurity.TimeUnit.getTimeUnitFrom(timeUnit);
            
            int amount = Integer.valueOf(info.getFirstValue("amountTime", "1"));
            
            String tokenJwt = TokenSecurity.getInstance().generate(
                    userID,
                    Integer.valueOf(projectID),
                    unit ,
                    amount
            );
            
            this.tokenDB.addToken(tokenJwt);
            return tokenJwt;
        } catch (TokenSecurityException ex) {
            throw new ResourceException(ex.getStatus(), ex.getMessage());
        }
    }

    @Get
    public Representation getTokenInformation() {
        Jws<Claims> jws = TokenSecurity.getInstance().getTokenInformation(this.tokenParam);
        return new JsonRepresentation(jws);
    }

}
