package fr.cnes.doi.ldap.exceptions;

public class LDAPAccessException extends Exception {

    public LDAPAccessException(String string, Exception e) {
        super(string, e);
    }
}
