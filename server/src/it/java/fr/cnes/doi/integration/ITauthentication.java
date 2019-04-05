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
package fr.cnes.doi.integration;

import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.exception.AuthenticationAccessException;
import fr.cnes.doi.db.model.AuthSystemUser;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.util.List;
import static org.junit.Assert.assertTrue;
import fr.cnes.doi.db.IAuthenticationDBHelper;
import fr.cnes.doi.plugin.PluginFactory;
import fr.cnes.doi.plugin.impl.db.DefaultLDAPImpl;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(IntegrationTest.class)
public class ITauthentication {

    private static Client cl;
    private static boolean isDatabaseConfigured;

    public static final String DOI = "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b";
    
    private static String userAdmin;
    private static String password;

    public ITauthentication() {
    }

    @BeforeClass
    public static void setUpClass() throws ClientMdsException {
	try {
	    isDatabaseConfigured = true;
	    InitServerForTest.init(InitSettingsForTest.CONFIG_IT_PROPERTIES);
	    cl = new Client(new Context(), Protocol.HTTPS);
	    Series<Parameter> parameters = cl.getContext().getParameters();
	    parameters.set(RESTLET_MAX_TOTAL_CONNECTIONS,
		    DoiSettings.getInstance().getString(
			    fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS,
			    DEFAULT_MAX_TOTAL_CONNECTIONS));
	    parameters.set(RESTLET_MAX_CONNECTIONS_PER_HOST,
		    DoiSettings.getInstance().getString(
			    fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST,
			    DEFAULT_MAX_CONNECTIONS_PER_HOST));
	    parameters.add("truststorePath", JKS_DIRECTORY + File.separatorChar + JKS_FILE);
	    parameters.add("truststorePassword",
		    DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
	    parameters.add("truststoreType", "JKS");
            userAdmin = DoiSettings.getInstance().getString(DefaultLDAPImpl.LDAP_DOI_ADMIN);
            password = System.getProperty("doi-admin-pwd");
	} catch (Error ex) {
	    isDatabaseConfigured = false;
	}
    }

    @AfterClass
    public static void tearDownClass() {
	try {
	    InitServerForTest.close();
	} catch (Error ex) {

	}
    }

    @Before
    public void setUp() {
	Assume.assumeTrue("Database is not configured, please configure it and rerun the tests",
		isDatabaseConfigured);
    }

    @After
    public void tearDown() {

    }
    
    @Test
    public void testLDAPWithDoiGroup() throws AuthenticationAccessException {
        IAuthenticationDBHelper ldapaccessservice = PluginFactory.getAuthenticationSystem();
        List<AuthSystemUser> ldap = ldapaccessservice.getDOIProjectMembers();
        assertTrue(!ldap.isEmpty());
    }
    
    @Test
    public void testLDAPAuthentication() throws AuthenticationAccessException {
        IAuthenticationDBHelper ldapaccessservice = PluginFactory.getAuthenticationSystem();
        boolean isAuthenticated = ldapaccessservice.authenticateUser(userAdmin,password);
        assertTrue(isAuthenticated);
    }    

}
