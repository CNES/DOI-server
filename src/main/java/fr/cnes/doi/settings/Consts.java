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

/**
 * Consts contains all configuration variables, which are possible to use in the config.properties.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class Consts {

    /**
     * Application's name.
     */
    public static final String APP_NAME = "Starter.APP_NAME";

    /**
     * Application's version.
     */
    public static final String VERSION = "Starter.VERSION";

    /**
     * Name of the configuration file.
     */
    public static final String NAME = "Starter.NAME";

    /**
     * Application's copyright.
     */
    public static final String COPYRIGHT = "Starter.COPYRIGHT";

    /**
     * Context (DEV, POST_DEV, PRE_PROD, PROD) of the use.
     */
    public static final String CONTEXT_MODE = "Starter.CONTEXT_MODE";

    /**
     * DOI prefix, which is given by INIST.
     */
    public static final String INIST_DOI = "Starter.Inist.doi";

    /**
     * INIST login to use DATACITE's web services. The login is encrypted.
     */
    public static final String INIST_LOGIN = "Starter.Inist.login";

    /**
     * INIST password to use DATACITE's web services. The password is encrypted.
     */
    public static final String INIST_PWD = "Starter.Inist.pwd";

    /**
     * HTTP port number.
     */
    public static final String SERVER_HTTP_PORT = "Starter.Server.HTTP.Port";

    /**
     * HTTPS password for this application. The password is encrypted.
     */
    public static final String SERVER_HTTPS_SECRET_KEY = "Starter.Server.HTTPS.SecretKey";

    /**
     * HTTPS port number.
     */
    public static final String SERVER_HTTPS_PORT = "Starter.Server.HTTPS.Port";

    /**
     * HTTPS keystore path.
     */
    public static final String SERVER_HTTPS_KEYSTORE_PATH = "Starter.Server.HTTPS.KeystorePath";

    /**
     * HTTPS keystore password. The password is encrypted
     */
    public static final String SERVER_HTTPS_KEYSTORE_PASSWD
            = "Starter.Server.HTTPS.keystorePassword";

    /**
     * Trust store path.
     */
    public static final String SERVER_HTTPS_TRUST_STORE_PATH
            = "Starter.Server.HTTPS.TrustStorePath";

    /**
     * Trust store password. The password is encrypted
     */
    public static final String SERVER_HTTPS_TRUST_STORE_PASSWD
            = "Starter.Server.HTTPS.TrustStorePassword";

    /**
     * TODO.
     */
    public static final String SERVER_PROXY_TYPE = "Starter.Proxy.type";

    /**
     * Proxy host name.
     */
    public static final String SERVER_PROXY_HOST = "Starter.Proxy.host";

    /**
     * Proxy port number.
     */
    public static final String SERVER_PROXY_PORT = "Starter.Proxy.port";

    /**
     * Proxy login. The login is encrypted
     */
    public static final String SERVER_PROXY_LOGIN = "Starter.Proxy.login";

    /**
     * Proxy password. The password is encrypted
     */
    public static final String SERVER_PROXY_PWD = "Starter.Proxy.pwd";

    /**
     * Set of hostname on which the authentication to the proxy is disabled.
     */
    public static final String SERVER_NONPROXY_HOSTS = "Starter.NoProxy.hosts";

    /**
     * TODO.
     */
    public static final String SERVER_PROXY_JAAS_SPN = "Starter.Proxy.Jass.Spn";

    /**
     * TODO.
     */
    public static final String SERVER_PROXY_JAAS_FILE = "Starter.Proxy.Jass.File";

    /**
     * TODO.
     */
    public static final String SERVER_PROXY_JAAS_CONTEXT = "Starter.Proxy.Jass.Context";

    /**
     * Set token expiration delay during creation
     */
    public static final String TOKEN_EXPIRATION_DELAY = "Starter.Token.Delay";

    /**
     * Set token expiration unit during creation
     */
    public static final String TOKEN_EXPIRATION_UNIT = "Starter.Token.Unit";

    /**
     * LOG format.
     */
    public static final String LOG_FORMAT = "Starter.Server.Log.format";

    /**
     * Email of the administrator.
     */
    public static final String SERVER_CONTACT_ADMIN = "Starter.Server.contactAdmin";

    /**
     * Protocol name.
     */
    public static final String SMTP_PROTOCOL = "Starter.mail.send.protocol";
    /**
     * SMTP URL.
     */
    public static final String SMTP_URL = "Starter.mail.send.server";
    /**
     * TLS.
     */
    public static final String SMTP_STARTTLS_ENABLE = "Starter.mail.send.tls";

    /**
     * SMTP login. The login is encrypted.
     */
    public static final String SMTP_AUTH_USER = "Starter.mail.send.identifier";

    /**
     * SMTP password. The password is encrypted.
     */
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
     * Jetty : Thread pool idle timeout in milliseconds; threads that are idle for longer than this
     * period may be stopped.
     */
    public static final String JETTY_THREAD_MAX_IDLE_TIME_MS = "Starter.THREAD_MAX_IDLE_TIME_MS";

    /**
     * Thread pool stop timeout in milliseconds; the maximum time allowed for the service to
     * shutdown.
     */
    public static final String JETTY_THREAD_STOP_TIME_MS = "Starter.THREAD_MAX_STOP_TIME_MS";

    /**
     * Time in ms that connections will persist if listener is low on resources.
     */
    public static final String JETTY_LOW_RESOURCES_MAX_IDLE_TIME_MS
            = "Starter.LOW_RESOURCES_MAX_IDLE_TIME_MS";

    /**
     * Low resource monitor period in milliseconds; when 0, low resource monitoring is disabled.
     */
    public static final String JETTY_LOW_RESOURCES_PERIOD = "Starter.LOW_RESOURCES_PERIOD";

    /**
     * Low resource monitor max memory in bytes; when 0, the check disabled; memory used is
     * calculated as (totalMemory-freeMemory).
     */
    public static final String JETTY_LOW_RESOURCES_MAX_MEMORY = "Starter.LOW_RESOURCES_MAX_MEMORY";

    /**
     * Low resource monitor max connections; when 0, the check is disabled.
     */
    public static final String JETTY_LOW_RESOURCES_MAX_CONNECTIONS
            = "Starter.LOW_RESOURCES_MAX_CONNECTIONS";

    /**
     * Low resource monitor, whether to check if we're low on threads.
     */
    public static final String JETTY_LOW_RESOURCES_THREADS = "Starter.LOW_RESOURCES_THREADS";

    /**
     * Connector acceptor thread count; when -1, Jetty will default to Runtime.availableProcessors()
     * / 2, with a minimum of 1.
     */
    public static final String JETTY_ACCEPTOR_THREADS = "Starter.ACCEPTOR_THREADS";

    /**
     * Connector selector thread count; when -1, Jetty will default to
     * Runtime.availableProcessors().
     */
    public static final String JETTY_SELECTOR_THREADS = "Starter.SELECTOR_THREADS";
    /**
     * Connector accept queue size; also known as accept backlog.
     */
    public static final String JETTY_ACCEPT_QUEUE_SIZE = "Starter.ACCEPT_QUEUE_SIZE";
    /**
     * HTTP request header size in bytes; larger headers will allow for more and/or larger cookies
     * plus larger form content encoded in a URL; however, larger headers consume more memory and
     * can make a server more vulnerable to denial of service attacks.
     */
    public static final String JETTY_REQUEST_HEADER_SIZE = "Starter.REQUEST_HEADER_SIZE";
    /**
     * HTTP response header size in bytes; larger headers will allow for more and/or larger cookies
     * and longer HTTP headers (e.g. for redirection); however, larger headers will also consume
     * more memory
     */
    public static final String JETTY_RESPONSE_HEADER_SIZE = "Starter.RESPONSE_HEADER_SIZE";
    /**
     * HTTP header cache size in bytes.
     */
    public static final String JETTY_REQUEST_BUFFER_SIZE = "Starter.REQUEST_BUFFER_SIZE";
    /**
     * HTTP output buffer size in bytes; a larger buffer can improve performance by allowing a
     * content producer to run without blocking, however larger buffers consume more memory and may
     * induce some latency before a client starts processing the content.
     */
    public static final String JETTY_RESPONSE_BUFFER_SIZE = "Starter.RESPONSE_BUFFER_SIZE";
    /**
     * Connector idle timeout in milliseconds; see Socket.setSoTimeout(int); this value is
     * interpreted as the maximum time between some progress being made on the connection; so if a
     * single byte is read or written, then the timeout is reset.
     */
    public static final String JETTY_IO_MAX_IDLE_TIME_MS = "Starter.IO_MAX_IDLE_TIME_MS";
    /**
     * Connector TCP/IP SO linger time in milliseconds; when -1 is disabled; see
     * Socket.setSoLinger(boolean, int).
     */
    public static final String JETTY_SO_LINGER_TIME = "Starter.SO_LINGER_TIME";
    /**
     * Connector stop timeout in milliseconds; the maximum time allowed for the service to shutdown
     */
    public static final String JETTY_GRACEFUL_SHUTDOWN = "Starter.GRACEFUL_SHUTDOWN";
    /**
     * Path of the cache file to store landingPage/DOI association
     */
    public static final String DOI_CONF_PATH = "Starter.UniqueDoi.cache.file";
    /**
     * key for token signature encoded with the algorithm HS256 in base64
     */
    public static final String TOKEN_KEY = "Starter.Token.key";

    /**
     * Plugin to load the list of users and to fill group with users
     */
    public static final String PLUGIN_USER_GROUP_MGT = "Starter.Plugin.UserGroupMgt";

    /**
     * Plugin to load the project suffix database.
     */
    public static final String PLUGIN_PROJECT_SUFFIX = "Starter.Plugin.ProjectSuffix";

    /**
     * Plugin to load the token database.
     */
    public static final String PLUGIN_TOKEN = "Starter.Plugin.Token";

    /**
     * Plugin to load the DOI database.
     */
    public static final String PLUGIN_DOI = "Starter.Plugin.Doi";

    /**
     * Allowed IPs for the administration application.
     */
    public static final String ADMIN_IP_ALLOWER = "Starter.admin.IP.allower";

    /**
     * Level for which an alert must be send.
     */
    public static final String THRESHOLD_SPEED_PERCENT = "Starter.Monitoring.Threshold_speed";

    /**
     * Tunning connection for Restlet.
     */
    public static final String RESTLET_MAX_TOTAL_CONNECTIONS = "Starter.maxTotalConnections";

    /**
     * Tunning Connection for Restlet
     */
    public static final String RESTLET_MAX_CONNECTIONS_PER_HOST = "Starter.maxConnectionsPerHost";

    /**
     * DataCite schema.
     */
    public static final String DATACITE_SCHEMA = "Starter.dataciteSchema";

    /**
     * Update Database job period
     */
    public static final String DB_UPDATE_JOB_PERIOD = "Starter.updateDatabaseJob.period";

    /**
     * Database url
     */
    public static final String DB_URL = "Starter.Database.Doidburl";

    /**
     * Database user
     */
    public static final String DB_USER = "Starter.Database.User";

    /**
     * Database password
     */
    public static final String DB_PWD = "Starter.Database.Pwd";

    /**     
     * Minimum number of connection object that are to be kept alive in the pool
     */
    public static final String DB_MIN_IDLE_CONNECTIONS = "Starter.Database.MinIdleConnections";
    
    /**     
     * Minimum number of connection object that are to be kept alive in the pool
     */
    public static final String DB_MAX_IDLE_CONNECTIONS = "Starter.Database.MaxIdleConnections";    

    /**
     * Maximum number of active connections that can be allocated at the same time.
     */
    public static final String DB_MAX_ACTIVE_CONNECTIONS = "Starter.Database.MaxActiveConnections";

    /**
     * LDAP url
     */
    public static final String LDAP_URL = "Starter.LDAP.url";

    /**
     * LDAP user
     */
    public static final String LDAP_USER = "Starter.LDAP.user";

    /**
     * LDAP pwd
     */
    public static final String LDAP_PWD = "Starter.LDAP.password";

    /**
     * LDAP project
     */
    public static final String LDAP_PROJECT = "Starter.LDAP.project";
    
    /**
     * LDAP username who is the administrator of the DOI server
     */
    public static final String LDAP_DOI_ADMIN = "Starter.LDAP.user.admin";    
    
    /**
     * "Static" class cannot be instantiated
     */
    private Consts() {}

}
