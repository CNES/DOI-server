/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
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
    private static final Logger LOGGER = LogManager
	    .getLogger(LDAPAccessServiceImpl.class.getName());
    /**
     * Configuration file.
     */
    private final DoiSettings conf = DoiSettings.getInstance();

    @Override
    public List<LDAPUser> getDOIProjectMembers() throws LDAPAccessException {
	DirContext context = null;
	try {
	    context = getContext();
	    return getAllDOIProjectMembers((InitialLdapContext) context);
	} finally {
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
	    prop.put(Context.SECURITY_PRINCIPAL,
		    "uid=" + UtilsCryptography.decrypt(conf.getString(Consts.LDAP_USER))
			    + ",cn=users,cn=accounts,dc=sis,dc=cnes,dc=fr");
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

    public List<LDAPUser> getAllDOIProjectMembers(InitialLdapContext context)
	    throws LDAPAccessException {
	try {
	    SearchControls constraints = new SearchControls();
	    constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    String[] attrIDs = { "gidNumber", };
	    constraints.setReturningAttributes(attrIDs);
	    NamingEnumeration answer = context.search("cn=groups,cn=accounts,dc=sis,dc=cnes,dc=fr",
		    "cn=" + conf.getString(Consts.LDAP_PROJECT), constraints);
	    List<LDAPUser> members = new ArrayList<LDAPUser>();
	    if (answer.hasMore()) {
		NamingEnumeration<?> attrs = ((SearchResult) answer.next()).getAttributes()
			.get("gidNumber").getAll();
		members = getLdapUsers(context, attrs.next().toString());
	    }
	    return members;
	} catch (Exception e) {
	    throw new LDAPAccessException("", e);
	}
    }

    private List<LDAPUser> getLdapUsers(DirContext context, String gidNumber)
	    throws NamingException {
	List<LDAPUser> ldapuserList = new ArrayList<LDAPUser>();
	SearchControls controls = new SearchControls();
	controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	String[] attrIDs = { "uid", "mail", "cn" };
	controls.setReturningAttributes(attrIDs);

	NamingEnumeration answer = context.search("cn=users,cn=accounts,dc=sis,dc=cnes,dc=fr",
		"(|(gidNumber=" + gidNumber + ")(memberOf=cn=" + conf.getString(Consts.LDAP_PROJECT)
			+ ",cn=groups,cn=accounts,dc=sis,dc=cnes,dc=fr))",
		controls);
	while (answer.hasMore()) {
	    final NamingEnumeration<? extends Attribute> attrbs = ((SearchResult) answer.next())
		    .getAttributes().getAll();
	    String fullname = null;
	    String uid = null;
	    String mail = null;
	    LDAPUser ldapuser = new LDAPUser();
	    while (attrbs.hasMore()) {
		final Attribute att = attrbs.next();
		final String attId = att.getID();
		if ("cn".equals(attId)) {
		    final NamingEnumeration<?> values = att.getAll();
		    while (values.hasMoreElements()) {
			fullname = values.next().toString();
			break;
		    }
		} else if ("mail".equals(attId)) {
		    final NamingEnumeration<?> values = att.getAll();
		    while (values.hasMoreElements()) {
			mail = values.next().toString();
			break;
		    }
		} else if ("uid".equals(attId)) {
		    final NamingEnumeration<?> values = att.getAll();
		    while (values.hasMoreElements()) {
			uid = values.next().toString();
			break;
		    }
		}
		if ((mail != null) && (uid != null)) {
		    ldapuser = new LDAPUser();
		    ldapuser.setFullname(fullname);
		    ldapuser.setEmail(mail);
		    ldapuser.setUsername(uid);
		}
	    }
	    ldapuserList.add(ldapuser);
	}
	return ldapuserList;
    }
}
