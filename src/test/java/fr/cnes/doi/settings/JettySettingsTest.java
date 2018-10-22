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
package fr.cnes.doi.settings;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@link fr.cnes.doi.settings.JettySettings}
 * @author Jean-Christophe Malapert
 */
public class JettySettingsTest {

    private final DoiSettings doiSettings = DoiSettings.getInstance();
    private final JettySettings instance;

    public JettySettingsTest() {
        instance = new JettySettings(null, doiSettings);
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("------ TEST JettySettings ------");
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
        System.out.println("TEST: GetHttpRequestHeaderSize");
        int expResult = 8192;
        int result = instance.getHttpRequestHeaderSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHttpResponseHeaderSize method, of class JettySettings.
     */
    @Test
    public void testGetHttpResponseHeaderSize() {
        System.out.println("TEST: GetHttpResponseHeaderSize");
        int expResult = 8192;
        int result = instance.getHttpResponseHeaderSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolMinThreads method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolMinThreads() {
        System.out.println("TEST: GetThreadPoolMinThreads");
        int expResult = 8;
        int result = instance.getThreadPoolMinThreads();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolMaxThreads method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolMaxThreads() {
        System.out.println("TEST: GetThreadPoolMaxThreads");
        int expResult = 200;
        int result = instance.getThreadPoolMaxThreads();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolThreadsPriority method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolThreadsPriority() {
        System.out.println("TEST: GetThreadPoolThreadsPriority");
        int expResult = 5;
        int result = instance.getThreadPoolThreadsPriority();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolIdleTimeout method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolIdleTimeout() {
        System.out.println("TEST: GetThreadPoolIdleTimeout");
        int expResult = 60000;
        int result = instance.getThreadPoolIdleTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolStopTimeout method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolStopTimeout() {
        System.out.println("TEST: GetThreadPoolStopTimeout");
        long expResult = 5000L;
        long result = instance.getThreadPoolStopTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorAcceptors method, of class JettySettings.
     */
    @Test
    public void testGetConnectorAcceptors() {
        System.out.println("TEST: GetConnectorAcceptors");
        int expResult = -1;
        int result = instance.getConnectorAcceptors();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorSelectors method, of class JettySettings.
     */
    @Test
    public void testGetConnectorSelectors() {
        System.out.println("TEST: GetConnectorSelectors");
        int expResult = -1;
        int result = instance.getConnectorSelectors();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorIdleTimeout method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorIdleTimeout() {
        System.out.println("TEST: GetLowResourceMonitorIdleTimeout");
        int expResult = 1000;
        int result = instance.getLowResourceMonitorIdleTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorPeriod method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorPeriod() {
        System.out.println("TEST: GetLowResourceMonitorPeriod");
        int expResult = 1000;
        int result = instance.getLowResourceMonitorPeriod();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorMaxMemory method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorMaxMemory() {
        System.out.println("TEST: GetLowResourceMonitorMaxMemory");
        long expResult = 0L;
        long result = instance.getLowResourceMonitorMaxMemory();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorMaxConnections method, of class
     * JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorMaxConnections() {
        System.out.println("TEST: GetLowResourceMonitorMaxConnections");
        int expResult = 0;
        int result = instance.getLowResourceMonitorMaxConnections();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorThreads method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorThreads() {
        System.out.println("TEST: GetLowResourceMonitorThreads");
        boolean expResult = true;
        boolean result = instance.getLowResourceMonitorThreads();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorAcceptQueueSize method, of class JettySettings.
     */
    @Test
    public void testGetConnectorAcceptQueueSize() {
        System.out.println("TEST: GetConnectorAcceptQueueSize");
        int expResult = 0;
        int result = instance.getConnectorAcceptQueueSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorSoLingerTime method, of class JettySettings.
     */
    @Test
    public void testGetConnectorSoLingerTime() {
        System.out.println("TEST: GetConnectorSoLingerTime");
        int expResult = -1;
        int result = instance.getConnectorSoLingerTime();
        assertEquals(expResult, result);

    }

    /**
     * Test of getConnectorIdleTimeout method, of class JettySettings.
     */
    @Test
    public void testGetConnectorIdleTimeout() {
        System.out.println("TEST: GetConnectorIdleTimeout");
        int expResult = 30000;
        int result = instance.getConnectorIdleTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHttpOutputBufferSize method, of class JettySettings.
     */
    @Test
    public void testGetHttpOutputBufferSize() {
        System.out.println("TEST: GetHttpOutputBufferSize");
        int expResult = 32768;
        int result = instance.getHttpOutputBufferSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHttpHeaderCacheSize method, of class JettySettings.
     */
    @Test
    public void testGetHttpHeaderCacheSize() {
        System.out.println("TEST: GetHttpHeaderCacheSize");
        int expResult = 512;
        int result = instance.getHttpHeaderCacheSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorStopTimeout method, of class JettySettings.
     */
    @Test
    public void testGetConnectorStopTimeout() {
        System.out.println("TEST: GetConnectorStopTimeout");
        int expResult = 30000;
        int result = instance.getConnectorStopTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorStopTimeout method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorStopTimeout() {
        System.out.println("TEST: GetLowResourceMonitorStopTimeout");
        long expResult = 30000;
        long result = instance.getLowResourceMonitorStopTimeout();
        assertEquals(expResult, result);
    }
}
