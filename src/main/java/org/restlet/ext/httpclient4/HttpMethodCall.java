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
package org.restlet.ext.httpclient4;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Facade between HTTP call and the HTTP plugin.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class HttpMethodCall extends ClientCall {   

    /**
     * The associated HTTP client.
     */
    private volatile HttpClientHelper clientHelper;

    /**
     * The wrapped HTTP request.
     */
    private volatile HttpUriRequest httpRequest;

    /**
     * The wrapped HTTP response.
     */
    private volatile HttpResponse httpResponse;

    /**
     * Indicates if the response headers were added.
     */
    private volatile boolean responseHeadersAdded;

    /**
     * Constructor.
     *
     * @param helper The parent HTTP client helper.
     * @param method The method name.
     * @param requestUri The request URI.
     * @param hasEntity Indicates if the call will have an entity to send to the server.
     * @throws IOException when an error happens
     */
    public HttpMethodCall(HttpClientHelper helper, final String method,
            final String requestUri, boolean hasEntity) throws IOException {
        super(helper, method, requestUri);
        this.clientHelper = helper;

        if (requestUri.startsWith("http")) {
            if (method.equalsIgnoreCase(Method.GET.getName())) {
                this.httpRequest = new HttpGet(requestUri);
            } else if (method.equalsIgnoreCase(Method.POST.getName())) {
                this.httpRequest = new HttpPost(requestUri);
            } else if (method.equalsIgnoreCase(Method.PUT.getName())) {
                this.httpRequest = new HttpPut(requestUri);
            } else if (method.equalsIgnoreCase(Method.HEAD.getName())) {
                this.httpRequest = new HttpHead(requestUri);
            } else if (method.equalsIgnoreCase(Method.DELETE.getName())) {
                this.httpRequest = new HttpDelete(requestUri);
                if (hasEntity) {
                    getLogger()
                            .warning(
                                    "The current DELETE request provides an entity that may be not "
                                            + "supported by the Apache HTTP Client library. If you "
                                            + "face such issues, you can still move to another HTTP"
                                            + " client connector.");
                }
            } else if (method.equalsIgnoreCase(Method.OPTIONS.getName())) {
                this.httpRequest = new HttpOptions(requestUri);
            } else if (method.equalsIgnoreCase(Method.TRACE.getName())) {
                this.httpRequest = new HttpTrace(requestUri);
            } else {
                this.httpRequest = new HttpEntityEnclosingRequestBase() {

                    @Override
                    public String getMethod() {
                        return method;
                    }

                    @Override
                    public URI getURI() {
                        try {
                            return new URI(requestUri);
                        } catch (URISyntaxException e) {
                            getLogger().log(Level.WARNING,
                                    "Invalid URI syntax", e);
                            return null;
                        }
                    }
                };
            }

            this.responseHeadersAdded = false;
            setConfidential(this.httpRequest.getURI().getScheme()
                    .equalsIgnoreCase(Protocol.HTTPS.getSchemeName()));
        } else {
            throw new IllegalArgumentException(
                    "Only HTTP or HTTPS resource URIs are allowed here");
        }
    }

    /**
     * Returns the HTTP request.
     *
     * @return The HTTP request.
     */
    public HttpUriRequest getHttpRequest() {
        return this.httpRequest;
    }

    /**
     * Returns the HTTP response.
     *
     * @return The HTTP response.
     */
    public HttpResponse getHttpResponse() {
        return this.httpResponse;
    }

   /**
    * {@inheritDoc}
    */
    @Override
    public String getReasonPhrase() {
        if ((getHttpResponse() != null)
                && (getHttpResponse().getStatusLine() != null)) {
            return getHttpResponse().getStatusLine().getReasonPhrase();
        }
        return null;
    }

   /**
    * {@inheritDoc}
    * @return null
    */    
    @Override
    public WritableByteChannel getRequestEntityChannel() {
        return null;
    }

   /**
    * {@inheritDoc}
    * @return null
    */    
    @Override
    public OutputStream getRequestEntityStream() {
        return null;
    }

   /**
    * {@inheritDoc}
    * @return null
    */    
    @Override
    public OutputStream getRequestHeadStream() {
        return null;
    }

   /**
    * {@inheritDoc}
    * @return null
    */    
    @Override
    public ReadableByteChannel getResponseEntityChannel(long size) {
        return null;
    }

   /**
    * {@inheritDoc}
    */    
    @Override
    public InputStream getResponseEntityStream(long size) {
        InputStream result = null;

        try {
            //EntityUtils.toString(getHttpResponse().getEntity());
            // Return a wrapper filter that will release the connection when
            // needed
            InputStream responseStream = (getHttpResponse() == null) ? null
                    : (getHttpResponse().getEntity() == null) ? null
                    : getHttpResponse().getEntity().getContent();
            if (responseStream != null) {
                result = new FilterInputStream(responseStream) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        EntityUtils.consume(getHttpResponse().getEntity());
                    }
                };
            }
        } catch (IOException ioe) {
            this.clientHelper
                    .getLogger()
                    .log(Level.WARNING,
                            "An error occurred during the communication with the remote HTTP server.",
                            ioe);        
        }

        return result;
    }

   /**
    * {@inheritDoc}
    */
    @Override
    public Series<org.restlet.data.Header> getResponseHeaders() {
        final Series<org.restlet.data.Header> result = super.getResponseHeaders();

        if (!this.responseHeadersAdded) {
            if ((getHttpResponse() != null)
                    && (getHttpResponse().getAllHeaders() != null)) {
                for (Header header : getHttpResponse().getAllHeaders()) {
                    result.add(header.getName(), header.getValue());
                }
            }

            this.responseHeadersAdded = true;
        }

        return result;
    }

   /**
    * {@inheritDoc}
    */
    @Override
    public String getServerAddress() {
        return getHttpRequest().getURI().getHost();
    }

   /**
    * {@inheritDoc}
    */
    @Override
    public int getStatusCode() {
        if (getHttpResponse() != null
                && getHttpResponse().getStatusLine() != null) {
            return getHttpResponse().getStatusLine().getStatusCode();
        }
        return Status.CONNECTOR_ERROR_COMMUNICATION.getCode();
    }

   /**
    * {@inheritDoc}
    */
    @Override
    public Status sendRequest(Request request) {
        Status result = null;

        try {
            final Representation entity = request.getEntity();

            // Set the request headers
            for (org.restlet.data.Header header : getRequestHeaders()) {
                if (!header.getName().equals(
                        HeaderConstants.HEADER_CONTENT_LENGTH)) {
                    getHttpRequest().addHeader(header.getName(),
                            header.getValue());
                }
            }

            // For those method that accept enclosing entities, provide it
            if ((entity != null)
                    && (getHttpRequest() instanceof HttpEntityEnclosingRequestBase)) {
                final HttpEntityEnclosingRequestBase eem = (HttpEntityEnclosingRequestBase) getHttpRequest();
                eem.setEntity(new AbstractHttpEntity() {
                    /**
                     * {@inheritDoc}
                     */                    
                    @Override
                    public InputStream getContent() throws IOException,
                            IllegalStateException {
                        return entity.getStream();
                    }

                    /**
                     * {@inheritDoc}
                     */                    
                    @Override
                    public long getContentLength() {
                        return entity.getSize();
                    }

                    /**
                     * {@inheritDoc}
                     */                    
                    @Override
                    public Header getContentType() {
                        return new BasicHeader(
                                HeaderConstants.HEADER_CONTENT_TYPE, (entity
                                        .getMediaType() != null) ? entity
                                                .getMediaType().toString() : null);
                    }

                    /**
                     * {@inheritDoc}
                     */                    
                    @Override
                    public boolean isRepeatable() {
                        return !entity.isTransient();
                    }

                    /**
                     * {@inheritDoc}
                     */                    
                    @Override
                    public boolean isStreaming() {
                        return (entity.getSize() == Representation.UNKNOWN_SIZE);
                    }

                    /**
                     * {@inheritDoc}
                     */                    
                    @Override
                    public void writeTo(OutputStream os) throws IOException {
                        entity.write(os);
                        os.flush();
                    }
                });
            }

            // Ensure that the connection is active
            this.httpResponse = this.clientHelper.getHttpClient().execute(
                    getHttpRequest());
            //EntityUtils.toString(this.httpResponse.getEntity());
            // Now we can access the status code, this MUST happen after closing
            // any open request stream.
            result = new Status(getStatusCode(), getReasonPhrase());
        } catch (IOException ioe) {
            this.clientHelper
                    .getLogger()
                    .log(Level.WARNING,
                            "An error occurred during the communication with the remote HTTP server.",
                            ioe);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, ioe);

            // Release the connection
            getHttpRequest().abort();
        }

        return result;
    }

   /**
    * {@inheritDoc}
    */    
    @Override
    public void sendRequest(Request request, Response response, Uniform callback) throws Exception {
        sendRequest(request);

        if (request.getOnSent() != null) {
            request.getOnSent().handle(request, response);
        }

        if (callback != null) {
            // Transmit to the callback, if any.
            callback.handle(request, response);
        }
    }

}
