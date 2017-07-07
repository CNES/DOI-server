/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
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
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod("http://www.google.fr");//https://kodejava.org

        HostConfiguration config = client.getHostConfiguration();
        config.setProxy("proxy-HTTP2.cnes.fr", 8050);

        String username = "guest";
        String password = "s3cr3t";
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        AuthScope authScope = new AuthScope("proxy-HTTP2.cnes.fr", 8050);

        client.getState().setProxyCredentials(authScope, credentials);

        try {
            client.executeMethod(method);

            if (method.getStatusCode() == HttpStatus.SC_OK) {
                String response = method.getResponseBodyAsString();
                System.out.println("Response = " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }

    }

}
