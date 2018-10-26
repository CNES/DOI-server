package fr.cnes.lib.proxy;

/*
 * Copyright [2018] [S. ETCHEVERRY (CNES)]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * This class is an example of the used of the ProxySPNego classes It uses a
 * ProxyHttpClientBuilder to connect to authenticate to a proxy host Several
 * websites are called to demonstrate the cache of the kerberos token
 */
public class ProxySPNegoExample {

    private static final Logger LOGGER = Logger.getLogger("ProxySPNegoExmaple");

    /**
     * Example of the use of the Proxy SPNego scheme
     * 
     * @param args
     *            <proxyHost> <proxyPort> <userId> (<keytabFilePath>)
     *            (<ticketCachePath>)
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {

	if (args.length < 3) {
	    System.out.println(
		    "Usage: ProxySPNegoExample <proxyHost> <proxyPort> <userId> (<keytabFilePath>) (<ticketCachePath>)");
	    System.exit(1);
	}

	final String proxyHost = args[0];
	final int proxyPort = Integer.parseInt(args[1]);
	final String userId = args[2];

	String keytabPath = null;
	if (args.length > 3) {
	    keytabPath = args[3];
	}

	String ticketCachePath = null;
	if (args.length > 4) {
	    ticketCachePath = args[4];
	}

	final String krbConfPath = "/etc/krb5.conf";

	try {

	    // Get a Http Builder prepared for Kerberized Auth with a web proxy
	    HttpClientBuilder builder = ProxyHttpClientBuilder.getHttpClientBuilder(userId,
		    keytabPath, ticketCachePath, krbConfPath, proxyHost, proxyPort);
	    CloseableHttpClient httpclient = builder.build();

	    try {
		// prepare the request
		HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
		RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
		HttpGet request = new HttpGet("/");
		request.setConfig(config);

		HttpHost target = new HttpHost("www.google.com", 443, "https");
		System.out.println("Executing request " + request.getRequestLine() + " to " + target
			+ " via " + proxy);
		CloseableHttpResponse response = httpclient.execute(target, request);
		try {
		    System.out.println("----------------------------------------\n"
			    + response.getStatusLine());
		    // System.out.println(EntityUtils.toString(response.getEntity()));
		} finally {
		    response.close();
		}

		target = new HttpHost("www.nasa.gov", 443, "https");
		System.out.println("Executing request " + request.getRequestLine() + " to " + target
			+ " via " + proxy);
		response = httpclient.execute(target, request);
		try {
		    System.out.println("----------------------------------------\n"
			    + response.getStatusLine());
		    // System.out.println(EntityUtils.toString(response.getEntity()));
		} finally {
		    response.close();
		}

		target = new HttpHost("www.larousse.fr", 80, "http");
		System.out.println("Executing request " + request.getRequestLine() + " to " + target
			+ " via " + proxy);
		response = httpclient.execute(target, request);
		try {
		    System.out.println("----------------------------------------\n"
			    + response.getStatusLine());
		    // System.out.println(EntityUtils.toString(response.getEntity()));
		} finally {
		    response.close();
		}

	    } finally {
		httpclient.close();
	    }

	} catch (Exception e) {
	    LOGGER.info(e.toString());
	}
    }

}
