/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import fr.cnes.doi.utils.Utils;
import java.util.List;
import java.util.logging.Logger;
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
     * Logger.
     */
    private static final Logger LOGGER = Utils.getAppLogger();

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
    //TODO check io on ne peut pas utiliser the challenge authentication depuis que j'ai corrigé des problèmes

    /**
     * Returns the client.
     * @return the client
     */
    public ClientResource getClient() {
        return client;
    }
    
    /**
     * Returns the logger.
     * @return the LOGGER
     */
    public static Logger getLOGGER() {
        return LOGGER;
    }    

}
