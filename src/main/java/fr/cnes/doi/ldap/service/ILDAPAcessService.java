package fr.cnes.doi.ldap.service;

import java.util.List;

import fr.cnes.doi.ldap.exceptions.LDAPAccessException;
import fr.cnes.doi.ldap.util.LDAPUser;

public interface ILDAPAcessService {

    public List<LDAPUser> getDOIProjectMembers() throws LDAPAccessException;

    public boolean authenticateUser(String login, String password);

}
