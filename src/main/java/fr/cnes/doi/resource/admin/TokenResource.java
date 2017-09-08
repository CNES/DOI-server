/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.admin;

import fr.cnes.doi.application.AdminApplication;
import static fr.cnes.doi.application.AdminApplication.TOKEN_TEMPLATE;
import fr.cnes.doi.resource.BaseResource;
import fr.cnes.doi.db.TokenDB;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.exception.TokenSecurityException;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.security.TokenSecurity.TimeUnit;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.logging.Level;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * Provides a resource to create token and to decrypt token
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class TokenResource extends BaseResource {

    /**
     * User ID of the operator that creates the token.
     */
    public static String IDENTIFIER_PARAMETER = "identifier";

    /**
     * Token for a specific project.
     */
    public static String PROJECT_ID_PARAMETER = "projectID";

    /**
     * Unit of time used to define the expiration time of the token.
     */
    public static String UNIT_OF_TIME_PARAMETER = "typeOfTime";

    /**
     * Amount of time for which the token is not expirated.
     */
    public static String AMOUNT_OF_TIME_PARAMETER = "amountTime";

    /**
     * Token parameter catched from the URL.
     */
    private String tokenParam;
    
    /**
     * The token database.
     */
    private TokenDB tokenDB;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.tokenParam = getAttribute(TOKEN_TEMPLATE);
        this.tokenDB = ((AdminApplication) this.getApplication()).getTokenDB();
    }

    @Post
    public String createToken(Form info) {
        getLogger().entering(TokenResource.class.getName(), "createToken", info);

        checkInputs(info);
        try {
            String userID = info.getFirstValue(IDENTIFIER_PARAMETER, null);
            String projectID = info.getFirstValue(PROJECT_ID_PARAMETER, null);
            String timeParam = info.getFirstValue(UNIT_OF_TIME_PARAMETER, "0");

            int timeUnit = timeParam.equals("0") ? 1 : Integer.valueOf(timeParam);
            TimeUnit unit = TokenSecurity.TimeUnit.getTimeUnitFrom(timeUnit);

            int amount = Integer.valueOf(info.getFirstValue(AMOUNT_OF_TIME_PARAMETER, "1"));

            String tokenJwt = TokenSecurity.getInstance().generate(
                    userID,
                    Integer.valueOf(projectID),
                    unit,
                    amount
            );
            getLogger().log(Level.INFO, "Token created {0}for project {1} during {2} {3}", new Object[]{tokenJwt, projectID, amount, unit.name()});

            this.tokenDB.addToken(tokenJwt);

            getLogger().exiting(TokenResource.class.getName(), "createToken", tokenJwt);
            return tokenJwt;
        } catch (TokenSecurityException ex) {
            getLogger().throwing(TokenResource.class.getName(), "createToken", ex);
            throw new ResourceException(ex.getStatus(), ex.getMessage());
        }
    }

    /**
     * Checks input parameters
     *
     * @param mediaForm the parameters
     * @ResourceException if PROJECT_ID_PARAMETER and IDENTIFIER_PARAMETER are
     * not set
     */
    private void checkInputs(final Form mediaForm) {
        getLogger().entering(this.getClass().getName(), "checkInputs", mediaForm);
        StringBuilder errorMsg = new StringBuilder();
        if (isValueNotExist(mediaForm, IDENTIFIER_PARAMETER)) {
            getLogger().log(Level.FINE, "{0} value is not set", IDENTIFIER_PARAMETER);
            errorMsg = errorMsg.append(IDENTIFIER_PARAMETER).append(" value is not set.");
        }
        if (isValueNotExist(mediaForm, PROJECT_ID_PARAMETER)) {
            getLogger().log(Level.FINE, "{0} value is not set", PROJECT_ID_PARAMETER);
            errorMsg = errorMsg.append(PROJECT_ID_PARAMETER).append(" value is not set.");
        }
        if (errorMsg.length() == 0) {
            getLogger().fine("The form is valid");
        } else {
            ResourceException ex = new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, errorMsg.toString());
            getLogger().throwing(this.getClass().getName(), "checkInputs", ex);
            throw ex;
        }
    }

    @Get
    public Representation getTokenInformation() {
        getLogger().entering(this.getClass().getName(), "getTokenInformation");
        try {
            Jws<Claims> jws = TokenSecurity.getInstance().getTokenInformation(this.tokenParam);
            getLogger().exiting(this.getClass().getName(), "getTokenInformation");
            return new JsonRepresentation(jws);
        } catch (DoiRuntimeException ex) {
            getLogger().throwing(this.getClass().getName(), "getTokenInformation", ex);            
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex);
        }
    }

}
