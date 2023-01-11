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
package fr.cnes.doi.plugin.impl.db;

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

import fr.cnes.doi.exception.AuthenticationAccessException;
import fr.cnes.doi.db.IAuthenticationDBHelper;
import fr.cnes.doi.db.model.AuthSystemUser;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.utils.Utils;
import fr.cnes.doi.plugin.AbstractAuthenticationPluginHelper;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the LDAP.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class DefaultLDAPImpl extends AbstractAuthenticationPluginHelper {

    /**
     * LDAP url
     */
    public static final String LDAP_URL = "Starter.LDAP.url";

    /**
     * LDAP user
     */
    public static final String LDAP_USER = "Starter.LDAP.user";

    /**
     * LDAP pwd
     */
    public static final String LDAP_PWD = "Starter.LDAP.password";

    /**
     * LDAP project
     */
    public static final String LDAP_PROJECT = "Starter.LDAP.project";

    /**
     * LDAP username who is the administrator of the DOI server
     */
    public static final String LDAP_DOI_ADMIN = "Starter.LDAP.user.admin";

    /**
     * Specifies the filter expression to get the group.
     */
    public static final String LDAP_SEARCH_GROUP = "Starter.LDAP.search.group";

    /**
     * Specifies the filter expression to get the users.
     */
    public static final String LDAP_SEARCH_USER = "Starter.LDAP.search.user";

    /**
     * Attributes name in LDAP for username.
     */
    public static final String LDAP_ATTR_USERNAME = "Starter.LDAP.attr.username";

    /**
     * Attributes name in LDAP for mail.
     */
    public static final String LDAP_ATTR_MAIL = "Starter.LDAP.attr.mail";

    /**
     * Attributes name in LDAP for fullname.
     */
    public static final String LDAP_ATTR_FULLNAME = "Starter.LDAP.attr.fullname";

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(DefaultLDAPImpl.class.getName());

    /**
     * Plugin description.
     */
    private static final String DESCRIPTION = "Provides a pre-defined list of users and groups";
    /**
     * Plugin version.
     */
    private static final String VERSION = "1.0.0";
    /**
     * Plugin owner.
     */
    private static final String OWNER = "CNES";
    /**
     * Plugin author.
     */
    private static final String AUTHOR = "Jean-Christophe Malapert";
    /**
     * Plugin license.
     */
    private static final String LICENSE = "LGPLV3";
    /**
     * Plugin name.
     */
    private final String NAME = this.getClass().getName();

    /**
     * Configuration file.
     */
    private Map<String, String> conf;

    /**
     * Status of the plugin configuration.
     */
    private boolean configured = false;

    /**
     * {@inheritDoc }
     */
    @Override
    public void setConfiguration(final Object configuration) {
        this.conf = (Map<String, String>) configuration;
        LOGGER.info("[CONF] Plugin LDAP URL : {}", this.conf.get(LDAP_URL));
        LOGGER.info("[CONF] Plugin LDAP user : {}", this.conf.get(LDAP_USER));
        LOGGER.info("[CONF] Plugin LDAP password : {}", Utils.transformPasswordToStars(this.conf.
                get(LDAP_PWD)));
        LOGGER.info("[CONF] Plugin LDAP project : {}", this.conf.get(LDAP_PROJECT));
        LOGGER.info("[CONF] Plugin LDAP admin for DOI : {}", this.conf.get(LDAP_DOI_ADMIN));
        LOGGER.info("[CONF] Plugin LDAP attribute for fullname : {}", this.conf.get(
                LDAP_ATTR_FULLNAME));
        LOGGER.info("[CONF] Plugin LDAP attribute for mail : {}", this.conf.get(LDAP_ATTR_MAIL));
        LOGGER.info("[CONF] Plugin LDAP attribute for username : {}", this.conf.get(
                LDAP_ATTR_USERNAME));
        LOGGER.info("[CONF] Plugin LDAP search group : {}", this.conf.get(LDAP_SEARCH_GROUP));
        LOGGER.info("[CONF] Plugin LDAP search user : {}", this.conf.get(LDAP_SEARCH_USER));
        this.configured = true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void initConnection() throws DoiRuntimeException {
        try {
            final InitialLdapContext context = getContext();
            if (context == null) {
                throw new DoiRuntimeException("LDAPAccessImpl: Unable to connect to Ldap");
            } else {
                context.close();
            }
        } catch (NamingException ex) {
            throw new DoiRuntimeException("LDAPAccessImpl: Unable to connect to Ldap", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuthSystemUser> getDOIProjectMembers() throws AuthenticationAccessException {
        LOGGER.traceEntry();
        DirContext context = null;
        try {
            try {
                context = getContext();
            } catch (NamingException ex) {
                LOGGER.error("LDAPAccessImpl getContext: Unable to connect to Ldap", ex);
            }
            if (context == null) {
                throw new AuthenticationAccessException("Configuration problem with the LDAP",
                        new Exception());
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
     * Returns true when the LDAP context is well configured.
     *
     * @return true when the LDAP context is well configured otherwise false
     */
    private boolean isLdapConfigured() {
        final String ldapUser = conf.getOrDefault(LDAP_USER, "");
        final String ldapPwd = conf.getOrDefault(LDAP_PWD, "");
        final String ldapSearchUser = conf.getOrDefault(LDAP_SEARCH_USER, "");
        return !ldapUser.isEmpty() && !ldapPwd.isEmpty() && !ldapSearchUser.isEmpty();
    }

    /**
     * Init LDAP context.
     *
     * @return the context or null when a LDAP configuration is missing
     * @throws NamingException Unable to connect to Ldap
     */
    private InitialLdapContext getContext() throws NamingException {
        LOGGER.traceEntry();
        InitialLdapContext context = null;
        if (isLdapConfigured()) {
            final Hashtable<String, String> prop = new Hashtable<>();
            final String ldapUser = conf.get(LDAP_USER);
            final String ldapPwd = DoiSettings.getInstance().getSecretValue(conf.get(LDAP_PWD));
            final String securityPrincipal = ldapUser;
            final String ldapUrl = conf.get(LDAP_URL);
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
            context = new InitialLdapContext(prop, null);
        } else {
            LOGGER.error("LDAP is not well configured. Checks the configuration file");
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
                login,
                conf.get(LDAP_SEARCH_USER));
        final String ldapUrl = conf.get(LDAP_URL);
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
     * @throws AuthenticationAccessException Exception
     */
    public List<AuthSystemUser> getAllDOIProjectMembers(final InitialLdapContext context)
            throws AuthenticationAccessException {
        try {
            LOGGER.traceEntry("Parameters : {}", context);
            final String searchGroup = conf.get(LDAP_SEARCH_GROUP);
            final String ldapProject = conf.get(LDAP_PROJECT);

            final SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            final String[] attrIDs = {"gidNumber",};
            constraints.setReturningAttributes(attrIDs);

            LOGGER.info("LDAP search({},{},{})", searchGroup, "cn=" + ldapProject, constraints);
            final NamingEnumeration answer = context.search(searchGroup, "cn=" + ldapProject,
                    constraints);
            LOGGER.info("LDAP search : OK");
            final List<AuthSystemUser> members = new ArrayList<>();
            if (answer.hasMore()) {
                final NamingEnumeration<?> attrs = ((SearchResult) answer.next()).getAttributes().
                        get("gidNumber").getAll();
                members.addAll(getLdapUsers(context, attrs.next().toString()));
            }
            return LOGGER.traceExit(members);
        } catch (NamingException e) {
            LOGGER.error(e);
            throw new AuthenticationAccessException("", e);
        }
    }

    /**
     * Search on LDAP all users which are in the group Consts.LDAP_PROJECT in a
     * LDAP group
     *
     * @param context context
     * @param gidNumber LDAP group ID
     * @return all LDAP users which are in the group Consts.LDAP_PROJECT for a
     * specific LDAP group
     * @throws NamingException Exception
     */
    private List<AuthSystemUser> getLdapUsers(final DirContext context, final String gidNumber)
            throws NamingException {
        LOGGER.traceEntry("Parameters : {}", context, gidNumber);
        final List<AuthSystemUser> ldapuserList = new ArrayList<>();
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        final String[] attrIDs = {
            conf.get(LDAP_ATTR_USERNAME),
            conf.get(LDAP_ATTR_MAIL),
            conf.get(LDAP_ATTR_FULLNAME)
        };
        LOGGER.info("Getting attributes from LDAP: {}", (Object[]) attrIDs);
        controls.setReturningAttributes(attrIDs);

        final String ldapProject = conf.get(LDAP_PROJECT);
        final String ldapSearchGroup = conf.get(LDAP_SEARCH_GROUP);
        final String ldapSearchUser = conf.get(LDAP_SEARCH_USER);
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
                final AuthSystemUser ldapuser = new AuthSystemUser();
                ldapuser.setFullname(fullname);
                ldapuser.setEmail(mail);
                ldapuser.setUsername(uid);
                ldapuserList.add(ldapuser);
                LOGGER.debug("Create LDAP user : {}", ldapuser);
            }
        }
        return LOGGER.traceExit(ldapuserList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOwner() {
        return OWNER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLicense() {
        return LICENSE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDOIAdmin() {
        return conf.get(LDAP_DOI_ADMIN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuilder validate() {
        final StringBuilder validation = new StringBuilder();
        final String message = "Sets ";
        if (!this.conf.containsKey(LDAP_ATTR_FULLNAME)) {
            validation.append(message).append(LDAP_ATTR_FULLNAME).append("\n");
        }
        if (!this.conf.containsKey(LDAP_ATTR_MAIL)) {
            validation.append(message).append(LDAP_ATTR_MAIL).append("\n");
        }
        if (!this.conf.containsKey(LDAP_ATTR_USERNAME)) {
            validation.append(message).append(LDAP_ATTR_USERNAME).append("\n");
        }
        if (!this.conf.containsKey(LDAP_PROJECT)) {
            validation.append(message).append(LDAP_PROJECT).append("\n");
        }
        if (!this.conf.containsKey(LDAP_URL)) {
            validation.append(message).append(LDAP_URL).append("\n");
        }
        if (!this.conf.containsKey(LDAP_SEARCH_GROUP)) {
            validation.append(message).append(LDAP_SEARCH_GROUP).append("\n");
        }
        if (!this.conf.containsKey(LDAP_SEARCH_USER)) {
            validation.append(message).append(LDAP_SEARCH_USER).append("\n");
        }
        if (!this.conf.containsKey(LDAP_USER)) {
            validation.append(message).append(LDAP_USER).append("\n");
        }
        if (!this.conf.containsKey(LDAP_PWD)) {
            validation.append(message).append(LDAP_PWD).append("\n");
        }
        return validation;
    }

    /**
     * Checks if the keyword is a password.
     *
     * @param key keyword to check
     * @return True when the keyword is a password otherwise False
     */
    public static boolean isPassword(final String key) {
        return LDAP_PWD.equals(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        this.configured = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigured() {
        return this.configured;
    }
    
    /**
     * FOR TESTING
     * @param args
     */
    public static void main(String[] args) {
    	
    	ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();
    	settings.put(Consts.PLUGIN_USER_GROUP_MGT, "fr.cnes.doi.plugin.impl.db.DefaultUserRoleImpl");
    	settings.put(Consts.PLUGIN_PROJECT_SUFFIX, "fr.cnes.doi.plugin.impl.db.DefaultProjectSuffixImpl");
    	settings.put(Consts.PLUGIN_TOKEN, "fr.cnes.doi.plugin.impl.db.DefaultTokenImpl");
    	settings.put(Consts.PLUGIN_AUTHENTICATION, "fr.cnes.doi.plugin.impl.db.DefaultLDAPImpl");
    	
    	PluginFactory.init(settings);
    	PluginFactory.getAuthenticationSystem();

	}
}
