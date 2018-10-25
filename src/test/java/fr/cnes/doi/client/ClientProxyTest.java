/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.client;

import static fr.cnes.doi.AbstractSpec.classTitle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.doi.InitSettingsForTest;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.HttpClientHelperPatch;
import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class ClientProxyTest {

    /**
     * Init the settings
     */
    @BeforeClass
    public static void setUpClass() {
        InitSettingsForTest.init();
        classTitle("ClientProxy");
    }

    /**
     * Executed after the test class
     */
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Executed before each test
     */
    @Before
    public void setUp() {
    }

    /**
     * Executed after each test
     */
    @After
    public void tearDown() {
    }

    /**
     * Test the connection through the proxy. Works only if the test is executed
     * behind a proxy
     *
     * @throws Exception - if OutOfMemoryErrors
     */
    @Test
    @Ignore
    public void testProxy() throws Exception {

        // Execute only if proxy is enabled
        if (DoiSettings.getInstance().getBoolean(Consts.SERVER_PROXY_USED)) {
            Engine.getInstance().getRegisteredClients().clear();
            Engine.getInstance().getRegisteredClients().add(new HttpClientHelperPatch(null));
            Client proxy = new Client(new Context(), Protocol.HTTP);
            Series<Parameter> parameters = proxy.getContext().getParameters();            
            parameters.set(RESTLET_MAX_TOTAL_CONNECTIONS, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS, "-1"));
            parameters.set(RESTLET_MAX_CONNECTIONS_PER_HOST, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST, "-1"));            
            parameters.add("proxyHost",DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST));
            parameters.add("proxyPort",DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT));

            ClientResource client = new ClientResource("http://www.google.fr");
            client.setProxyChallengeResponse(ChallengeScheme.HTTP_BASIC,
                    DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN),
                    DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD));
            client.setNext(proxy);

            Representation rep = client.get();
            Status status = client.getStatus();
            Assert.assertTrue("Test si la requete est OK", status.isSuccess());

            System.out.println(rep.getText());
        } else {
            System.out.println("Proxy not enabled, no test");
            Assert.assertTrue("No test executed", true);
        }

    }

    /**
     * Test the connection through the proxy with BaseClient. Works only if the
     * test is executed behind a proxy
     *
     * @throws Exception - if OutOfMemoryErrors
     */
    @Test
    @Ignore
    public void testBaseClient() throws Exception {
        BaseClient baseClient = new BaseClient("http://www.google.fr");
        if (DoiSettings.getInstance().getBoolean(Consts.SERVER_PROXY_USED)) {
            baseClient.setProxyAuthentication(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST),
                    DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT),
                    DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN),
                    DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD));
        }

        Representation rep = baseClient.getClient().get();
        Status status = baseClient.getClient().getStatus();
        Assert.assertTrue("Test si la requete est OK", status.isSuccess());

        System.out.println(rep.getText());

    }

    /**
     * Test the connection through the proxy with BaseClient and crossCite URL.
     * Works only if the test is executed behind a proxy
     *
     * @throws Exception - if OutOfMemoryErrors
     */
    @Test
    @Ignore
    public void testBaseClientCrossCite() throws Exception {
        BaseClient baseClient = new BaseClient("https://citation.crosscite.org/styles");
        if (DoiSettings.getInstance().getBoolean(Consts.SERVER_PROXY_USED)) {
            baseClient.setProxyAuthentication(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST),
                    DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT),
                    DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN),
                    DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD));
        }

        Representation rep = baseClient.getClient().get();
        Status status = baseClient.getClient().getStatus();
        Assert.assertTrue("Test si la requete est OK", status.isSuccess());
    }

    @Test
    @Ignore
    public void testProxyWhithHttp() throws IOException {
        HttpHost proxy = new HttpHost(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST), Integer.valueOf(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT)), "http");

        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.getCredentialsProvider().setCredentials(
                new AuthScope(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST), Integer.valueOf(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT))),
                new UsernamePasswordCredentials(DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN), DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD))
        );
        try {

            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
                    proxy);

            HttpGet req = new HttpGet("https://www.google.fr");

            System.out.println("executing request to " + req + " via "
                    + proxy);
            HttpResponse rsp = httpclient.execute(req);
            HttpEntity entity = rsp.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(rsp.getStatusLine());
            Header[] headers = rsp.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                System.out.println(headers[i]);
            }
            System.out.println("----------------------------------------");

            if (entity != null) {
                System.out.println(EntityUtils.toString(entity));
            }

        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }

    }

}
