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
import fr.cnes.doi.ldap.util.LDAPUser;
import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.ldap.service.ILDAPAccessService;

/**
 * Implementation of the LDAP.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class LDAPAccessServiceImpl implements ILDAPAccessService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(LDAPAccessServiceImpl.class.getName());
    /**
     * Configuration file.
     */
    private final DoiSettings conf = DoiSettings.getInstance();

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LDAPUser> getDOIProjectMembers() throws LDAPAccessException {
        LOGGER.traceEntry();
        DirContext context = null;
        try {
            context = getContext();
            if (context == null) {
                throw new LDAPAccessException("Configuration problem with the LDAP", new Exception());
            } else {
                return LOGGER.traceExit(getAllDOIProjectMembers((InitialLdapContext) context));
            }
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    LOGGER.warn("LDAPAccessImpl getDOIProjectMembers: Unable to close the context",
                            e);
                }
            }
        }
    }

    /**
     * Init LDAP context.
     *
     * @return the context or null when a LDAP configuration is missing
     */
    private InitialLdapContext getContext() {
        LOGGER.traceEntry();
        final Hashtable<String, String> prop = new Hashtable<>();
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
        return LOGGER.traceExit(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authenticateUser(final String login, final String password) {
        final Hashtable<String, String> prop = new Hashtable<>();
        prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        prop.put(Context.PROVIDER_URL, conf.getString(Consts.LDAP_URL));
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
            LOGGER.error("LDAPAccessImpl getContext: Unable to identify user", e);
            isAuthenticate = false;
        } catch (Exception e) {
            LOGGER.error("LDAPAccessImpl getContext: Unexpected exception", e);
            isAuthenticate = false;
        } finally {
            try {
                if (context != null) {
                    context.close();
                }
            } catch (NamingException e) {
                LOGGER.error("LDAPAccessImpl getContext: Unable to close context", e);
            }
        }
        return isAuthenticate;
    }

    /**
     * Search on LDAP all users which are in the group Consts.LDAP_PROJECT.
     *
     * @param context context
     * @return all LDAP users which are in the group Consts.LDAP_PROJECT
     * @throws LDAPAccessException Exception
     */
    public List<LDAPUser> getAllDOIProjectMembers(final InitialLdapContext context)
            throws LDAPAccessException {
        try {
            LOGGER.traceEntry("Parameters : {}", context);
            final SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            final String[] attrIDs = {"gidNumber",};
            constraints.setReturningAttributes(attrIDs);
            final NamingEnumeration answer = context.search(
                    "cn=groups,cn=accounts,dc=sis,dc=cnes,dc=fr",
                    "cn=" + conf.getString(Consts.LDAP_PROJECT), constraints);
            final List<LDAPUser> members = new ArrayList<>();
            if (answer.hasMore()) {
                NamingEnumeration<?> attrs = ((SearchResult) answer.next()).getAttributes().get(
                        "gidNumber").getAll();
                members.addAll(getLdapUsers(context, attrs.next().toString()));
            }
            return LOGGER.traceExit(members);
        } catch (NamingException e) {
            LOGGER.error(e);
            throw new LDAPAccessException("", e);
        }
    }

    /**
     * Search on LDAP all users which are in the group Consts.LDAP_PROJECT in a LDAP group
     *
     * @param context context
     * @param gidNumber LDAP group ID
     * @return all LDAP users which are in the group Consts.LDAP_PROJECT for a specific LDAP group
     * @throws NamingException Exception
     */
    private List<LDAPUser> getLdapUsers(final DirContext context, final String gidNumber)
            throws NamingException {
        LOGGER.traceEntry("Parameters : {}", context, gidNumber);
        final List<LDAPUser> ldapuserList = new ArrayList<>();
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        final String[] attrIDs = {"uid", "mail", "cn"};
        controls.setReturningAttributes(attrIDs);

        final NamingEnumeration answer = context.search("cn=users,cn=accounts,dc=sis,dc=cnes,dc=fr",
                "(|(gidNumber=" + gidNumber + ")(memberOf=cn=" + conf.getString(Consts.LDAP_PROJECT)
                + ",cn=groups,cn=accounts,dc=sis,dc=cnes,dc=fr))",
                controls);
        while (answer.hasMore()) {
            final NamingEnumeration<? extends Attribute> attrbs = ((SearchResult) answer.next())
                    .getAttributes().getAll();
            String fullname = null;
            String uid = null;
            String mail = null;
            while (attrbs.hasMore()) {
                final Attribute att = attrbs.next();
                final String attId = att.getID();
                if (null != attId) {
                    switch (attId) {
                        case "cn": {
                            final NamingEnumeration<?> values = att.getAll();
                            while (values.hasMoreElements()) {
                                fullname = values.next().toString();
                                break;
                            }
                            break;
                        }
                        case "mail": {
                            final NamingEnumeration<?> values = att.getAll();
                            while (values.hasMoreElements()) {
                                mail = values.next().toString();
                                break;
                            }
                            break;
                        }
                        case "uid": {
                            final NamingEnumeration<?> values = att.getAll();
                            while (values.hasMoreElements()) {
                                uid = values.next().toString();
                                break;
                            }
                            break;
                        }
                        default:
                            break;
                    }
                }
            }
            if ((mail != null) && (uid != null)) {
                final LDAPUser ldapuser = new LDAPUser();
                ldapuser.setFullname(fullname);
                ldapuser.setEmail(mail);
                ldapuser.setUsername(uid);
                ldapuserList.add(ldapuser);
                LOGGER.debug("Create LDAP user : {}", ldapuser);
            }
        }
        return LOGGER.traceExit(ldapuserList);
    }
}
