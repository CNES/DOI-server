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

import java.io.File;

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.KerberosCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.ietf.jgss.GSSName;

/**
 * Builder for {@link CloseableHttpClient} instances set up for Proxy SPNego
 * Authentication
 */

public final class ProxyHttpClientBuilder {

    /** */
    private ProxyHttpClientBuilder() {
    }

    /**
     * Initiate an HttpClientBuilder set up for Proxy SPNego Authentication
     * 
     * @param userId
     *            the user Id
     * @param keytabFileName
     *            the path of the keytab file
     * @param ticketCacheFileName
     *            the path of the ticket cache file
     * @param krbConfPath
     *            the path of the kerberos configuration file
     * @param proxyHost
     *            the SPN of the proxy
     * @param proxyPort
     *            the port of the proxy
     * @return an HttpClientBuilder setup for kerberos authentication with the
     *         specified proxy
     */
    public static HttpClientBuilder getHttpClientBuilder(String userId, String keytabFileName,
	    String ticketCacheFileName, String krbConfPath, String proxyHost, int proxyPort) {
	// Init a dummy kerberos credential provider for the kerberized proxy
	final CredentialsProvider credsProvider = new BasicCredentialsProvider();
	credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
		new KerberosCredentials(null));

	// init an register a SPNEGO auth scheme
	final ProxyGSSClient gssClient = new ProxyGSSClient(userId, keytabFileName,
		ticketCacheFileName, new File(krbConfPath));
	final ProxySPNegoSchemeFactory sisfact = new ProxySPNegoSchemeFactory(gssClient, proxyHost,
		GSSName.NT_HOSTBASED_SERVICE);
	final Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder
		.<AuthSchemeProvider>create().register(AuthSchemes.SPNEGO, sisfact).build();

	// Init a http request with this kerberos credential provider
	final HttpClientBuilder builder = HttpClients.custom();
	builder.setDefaultCredentialsProvider(credsProvider);
	builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);

	return builder;
    }
}
