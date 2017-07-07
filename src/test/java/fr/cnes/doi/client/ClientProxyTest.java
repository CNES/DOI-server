/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

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

DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
 
        httpclient.getCredentialsProvider().setCredentials(
                new AuthScope("proxy-HTTP2.cnes.fr", 8050),
                new UsernamePasswordCredentials("", ""));
 
        HttpHost targetHost = new HttpHost("www.google.fr", 80, "http");
        HttpHost proxy = new HttpHost("proxy-HTTP2.cnes.fr", 8050);
 
        httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
  
        System.out.println("via proxy: " + proxy);
        System.out.println("to target: " + targetHost);
 
        RequestConfig config = RequestConfig.custom()
        .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC, AuthSchemes.NTLM))
        .build();
        HttpGet target = new HttpGet("http://www.google.fr");
        target.setConfig(config);
        HttpResponse response = httpclient.execute(target);
        HttpEntity entity = response.getEntity();
 
        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        if (entity != null) {
            System.out.println("Response content length: " + entity.getContentLength());
        }
       if (entity != null) {
            entity.consumeContent();
        }
 
        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        httpclient.getConnectionManager().shutdown();   

    }

}
