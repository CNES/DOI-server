package fr.cnes.doi.ldap.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.doi.ldap.exceptions.LDAPAccessException;
import fr.cnes.doi.ldap.service.ILDAPAcessService;
import fr.cnes.doi.ldap.util.LDAPUser;
import fr.cnes.doi.ldap.util.PasswordEncrypter;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

public class LDAPAccessServiceImpl implements ILDAPAcessService {

	private Logger logger = LoggerFactory.getLogger(LDAPAccessServiceImpl.class);

	private DoiSettings conf = DoiSettings.getInstance();
    
	public List<LDAPUser> getDOIProjectMembers() throws LDAPAccessException {
		DirContext context = null;
		try {
			// LDAP context
			context = getContext();
			SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {
                "member",
                 };
            constraints.setReturningAttributes(attrIDs);
            
            NamingEnumeration answer = context.search("cn=groups,cn=accounts,dc=sis,dc=cnes,dc=fr", "cn={0}", new String[] {conf.getString(Consts.LDAP_PROJECT)}, constraints);
            List<LDAPUser> members = new ArrayList<LDAPUser>();
            if (answer.hasMore()) {
            	 NamingEnumeration<?> attrs = ((SearchResult) answer.next()).getAttributes().get("member").getAll();
 			     while (attrs.hasMoreElements()) {
 					members.add(getLdapUser(context, attrs.next().toString()));
 				 }
            }    
 			return members;
	    } catch (Exception e) {
	    	throw new LDAPAccessException("",e);
	    } finally {
	    	try {
	    		if (context != null) {
				  context.close();
	    		}
			} catch (NamingException e) {
				logger.error("LDAPAccessImpl getContext: Unable to close context", e);
			}
	    }
	}

	// Methodes utilitaires
	private InitialLdapContext getContext() {
		Hashtable<String, String> prop = new Hashtable<String, String>();
		prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		prop.put(Context.PROVIDER_URL, conf.getString(Consts.LDAP_URL));
		prop.put(Context.SECURITY_AUTHENTICATION, "simple");
		try {
			prop.put(Context.SECURITY_PRINCIPAL,
					PasswordEncrypter.getInstance().decryptPasswd(conf.getString(Consts.LDAP_USER)));
			prop.put(Context.SECURITY_CREDENTIALS,
					PasswordEncrypter.getInstance().decryptPasswd(conf.getString(Consts.LDAP_PWD)));
		} catch (Exception e) {
			logger.error("LDAPAccessImpl getContext: Unable to get Ldap password", e);
		}
		InitialLdapContext context = null;
		try {
			context = new InitialLdapContext(prop, null);
		} catch (NamingException e) {
			logger.error("LDAPAccessImpl getContext: Unable to connect to Ldap", e);
		}
		return context;
	}

	private LDAPUser getLdapUser(DirContext context, String dn) throws NamingException {
		LDAPUser ldapuser = new LDAPUser();
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String[] attrIDs = {
                "uid", "mail", "name"
                 };
		NamingEnumeration<? extends Attribute> attrbs = context.getAttributes(dn, attrIDs).getAll();
		while (attrbs.hasMore()) {
			Attribute att = attrbs.next();
			String attId = att.getID();
			if (attId == "name") {
				NamingEnumeration<?> values = att.getAll();
				while (values.hasMoreElements()) {
					ldapuser.setFullname(values.next().toString());
					break;
				}
			} else if (attId == "mail") {
				NamingEnumeration<?> values = att.getAll();
				while (values.hasMoreElements()) {
					ldapuser.setEmail(values.next().toString());
					break;
				}
			} else if (attId == "uid") {
				NamingEnumeration<?> values = att.getAll();
				while (values.hasMoreElements()) {
					ldapuser.setUsername(values.next().toString());
					break;
				}
			}
		}
		return ldapuser;
	}

	@Override
	public boolean authenticateUser(String login, String password) {
       	boolean result = false;	
		Hashtable<String, String> prop = new Hashtable<String, String>();
		prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		prop.put(Context.PROVIDER_URL, conf.getString(Consts.LDAP_URL));
		prop.put(Context.SECURITY_AUTHENTICATION, "simple");
		prop.put(Context.SECURITY_PRINCIPAL, login);
		prop.put(Context.SECURITY_CREDENTIALS, password);
		
		InitialLdapContext context = null;
		try {
			context = new InitialLdapContext(prop, null);
			result = true;
			return result;
		} catch (NamingException e) {
			logger.error("LDAPAccessImpl getContext: Unable to identify user", e);
			return result;
		} catch (Exception e) {
			logger.error("LDAPAccessImpl getContext: Unexpected exception", e);
			return result;
		} finally {
			try {
				if (context != null) {
				  context.close();
				}
			} catch (NamingException e) {
				logger.error("LDAPAccessImpl getContext: Unable to close context", e);
			}	
		}
		
	}
}
