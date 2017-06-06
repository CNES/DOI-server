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

    public JettySettings(final Server server, final DoiSettings settings) {
        super(server);
        this.server = server;
        this.settings = settings;
    }

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

    @Override
    public int getThreadPoolMinThreads() {
        try {
            return settings.getInt(Consts.JETTY_MIN_THREADS);
        } catch (Exception e) {
            return super.getThreadPoolMinThreads();
        }
    }

    @Override
    public int getThreadPoolMaxThreads() {
        try {
            return settings.getInt(Consts.JETTY_MAX_THREADS);
        } catch (Exception e) {
            return super.getThreadPoolMaxThreads();
        }
    }

    @Override
    public int getThreadPoolThreadsPriority() {
        try {
            return settings.getInt(Consts.JETTY_THREADS_PRIORITY);
        } catch (Exception e) {
            return super.getThreadPoolThreadsPriority();
        }        
    }
        
    @Override
    public int getThreadPoolIdleTimeout() {
        try {
            return settings.getInt(Consts.JETTY_THREAD_MAX_IDLE_TIME_MS);
        } catch (Exception e) {
            return super.getThreadPoolIdleTimeout();
        }
    }

    @Override
    public long getThreadPoolStopTimeout() {
        try {
            return settings.getLong(Consts.JETTY_THREAD_STOP_TIME_MS);
        } catch (Exception e) {
            return super.getThreadPoolStopTimeout();
        }        
    }
    
    @Override
    public int getConnectorAcceptors() {
        try {
            return settings.getInt(Consts.JETTY_ACCEPTOR_THREADS);
        } catch (Exception e) {
            return super.getConnectorAcceptors();
        }
    }

    @Override
    public int getConnectorSelectors() {
        try {
            return settings.getInt(Consts.JETTY_SELECTOR_THREADS);
        } catch (Exception e) {
            return super.getConnectorSelectors();
        }        
    }    

    @Override
    public int getLowResourceMonitorIdleTimeout() {
        try {
            return settings.getInt(Consts.JETTY_LOW_RESOURCES_MAX_IDLE_TIME_MS);
        } catch (Exception e) {
            return super.getLowResourceMonitorIdleTimeout();
        }
    }

    @Override
    public int getLowResourceMonitorPeriod() {
        try {
            return settings.getInt(Consts.JETTY_LOW_RESOURCES_PERIOD);
        } catch (Exception e) {
            return super.getLowResourceMonitorPeriod();
        }        
    }

    @Override
    public long getLowResourceMonitorMaxMemory() {
        try {
            return settings.getLong(Consts.JETTY_LOW_RESOURCES_MAX_MEMORY);
        } catch (Exception e) {
            return super.getLowResourceMonitorMaxMemory();
        }          
    }

    @Override
    public int getLowResourceMonitorMaxConnections() {
        try {
            return settings.getInt(Consts.JETTY_LOW_RESOURCES_MAX_MEMORY);
        } catch (Exception e) {
            return super.getLowResourceMonitorMaxConnections();
        }          
    }

    @Override
    public boolean getLowResourceMonitorThreads() {
        try {
            return settings.getBoolean(Consts.JETTY_LOW_RESOURCES_THREADS);
        } catch (Exception e) {
            return super.getLowResourceMonitorThreads();
        }         
    }  


    @Override
    public int getConnectorAcceptQueueSize() {
        try {
            return settings.getInt(Consts.JETTY_ACCEPT_QUEUE_SIZE);
        } catch (Exception e) {
            return super.getConnectorAcceptQueueSize();
        }
    }
        

    @Override
    public int getConnectorSoLingerTime() {
        try {
            return settings.getInt(Consts.JETTY_SO_LINGER_TIME);
        } catch (Exception e) {
            return super.getConnectorSoLingerTime();
        }
    }

    @Override
    public int getConnectorIdleTimeout() {
        try {
            return settings.getInt(Consts.JETTY_IO_MAX_IDLE_TIME_MS);
        } catch (Exception e) {
            return super.getConnectorIdleTimeout();
        }
    }

    @Override
    public int getHttpOutputBufferSize() {
        try {
            return settings.getInt(Consts.JETTY_RESPONSE_BUFFER_SIZE);
        } catch (Exception e) {
            return super.getHttpOutputBufferSize();
        }
    }

    @Override
    public int getHttpHeaderCacheSize() {
        try {
            return settings.getInt(Consts.JETTY_REQUEST_BUFFER_SIZE);
        } catch (Exception e) {
            return super.getHttpHeaderCacheSize();
        }
    }

    @Override
    public int getConnectorStopTimeout() {
        try {
            return settings.getInt(Consts.JETTY_GRACEFUL_SHUTDOWN);
        } catch (Exception e) {
            return super.getConnectorStopTimeout();
        }
    }

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
     *
     * @param serverHTTP the service to add the parameters to
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
