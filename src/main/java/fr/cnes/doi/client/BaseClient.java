/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import java.util.Arrays;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.httpclient.HttpClientHelper;
import org.restlet.resource.ClientResource;

import fr.cnes.doi.settings.ProxySettings;

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

	/**
	 * Constructor.
	 * 
	 * @param uri
	 *            URI of the client's end point
	 */
	public BaseClient(final String uri) {
		Engine.getInstance().getRegisteredClients().clear();
		Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
		this.client = new ClientResource(uri);
		configureProxyIfNeeded();
	}

	/**
	 * Configure the proxy parameters is needed (Starter.Proxy.used = true)
	 */
	public void configureProxyIfNeeded() {
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
		this.client.setProxyChallengeResponse(ChallengeScheme.HTTP_BASIC, login, pwd);
		this.client.setNext(proxy);
	}

}
