/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.settings;

/**
 *
 * @author malapert
 */
public class Consts {
    
    public static final String APP_NAME = "Starter.APP_NAME";
    public static final String VERSION = "Starter.VERSION";
    public static final String COPYRIGHT = "Starter.COPYRIGHT";
 
    public static final String INIST_DOI = "Starter.Inist.doi";        
    public static final String INIST_LOGIN = "Starter.Inist.login";
    public static final String INIST_PWD = "Starter.Inist.pwd";
    
    public static final String SERVER_HTTP_PORT = "Starter.Server.HTTP.Port";
    
    public static final String SERVER_PROXY_USED = "Starter.Proxy.used";
    public static final String SERVER_PROXY_HOST = "Starter.Proxy.host";
    public static final String SERVER_PROXY_LOGIN = "Starter.Proxy.login";
    public static final String SERVER_PROXY_PWD = "Starter.Proxy.pwd";    
    
    
    public static final String PROPERTY_CONTACT_ADMIN = "CONTACT_ADMIN";
    public static final String PROPERTY_LOG_FORMAT = "LOG_FORMAT";
    public static final String PROPERTY_LOG_FILE = "PROPERTY_LOG_FILE";

    public static final String LOGSERVICE_LOGNAME = "Starter.LogService.logName";
    public static final String LOGSERVICE_ACTIVE = "Starter.LogService.active";
    public static final String LOG_FORMAT = "Starter.Log.format";
    
    
    // Server
    public static final String SERVER_MAX_CONNECTIONS = "Starter.Server.maxConnections";
    public static final String SERVER_CONTACT_ADMIN = "Starter.Server.contactAdmin";
    

    public static final String PROXY_USED = "Starter.Proxy.used";
    public static final String PROXY_HOST = "Starter.Proxy.host";
    public static final String PROXY_PORT = "Starter.Proxy.port";
    public static final String PROXY_USER = "Starter.Proxy.login";
    public static final String PROXY_PASSWORD = "Starter.Proxy.pwd";
    public static final String NONPROXY_HOSTS = "Starter.NoProxy.hosts"; 

    public static final String SMTP_PROTOCOL = "Starter.mail.send.protocol";    
    public static final String SMTP_URL = "Starter.mail.send.server";
    public static final String SMTP_STARTTLS_ENABLE = "Starter.mail.send.tls";
    public static final String SMTP_AUTH_USER = "Starter.mail.send.identifier";
    public static final String SMTP_AUTH_PWD = "Starter.mail.send.secret";    
    

    // Jetty contants
    /**
     * Jetty : Thread pool minimum threads.
     */
    public static final String JETTY_MIN_THREADS = "Starter.MIN_THREADS";
    
    /**
     * Jetty : Thread pool max threads.
     */
    public static final String JETTY_MAX_THREADS = "Starter.MAX_THREADS";
    
    /**
     * Jetty : Thread pool threads priority.
     */
    public static final String JETTY_THREADS_PRIORITY = "Starter.THREADS_PRIORITY";
    
    /**
     * Jetty : Thread pool idle timeout in milliseconds; threads that are idle for longer than this period may be stopped.
     */
    public static final String JETTY_THREAD_MAX_IDLE_TIME_MS = "Starter.THREAD_MAX_IDLE_TIME_MS";
    
    /**
     * Thread pool stop timeout in milliseconds; the maximum time allowed for the service to shutdown.
     */
    public static final String JETTY_THREAD_STOP_TIME_MS = "Starter.THREAD_MAX_STOP_TIME_MS";
    
    /**
     * Time in ms that connections will persist if listener is low on resources.
     */
    public static final String JETTY_LOW_RESOURCES_MAX_IDLE_TIME_MS = "Starter.LOW_RESOURCES_MAX_IDLE_TIME_MS";
    
    /**
     * Low resource monitor period in milliseconds; when 0, low resource monitoring is disabled.
     */
    public static final String JETTY_LOW_RESOURCES_PERIOD = "Starter.LOW_RESOURCES_PERIOD";
    
    /**
     * Low resource monitor max memory in bytes; when 0, the check disabled; memory used is calculated as (totalMemory-freeMemory).
     */
    public static final String JETTY_LOW_RESOURCES_MAX_MEMORY = "Starter.LOW_RESOURCES_MAX_MEMORY";
    
    /**
     * Low resource monitor max connections; when 0, the check is disabled.
     */
    public static final String JETTY_LOW_RESOURCES_MAX_CONNECTIONS = "Starter.LOW_RESOURCES_MAX_CONNECTIONS";
    
    /**
     * Low resource monitor, whether to check if we're low on threads.
     */
    public static final String JETTY_LOW_RESOURCES_THREADS = "Starter.LOW_RESOURCES_THREADS";
    
    /**
     * Connector acceptor thread count; when -1, Jetty will default to Runtime.availableProcessors() / 2, with a minimum of 1.
     */
    public static final String JETTY_ACCEPTOR_THREADS = "Starter.ACCEPTOR_THREADS";
    
    /**
     * Connector selector thread count; when -1, Jetty will default to Runtime.availableProcessors().
     */
    public static final String JETTY_SELECTOR_THREADS = "Starter.SELECTOR_THREADS";
    /**
     * Connector accept queue size; also known as accept backlog.
     */
    public static final String JETTY_ACCEPT_QUEUE_SIZE = "Starter.ACCEPT_QUEUE_SIZE";
    /**
     * HTTP request header size in bytes; larger headers will allow for more 
     * and/or larger cookies plus larger form content encoded in a URL;
     * however, larger headers consume more memory and can make a server more 
     * vulnerable to denial of service attacks.
     */
    public static final String JETTY_REQUEST_HEADER_SIZE = "Starter.REQUEST_HEADER_SIZE";
    /**     
     * HTTP response header size in bytes; larger headers will allow for more 
     * and/or larger cookies and longer HTTP headers (e.g. for redirection); 
     * however, larger headers will also consume more memory
     */
    public static final String JETTY_RESPONSE_HEADER_SIZE = "Starter.RESPONSE_HEADER_SIZE";
    /**
     * HTTP header cache size in bytes.
     */
    public static final String JETTY_REQUEST_BUFFER_SIZE = "Starter.REQUEST_BUFFER_SIZE";
    /**
     * 	HTTP output buffer size in bytes; a larger buffer can improve performance
     * by allowing a content producer to run without blocking, however larger 
     * buffers consume more memory and may induce some latency before a client
     * starts processing the content.
     */
    public static final String JETTY_RESPONSE_BUFFER_SIZE = "Starter.RESPONSE_BUFFER_SIZE";
    /**
     * Connector idle timeout in milliseconds; see Socket.setSoTimeout(int); 
     * this value is interpreted as the maximum time between some progress 
     * being made on the connection; so if a single byte is read or written, 
     * then the timeout is reset.
     */
    public static final String JETTY_IO_MAX_IDLE_TIME_MS = "Starter.IO_MAX_IDLE_TIME_MS";
    /**
     * Connector TCP/IP SO linger time in milliseconds; when -1 is disabled; 
     * see Socket.setSoLinger(boolean, int).
     */
    public static final String JETTY_SO_LINGER_TIME = "Starter.SO_LINGER_TIME";
    /**
     * Connector stop timeout in milliseconds; 
     * the maximum time allowed for the service to shutdown
     */
    public static final String JETTY_GRACEFUL_SHUTDOWN = "Starter.GRACEFUL_SHUTDOWN";

}
