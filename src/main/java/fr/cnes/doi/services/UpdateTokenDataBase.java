package fr.cnes.doi.services;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.db.AbstractTokenDBHelper;
import fr.cnes.doi.security.TokenSecurity;


public class UpdateTokenDataBase implements Runnable {

		 /**
	     * Token database.
	     */
	    private final AbstractTokenDBHelper tokenDB = TokenSecurity.getInstance().getTOKEN_DB();

		// logger
		private Logger LOG = LogManager.getLogger(UpdateTokenDataBase.class.getName());

		@Override
		public void run() {
			LOG.info("Executing task that remove expired token from database.");
			List<String> tokenList = tokenDB.getTokens();
			for(String token : tokenList) {
				if(tokenDB.isExpired(token)){
					tokenDB.deleteToken(token);
				}
			}
		}

}
