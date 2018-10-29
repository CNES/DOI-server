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

import static fr.cnes.doi.AbstractSpec.classTitle;
import static fr.cnes.doi.AbstractSpec.testTitle;
import fr.cnes.doi.UnitTest;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test class for {@link fr.cnes.doi.settings.JettySettings}
 * @author Jean-Christophe Malapert
 */
@Category(UnitTest.class)
public class JettySettingsTest {

    private final DoiSettings doiSettings = DoiSettings.getInstance();
    private final JettySettings instance;

    public JettySettingsTest() {
        instance = new JettySettings(null, doiSettings);
    }

    @BeforeClass
    public static void setUpClass() {
        classTitle("JettySettings");
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
        testTitle("testGetHttpRequestHeaderSize");
        int expResult = 8192;
        int result = instance.getHttpRequestHeaderSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHttpResponseHeaderSize method, of class JettySettings.
     */
    @Test
    public void testGetHttpResponseHeaderSize() {
        testTitle("testGetHttpResponseHeaderSize");
        int expResult = 8192;
        int result = instance.getHttpResponseHeaderSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolMinThreads method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolMinThreads() {
        testTitle("testGetThreadPoolMinThreads");
        int expResult = 8;
        int result = instance.getThreadPoolMinThreads();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolMaxThreads method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolMaxThreads() {
        testTitle("testGetThreadPoolMaxThreads");
        int expResult = 200;
        int result = instance.getThreadPoolMaxThreads();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolThreadsPriority method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolThreadsPriority() {
        testTitle("testGetThreadPoolThreadsPriority");
        int expResult = 5;
        int result = instance.getThreadPoolThreadsPriority();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolIdleTimeout method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolIdleTimeout() {
        testTitle("testGetThreadPoolIdleTimeout");
        int expResult = 60000;
        int result = instance.getThreadPoolIdleTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadPoolStopTimeout method, of class JettySettings.
     */
    @Test
    public void testGetThreadPoolStopTimeout() {
        testTitle("testGetThreadPoolStopTimeout");
        long expResult = 5000L;
        long result = instance.getThreadPoolStopTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorAcceptors method, of class JettySettings.
     */
    @Test
    public void testGetConnectorAcceptors() {
        testTitle("testGetConnectorAcceptors");
        int expResult = -1;
        int result = instance.getConnectorAcceptors();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorSelectors method, of class JettySettings.
     */
    @Test
    public void testGetConnectorSelectors() {
        testTitle("testGetConnectorSelectors");
        int expResult = -1;
        int result = instance.getConnectorSelectors();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorIdleTimeout method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorIdleTimeout() {
        testTitle("testGetLowResourceMonitorIdleTimeout");
        int expResult = 1000;
        int result = instance.getLowResourceMonitorIdleTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorPeriod method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorPeriod() {
        testTitle("testGetLowResourceMonitorPeriod");
        int expResult = 1000;
        int result = instance.getLowResourceMonitorPeriod();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorMaxMemory method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorMaxMemory() {
        testTitle("testGetLowResourceMonitorMaxMemory");
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
        testTitle("testGetLowResourceMonitorMaxConnections");
        int expResult = 0;
        int result = instance.getLowResourceMonitorMaxConnections();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorThreads method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorThreads() {
        testTitle("testGetLowResourceMonitorThreads");
        boolean expResult = true;
        boolean result = instance.getLowResourceMonitorThreads();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorAcceptQueueSize method, of class JettySettings.
     */
    @Test
    public void testGetConnectorAcceptQueueSize() {
        testTitle("testGetConnectorAcceptQueueSize");
        int expResult = 0;
        int result = instance.getConnectorAcceptQueueSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorSoLingerTime method, of class JettySettings.
     */
    @Test
    public void testGetConnectorSoLingerTime() {
        testTitle("testGetConnectorSoLingerTime");
        int expResult = -1;
        int result = instance.getConnectorSoLingerTime();
        assertEquals(expResult, result);

    }

    /**
     * Test of getConnectorIdleTimeout method, of class JettySettings.
     */
    @Test
    public void testGetConnectorIdleTimeout() {
        testTitle("testGetConnectorIdleTimeout");
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
        testTitle("testGetHttpOutputBufferSize");
        int expResult = 32768;
        int result = instance.getHttpOutputBufferSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHttpHeaderCacheSize method, of class JettySettings.
     */
    @Test
    public void testGetHttpHeaderCacheSize() {
        testTitle("testGetHttpHeaderCacheSize");
        int expResult = 512;
        int result = instance.getHttpHeaderCacheSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConnectorStopTimeout method, of class JettySettings.
     */
    @Test
    public void testGetConnectorStopTimeout() {
        testTitle("testGetConnectorStopTimeout");
        int expResult = 30000;
        int result = instance.getConnectorStopTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLowResourceMonitorStopTimeout method, of class JettySettings.
     */
    @Test
    public void testGetLowResourceMonitorStopTimeout() {
        testTitle("testGetLowResourceMonitorStopTimeout");
        long expResult = 30000;
        long result = instance.getLowResourceMonitorStopTimeout();
        assertEquals(expResult, result);
    }
}
