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

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;

import java.util.List;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;

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
    private volatile ClientResource client;

    /**
     * Constructor.
     *
     * @param uri URI of the client's end point
     */
    public BaseClient(final String uri) {
        final List<ConnectorHelper<Client>> registeredClients = Engine.getInstance().
                getRegisteredClients();
        for (int i = registeredClients.size() - 1; i >= 0; i--) {
            ConnectorHelper<Client> conn = registeredClients.get(i);
            if (conn.getProtocols().contains(Protocol.HTTP) || conn.getProtocols().contains(
                    Protocol.HTTPS)) {
                registeredClients.remove(i);
            }
        }
        registeredClients.add(new HttpClientHelperPatch());
        Engine.getInstance().setRegisteredClients(registeredClients);
        this.client = new ClientResource(uri);
        this.client.setLoggable(false);
        this.client.setRetryOnError(true);
        this.client.setRetryAttempts(10);
        this.client.setRetryDelay(1000);
    }

    /**
     * Returns the client.
     *
     * @return the client
     */
    public final ClientResource getClient() {
        return client;
    }

}
