/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.admin;

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class ServicesTest {
    
    private static Client cl;

    public ServicesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        InitServerForTest.init();
        cl = new Client(new Context(), Protocol.HTTPS);
        Series<Parameter> parameters = cl.getContext().getParameters();
        parameters.add("truststorePath", "jks/doiServerKey.jks");
        parameters.add("truststorePassword", DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
        parameters.add("truststoreType", "JKS");
    }

    @AfterClass
    public static void tearDownClass() {
        InitServerForTest.close();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of createToken method, of class TokenResource.
     */
    @Test
    public void getStatus()  {
        System.out.println("getProjectsNameAsJson");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/status");        
        client.setNext(cl);    
        Status status;
        try {
            Representation rep = client.get();
            status = client.getStatus();
        } catch(ResourceException ex) {
            status = ex.getStatus();
        }
        assertEquals(Status.SUCCESS_OK.getCode(), status.getCode());
    }    
    
    /**
     * Test of createToken method, of class TokenResource.
     */
    @Test
    @Ignore
    public void getStats()  {
        System.out.println("getProjectsNameAsJson");
        String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_PORT);
        ClientResource client = new ClientResource("https://localhost:"+port+"/stats");        
        client.setNext(cl);    
        Status status;
        try {
            Representation rep = client.get();
            status = client.getStatus();
        } catch(ResourceException ex) {
            status = ex.getStatus();
        }
        assertEquals(Status.SUCCESS_OK.getCode(), status.getCode());
    }      
    
}
