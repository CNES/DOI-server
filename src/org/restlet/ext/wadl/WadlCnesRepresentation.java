/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.restlet.ext.wadl;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.Engine;
import org.restlet.ext.xml.TransformRepresentation;
import org.restlet.representation.Representation;
import org.restlet.ext.xml.XmlRepresentation;
import org.restlet.representation.InputRepresentation;

/**
 *
 * @author malapert
 */
public class WadlCnesRepresentation extends WadlRepresentation {
    
    public WadlCnesRepresentation(ApplicationInfo application) {
        super(application);
    }
    
    @Override
    public Representation getHtmlRepresentation() {
        Representation representation = null;
        URL wadl2htmlXsltUrl = Engine
                .getResource("org/restlet/ext/wadl/wadlcnes2html.xslt");

        if (wadl2htmlXsltUrl != null) {
            try {
                // The SAX source is systematically generated:
                // - when instantiated using an ApplicationInfo or a
                // ResourceInfo the sax source is null.
                // - when instantiated using an XML representation, the
                // underlying sax source is exhausted, because we parse it in
                // order to recover the WADL document.
                setSaxSource(XmlRepresentation.getSaxSource(this));
                InputRepresentation xslRep = new InputRepresentation(
                        wadl2htmlXsltUrl.openStream(),
                        MediaType.APPLICATION_W3C_XSLT);
                representation = new TransformRepresentation(
                        Context.getCurrent(), this, xslRep);
                representation.setMediaType(MediaType.TEXT_HTML);
            } catch (IOException e) {
                Context.getCurrent()
                        .getLogger()
                        .log(Level.WARNING,
                                "Unable to generate the WADL HTML representation",
                                e);
            }
        }

        return representation;
    }    
    
}
