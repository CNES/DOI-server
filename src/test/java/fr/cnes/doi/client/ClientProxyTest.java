/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import java.io.IOException;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
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
    public void testProxy() throws IOException {
        Properties properties = System.getProperties();
        properties.put("http.proxyHost", "proxy-HTTP2.cnes.fr");
        properties.put("http.proxyPort", "8050");

        // Add the client authentication to the call
        ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;        

        // User + Password sur le proxy
        ChallengeResponse proxyAuthentication = new ChallengeResponse(scheme, "TODO", "TODO");
        ClientResource client = new ClientResource("http://google.com");
        client.setProxyChallengeResponse(proxyAuthentication);
        client.setProtocol(Protocol.HTTP);
        Representation rep = client.get();
        System.out.println(rep.getText());
    }

}
