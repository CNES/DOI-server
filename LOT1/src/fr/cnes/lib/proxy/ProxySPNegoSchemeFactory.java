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
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.ietf.jgss.Oid;

/**
 * {@link AuthSchemeProvider} implementation that creates and initializes
 * {@link ProxySPNegoScheme} instances.
 */

@Contract(threading = ThreadingBehavior.IMMUTABLE)
@SuppressWarnings("deprecation")
public class ProxySPNegoSchemeFactory implements AuthSchemeFactory, AuthSchemeProvider {

    /** The gss client with all the client configuration */
    private ProxyGSSClient gssClient;
    
    /** The SPN (proxy host) */
    private String servicePrincipalName;
    
    /** The oid representing the type of service name form */
    private Oid servicePrincipalOid;

    /**
     * Init a ProxySPNegoSchemeFactory
     * 
     * @param gssClient
     *            the gss client with all the client configuration
     * @param servicePrincipalName
     *            the SPN (proxy host)
     * @param servicePrincipalOid
     *            the oid representing the type of service name form
     */
    public ProxySPNegoSchemeFactory(ProxyGSSClient gssClient, String servicePrincipalName,
	    Oid servicePrincipalOid) {
	super();
	this.gssClient = gssClient;
	this.servicePrincipalName = servicePrincipalName;
	this.servicePrincipalOid = servicePrincipalOid;
    }

    @Override
    public AuthScheme newInstance(final HttpParams params) {
	return new ProxySPNegoScheme(gssClient, servicePrincipalName, servicePrincipalOid);
    }

    @Override
    public AuthScheme create(final HttpContext context) {
	return new ProxySPNegoScheme(gssClient, servicePrincipalName, servicePrincipalOid);
    }

}
