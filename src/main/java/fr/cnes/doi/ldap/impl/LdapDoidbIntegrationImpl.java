package fr.cnes.doi.ldap.impl;

import java.util.List;

import fr.cnes.doi.ldap.exceptions.LDAPAccessException;
import fr.cnes.doi.ldap.persistence.LdapDoidbIntegration;
import fr.cnes.doi.ldap.service.ILDAPAcessService;
import fr.cnes.doi.ldap.util.LDAPUser;
import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.impl.DOIDbDataAccessServiceImpl;
import fr.cnes.doi.persistence.model.DOIUser;
import fr.cnes.doi.persistence.service.DOIDbDataAccessService;

public class LdapDoidbIntegrationImpl implements LdapDoidbIntegration {

	ILDAPAcessService ldapaccessservice = new LDAPAccessServiceImpl();
	DOIDbDataAccessService dbaccessservice = new DOIDbDataAccessServiceImpl();

	public void updateDoiServerDataBaseFromLdap() throws LDAPAccessException, DOIDbException {
		List<LDAPUser> ldapmembers = ldapaccessservice.getDOIProjectMembers();
		List<DOIUser> dbusers = dbaccessservice.getAllDOIusers();
		// remove from database users that are no longer members of doi_server project
		for (DOIUser dbuser : dbusers) {
			if (!isContained(dbuser, ldapmembers)) {
				// delete dbuser from doi database
				dbaccessservice.removeDOIUser(dbuser.getUsername());
			}
		}
		// add to database users that are new members of doi_server project
		for (LDAPUser ldapmember : ldapmembers) {
			if (!isContained(ldapmember, dbusers)) {
				// add ldapmember to doi database
				dbaccessservice.addDOIUser(ldapmember.getUsername(), false, ldapmember.getEmail());
			}
		}
	}

	// Méthodes utilitaires
	private boolean isContained(LDAPUser ldapmember, List<DOIUser> dbusers) {
		for (DOIUser dbuser : dbusers) {
			if (dbuser.getUsername() == ldapmember.getUsername()) {
				return true;
			}
		}
		return false;
	}

	private boolean isContained(DOIUser dbuser, List<LDAPUser> ldapmembers) {
		for (LDAPUser ldapuser : ldapmembers) {
			if (ldapuser.getUsername() == dbuser.getUsername()) {
				return true;
			}
		}
		return false;
	}

}
