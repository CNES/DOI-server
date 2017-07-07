/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import java.io.IOException;
import java.util.Properties;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.ext.httpclient.HttpClientHelper;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class ClientProxyTest {

    public ClientProxyTest() {

    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testProxy() throws IOException, Exception {

//        HttpClientBuilder builder = HttpClientBuilder.create();
//        HttpHost proxy = new HttpHost("proxy-HTTP2.cnes.fr", 8050, "http");
//        builder.setProxy(proxy);
//        HttpClient client = builder.build();
//        HttpUriRequest httpRequest = new HttpGet("http://www.google.com");
//        HttpResponse response = client.execute(httpRequest);        
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope("proxy-HTTP2.cnes.fr", 8050),
                new UsernamePasswordCredentials("my_username", "my_password"));
        HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();
        HttpResponse response = client.execute(new HttpGet("http://www.google.com"));

        response.getEntity().writeTo(System.out);

    }

}
