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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.ldap.exceptions.LDAPAccessException;
import fr.cnes.doi.ldap.service.ILDAPAcessService;
import fr.cnes.doi.ldap.util.LDAPUser;
import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;

public class LDAPAccessServiceImpl implements ILDAPAcessService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(LDAPAccessServiceImpl.class.getName());
    
    private final DoiSettings conf = DoiSettings.getInstance();

    @Override
    public List<LDAPUser> getDOIProjectMembers() throws LDAPAccessException {
        /*DirContext context = null;
        try {
            context = getContext();
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {
                "member",};
            constraints.setReturningAttributes(attrIDs);

            NamingEnumeration answer = context.search("cn=groups,cn=accounts,dc=sis,dc=cnes,dc=fr",
                    "cn={0}", new String[]{conf.getString(Consts.LDAP_PROJECT)}, constraints);
            List<LDAPUser> members = new ArrayList<>();
            if (answer.hasMore()) {
                NamingEnumeration<?> attrs = ((SearchResult) answer.next()).getAttributes().get(
                        "member").getAll();
                while (attrs.hasMoreElements()) {
                    members.add(getLdapUser(context, attrs.next().toString()));
                }
            }
            return members;
        } catch (NamingException e) {
            throw new LDAPAccessException("", e);
        } finally {
            try {
                if (context != null) {
                    context.close();
                }
            } catch (NamingException e) {
                LOGGER.error("LDAPAccessImpl getContext: Unable to close context", e);
            }
        }*/
    	DirContext context = null;
    	try {
    		context = getContext();
    		return getAllDOIProjectMembers((InitialLdapContext) context);
    	}
    	finally {
    		if (context != null) {
              try {
				context.close();
			} catch (NamingException e) {
				e.printStackTrace();
			}
          }
    	}
    }

    /* Methodes utilitaires */
    private InitialLdapContext getContext() {
        Hashtable<String, String> prop = new Hashtable<>();
        prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        prop.put(Context.PROVIDER_URL, conf.getString(Consts.LDAP_URL));
        prop.put(Context.SECURITY_AUTHENTICATION, "simple");
        try {
            prop.put(Context.SECURITY_PRINCIPAL, "uid=" 
            		+ UtilsCryptography.decrypt(conf.getString(Consts.LDAP_USER)) 
            		+ ",cn=users,cn=accounts,dc=sis,dc=cnes,dc=fr"
                   );
            prop.put(Context.SECURITY_CREDENTIALS,
                    UtilsCryptography.decrypt(conf.getString(Consts.LDAP_PWD)));
        } catch (Exception e) {
            LOGGER.error("LDAPAccessImpl getContext: Unable to get Ldap password", e);
        }
        InitialLdapContext context = null;
        try {
            context = new InitialLdapContext(prop, null);
        } catch (NamingException e) {
            LOGGER.error("LDAPAccessImpl getContext: Unable to connect to Ldap", e);
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
            if (null != attId) {
                switch (attId) {
                    case "name": {
                        NamingEnumeration<?> values = att.getAll();
                        while (values.hasMoreElements()) {
                            ldapuser.setFullname(values.next().toString());
                            break;
                        }
                        break;
                    }
                    case "mail": {
                        NamingEnumeration<?> values = att.getAll();
                        while (values.hasMoreElements()) {
                            ldapuser.setEmail(values.next().toString());
                            break;
                        }
                        break;
                    }
                    case "uid": {
                        NamingEnumeration<?> values = att.getAll();
                        while (values.hasMoreElements()) {
                            ldapuser.setUsername(values.next().toString());
                            break;
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return ldapuser;
    }

    @Override
    public boolean authenticateUser(String login, String password) {
        boolean result = false;
        Hashtable<String, String> prop = new Hashtable<>();
        prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        prop.put(Context.PROVIDER_URL, conf.getString(Consts.LDAP_URL));
        prop.put(Context.SECURITY_AUTHENTICATION, "simple");
        prop.put(Context.SECURITY_CREDENTIALS, password);
        prop.put(Context.SECURITY_PRINCIPAL,
                "uid=" + login + ",cn=users,cn=accounts,dc=sis,dc=cnes,dc=fr");
        InitialLdapContext context = null;
        try {
            context = new InitialLdapContext(prop, null);
            result = true;
            return result;
        } catch (NamingException e) {
            LOGGER.error("LDAPAccessImpl getContext: Unable to identify user", e);
            return result;
        } catch (Exception e) {
            LOGGER.error("LDAPAccessImpl getContext: Unexpected exception", e);
            return result;
        } finally {
            try {
                if (context != null) {
                    context.close();
                }
            } catch (NamingException e) {
                LOGGER.error("LDAPAccessImpl getContext: Unable to close context", e);
            }
        }

    }
    
    public List<LDAPUser> getAllDOIProjectMembers(InitialLdapContext context) throws LDAPAccessException {
		try {
			SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {
                "gidNumber",
                 };
            constraints.setReturningAttributes(attrIDs);
            NamingEnumeration answer = context.search("cn=groups,cn=accounts,dc=sis,dc=cnes,dc=fr", "cn=" + conf.getString(Consts.LDAP_PROJECT), constraints);
            List<LDAPUser> members = new ArrayList<LDAPUser>();
            if (answer.hasMore()) {
            	 NamingEnumeration<?> attrs = ((SearchResult) answer.next()).getAttributes().get("gidNumber").getAll();
 			     members = getLdapUsers(context, attrs.next().toString());
            }
 			return members;
	    } catch (Exception e) {
	    	throw new LDAPAccessException("",e);
	    }
	}

private List<LDAPUser> getLdapUsers(DirContext context, String gidNumber) throws NamingException {
		List<LDAPUser> ldapuserList = new ArrayList<LDAPUser>();
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String[] attrIDs = {
                "uid", "mail", "cn"
                 };
		controls.setReturningAttributes(attrIDs);
		
		NamingEnumeration answer = context.search("cn=users,cn=accounts,dc=sis,dc=cnes,dc=fr", "|((gidNumber=" + gidNumber + ")(memberOf=cn=" + conf.getString(Consts.LDAP_PROJECT) + ",cn=groups,cn=accounts,dc=sis,dc=cnes,dc=fr))", controls);
		while (answer.hasMore()) {
			NamingEnumeration<? extends Attribute> attrbs = ((SearchResult) answer.next()).getAttributes().getAll();
			String fullname = null;
			String uid = null;
			String mail = null;
			LDAPUser ldapuser = new LDAPUser();
			while (attrbs.hasMore()) {
				Attribute att = attrbs.next();
				String attId = att.getID();
				if (attId == "cn") {
					NamingEnumeration<?> values = att.getAll();
					while (values.hasMoreElements()) {
						fullname = values.next().toString();
						break;
					}
				} else if (attId == "mail") {
					NamingEnumeration<?> values = att.getAll();
					while (values.hasMoreElements()) {
						mail = values.next().toString();
						break;
					}
				} else if (attId == "uid") {
					NamingEnumeration<?> values = att.getAll();
					while (values.hasMoreElements()) {
						uid = values.next().toString();
						break;
					}
				}
				if ((mail!=null)&&(uid!=null)) {
					ldapuser = new LDAPUser();
					ldapuser.setFullname(fullname);
					ldapuser.setEmail(mail);
					ldapuser.setUsername(uid);
					ldapuserList.add(ldapuser);
				}
			}
		}
		return ldapuserList;
	}
}
