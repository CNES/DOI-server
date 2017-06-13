/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.ext.jetty.JettyServerHelper;

/**
 * Fills Jetty configuration file based on DoiSettings and registers it in Jetty.
 * @author Jean-Christophe Malapert
 */
public class JettySettings extends JettyServerHelper {

    private final DoiSettings settings;
    private final Server server;

    /**
     * Constructs settings for Jetty.
     * @param server
     * @param settings 
     */
    public JettySettings(final Server server, final DoiSettings settings) {
        super(server);
        this.server = server;
        this.settings = settings;
    }

    /**
     * HTTP request header size in bytes. Defaults to 8*1024.
     * Larger headers will allow for more and/or larger cookies plus larger form
     * content encoded in a URL. However, larger headers consume more memory and
     * can make a server more vulnerable to denial of service attacks.
     * @return HTTP request header size.
     */
    @Override
    public int getHttpRequestHeaderSize() {
        int result;
        try {
            result = settings.getInt(Consts.JETTY_REQUEST_HEADER_SIZE);
        } catch (Exception e) {
            result = super.getHttpRequestHeaderSize();
        }
        return result;
    }

    /**
     * HTTP response header size in bytes. Defaults to 8*1024.
     * Larger headers will allow for more and/or larger cookies and longer HTTP 
     * headers (e.g. for redirection). However, larger headers will also consume
     * more memory.
     * @return HTTP response header size.
     */
    @Override
    public int getHttpResponseHeaderSize() {
        int result;
        try {
            result = settings.getInt(Consts.JETTY_RESPONSE_HEADER_SIZE);
        } catch (Exception e) {
            result = super.getHttpResponseHeaderSize();
        }
        return result;
    }

    /**
     * Thread pool minimum threads. Defaults to 8.
     * @return Thread pool minimum threads.
     */
    @Override
    public int getThreadPoolMinThreads() {
        try {
            return settings.getInt(Consts.JETTY_MIN_THREADS);
        } catch (Exception e) {
            return super.getThreadPoolMinThreads();
        }
    }

    /**
     * Thread pool maximum threads. Defaults to 200.
     * @return Thread pool maximum threads.
     */
    @Override
    public int getThreadPoolMaxThreads() {
        try {
            return settings.getInt(Consts.JETTY_MAX_THREADS);
        } catch (Exception e) {
            return super.getThreadPoolMaxThreads();
        }
    }

    /**
     * Thread pool threads priority. Defaults to Thread.NORM_PRIORITY.
     * @return Thread pool maximum threads.
     */
    @Override
    public int getThreadPoolThreadsPriority() {
        try {
            return settings.getInt(Consts.JETTY_THREADS_PRIORITY);
        } catch (Exception e) {
            return super.getThreadPoolThreadsPriority();
        }        
    }
    
    /**
     * Thread pool idle timeout in milliseconds. Defaults to 60000.
     * Threads that are idle for longer than this period may be stopped.     
     * @return Thread pool idle timeout.
     */
    @Override
    public int getThreadPoolIdleTimeout() {
        try {
            return settings.getInt(Consts.JETTY_THREAD_MAX_IDLE_TIME_MS);
        } catch (Exception e) {
            return super.getThreadPoolIdleTimeout();
        }
    }

    /**
     * Thread pool stop timeout in milliseconds. Defaults to 5000.
     * The maximum time allowed for the service to shutdown.
     * @return Thread pool stop timeout.
     */
    @Override
    public long getThreadPoolStopTimeout() {
        try {
            return settings.getLong(Consts.JETTY_THREAD_STOP_TIME_MS);
        } catch (Exception e) {
            return super.getThreadPoolStopTimeout();
        }        
    }
    
    /**
     * Connector acceptor thread count. Defaults to -1. When -1, Jetty will 
     * default to Runtime.availableProcessors() / 2, with a minimum of 1.
     * @return Connector acceptor thread count.
     */
    @Override
    public int getConnectorAcceptors() {
        try {
            return settings.getInt(Consts.JETTY_ACCEPTOR_THREADS);
        } catch (Exception e) {
            return super.getConnectorAcceptors();
        }
    }

    /**
     * Connector selector thread count. Defaults to -1. When 0, Jetty will 
     * default to Runtime.availableProcessors().
     * @return Connector acceptor thread count.
     */
    @Override
    public int getConnectorSelectors() {
        try {
            return settings.getInt(Consts.JETTY_SELECTOR_THREADS);
        } catch (Exception e) {
            return super.getConnectorSelectors();
        }        
    }    

    /**
     * Low resource monitor idle timeout in milliseconds. Defaults to 1000.
     * Applied to EndPoints when in the low resources state.
     * @return Low resource monitor idle timeout.
     */
    @Override
    public int getLowResourceMonitorIdleTimeout() {
        try {
            return settings.getInt(Consts.JETTY_LOW_RESOURCES_MAX_IDLE_TIME_MS);
        } catch (Exception e) {
            return super.getLowResourceMonitorIdleTimeout();
        }
    }

    /**
     * Low resource monitor period in milliseconds. Defaults to 1000. When 0, 
     * low resource monitoring is disabled.
     * @return Low resource monitor period.
     */
    @Override
    public int getLowResourceMonitorPeriod() {
        try {
            return settings.getInt(Consts.JETTY_LOW_RESOURCES_PERIOD);
        } catch (Exception e) {
            return super.getLowResourceMonitorPeriod();
        }        
    }

    /**
     * Low resource monitor max memory in bytes. Defaults to 0. When 0, the check disabled.
     * Memory used is calculated as (totalMemory-freeMemory).
     * @return Low resource monitor max memory.
     */
    @Override
    public long getLowResourceMonitorMaxMemory() {
        try {
            return settings.getLong(Consts.JETTY_LOW_RESOURCES_MAX_MEMORY);
        } catch (Exception e) {
            return super.getLowResourceMonitorMaxMemory();
        }          
    }

    /**
     * Low resource monitor max connections. Defaults to 0. When 0, the check is disabled.
     * @return Low resource monitor max connections.
     */
    @Override
    public int getLowResourceMonitorMaxConnections() {
        try {
            return settings.getInt(Consts.JETTY_LOW_RESOURCES_MAX_MEMORY);
        } catch (Exception e) {
            return super.getLowResourceMonitorMaxConnections();
        }          
    }

    /**
     * Low resource monitor, whether to check if we're low on threads. Defaults to true.
     * @return Low resource monitor threads.
     */
    @Override
    public boolean getLowResourceMonitorThreads() {
        try {
            return settings.getBoolean(Consts.JETTY_LOW_RESOURCES_THREADS);
        } catch (Exception e) {
            return super.getLowResourceMonitorThreads();
        }         
    }  

    /**
     * Connector accept queue size. Defaults to 0.
     * Also known as accept backlog.
     * @return Connector accept queue size.
     */
    @Override
    public int getConnectorAcceptQueueSize() {
        try {
            return settings.getInt(Consts.JETTY_ACCEPT_QUEUE_SIZE);
        } catch (Exception e) {
            return super.getConnectorAcceptQueueSize();
        }
    }
        
    /**
     * Connector TCP/IP SO linger time in milliseconds. Defaults to -1 (disabled).
     * @return Connector TCP/IP SO linger time.
     */
    @Override
    public int getConnectorSoLingerTime() {
        try {
            return settings.getInt(Consts.JETTY_SO_LINGER_TIME);
        } catch (Exception e) {
            return super.getConnectorSoLingerTime();
        }
    }

    /**
     * Connector idle timeout in milliseconds. Defaults to 30000.
     * This value is interpreted as the maximum time between some progress being
     * made on the connection. So if a single byte is read or written, then the 
     * timeout is reset.
     * @return Connector idle timeout.
     */
    @Override
    public int getConnectorIdleTimeout() {
        try {
            return settings.getInt(Consts.JETTY_IO_MAX_IDLE_TIME_MS);
        } catch (Exception e) {
            return super.getConnectorIdleTimeout();
        }
    }

    /**
     * HTTP output buffer size in bytes. Defaults to 32*1024.
     * A larger buffer can improve performance by allowing a content producer 
     * to run without blocking, however larger buffers consume more memory and
     * may induce some latency before a client starts processing the content.
     * @return HTTP output buffer size.
     */
    @Override
    public int getHttpOutputBufferSize() {
        try {
            return settings.getInt(Consts.JETTY_RESPONSE_BUFFER_SIZE);
        } catch (Exception e) {
            return super.getHttpOutputBufferSize();
        }
    }

    /**
     * HTTP header cache size in bytes. Defaults to 512.
     * @return HTTP header cache size.
     */
    @Override
    public int getHttpHeaderCacheSize() {
        try {
            return settings.getInt(Consts.JETTY_REQUEST_BUFFER_SIZE);
        } catch (Exception e) {
            return super.getHttpHeaderCacheSize();
        }
    }

    /**
     * Connector stop timeout in milliseconds. Defaults to 30000.
     * The maximum time allowed for the service to shutdown.
     * @return Connector stop timeout.
     */
    @Override
    public int getConnectorStopTimeout() {
        try {
            return settings.getInt(Consts.JETTY_GRACEFUL_SHUTDOWN);
        } catch (Exception e) {
            return super.getConnectorStopTimeout();
        }
    }

    /**
     * Low resource monitor stop timeout in milliseconds. Defaults to 30000.
     * The maximum time allowed for the service to shutdown.
     * @return Low resource monitor stop timeout.
     */
    @Override
    public long getLowResourceMonitorStopTimeout() {
        try {
            return settings.getLong(Consts.JETTY_GRACEFUL_SHUTDOWN);
        } catch (Exception e) {
            return super.getLowResourceMonitorStopTimeout();
        }
    }

    /**
     * addParamsToServerContext
     *
     */
    public void addParamsToServerContext() {
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
    }

    /**
     * Add a parameter to a Jetty server.
     * @param propName the name of the property to add
     * @param propValue the value to set
     */
    private void addParam(final String propName, final Object propValue) {
        Parameter param = this.server.getContext().getParameters().getFirst(propName);
        if (param != null) {
            param.setValue("" + propValue);
        } else {
            this.server.getContext().getParameters().add(propName, "" + propValue);
        }
    }
}
