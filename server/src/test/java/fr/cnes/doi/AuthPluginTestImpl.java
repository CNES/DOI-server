/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi;

import fr.cnes.doi.db.model.AuthSystemUser;
import fr.cnes.doi.exception.AuthenticationAccessException;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.plugin.AbstractAuthenticationPluginHelper;
import static fr.cnes.doi.plugin.impl.db.DefaultLDAPImpl.LDAP_ATTR_FULLNAME;
import static fr.cnes.doi.plugin.impl.db.DefaultLDAPImpl.LDAP_ATTR_MAIL;
import static fr.cnes.doi.plugin.impl.db.DefaultLDAPImpl.LDAP_ATTR_USERNAME;
import static fr.cnes.doi.plugin.impl.db.DefaultLDAPImpl.LDAP_PROJECT;
import static fr.cnes.doi.plugin.impl.db.DefaultLDAPImpl.LDAP_PWD;
import static fr.cnes.doi.plugin.impl.db.DefaultLDAPImpl.LDAP_SEARCH_GROUP;
import static fr.cnes.doi.plugin.impl.db.DefaultLDAPImpl.LDAP_SEARCH_USER;
import static fr.cnes.doi.plugin.impl.db.DefaultLDAPImpl.LDAP_URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author malapert
 */
public class AuthPluginTestImpl extends AbstractAuthenticationPluginHelper {
    
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
    
    private final List<AuthSystemUser> authUserList = new ArrayList<>();
    
    private final Map<String, String> credentials = new HashMap<>();
    
    private Map<String, String> conf;
    
    /**
     * Status of the plugin configuration.
     */
    private boolean isConfigured = false;    
    
    public AuthPluginTestImpl() {
        AuthSystemUser user1 = new AuthSystemUser();
        user1.setEmail("doidbuser@mail.com");
        user1.setUsername("malapertjc");
        user1.setFullname("malapertjc");
        
        AuthSystemUser user2 = new AuthSystemUser();
        user2.setEmail("doidbuser@mail.com");
        user2.setUsername("malapert");
        user2.setFullname("malapert");        
     
        AuthSystemUser user3 = new AuthSystemUser();
        user3.setEmail("admin@mail.com");
        user3.setUsername("admin");
        user3.setFullname("admin"); 

        AuthSystemUser user4 = new AuthSystemUser();
        user4.setEmail("norole@mail.com");
        user4.setUsername("norole");
        user4.setFullname("norole"); 
        
        AuthSystemUser user5 = new AuthSystemUser();
        user5.setEmail("kerberos@mail.com");
        user5.setUsername("doi_kerberos");
        user5.setFullname("doi_kerberos"); 

        
        authUserList.add(user1);
        authUserList.add(user2);
        authUserList.add(user3);
        authUserList.add(user4);
        authUserList.add(user5);
        
        
        credentials.put(user1.getUsername(), "admin");
        credentials.put(user2.getUsername(), "pwd");
        credentials.put(user3.getUsername(), "admin");
        credentials.put(user4.getUsername(), "norole");
        credentials.put(user5.getUsername(), "doi_kerberos");
    }
    
    @Override
    public void setConfiguration(Object configuration) {
        this.conf = (Map<String, String>) configuration;
        this.isConfigured = true;
    }   
    

    @Override
    public void initConnection() throws DoiRuntimeException {
    }    

    @Override
    public List<AuthSystemUser> getDOIProjectMembers() throws AuthenticationAccessException {
        return authUserList;
    }

    @Override
    public boolean authenticateUser(String login, String password) {        
        final String pwd = this.credentials.get(login);
        return password.equals(pwd);
    }

    @Override
    public String getDOIAdmin() {
        return "malapertjc";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getOwner() {
        return OWNER;
    }

    @Override
    public String getLicense() {
        return LICENSE;
    }

    @Override
    public StringBuilder validate() {
        final StringBuilder validation = new StringBuilder();
        final String message = "Sets ";
        if(!this.conf.containsKey(LDAP_ATTR_FULLNAME)) {
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

        return validation;
    }
    
    public static boolean isPassword(String key) {
        return LDAP_PWD.equals(key);
    }

    @Override
    public void release() {
        this.conf = null; 
        this.isConfigured = false;
    }

    @Override
    public boolean isConfigured() {
        return this.isConfigured;
    }
    
}
