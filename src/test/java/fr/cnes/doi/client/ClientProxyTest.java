/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.ext.httpclient.HttpClientHelper;
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

        Client proxy = new Client(new Context(), Protocol.HTTP);
        proxy.getContext().getParameters().add("proxyHost", "proxy-HTTP2.cnes.fr");
        proxy.getContext().getParameters().add("proxyPort", "8050");
        byte[] credentials = Base64.encodeBase64(("" + ":" + "").getBytes(StandardCharsets.UTF_8));
        proxy.getContext().getParameters().add("Authorization", new String(credentials, StandardCharsets.UTF_8));
        ClientResource client = new ClientResource("http://www.google.fr");
        client.setNext(proxy);
        System.out.println(client.get().getText());
        //HttpClientHelper c = new HttpClientHelper(proxy);
        //System.out.println("host="+c.getProxyHost());
        //HttpClient client = c.getHttpClient();
        

//        // Add the client authentication to the call
//        ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
//
//        // User + Password sur le proxy
//        ChallengeResponse proxyAuthentication = new ChallengeResponse(scheme, "TODO", "TODO");
//        ClientResource client = new ClientResource("http://google.com");
//        client.setNext(proxy);
//        client.setProxyChallengeResponse(proxyAuthentication);
//        client.setProtocol(Protocol.HTTP);
//        //client.setProtocol(Protocol.HTTP);
//        Representation rep = client.get();
//        System.out.println(rep.getText());

    }

}
