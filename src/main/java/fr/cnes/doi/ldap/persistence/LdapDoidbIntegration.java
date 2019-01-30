package fr.cnes.doi.ldap.persistence;

import fr.cnes.doi.ldap.exceptions.LDAPAccessException;
import fr.cnes.doi.persistence.exceptions.DOIDbException;

public interface LdapDoidbIntegration {

	public void updateDoiServerDataBaseFromLdap() throws LDAPAccessException, DOIDbException;
	
}
