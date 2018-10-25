/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.client;

import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.HttpClientHelperPatch;
import java.util.Arrays;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;

import fr.cnes.doi.settings.ProxySettings;
import java.util.List;
import org.restlet.data.Parameter;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.util.Series;

/**
 * Base client
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr(
 */
public class BaseClient {
    
    /**
     * Port of the Datacite mockserver
     */
    public static final int DATACITE_MOCKSERVER_PORT = 1080;

    /**
     * Client, which executes request.
     */
    private final ClientResource client;

    /**
     * Constructor.
     *
     * @param uri URI of the client's end point
     */
    public BaseClient(final String uri) {        
        final  List<ConnectorHelper<Client>> registeredClients = 
                Engine.getInstance().getRegisteredClients();
        //Engine.getInstance().getRegisteredClients().clear();
        registeredClients.add(new HttpClientHelperPatch(null));
        //Engine.setLogLevel(Level.WARNING);
        this.client = new ClientResource(uri);
        this.client.setLoggable(false);
        this.client.setRetryOnError(true);
        this.client.setRetryAttempts(10);
        this.client.setRetryDelay(1000);
        configureProxyIfNeeded();
    }

    /**
     * Configure the proxy parameters is needed (Starter.Proxy.used = true)
     */
    private void configureProxyIfNeeded() {
        final ProxySettings proxySettings = ProxySettings.getInstance();
        if (proxySettings.isWithProxy()) {
            setProxyAuthentication(proxySettings.getProxyHost(),
                    proxySettings.getProxyPort(), proxySettings.getProxyUser(),
                    proxySettings.getProxyPassword());
        }
    }

    /**
     * Sets proxy authentication for HTTP and HTTP connection.
     *
     * @param host Proxy hostname
     * @param port proxy port
     * @param login proxy login
     * @param pwd proxy password
     */
    public void setProxyAuthentication(final String host, final String port, 
            final String login, final String pwd) {        
        final Client proxy = new Client(
                new Context(), 
                Arrays.asList(Protocol.HTTP, Protocol.HTTPS)
        );
        final Series<Parameter> params = proxy.getContext().getParameters(); 
        params.set(RESTLET_MAX_TOTAL_CONNECTIONS, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS));        
        params.set(RESTLET_MAX_CONNECTIONS_PER_HOST, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST, DEFAULT_MAX_CONNECTIONS_PER_HOST));
        params.add("proxyHost", host);
        params.add("proxyPort", port);
        this.getClient().setNext(proxy);
    }

    /**
     * Returns the client.
     * @return the client
     */
    public final ClientResource getClient() {
        return client;
    }  

}
