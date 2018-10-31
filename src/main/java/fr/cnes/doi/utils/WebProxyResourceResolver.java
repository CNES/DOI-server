/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package fr.cnes.doi.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class WebProxyResourceResolver implements LSResourceResolver {

    final ClientResource client;
    final String referenceSchemaURI;

    public WebProxyResourceResolver(final ClientResource client,
            final String schemaURL) {
        this.client = client;
        this.referenceSchemaURI = schemaURL.substring(0, schemaURL.lastIndexOf('/'));
    }

    private Representation loadContent(final String schemaURL) throws ResourceException {
        this.client.setReference(schemaURL);
        try {
            Representation rep = this.client.get();
            return rep;
        } finally {
            this.client.release();
        }
    }

    @Override
    public LSInput resolveResource(String type,
            String namespaceURI,
            String publicId,
            String systemId,
            String baseURI) {
        final String schemaURL = this.referenceSchemaURI != null ? (this.referenceSchemaURI + "/" + systemId) : systemId;
        final Representation rep = loadContent(schemaURL);
        WebProxyInput input = new WebProxyInput(publicId, systemId, rep);
        input.setBaseURI(baseURI);
        return input;
    }

    public class WebProxyInput implements LSInput {

        private String publicId;
        private String systemId;
        private Representation rep;

        public WebProxyInput(String publicId,
                String sysId,
                Representation rep) {
            this.publicId = publicId;
            this.systemId = sysId;
        }

        @Override
        public String getPublicId() {
            return publicId;
        }

        @Override
        public void setPublicId(String publicId) {
            this.publicId = publicId;
        }

        @Override
        public String getBaseURI() {
            return null;
        }

        @Override
        public InputStream getByteStream() {
            return null;
        }

        @Override
        public boolean getCertifiedText() {
            return false;
        }

        @Override
        public Reader getCharacterStream() {
            return null;
        }

        @Override
        public String getEncoding() {
            return null;
        }

        @Override
        public String getStringData() {
            try {
                return rep.getText();
            } catch (IOException ex) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot get the schema");
            }
        }

        @Override
        public void setBaseURI(String baseURI) {
        }

        @Override
        public void setByteStream(InputStream byteStream) {
        }

        @Override
        public void setCertifiedText(boolean certifiedText) {
        }

        @Override
        public void setCharacterStream(Reader characterStream) {
        }

        @Override
        public void setEncoding(String encoding) {
        }

        @Override
        public void setStringData(String stringData) {
        }

        @Override
        public String getSystemId() {
            return systemId;
        }

        @Override
        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }

    }

}
