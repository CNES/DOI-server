/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

import fr.cnes.doi.utils.spec.Requirement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.ext.jetty.JettyServerHelper;

/**
 * Fills Jetty configuration file based on DoiSettings and registers it in
 * Jetty.
 *
 * @author Jean-Christophe Malapert
 */
@Requirement(
        reqId = Requirement.DOI_CONFIG_010,
        reqName = Requirement.DOI_CONFIG_010_NAME
)
public final class JettySettings extends JettyServerHelper {

    private final DoiSettings settings;
    private final Server server;
    
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JettySettings.class.getName());    

    /**
     * Constructs settings for Jetty.
     *
     * @param server Jetty server
     * @param settings DOI settings
     */
    public JettySettings(final Server server, final DoiSettings settings) {
        super(server);
        LOGGER.entering(this.getClass().getName(), "Constructor");
        this.server = server;
        this.settings = settings;
        LOGGER.exiting(this.getClass().getName(), "Constructor");        
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
        LOGGER.entering(this.getClass().getName(), "getHttpRequestHeaderSize");        
        int result;
        try {
            result = settings.getInt(Consts.JETTY_REQUEST_HEADER_SIZE);
        } catch (Exception e) {
            result = super.getHttpRequestHeaderSize();
        }
        LOGGER.exiting(this.getClass().getName(), "getHttpRequestHeaderSize", result);                
        return result;
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
        LOGGER.entering(this.getClass().getName(), "getHttpResponseHeaderSize");                
        int result;
        try {
            result = settings.getInt(Consts.JETTY_RESPONSE_HEADER_SIZE);
        } catch (Exception e) {
            result = super.getHttpResponseHeaderSize();
        }
        LOGGER.exiting(this.getClass().getName(), "getHttpResponseHeaderSize", result);                        
        return result;
    }

    /**
     * Thread pool minimum threads. Defaults to 8.
     *
     * @return Thread pool minimum threads.
     */
    @Override
    public int getThreadPoolMinThreads() {
        LOGGER.entering(this.getClass().getName(), "getThreadPoolMinThreads");                        
        int result;
        try {
            result =  settings.getInt(Consts.JETTY_MIN_THREADS);
        } catch (Exception e) {
            result = super.getThreadPoolMinThreads();
        }
        LOGGER.exiting(this.getClass().getName(), "getThreadPoolMinThreads", result);                                
        return result;
    }

    /**
     * Thread pool maximum threads. Defaults to 200.
     *
     * @return Thread pool maximum threads.
     */
    @Override
    public int getThreadPoolMaxThreads() {
        LOGGER.entering(this.getClass().getName(), "getThreadPoolMaxThreads");   
        int result;
        try {
            result = settings.getInt(Consts.JETTY_MAX_THREADS);
        } catch (Exception e) {
            result = super.getThreadPoolMaxThreads();
        }
        LOGGER.exiting(this.getClass().getName(), "getThreadPoolMaxThreads", result);           
        return result;
    }

    /**
     * Thread pool threads priority. Defaults to Thread.NORM_PRIORITY.
     *
     * @return Thread pool maximum threads.
     */
    @Override
    public int getThreadPoolThreadsPriority() {
        LOGGER.entering(this.getClass().getName(), "getThreadPoolThreadsPriority");           
        int result;
        try {
            result = settings.getInt(Consts.JETTY_THREADS_PRIORITY);
        } catch (Exception e) {
            result = super.getThreadPoolThreadsPriority();
        }
        LOGGER.exiting(this.getClass().getName(), "getThreadPoolThreadsPriority", result);                   
        return result;
    }

    /**
     * Thread pool idle timeout in milliseconds. Defaults to 60000. Threads that
     * are idle for longer than this period may be stopped.
     *
     * @return Thread pool idle timeout.
     */
    @Override
    public int getThreadPoolIdleTimeout() {
        LOGGER.entering(this.getClass().getName(), "getThreadPoolIdleTimeout"); 
        int result;
        try {
            result = settings.getInt(Consts.JETTY_THREAD_MAX_IDLE_TIME_MS);
        } catch (Exception e) {
            result = super.getThreadPoolIdleTimeout();
        }
        LOGGER.exiting(this.getClass().getName(), "getThreadPoolIdleTimeout", result);         
        return result;
    }

    /**
     * Thread pool stop timeout in milliseconds. Defaults to 5000. The maximum
     * time allowed for the service to shutdown.
     *
     * @return Thread pool stop timeout.
     */
    @Override
    public long getThreadPoolStopTimeout() {
        LOGGER.entering(this.getClass().getName(), "getThreadPoolStopTimeout");         
        long result;
        try {
            result = settings.getLong(Consts.JETTY_THREAD_STOP_TIME_MS);
        } catch (Exception e) {
            result = super.getThreadPoolStopTimeout();
        }
        LOGGER.exiting(this.getClass().getName(), "getThreadPoolStopTimeout", result);                 
        return result;
    }

    /**
     * Connector acceptor thread count. Defaults to -1. When -1, Jetty will
     * default to Runtime.availableProcessors() / 2, with a minimum of 1.
     *
     * @return Connector acceptor thread count.
     */
    @Override
    public int getConnectorAcceptors() {
        LOGGER.entering(this.getClass().getName(), "getConnectorAcceptors");   
        int result;
        try {
            result = settings.getInt(Consts.JETTY_ACCEPTOR_THREADS);
        } catch (Exception e) {
            result = super.getConnectorAcceptors();
        }
        LOGGER.exiting(this.getClass().getName(), "getConnectorAcceptors", result);           
        return result;
    }

    /**
     * Connector selector thread count. Defaults to -1. When 0, Jetty will
     * default to Runtime.availableProcessors().
     *
     * @return Connector acceptor thread count.
     */
    @Override
    public int getConnectorSelectors() {
        LOGGER.entering(this.getClass().getName(), "getConnectorSelectors");        
        int result;
        try {
            result = settings.getInt(Consts.JETTY_SELECTOR_THREADS);
        } catch (Exception e) {
            result = super.getConnectorSelectors();
        }
        LOGGER.exiting(this.getClass().getName(), "getConnectorSelectors", result);                   
        return result;
    }

    /**
     * Low resource monitor idle timeout in milliseconds. Defaults to 1000.
     * Applied to EndPoints when in the low resources state.
     *
     * @return Low resource monitor idle timeout.
     */
    @Override
    public int getLowResourceMonitorIdleTimeout() {
        LOGGER.entering(this.getClass().getName(), "getLowResourceMonitorIdleTimeout");                   
        int result;
        try {
            result = settings.getInt(Consts.JETTY_LOW_RESOURCES_MAX_IDLE_TIME_MS);
        } catch (Exception e) {
            result = super.getLowResourceMonitorIdleTimeout();
        }
        LOGGER.exiting(this.getClass().getName(), "getLowResourceMonitorIdleTimeout", result);                           
        return result;
    }

    /**
     * Low resource monitor period in milliseconds. Defaults to 1000. When 0,
     * low resource monitoring is disabled.
     *
     * @return Low resource monitor period.
     */
    @Override
    public int getLowResourceMonitorPeriod() {
        LOGGER.entering(this.getClass().getName(), "getLowResourceMonitorPeriod");                           
        int result;
        try {
            result = settings.getInt(Consts.JETTY_LOW_RESOURCES_PERIOD);
        } catch (Exception e) {
            result = super.getLowResourceMonitorPeriod();
        }
        LOGGER.exiting(this.getClass().getName(), "getLowResourceMonitorPeriod", result);                                   
        return result;
    }

    /**
     * Low resource monitor max memory in bytes. Defaults to 0. When 0, the
     * check disabled. Memory used is calculated as (totalMemory-freeMemory).
     *
     * @return Low resource monitor max memory.
     */
    @Override
    public long getLowResourceMonitorMaxMemory() {
        LOGGER.entering(this.getClass().getName(), "getLowResourceMonitorMaxMemory");                                   
        long result;
        try {
            result = settings.getLong(Consts.JETTY_LOW_RESOURCES_MAX_MEMORY);
        } catch (Exception e) {
            result = super.getLowResourceMonitorMaxMemory();
        }
        LOGGER.exiting(this.getClass().getName(), "getLowResourceMonitorMaxMemory", result);                                   
        return result;
    }

    /**
     * Low resource monitor max connections. Defaults to 0. When 0, the check is
     * disabled.
     *
     * @return Low resource monitor max connections.
     */
    @Override
    public int getLowResourceMonitorMaxConnections() {
        LOGGER.entering(this.getClass().getName(), "getLowResourceMonitorMaxConnections");                                           
        int result;
        try {
            result = settings.getInt(Consts.JETTY_LOW_RESOURCES_MAX_MEMORY);
        } catch (Exception e) {
            result = super.getLowResourceMonitorMaxConnections();
        }
        LOGGER.exiting(this.getClass().getName(), "getLowResourceMonitorMaxConnections", result);                                                   
        return result;
    }

    /**
     * Low resource monitor, whether to check if we're low on threads. Defaults
     * to true.
     *
     * @return Low resource monitor threads.
     */
    @Override
    public boolean getLowResourceMonitorThreads() {
        LOGGER.entering(this.getClass().getName(), "getLowResourceMonitorThreads");                                                   
        boolean result;
        try {
            result = settings.getBoolean(Consts.JETTY_LOW_RESOURCES_THREADS);
        } catch (Exception e) {
            result =  super.getLowResourceMonitorThreads();
        }
        LOGGER.exiting(this.getClass().getName(), "getLowResourceMonitorThreads", result);                                                   
        return result;
    }

    /**
     * Connector accept queue size. Defaults to 0. Also known as accept backlog.
     *
     * @return Connector accept queue size.
     */
    @Override
    public int getConnectorAcceptQueueSize() {
        LOGGER.entering(this.getClass().getName(), "getConnectorAcceptQueueSize");                                                   
        int result;
        try {
            result = settings.getInt(Consts.JETTY_ACCEPT_QUEUE_SIZE);
        } catch (Exception e) {
            result = super.getConnectorAcceptQueueSize();
        }
        LOGGER.exiting(this.getClass().getName(), "getConnectorAcceptQueueSize");                                                           
        return result;
    }

    /**
     * Connector TCP/IP SO linger time in milliseconds. Defaults to -1
     * (disabled).
     *
     * @return Connector TCP/IP SO linger time.
     */
    @Override
    public int getConnectorSoLingerTime() {
        LOGGER.entering(this.getClass().getName(), "getConnectorSoLingerTime");                                                           
        int result;
        try {
            result = settings.getInt(Consts.JETTY_SO_LINGER_TIME);
        } catch (Exception e) {
            result = super.getConnectorSoLingerTime();
        }
        LOGGER.exiting(this.getClass().getName(), "getConnectorSoLingerTime", result);                                                           
        return result;
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
        LOGGER.entering(this.getClass().getName(), "getConnectorIdleTimeout");  
        int result;
        try {
            result = settings.getInt(Consts.JETTY_IO_MAX_IDLE_TIME_MS);
        } catch (Exception e) {
            result = super.getConnectorIdleTimeout();
        }
        LOGGER.exiting(this.getClass().getName(), "getConnectorIdleTimeout", result);                                                                   
        return result;
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
        LOGGER.entering(this.getClass().getName(), "getHttpOutputBufferSize");                                                                           
        int result;
        try {
            result = settings.getInt(Consts.JETTY_RESPONSE_BUFFER_SIZE);
        } catch (Exception e) {
            result = super.getHttpOutputBufferSize();
        }
        LOGGER.exiting(this.getClass().getName(), "getHttpOutputBufferSize", result);                                                                                   
        return result;
    }

    /**
     * HTTP header cache size in bytes. Defaults to 512.
     *
     * @return HTTP header cache size.
     */
    @Override
    public int getHttpHeaderCacheSize() {
        LOGGER.entering(this.getClass().getName(), "getHttpHeaderCacheSize");
        int result;
        try {
            result = settings.getInt(Consts.JETTY_REQUEST_BUFFER_SIZE);
        } catch (Exception e) {
            result = super.getHttpHeaderCacheSize();
        }
        LOGGER.exiting(this.getClass().getName(), "getHttpHeaderCacheSize", result);        
        return result;
    }

    /**
     * Connector stop timeout in milliseconds. Defaults to 30000. The maximum
     * time allowed for the service to shutdown.
     *
     * @return Connector stop timeout.
     */
    @Override
    public int getConnectorStopTimeout() {
        LOGGER.entering(this.getClass().getName(), "getConnectorStopTimeout");   
        int result;
        try {
            result = settings.getInt(Consts.JETTY_GRACEFUL_SHUTDOWN);
        } catch (Exception e) {
            result = super.getConnectorStopTimeout();
        }
        LOGGER.exiting(this.getClass().getName(), "getConnectorStopTimeout", result);                
        return result;
    }

    /**
     * Low resource monitor stop timeout in milliseconds. Defaults to 30000. The
     * maximum time allowed for the service to shutdown.
     *
     * @return Low resource monitor stop timeout.
     */
    @Override
    public long getLowResourceMonitorStopTimeout() {
        LOGGER.entering(this.getClass().getName(), "getLowResourceMonitorStopTimeout"); 
        long result;
        try {
            result = settings.getLong(Consts.JETTY_GRACEFUL_SHUTDOWN);
        } catch (Exception e) {
            result = super.getLowResourceMonitorStopTimeout();
        }
        LOGGER.entering(this.getClass().getName(), "getLowResourceMonitorStopTimeout", result);                        
        return result;
    }

    /**
     * addParamsToServerContext
     *
     */
    public void addParamsToServerContext() {
        LOGGER.entering(this.getClass().getName(), "addParamsToServerContext");
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
        LOGGER.entering(this.getClass().getName(), "addParamsToServerContext");        
    }

    /**
     * Add a parameter to a Jetty server.
     *
     * @param propName the name of the property to add
     * @param propValue the value to set
     */
    private void addParam(final String propName, final Object propValue) {
        Parameter param = this.server.getContext().getParameters().getFirst(propName);
        if (param != null) {
            param.setValue("" + propValue);
            LOGGER.log(Level.CONFIG, "add to an existant param {0} : {1}", new Object[]{propName, propValue});            
        } else {
            this.server.getContext().getParameters().add(propName, "" + propValue);
            LOGGER.log(Level.CONFIG, "add to a new param {0} : {1}", new Object[]{propName, propValue});                        
        }
    }
}
