/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import javax.net.ssl.SSLException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.engine.adapter.ClientCall;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.util.ReferenceUtils;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class HttpClientHelperJC extends org.restlet.engine.adapter.HttpClientHelper {

    private volatile CloseableHttpClient httpClient;
    
    /** the idle connection reaper. */
    private volatile HttpIdleConnectionReaper idleConnectionReaper;    

    public HttpClientHelperJC(Client client) {
        super(client);
        this.httpClient = null;
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
    }

    /**
     * Returns the wrapped Apache HTTP Client.
     *
     * @return The wrapped Apache HTTP Client.
     */
    public CloseableHttpClient getHttpClient() {
        return this.httpClient;
    }

    @Override
    public ClientCall create(Request request) {
        ClientCall result = null;

        try {
            result = new HttpMethodCall(this, request.getMethod().toString(),
                    ReferenceUtils.update(request.getResourceRef(), request)
                            .toString(), request.isEntityAvailable());
        } catch (IOException ex) {
            getLogger().log(Level.WARNING,
                    "Unable to create the HTTP client call", ex);
        }
        return result;
    }

    protected RequestConfig createHttpRequestParams() {
        RequestConfig.Builder rcb = RequestConfig.custom();
        rcb = rcb.setAuthenticationEnabled(false);
        rcb = rcb.setRedirectsEnabled(isFollowRedirects());
        rcb = rcb.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
        String httpProxyHost = getProxyHost();
        if (httpProxyHost != null) {
            HttpHost proxy = new HttpHost(httpProxyHost, getProxyPort());
            rcb = rcb.setProxy(proxy);
        }        
        return rcb.build();
    }    
    
    protected SocketConfig createSocketParams() {
        SocketConfig.Builder scb = SocketConfig.custom();
        scb = scb.setSoKeepAlive(true);
        scb = scb.setTcpNoDelay(getTcpNoDelay());
        scb = scb.setSoTimeout(getSocketTimeout());
        return scb.build();
        ////setConnectionTimeout
    }
    

    /**
     * Returns the host name of the HTTP proxy, if specified.
     *
     * @return the host name of the HTTP proxy, if specified.
     */
    public String getProxyHost() {
        return getHelpedParameters().getFirstValue("proxyHost",
                System.getProperty("http.proxyHost"));
    }

    /**
     * Returns the port of the HTTP proxy, if specified, 3128 otherwise.
     *
     * @return the port of the HTTP proxy.
     */
    public int getProxyPort() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "proxyPort", System.getProperty("http.proxyPort", "3128")));
    }

    /**
     * Returns the socket timeout value. A timeout of zero is interpreted as an
     * infinite timeout. Defaults to 60000.
     *
     * @return The read timeout value.
     */
    public int getSocketTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "socketTimeout", "60000"));
    }

    /**
     * Returns the connection timeout. Defaults to 15000.
     *
     * @return The connection timeout.
     */
    public int getSocketConnectTimeoutMs() {
        int result = 0;

        if (getHelpedParameters().getNames().contains("socketConnectTimeoutMs")) {
            result = Integer.parseInt(getHelpedParameters().getFirstValue(
                    "socketConnectTimeoutMs", "15000"));
        }

        return result;
    }

    /**
     * Indicates if the protocol will use Nagle's algorithm
     *
     * @return True to enable TCP_NODELAY, false to disable.
     * @see java.net.Socket#setTcpNoDelay(boolean)
     */
    public boolean getTcpNoDelay() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "tcpNoDelay", "false"));
    }

    /**
     * Indicates if the protocol will automatically follow redirects.
     *
     * @return True if the protocol will automatically follow redirects.
     */
    public boolean isFollowRedirects() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "followRedirects", "false"));
    }

    /**
     * Returns the class name of the retry handler to use instead of HTTP Client
     * default behavior. The given class name must implement the
     * org.apache.commons.httpclient.HttpMethodRetryHandler interface and have a
     * default constructor.
     *
     * @return The class name of the retry handler.
     */
    public String getRetryHandler() {
        return getHelpedParameters().getFirstValue("retryHandler", null);
    }

    /**
     * Configures the HTTP client. By default, it try to set the retry handler.
     *
     * @param httpClient The HTTP client to configure.
     */
    protected void configure( HttpClientBuilder httpClient) {
        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

            @Override
            public boolean retryRequest(
                    IOException exception,
                    int executionCount,
                    HttpContext context) {
                if (executionCount >= 5) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (exception instanceof InterruptedIOException) {
                    // Timeout
                    return false;
                }
                if (exception instanceof UnknownHostException) {
                    // Unknown host
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {
                    // Connection refused
                    return false;
                }
                if (exception instanceof SSLException) {
                    // SSL handshake exception
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // Retry if the request is considered idempotent
                    return true;
                }
                return false;
            }
        };
        if (getRetryHandler() != null) {
            try {
                HttpRequestRetryHandler retryHandler = (HttpRequestRetryHandler) Engine
                        .loadClass(getRetryHandler()).newInstance();
                httpClient.setRetryHandler(retryHandler);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                getLogger()
                        .log(Level.WARNING,
                                "An error occurred during the instantiation of the retry handler.",
                                e);
            }
        }
//        HttpContext context = new HttpCoreContext();
//        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
//                .register(CookieSpecs.IGNORE_COOKIES, new IgnoreCookieSpecFactory().create(context));
//                .register(CookieSpecs.STANDARD, cookieSpecProvider)
//                .build();
//        httpClient.setDefaultCookieSpecRegistry(cookieSpecRegistry);
    }
    
    /**
     * Returns the maximum number of active connections.
     * 
     * @return The maximum number of active connections.
     */
    public int getMaxTotalConnections() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxTotalConnections", "20"));
    }   
    
    /**
     * Returns the maximum number of connections that will be created for any
     * particular host.
     * 
     * @return The maximum number of connections that will be created for any
     *         particular host.
     */
    public int getMaxConnectionsPerHost() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxConnectionsPerHost", "10"));
    }    
    
    /**
     * Returns the class name of the hostname verifier to use instead of HTTP
     * Client default behavior. The given class name must implement
     * org.apache.http.conn.ssl.X509HostnameVerifier and have default no-arg
     * constructor.
     * 
     * @return The class name of the hostname verifier.
     */
    public String getHostnameVerifier() {
        return getHelpedParameters().getFirstValue("hostnameVerifier", null);
    }    

        /**
     * Configures the scheme registry. By default, it registers the HTTP and the
     * HTTPS schemes.
     * 
     * @return schemeRegistry
     *            The scheme registry to configure.
     */
    protected Registry<ConnectionSocketFactory>  createSchemeRegistry() {
        Registry<ConnectionSocketFactory> registry;
        try {         
            ConnectionSocketFactory sslConnectionFactory = new SSLSocketFactory(new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }, new AllowAllHostnameVerifier());        
            registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslConnectionFactory)
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .build();        
        
        } catch(NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException ex) {
            throw new RuntimeException(ex);
        }
   
        return registry;
    }
    
    /**
     * Creates the connection manager. By default, it creates a thread safe
     * connection manager.
     * 
     * @param schemeRegistry
     *            The scheme registry to use.
     * @return The created connection manager.
     */
    protected HttpClientConnectionManager createClientConnectionManager(Registry<ConnectionSocketFactory> schemeRegistry) {
        return new PoolingHttpClientConnectionManager(schemeRegistry);
    }    
    
    /**
     * Sets the idle connections reaper.
     * 
     * @param connectionReaper
     *            The idle connections reaper.
     */
    public void setIdleConnectionReaper(
            HttpIdleConnectionReaper connectionReaper) {
        this.idleConnectionReaper = connectionReaper;
    }    
    
    /**
     * Time in milliseconds between two checks for idle and expired connections.
     * The check happens only if this property is set to a value greater than 0.
     * 
     * @return A value indicating the idle connection check interval or 0 if a
     *         value has not been provided
     * @see #getIdleTimeout()
     */
    public long getIdleCheckInterval() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                "idleCheckInterval", "0"));
    }  
    
    /**
     * Returns the time in ms beyond which idle connections are eligible for
     * reaping. The default value is 60000 ms.
     * 
     * @return The time in millis beyond which idle connections are eligible for
     *         reaping.
     * @see #getIdleCheckInterval()
     */
    public long getIdleTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                "idleTimeout", "60000"));
    }    
    
    
    @Override
    public void start() throws Exception {
        super.start();
        RequestConfig rqc = createHttpRequestParams();        
        SocketConfig sc = createSocketParams();
        Registry<ConnectionSocketFactory>  registry = createSchemeRegistry();
        HttpClientConnectionManager connectionManager = createClientConnectionManager(registry);                
        this.httpClient = HttpClients.custom()
                .setMaxConnTotal(getMaxTotalConnections())
                .setMaxConnPerRoute(getMaxConnectionsPerHost())
                .setDefaultRequestConfig(rqc)
                .setDefaultSocketConfig(sc)      
                .setConnectionManager(connectionManager)
                .build();
        if (this.idleConnectionReaper != null) {
            // If a previous reaper is present, stop it
            this.idleConnectionReaper.stop();
        }

        this.idleConnectionReaper = new HttpIdleConnectionReaper(httpClient,
                getIdleCheckInterval(), getIdleTimeout());
        
        getLogger().info("Starting the Apache HTTP client");        
    }

    @Override
    public void stop() throws Exception {
        if (this.idleConnectionReaper != null) {
            this.idleConnectionReaper.stop();
        }        
        if (getHttpClient() != null) {
            getHttpClient().close();
            getLogger().info("Stopping the HTTP client");
        }
    }

}
