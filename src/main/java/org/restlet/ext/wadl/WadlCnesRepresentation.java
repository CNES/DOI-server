/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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
package org.restlet.ext.wadl;

import fr.cnes.doi.utils.spec.Requirement;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.Engine;
import org.restlet.ext.xml.TransformRepresentation;
import org.restlet.representation.Representation;
import org.restlet.ext.xml.XmlRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;

/**
 * Generates a WADL based on a patch of the Restlet XSLT.
 *
 * @author Jean-Christoph Malapert
 */
@Requirement(reqId = Requirement.DOI_DOC_010,reqName = Requirement.DOI_DOC_010_NAME)
public class WadlCnesRepresentation extends WadlRepresentation {

    /**
     * Constructs a new Wadl representation based on a new XSLT.
     *
     * @param application application
     */
    public WadlCnesRepresentation(final ApplicationInfo application) {
        super(application);
    }

    /**
     * Returns the HTML representation of the WADL
     *
     * @return HTML representation of the WADL
     */
    @Override
    public Representation getHtmlRepresentation() {
        Representation representation;
        final URL wadl2htmlXsltUrl = Engine
                .getResource("org/restlet/ext/wadl/wadlcnes2html.xslt");

        if (wadl2htmlXsltUrl == null) {
            representation = new EmptyRepresentation();
        } else {        
            try {
                setSaxSource(XmlRepresentation.getSaxSource(this));
                final InputRepresentation xslRep = new InputRepresentation(
                        wadl2htmlXsltUrl.openStream(),
                        MediaType.APPLICATION_W3C_XSLT);
                representation = new TransformRepresentation(
                        Context.getCurrent(), this, xslRep);
                representation.setMediaType(MediaType.TEXT_HTML);
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                                "Unable to generate the WADL HTML representation",
                                e);
                representation = new EmptyRepresentation();
            }
        }

        return representation;
    }

}
