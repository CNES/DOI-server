/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.restlet.ext.wadl;

import fr.cnes.doi.utils.Requirement;
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
 * Generates a WADL based on a patch of the Restlet XSLT.
 * @author Jean-Christoph Malapert
 */
@Requirement(
        reqId = "DOI_DOC_010",
        reqName = "Documentation des interfaces"
)
public class WadlCnesRepresentation extends WadlRepresentation {
    
    /**
     * Constructs a new Wadl representation based on a new XSLT.
     * @param application application
     */
    public WadlCnesRepresentation(final ApplicationInfo application) {
        super(application);
    }
    
    /**
     * Returns the HTML representation of the WADL
     * @return HTML representation of the WADL
     */
    @Override
    public Representation getHtmlRepresentation() {
        Representation representation = null;
        URL wadl2htmlXsltUrl = Engine
                .getResource("org/restlet/ext/wadl/wadlcnes2html.xslt");

        if (wadl2htmlXsltUrl != null) {
            try {
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
