package fr.cnes.doi.ldap.impl;

import java.util.List;

import fr.cnes.doi.ldap.exceptions.LDAPAccessException;
import fr.cnes.doi.ldap.persistence.LdapDoidbIntegration;
import fr.cnes.doi.ldap.service.ILDAPAcessService;
import fr.cnes.doi.ldap.util.LDAPUser;
import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.utils.DOIUser;
import fr.cnes.doi.utils.ManageUsers;

public class LdapDoidbIntegrationImpl implements LdapDoidbIntegration {

    ILDAPAcessService ldapaccessservice = new LDAPAccessServiceImpl();

    @Override
    public void updateDoiServerDataBaseFromLdap() throws LDAPAccessException, DOIDbException {
        List<LDAPUser> ldapmembers = ldapaccessservice.getDOIProjectMembers();
        List<DOIUser> dbusers = ManageUsers.getInstance().getUsers();
        // remove from database users that are no longer members of doi_server project
        for (DOIUser dbuser : dbusers) {
            if (!isContained(dbuser, ldapmembers)) {
                // delete dbuser from doi database
                ManageUsers.getInstance().removeDOIUser(dbuser.getUsername());
            }
        }
        // add to database users that are new members of doi_server project
        for (LDAPUser ldapmember : ldapmembers) {
            if (!isContained(ldapmember, dbusers)) {
                // add ldapmember to doi database
                ManageUsers.getInstance().addDOIUser(ldapmember.getUsername(), false, ldapmember.
                        getEmail());
            }
        }
    }

    // Méthodes utilitaires
    private boolean isContained(LDAPUser ldapmember, List<DOIUser> dbusers) {
        boolean isContained = false;
        for (DOIUser dbuser : dbusers) {
            if (dbuser.getUsername().equals(ldapmember.getUsername())) {
                isContained =  true;
                break;
            }
        }
        return isContained;
    }

    private boolean isContained(DOIUser dbuser, List<LDAPUser> ldapmembers) {
        boolean isContained = false;
        for (LDAPUser ldapuser : ldapmembers) {
            if (ldapuser.getUsername().equals(dbuser.getUsername())) {
                isContained =  true;
                break;
            }
        }
        return isContained;
    }

}
