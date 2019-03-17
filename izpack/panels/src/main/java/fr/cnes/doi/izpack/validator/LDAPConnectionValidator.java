/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.izpack.validator;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.installer.DataValidator.Status;
import static fr.cnes.doi.izpack.utils.Constants.ID_LDAP_PWD;
import static fr.cnes.doi.izpack.utils.Constants.ID_LDAP_URL;
import static fr.cnes.doi.izpack.utils.Constants.ID_LDAP_USER;
import java.util.Hashtable;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

/**
 *
 * @author malapert
 */
public class LDAPConnectionValidator implements DataValidator {

    private static final Logger LOG = Logger.getLogger(LDAPConnectionValidator.class.getName());
    private static final String STR_DEFAULT_ERROR_MESSAGE = "Cannot connect to the specified LDAP.";

    protected InstallData installData;

    //Error and Warning Messages
    protected String str_errorMsg;
    protected String str_warningMsg = "";

    @Override
    public boolean getDefaultAnswer() {
        return true;
    }

    @Override
    public String getErrorMessageId() {
        if (str_errorMsg != null) {
            return str_errorMsg;
        } else {
            return STR_DEFAULT_ERROR_MESSAGE;
        }
    }

    @Override
    public String getWarningMessageId() {
        return str_warningMsg;
    }

    @Override
    public Status validateData(InstallData installData) {
        LOG.info("Validating LDAP connection parameters");
        this.installData = installData;

        final Status status;
        if(authenticateUser(this.getLogin(), this.getPwd())) {
            status = Status.OK;
        } else {
            status = Status.ERROR;
            this.str_errorMsg = "Connection failed to LDAP.";
        }

        LOG.info("validated LDAP connection parameters");
        return Status.OK;
    }      

    
    public boolean authenticateUser(final String login, final String password) {
        final Hashtable<String, String> prop = new Hashtable<>();
        prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        prop.put(Context.PROVIDER_URL, getUrl());
        prop.put(Context.SECURITY_AUTHENTICATION, "simple");
        prop.put(Context.SECURITY_CREDENTIALS, password);
        prop.put(Context.SECURITY_PRINCIPAL,
                "uid=" + login + ",cn=users,cn=accounts,dc=sis,dc=cnes,dc=fr");
        InitialLdapContext context = null;
        boolean isAuthenticate;
        try {
            context = new InitialLdapContext(prop, null);
            isAuthenticate = true;
        } catch (NamingException e) {
            isAuthenticate = false;
        } catch (Exception e) {
            isAuthenticate = false;
        } finally {
            try {
                if (context != null) {
                    context.close();
                }
            } catch (NamingException e) {
            }
        }
        return isAuthenticate;
    }    


    public String getUrl() {
        return installData.getVariable(ID_LDAP_URL);
    }
    
    public String getLogin() {
        return installData.getVariable(ID_LDAP_USER);
    }

    public String getPwd() {
        return installData.getVariable(ID_LDAP_PWD);
    }    
}
