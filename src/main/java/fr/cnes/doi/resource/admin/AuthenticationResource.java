package fr.cnes.doi.resource.admin;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.application.AdminApplication;
import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.exception.TokenSecurityException;
import fr.cnes.doi.resource.AbstractResource;
import fr.cnes.doi.security.TokenSecurity;
import fr.cnes.doi.security.TokenSecurity.TimeUnit;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Handle the creation and deletion of a token generated after authentication 
 * by login/password. Role authorizer skip methods in this class.
 * 
 */
public class AuthenticationResource extends AbstractResource {

	/**
     * Logger.
     */
    private volatile Logger LOG;
    
    /**
     * Parameter for the user name {@value #USER_NAME}.
     */
    public static final String USER_NAME = "user";
    
    /**
     * Parameter for the user token {@value #USER_TOKEN}.
     */
    public static final String USER_TOKEN = "token";
    
    /**
     * Instance of settings to get token properties {@value #doiSetting}.
     */
    private final DoiSettings doiSetting = DoiSettings.getInstance();
    
    /**
     * The token database.
     */
    private volatile AbstractTokenDBHelper tokenDB;
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        final AdminApplication app = (AdminApplication) getApplication();
        LOG = app.getLog();
        LOG.traceEntry();
        setDescription("This resource handles the authentication.");
        this.tokenDB = ((AdminApplication) this.getApplication()).getTokenDB();
        
        LOG.traceExit();
    }
    
    //TODO commentaires
    @Delete
    public void deleteToken(final Form mediaForm) {
    	LOG.traceEntry();
    	final String token = mediaForm.getFirstValue(USER_TOKEN);
    	this.tokenDB.deleteToken(token);
    }
    
    @Post
    public Representation authenticate(final Form mediaForm){
    	 LOG.traceEntry("Paramater : {}", mediaForm);
         checkInputs(mediaForm);
         final String userName = mediaForm.getFirstValue(USER_NAME);
         
         // token is valid for 12 hours
         final int amount = doiSetting.getInt(Consts.TOKEN_EXPIRATION_DELAY, "12");
         // unit = HOURS
         final int timeUnit = doiSetting.getInt(Consts.TOKEN_EXPIRATION_UNIT, "10");
         final TimeUnit unit = TokenSecurity.TimeUnit.getTimeUnitFrom(timeUnit);
         
         String token;
		token = TokenSecurity.getInstance().generate(
				 userName,
		         unit,
		         amount
		);
		boolean boo = this.tokenDB.addToken(token);
		
		LOG.info("Token created {} during {} {}",
				token, amount, unit.name());
		if(boo) {
			LOG.info("Token saved in data base.");
		}else {
			LOG.info("Token coud not be saved in data base.");
		}
		 
		return LOG.traceExit(new StringRepresentation(token));
    }
    
    /**
     * Tests if the {@link #USER_NAME} is set.
     *
     * @param mediaForm the parameters
     * @throws ResourceException - if USER_NAME is not set
     */
    private void checkInputs(final Form mediaForm) throws ResourceException {
        LOG.traceEntry("Parameter : {}", mediaForm);
        if (isValueNotExist(mediaForm, USER_NAME)) {
            throw LOG.throwing(Level.DEBUG, new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
            		USER_NAME + " parameter must be set"));
        }
        LOG.debug("The form is valid");
        LOG.traceExit();
    }
}
