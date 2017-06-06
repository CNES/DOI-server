/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author malapert
 */
public class JettySettingsTest {
    
    private InputStream inputStream = SettingsSuite.class.getResourceAsStream("/resources/doi.properties");
    private DoiSettings doiSettings = DoiSettings.getInstance();  
    private JettySettings instance;
    
    public JettySettingsTest() {
        instance = new JettySettings(null, doiSettings);        
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

    /**
     * Test of getHttpRequestHeaderSize method, of class JettySettings.
     */
    @Test
    public void testGetHttpRequestHeaderSize() {
        System.out.println("getHttpRequestHeaderSize");
        int expResult = 8192;
        int result = instance.getHttpRequestHeaderSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHttpResponseHeaderSize method, of class JettySettings.
     */
    @Test
    public void testGetHttpResponseHeaderSize() {
        System.out.println("getHttpResponseHeaderSize");
        int expResult = 8192;
        int result = instance.getHttpResponseHeaderSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolMinThreads method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolMinThreads() {
        System.out.println("getThreadPoolMinThreads");
        int expResult = 8;
        int result = instance.getThreadPoolMinThreads();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolMaxThreads method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolMaxThreads() {
        System.out.println("getThreadPoolMaxThreads");
        int expResult = 200;
        int result = instance.getThreadPoolMaxThreads();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolThreadsPriority method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolThreadsPriority() {
        System.out.println("getThreadPoolThreadsPriority");
        int expResult = 5;
        int result = instance.getThreadPoolThreadsPriority();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolIdleTimeout method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolIdleTimeout() {
        System.out.println("getThreadPoolIdleTimeout");
        int expResult = 60000;
        int result = instance.getThreadPoolIdleTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolStopTimeout method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolStopTimeout() {
        System.out.println("getThreadPoolStopTimeout");
        long expResult = 5000L;
        long result = instance.getThreadPoolStopTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorAcceptors method, of class JettySettings.
     */
    @Test
    public void testGetConnectorAcceptors() {
        System.out.println("getConnectorAcceptors");
        int expResult = -1;
        int result = instance.getConnectorAcceptors();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorSelectors method, of class JettySettings.
     */
    @Test
    public void testGetConnectorSelectors() {
        System.out.println("getConnectorSelectors");
        int expResult = -1;
        int result = instance.getConnectorSelectors();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorIdleTimeout method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorIdleTimeout() {
        System.out.println("getLowResourceMonitorIdleTimeout");
        int expResult = 1000;
        int result = instance.getLowResourceMonitorIdleTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorPeriod method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorPeriod() {
        System.out.println("getLowResourceMonitorPeriod");
        int expResult = 1000;
        int result = instance.getLowResourceMonitorPeriod();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorMaxMemory method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorMaxMemory() {
        System.out.println("getLowResourceMonitorMaxMemory");
        long expResult = 0L;
        long result = instance.getLowResourceMonitorMaxMemory();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorMaxConnections method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorMaxConnections() {
        System.out.println("getLowResourceMonitorMaxConnections");
        int expResult = 0;
        int result = instance.getLowResourceMonitorMaxConnections();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorThreads method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorThreads() {
        System.out.println("getLowResourceMonitorThreads");
        boolean expResult = true;
        boolean result = instance.getLowResourceMonitorThreads();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorAcceptQueueSize method, of class JettySettings.
     */
    @Test
    public void testGetConnectorAcceptQueueSize() {
        System.out.println("getConnectorAcceptQueueSize");
        int expResult = 0;
        int result = instance.getConnectorAcceptQueueSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorSoLingerTime method, of class JettySettings.
     */
    @Test
    public void testGetConnectorSoLingerTime() {
        System.out.println("getConnectorSoLingerTime");
        int expResult = -1;
        int result = instance.getConnectorSoLingerTime();
        assertEquals(expResult, result);

    }

    /**
     * Test of getConnectorIdleTimeout method, of class JettySettings.
     */
    @Test
    public void testGetConnectorIdleTimeout() {
        System.out.println("getConnectorIdleTimeout");
        int expResult = 30000;
        int result = instance.getConnectorIdleTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHttpOutputBufferSize method, of class JettySettings.
     */
    @Test
    public void testGetHttpOutputBufferSize() {
        System.out.println("getHttpOutputBufferSize");
        int expResult = 32768;
        int result = instance.getHttpOutputBufferSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHttpHeaderCacheSize method, of class JettySettings.
     */
    @Test
    public void testGetHttpHeaderCacheSize() {
        System.out.println("getHttpHeaderCacheSize");
        int expResult = 512;
        int result = instance.getHttpHeaderCacheSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorStopTimeout method, of class JettySettings.
     */
    @Test
    public void testGetConnectorStopTimeout() {
        System.out.println("getConnectorStopTimeout");
        int expResult = 30000;
        int result = instance.getConnectorStopTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorStopTimeout method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorStopTimeout() {
        System.out.println("getLowResourceMonitorStopTimeout");
        long expResult = 30000;
        long result = instance.getLowResourceMonitorStopTimeout();
        assertEquals(expResult, result);
    }
    
}
