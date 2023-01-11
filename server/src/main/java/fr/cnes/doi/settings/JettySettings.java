/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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

import fr.cnes.doi.utils.spec.Requirement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.ext.jetty.JettyServerHelper;

/**
 * Fills Jetty configuration file based on DoiSettings and registers it in
 * Jetty.
 *
 * @author Jean-Christophe Malapert
 */
@Requirement(reqId = Requirement.DOI_CONFIG_010, reqName = Requirement.DOI_CONFIG_010_NAME)
public final class JettySettings extends JettyServerHelper {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(JettySettings.class.getName());

    /**
     * DOI settings.
     */
    private final DoiSettings settings;

    /**
     * Server.
     */
    private final Server server;

    /**
     * Constructs settings for Jetty.
     *
     * @param server Jetty server
     * @param settings DOI settings
     */
    public JettySettings(final Server server,
            final DoiSettings settings) {
        super(server);
        LOG.trace("Entering in Constructor");
        this.server = server;
        this.settings = settings;
        LOG.trace("Exiting from Constructor");
    }

    /**
     * HTTP request header size in bytes. Defaults to 8*1024. Larger headers
     * will allow for more and/or larger cookies plus larger form content
     * encoded in a URL. However, larger headers consume more memory and can
     * make a server more vulnerable to denial of service attacks.
     *
     * @return HTTP request header size.
     */
    @Override
    public int getHttpRequestHeaderSize() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_REQUEST_HEADER_SIZE);
            LOG.debug("getHttpRequestHeaderSize : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getHttpRequestHeaderSize();
            LOG.debug("getHttpRequestHeaderSize : default value loaded");
        }
        LOG.info("getHttpRequestHeaderSize : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * HTTP response header size in bytes. Defaults to 8*1024. Larger headers
     * will allow for more and/or larger cookies and longer HTTP headers (e.g.
     * for redirection). However, larger headers will also consume more memory.
     *
     * @return HTTP response header size.
     */
    @Override
    public int getHttpResponseHeaderSize() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_RESPONSE_HEADER_SIZE);
            LOG.debug("getHttpResponseHeaderSize : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getHttpResponseHeaderSize();
            LOG.debug("getHttpResponseHeaderSize : default value loaded");
        }
        LOG.info("getHttpResponseHeaderSize : {}", result);
        LOG.trace("Exiting from getHttpResponseHeaderSize with result {}", result);
        return result;
    }

    /**
     * Thread pool minimum threads. Defaults to 8.
     *
     * @return Thread pool minimum threads.
     */
    @Override
    public int getThreadPoolMinThreads() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_MIN_THREADS);
            LOG.debug("getThreadPoolMinThreads : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getThreadPoolMinThreads();
            LOG.debug("getThreadPoolMinThreads : default value loaded");
        }
        LOG.info("getThreadPoolMinThreads : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Thread pool maximum threads. Defaults to 200.
     *
     * @return Thread pool maximum threads.
     */
    @Override
    public int getThreadPoolMaxThreads() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_MAX_THREADS);
            LOG.debug("getThreadPoolMaxThreads : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getThreadPoolMaxThreads();
            LOG.debug("getThreadPoolMaxThreads : default value loaded");
        }
        LOG.info("getThreadPoolMaxThreads : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Thread pool threads priority. Defaults to Thread.NORM_PRIORITY.
     *
     * @return Thread pool maximum threads.
     */
    @Override
    public int getThreadPoolThreadsPriority() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_THREADS_PRIORITY);
            LOG.debug("getThreadPoolThreadsPriority : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getThreadPoolThreadsPriority();
            LOG.debug("getThreadPoolThreadsPriority : default value loaded");
        }
        LOG.info("getThreadPoolThreadsPriority : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Thread pool idle timeout in milliseconds. Defaults to 60000. Threads that
     * are idle for longer than this period may be stopped.
     *
     * @return Thread pool idle timeout.
     */
    @Override
    public int getThreadPoolIdleTimeout() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_THREAD_MAX_IDLE_TIME_MS);
            LOG.debug("getThreadPoolIdleTimeout : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getThreadPoolIdleTimeout();
            LOG.debug("getThreadPoolIdleTimeout : default value loaded");
        }
        LOG.info("getThreadPoolIdleTimeout : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Thread pool stop timeout in milliseconds. Defaults to 5000. The maximum
     * time allowed for the service to shutdown.
     *
     * @return Thread pool stop timeout.
     */
    @Override
    public long getThreadPoolStopTimeout() {
        LOG.traceEntry();
        long result;
        try {
            result = settings.getLong(Consts.JETTY_THREAD_STOP_TIME_MS);
            LOG.debug("getThreadPoolStopTimeout : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getThreadPoolStopTimeout();
            LOG.debug("getThreadPoolStopTimeout : default value loaded");
        }
        LOG.info("getThreadPoolStopTimeout : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Connector acceptor thread count. Defaults to -1. When -1, Jetty will
     * default to Runtime.availableProcessors() / 2, with a minimum of 1.
     *
     * @return Connector acceptor thread count.
     */
    @Override
    public int getConnectorAcceptors() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_ACCEPTOR_THREADS);
            LOG.debug("getConnectorAcceptors : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getConnectorAcceptors();
            LOG.debug("getConnectorAcceptors : default value loaded");
        }
        LOG.info("getConnectorAcceptors : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Connector selector thread count. Defaults to -1. When 0, Jetty will
     * default to Runtime.availableProcessors().
     *
     * @return Connector acceptor thread count.
     */
    @Override
    public int getConnectorSelectors() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_SELECTOR_THREADS);
            LOG.debug("getConnectorSelectors : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getConnectorSelectors();
            LOG.debug("getConnectorSelectors : default value loaded");
        }
        LOG.info("getConnectorSelectors : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Low resource monitor idle timeout in milliseconds. Defaults to 1000.
     * Applied to EndPoints when in the low resources state.
     *
     * @return Low resource monitor idle timeout.
     */
    @Override
    public int getLowResourceMonitorIdleTimeout() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_LOW_RESOURCES_MAX_IDLE_TIME_MS);
            LOG.debug(
                    "getLowResourceMonitorIdleTimeout : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getLowResourceMonitorIdleTimeout();
            LOG.debug("getLowResourceMonitorIdleTimeout : default value loaded");
        }
        LOG.info("getLowResourceMonitorIdleTimeout : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Low resource monitor period in milliseconds. Defaults to 1000. When 0,
     * low resource monitoring is disabled.
     *
     * @return Low resource monitor period.
     */
    @Override
    public int getLowResourceMonitorPeriod() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_LOW_RESOURCES_PERIOD);
            LOG.debug("getLowResourceMonitorPeriod : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getLowResourceMonitorPeriod();
            LOG.debug("getLowResourceMonitorPeriod : default value loaded");
        }
        LOG.info("getLowResourceMonitorPeriod : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Low resource monitor max memory in bytes. Defaults to 0. When 0, the
     * check disabled. Memory used is calculated as (totalMemory-freeMemory).
     *
     * @return Low resource monitor max memory.
     */
    @Override
    public long getLowResourceMonitorMaxMemory() {
        LOG.traceEntry();
        long result;
        try {
            result = settings.getLong(Consts.JETTY_LOW_RESOURCES_MAX_MEMORY);
            LOG.debug(
                    "getLowResourceMonitorMaxMemory : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getLowResourceMonitorMaxMemory();
            LOG.debug("getLowResourceMonitorMaxMemory : default value loaded");
        }
        LOG.info("getLowResourceMonitorMaxMemory : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Low resource monitor max connections. Defaults to 0. When 0, the check is
     * disabled.
     *
     * @return Low resource monitor max connections.
     */
    @Override
    public int getLowResourceMonitorMaxConnections() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_LOW_RESOURCES_MAX_MEMORY);
            LOG.debug(
                    "getLowResourceMonitorMaxConnections : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getLowResourceMonitorMaxConnections();
            LOG.debug("getLowResourceMonitorMaxConnections : default value loaded");
        }
        LOG.info("getLowResourceMonitorMaxConnections : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Low resource monitor, whether to check if we're low on threads. Defaults
     * to true.
     *
     * @return Low resource monitor threads.
     */
    @Override
    public boolean getLowResourceMonitorThreads() {
        LOG.traceEntry();
        boolean result;
        try {
            result = settings.getBoolean(Consts.JETTY_LOW_RESOURCES_THREADS);
            LOG.debug("getLowResourceMonitorThreads : default value from configuration file loaded");
        } catch (IllegalArgumentException e) {
            result = super.getLowResourceMonitorThreads();
            LOG.debug("getLowResourceMonitorThreads : default value loaded");
        }
        LOG.info("getLowResourceMonitorThreads : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Connector accept queue size. Defaults to 0. Also known as accept backlog.
     *
     * @return Connector accept queue size.
     */
    @Override
    public int getConnectorAcceptQueueSize() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_ACCEPT_QUEUE_SIZE);
            LOG.debug("getConnectorAcceptQueueSize : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getConnectorAcceptQueueSize();
            LOG.debug("getConnectorAcceptQueueSize : default value loaded");
        }
        LOG.info("getConnectorAcceptQueueSize : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Connector TCP/IP SO linger time in milliseconds. Defaults to -1
     * (disabled).
     *
     * @return Connector TCP/IP SO linger time.
     */
    @Override
    public int getConnectorSoLingerTime() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_SO_LINGER_TIME);
            LOG.debug("getConnectorSoLingerTime : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getConnectorSoLingerTime();
            LOG.debug("getConnectorSoLingerTime : default value loaded");
        }
        LOG.info("getConnectorSoLingerTime : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Connector idle timeout in milliseconds. Defaults to 30000. This value is
     * interpreted as the maximum time between some progress being made on the
     * connection. So if a single byte is read or written, then the timeout is
     * reset.
     *
     * @return Connector idle timeout.
     */
    @Override
    public int getConnectorIdleTimeout() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_IO_MAX_IDLE_TIME_MS);
            LOG.debug("getConnectorIdleTimeout : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getConnectorIdleTimeout();
            LOG.debug("getConnectorIdleTimeout : default value loaded");
        }
        LOG.info("getConnectorIdleTimeout : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * HTTP output buffer size in bytes. Defaults to 32*1024. A larger buffer
     * can improve performance by allowing a content producer to run without
     * blocking, however larger buffers consume more memory and may induce some
     * latency before a client starts processing the content.
     *
     * @return HTTP output buffer size.
     */
    @Override
    public int getHttpOutputBufferSize() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_RESPONSE_BUFFER_SIZE);
            LOG.debug("getHttpOutputBufferSize : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getHttpOutputBufferSize();
            LOG.debug("getHttpOutputBufferSize : default value loaded");
        }
        LOG.info("getHttpOutputBufferSize : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * HTTP header cache size in bytes. Defaults to 512.
     *
     * @return HTTP header cache size.
     */
    @Override
    public int getHttpHeaderCacheSize() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_REQUEST_BUFFER_SIZE);
            LOG.debug("getHttpHeaderCacheSize : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getHttpHeaderCacheSize();
            LOG.debug("getHttpHeaderCacheSize : default value loaded");
        }
        LOG.info("getHttpHeaderCacheSize : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Connector stop timeout in milliseconds. Defaults to 30000. The maximum
     * time allowed for the service to shutdown.
     *
     * @return Connector stop timeout.
     */
    @Override
    public int getConnectorStopTimeout() {
        LOG.traceEntry();
        int result;
        try {
            result = settings.getInt(Consts.JETTY_GRACEFUL_SHUTDOWN);
            LOG.debug("getConnectorStopTimeout : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getConnectorStopTimeout();
            LOG.debug("getConnectorStopTimeout : default value loaded");
        }
        LOG.info("getConnectorStopTimeout : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * Low resource monitor stop timeout in milliseconds. Defaults to 30000. The
     * maximum time allowed for the service to shutdown.
     *
     * @return Low resource monitor stop timeout.
     */
    @Override
    public long getLowResourceMonitorStopTimeout() {
        LOG.traceEntry();
        long result;
        try {
            result = settings.getLong(Consts.JETTY_GRACEFUL_SHUTDOWN);
            LOG.debug(
                    "getLowResourceMonitorStopTimeout : default value from configuration file loaded");
        } catch (NumberFormatException e) {
            result = super.getLowResourceMonitorStopTimeout();
            LOG.debug("getLowResourceMonitorStopTimeout : default value loaded");
        }
        LOG.info("getLowResourceMonitorStopTimeout : {}", result);
        return LOG.traceExit(result);
    }

    /**
     * addParamsToServerContext.
     */
    public void addParamsToServerContext() {
        LOG.traceEntry();
        LOG.info("---- Jetty parameters ----");
        addParam("threadPool.minThreads", getThreadPoolMinThreads());
        addParam("threadPool.maxThreads", getThreadPoolMaxThreads());
        addParam("threadPool.threadsPriority", getThreadPoolThreadsPriority());
        addParam("threadPool.idleTimeout", getThreadPoolIdleTimeout());
        addParam("threadPool.stopTimeout", getThreadPoolStopTimeout());
        addParam("connector.acceptors", getConnectorAcceptors());
        addParam("connector.selectors", getConnectorSelectors());
        addParam("connector.acceptQueueSize", getConnectorAcceptQueueSize());
        addParam("connector.idleTimeout", getConnectorIdleTimeout());
        addParam("connector.soLingerTime", getConnectorSoLingerTime());
        addParam("connector.stopTimeout", getConnectorStopTimeout());
        addParam("http.headerCacheSize", getHttpHeaderCacheSize());
        addParam("http.requestHeaderSize", getHttpRequestHeaderSize());
        addParam("http.responseHeaderSize", getHttpResponseHeaderSize());
        addParam("http.outputBufferSize", getHttpOutputBufferSize());
        addParam("lowResource.period", getLowResourceMonitorPeriod());
        addParam("lowResource.threads", getLowResourceMonitorThreads());
        addParam("lowResource.maxMemory", getLowResourceMonitorMaxMemory());
        addParam("lowResource.maxConnections", getLowResourceMonitorMaxConnections());
        addParam("lowResource.idleTimeout", getLowResourceMonitorIdleTimeout());
        addParam("lowResource.stopTimeout", getLowResourceMonitorStopTimeout());
        LOG.info("Jetty settings have been loaded");
        LOG.info("---------------------------");
        LOG.traceExit();
    }

    /**
     * Add a parameter to a Jetty server.
     *
     * @param propName the name of the property to add
     * @param propValue the value to set
     * @throws IllegalArgumentException - if propValue is null
     */
    private void addParam(final String propName,
            final Object propValue) {
        LOG.traceEntry("Parameters : {} and {}", propName, propValue);
        if (propValue == null) {
            throw new IllegalArgumentException("propValue for " + propName + " is not defined");
        }
        final Parameter param = this.server.getContext().getParameters().getFirst(propName);
        if (param == null) {
            this.server.getContext().getParameters().add(propName, String.valueOf(propValue));
            LOG.info("add to a new param {} : {}", propName, propValue);
        } else {
            param.setValue(String.valueOf(propValue));
            LOG.info("add to an existant param {} : {}", propName, propValue);
        }
        LOG.traceExit();
    }

	@Override
	protected ConnectionFactory[] createConnectionFactories(HttpConfiguration configuration) {
		//TODO To override if necessary
		return null;
	}
}
