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

import fr.cnes.doi.utils.HttpClientHelperPatch;
import java.util.Arrays;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.resource.ClientResource;

import fr.cnes.doi.settings.ProxySettings;
import java.util.List;
import org.restlet.data.Parameter;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.util.Series;

/**
 * Base client
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr(
 */
public class BaseClient {

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
        this.client = new ClientResource(uri);
        this.client.setLoggable(false);
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
        params.add("proxyHost", host);
        params.add("proxyPort", port);
        this.getClient().setNext(proxy);
    }

    /**
     * Returns the client.
     * @return the client
     */
    public ClientResource getClient() {
        return client;
    }  

}
