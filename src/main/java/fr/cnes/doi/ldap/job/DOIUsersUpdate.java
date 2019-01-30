package fr.cnes.doi.ldap.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.ldap.exceptions.LDAPAccessException;
import fr.cnes.doi.ldap.impl.LdapDoidbIntegrationImpl;
import fr.cnes.doi.ldap.persistence.LdapDoidbIntegration;
import fr.cnes.doi.persistence.exceptions.DOIDbException;

public class DOIUsersUpdate implements Runnable {

	// ldap and doidb integration service
	private LdapDoidbIntegration service = new LdapDoidbIntegrationImpl();

	// logger
	private Logger LOG = LogManager.getLogger(DOIUsersUpdate.class.getName());

	@Override
	public void run() {
		LOG.info("executing task that updates database from ldap !");
		try {
			service.updateDoiServerDataBaseFromLdap();
		} catch (LDAPAccessException e) {
			LOG.error("error occured when calling DOIUsersUpdate job", e);
		} catch (DOIDbException e) {
			LOG.error("error occured when calling DOIUsersUpdate job", e);
		}

	}
}
