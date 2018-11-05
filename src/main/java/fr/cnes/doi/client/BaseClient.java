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
import java.util.List;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.resource.ClientResource;

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
     * Number of retry when an error happens.
     */
    private static final int NB_RETRY = 10;

    /**
     * Delay between two {@link #NB_RETRY} in ms.
     */
    private static final int NB_DELAY = 1000;
    
    /**
     * Client, which executes request.
     */
    private volatile ClientResource client;

    static {
        final List<ConnectorHelper<Client>> registeredClients = Engine.getInstance().
                getRegisteredClients();
        registeredClients.add(0, new HttpClientHelperPatch());
    }
    /**
     * Constructor.
     *
     * @param uri URI of the client's end point
     */
    public BaseClient(final String uri) {
        this.client = new ClientResource(uri);
        this.client.setLoggable(false);
        this.client.setRetryOnError(true);
        this.client.setRetryAttempts(NB_RETRY);
        this.client.setRetryDelay(NB_DELAY);
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
