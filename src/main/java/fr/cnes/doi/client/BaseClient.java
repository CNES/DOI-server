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
import java.util.logging.Logger;

/**
 * Base client
 * 
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr(
 */
public class BaseClient {

	/**
	 * Client, which executes request;
	 */
	protected final ClientResource client;
        
        protected Logger LOGGER = Utils.getAppLogger();

	/**
	 * Constructor.
	 * 
	 * @param uri
	 *            URI of the client's end point
	 */
	public BaseClient(final String uri) {
		//Engine.getInstance().getRegisteredClients().clear();
		Engine.getInstance().getRegisteredClients().add(new HttpClientHelperPatch(null));
		this.client = new ClientResource(uri);
		configureProxyIfNeeded();
	}

	/**
	 * Configure the proxy parameters is needed (Starter.Proxy.used = true)
	 */
	private void configureProxyIfNeeded() {
		if (ProxySettings.getInstance().isWithProxy()) {
			setProxyAuthentication(ProxySettings.getInstance().getProxyHost(),
					ProxySettings.getInstance().getProxyPort(), ProxySettings.getInstance().getProxyUser(),
					ProxySettings.getInstance().getProxyPassword());
		}
	}

	/**
	 * Sets proxy authentication for HTTP and HTTP connection.
	 * 
	 * @param host
	 *            Proxy hostname
	 * @param port
	 *            proxy port
	 * @param login
	 *            proxy login
	 * @param pwd
	 *            proxy password
	 */
	public void setProxyAuthentication(final String host, final String port, final String login, final String pwd) {
		Client proxy = new Client(new Context(), Arrays.asList(Protocol.HTTP, Protocol.HTTPS));
		proxy.getContext().getParameters().add("proxyHost", host);
		proxy.getContext().getParameters().add("proxyPort", port);    
		this.client.setNext(proxy);
	}
        //TODO check io on ne peut pas utiliser the challenge authentication depuis que j'ai corrigé des problèmes

}
