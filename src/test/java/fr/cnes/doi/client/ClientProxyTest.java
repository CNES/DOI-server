/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

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
import org.restlet.ext.httpclient.HttpClientHelper;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class ClientProxyTest {

    /**
     * Init the settings
     */
    @BeforeClass
    public static void setUpClass() {
        InitSettingsForTest.init();
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
     * @throws Exception
     */
    @Test
    @Ignore
    public void testProxy() throws Exception {

        // Execute only if proxy is enabled
        if (DoiSettings.getInstance().getBoolean(Consts.SERVER_PROXY_USED)) {
            Engine.getInstance().getRegisteredClients().clear();
            Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));

            Client proxy = new Client(new Context(), Protocol.HTTP);
            proxy.getContext().getParameters().add("proxyHost",
                    DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST));
            proxy.getContext().getParameters().add("proxyPort",
                    DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT));

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
     * @throws Exception
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

        Representation rep = baseClient.client.get();
        Status status = baseClient.client.getStatus();
        Assert.assertTrue("Test si la requete est OK", status.isSuccess());

        System.out.println(rep.getText());

    }

    /**
     * Test the connection through the proxy with BaseClient and crossCite URL.
     * Works only if the test is executed behind a proxy
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testBaseClientCrossCite() throws Exception {
        BaseClient baseClient = new BaseClient("http://citation.crosscite.org/styles");
        if (DoiSettings.getInstance().getBoolean(Consts.SERVER_PROXY_USED)) {
            baseClient.setProxyAuthentication(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST),
                    DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT),
                    DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN),
                    DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD));
        }

        Representation rep = baseClient.client.get();
        Status status = baseClient.client.getStatus();
        Assert.assertTrue("Test si la requete est OK", status.isSuccess());

    }

    @Test
    public void testHttpClient() throws Exception {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST), Integer.valueOf(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT))),
                new UsernamePasswordCredentials(DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN), DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD))
        );
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        
        
        int status = 0;
        try {
            //HttpHost target = new HttpHost("www.google.fr", 443, "https");
            HttpHost proxy = new HttpHost(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST), Integer.valueOf(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT)), "http");            
            RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
            HttpGet request = new HttpGet("https://www.google.fr");
            request.setConfig(config);
            
            System.out.println("Executing request " + request.getRequestLine()+" to "+ request.getURI() +" via "+proxy);
            CloseableHttpResponse response = httpclient.execute(request);
            status = response.getStatusLine().getStatusCode();
            try {
                System.out.println("--------");
                System.out.println(response.getStatusLine());
                System.out.println(EntityUtils.toString(response.getEntity()));
            } finally {
                response.close();
            }
            
        } finally {
            httpclient.close();
        }
        Assert.assertEquals("Test si la requete est OK", 200, status);
//        HttpClient httpclient = new HttpClient();
//        if (DoiSettings.getInstance().getBoolean(Consts.SERVER_PROXY_USED)) {
//            httpclient.getHostConfiguration().setProxy(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST), Integer.valueOf(DoiSettings.getInstance().getString(Consts.SERVER_PROXY_PORT)));
//            httpclient.getState().setProxyCredentials("my-proxy-realm", DoiSettings.getInstance().getString(Consts.SERVER_PROXY_HOST),
//                    new UsernamePasswordCredentials(DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_LOGIN), DoiSettings.getInstance().getSecret(Consts.SERVER_PROXY_PWD)));
//        }
//        
//        GetMethod method = new GetMethod("https://www.google.com");
//        int statusCode = httpclient.executeMethod(method);
//        
//        Assert.assertEquals("Test si la requete est OK", 200, statusCode);

    }

}
