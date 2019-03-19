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

import fr.cnes.doi.exception.LDAPAccessException;
import fr.cnes.doi.ldap.model.LDAPUser;
import fr.cnes.doi.security.UtilsCryptography;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.ldap.service.ILDAPAccessService;
import fr.cnes.doi.utils.Utils;

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
        final String ldapUser = UtilsCryptography.decrypt(conf.getString(Consts.LDAP_USER));
        final String ldapPwd = UtilsCryptography.decrypt(conf.getString(Consts.LDAP_PWD));
        final String securityPrincipal = String.format(
                "uid=%s,%s",
                ldapUser, conf.getString(Consts.LDAP_SEARCH_USER)
        );
        final String ldapUrl = conf.getString(Consts.LDAP_URL);
        prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        prop.put(Context.PROVIDER_URL, ldapUrl);
        prop.put(Context.SECURITY_AUTHENTICATION, "simple");
        prop.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        prop.put(Context.SECURITY_CREDENTIALS, ldapPwd);

        LOGGER.info("LDAP context:\n  {}={}\n  {}={}\n  {}={}\n  {}={}\n  {}={}",
                Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory",
                Context.PROVIDER_URL, ldapUrl,
                Context.SECURITY_AUTHENTICATION, "simple",
                Context.SECURITY_PRINCIPAL, securityPrincipal,
                Context.SECURITY_CREDENTIALS, Utils.transformPasswordToStars(ldapPwd));

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
        final String securityPrincipal = String.format(
                "uid=%s,%s",
                UtilsCryptography.decrypt(conf.getString(Consts.LDAP_DOI_ADMIN)),
                conf.getString(Consts.LDAP_SEARCH_USER));
        final String ldapUrl = conf.getString(Consts.LDAP_URL);
        prop.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        prop.put(Context.PROVIDER_URL, ldapUrl);
        prop.put(Context.SECURITY_AUTHENTICATION, "simple");
        prop.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        prop.put(Context.SECURITY_CREDENTIALS, password);

        LOGGER.info("LDAP authentication:\n  {}={}\n  {}={}\n  {}={}\n  {}={}\n  {}={}",
                Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory",
                Context.PROVIDER_URL, ldapUrl,
                Context.SECURITY_AUTHENTICATION, "simple",
                Context.SECURITY_PRINCIPAL, securityPrincipal,
                Context.SECURITY_CREDENTIALS, Utils.transformPasswordToStars(password));

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
        LOGGER.info("LDAP authentication: {}", isAuthenticate);
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
            final String searchGroup = conf.getString(Consts.LDAP_SEARCH_GROUP);
            final String ldapProject = conf.getString(Consts.LDAP_PROJECT);

            final SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            final String[] attrIDs = {"gidNumber",};
            constraints.setReturningAttributes(attrIDs);

            LOGGER.info("LDAP search({},{},{})", searchGroup, "cn=" + ldapProject, constraints);
            final NamingEnumeration answer = context.search(searchGroup, "cn=" + ldapProject,
                    constraints);
            LOGGER.info("LDAP search : OK");
            final List<LDAPUser> members = new ArrayList<>();
            if (answer.hasMore()) {
                final NamingEnumeration<?> attrs = ((SearchResult) answer.next()).getAttributes().
                        get("gidNumber").getAll();
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
        final String[] attrIDs = {
            conf.getString(Consts.LDAP_ATTR_USERNAME),
            conf.getString(Consts.LDAP_ATTR_MAIL),
            conf.getString(Consts.LDAP_ATTR_FULLNAME)
        };
        LOGGER.info("Getting attributes from LDAP: {}", (Object[]) attrIDs);
        controls.setReturningAttributes(attrIDs);        

        final String ldapProject = conf.getString(Consts.LDAP_PROJECT);
        final String ldapSearchGroup = conf.getString(Consts.LDAP_SEARCH_GROUP);
        final String ldapSearchUser = conf.getString(Consts.LDAP_SEARCH_USER);
        final String ldapSearchAttr = String.format(
                "(|(gidNumber=%s)(memberOf=cn=%s,%s))",
                gidNumber, ldapProject, ldapSearchGroup
        );

        LOGGER.info("LDAP search({},{},{}", ldapSearchUser, ldapSearchAttr, controls);
        final NamingEnumeration answer = context.search(ldapSearchUser, ldapSearchAttr, controls);
        LOGGER.info("LDAP search : OK");
        while (answer.hasMore()) {
            final NamingEnumeration<? extends Attribute> attrbs = ((SearchResult) answer.next())
                    .getAttributes().getAll();
            String fullname = null;
            String uid = null;
            String mail = null;
            while (attrbs.hasMore()) {
                final Attribute att = attrbs.next();
                final String attId = att.getID();
                if (attrIDs[0].equals(attId)) {
                    final NamingEnumeration<?> values = att.getAll();
                    while (values.hasMoreElements()) {
                        uid = values.next().toString();
                        break;
                    }
                } else if (attrIDs[1].equals(attId)) {
                    final NamingEnumeration<?> values = att.getAll();
                    while (values.hasMoreElements()) {
                        mail = values.next().toString();
                        break;
                    }
                } else if (attrIDs[2].equals(attId)) {
                    final NamingEnumeration<?> values = att.getAll();
                    while (values.hasMoreElements()) {
                        fullname = values.next().toString();
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
