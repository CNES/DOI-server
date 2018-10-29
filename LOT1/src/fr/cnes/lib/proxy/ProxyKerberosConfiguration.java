package fr.cnes.lib.proxy;

/*
 * Copyright [2018] [S. ETCHEVERRY (CNES)]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/**
 * This class removes the need for a jaas.conf file to configure the
 * com.sun.security.auth.module.Krb5LoginModule to be used for JAAS login for
 * Kerberos client (initiators).
 */
public class ProxyKerberosConfiguration extends Configuration {

    /** */
    private static final String LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule";
    /** */
    private AppConfigurationEntry[] appConfigEntries;

    /** */
    private String strTrue = "true";
    // /** */
    // private String strFalse = "false";

    /** The user ID */
    private String principalName;

    /** */
    private Map<String, String> options = new HashMap<String, String>();

    /**
     * @param principalName
     *            The user ID
     */
    public ProxyKerberosConfiguration(String principalName) {

	this.principalName = principalName;
    }

    /**
     * Set up kerberos configuration to use a keytab to initiate TGT
     * 
     * @param keytabFilename
     *            The path of the keytab file
     */
    public void setKeytab(String keytabFilename) {

	options.put("useKeyTab", strTrue);
	options.put("keyTab", keytabFilename);
	options.put("doNotPrompt", strTrue);
    }

    /**
     * Set up kerberos configuration to use the ticket cache file to retrieve TGT
     * from cache
     * 
     * @param ticketCacheFileName
     *            The path of the ticket cache file
     */
    public void setTicketCache(String ticketCacheFileName) {
	// first use kerberos cache as specified
	options.put("useTicketCache", strTrue);
	options.put("ticketCache", ticketCacheFileName);
    }

    /**
     * Initialize the kerberos configuration
     */
    public void initialize() {

	options.put("debug", strTrue);
	options.put("principal", principalName); // Ensure the correct TGT is used.
	options.put("refreshKrb5Config", strTrue);

	appConfigEntries = new AppConfigurationEntry[1];
	appConfigEntries[0] = new AppConfigurationEntry(LOGIN_MODULE,
		AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);

	Security.setProperty("login.configuration.provider", getClass().getName());

	// For each Kerberos client that the loads we
	// need a separate instance of this class, it gets set here, so next call
	// on the LoginContext will use this instance.
	setConfiguration(this);
    }

    /**
     * (non-Javadoc)
     * 
     * @param arg0
     *            the name used as the index into the Configuration
     * @return The appConfigEntries
     * @see javax.security.auth.login.Configuration#getAppConfigurationEntry(java.lang.String)
     */
    public AppConfigurationEntry[] getAppConfigurationEntry(String arg0) {

	return appConfigEntries;
    }

}
