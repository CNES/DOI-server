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
package fr.cnes.doi.resource;

import fr.cnes.doi.application.AbstractApplication;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.RequestInfo;
import org.restlet.ext.wadl.ResponseInfo;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.resource.ResourceException;

/**
 * Abstract resource.
 *
 * Each resource must extend from this abstract class. This abstract class
 * allows the WADL description.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 * @see <a href="https://www.w3.org/Submission/wadl/">WADL</a>
 */
@Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
public abstract class AbstractResource extends WadlServerResource {

    /**
     * Logger.
     */
	private volatile Logger LOG;

    /**
     * Init
     *
     * @throws ResourceException When an Exception happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.LOG = ((AbstractApplication) getApplication()).getLog();
    }

    /**
     * Tests if the form is not null and the parameter exists in the form.
     *
     * @param form submitted form
     * @param parameterName parameter name in the form
     * @return True when the form is not null and the parameter exists in the
     * form otherwise False
     */
    public boolean isValueExist(final Form form,
            final String parameterName) {
        LOG.traceEntry("Parameters : {} and {}", form, parameterName);
        final boolean result;
        if (isObjectNotExist(form)) {
            result = false;
        } else {
            result = form.getFirstValue(parameterName) != null;
        }
        return LOG.traceExit(result);
    }

    /**
     * The opposite of {@link #isValueExist}
     *
     * @param form submitted form
     * @param parameterName parameter name
     * @return False when the form is not null and the parameter exists in the
     * form otherwise True
     */
    public boolean isValueNotExist(final Form form,
            final String parameterName) {
        LOG.traceEntry("Parameters : {} and {}", form, parameterName);
        return LOG.traceExit(!isValueExist(form, parameterName));
    }

    /**
     * Test if an object is null.
     *
     * @param obj object to test
     * @return True when the object is not null otherwise False
     */
    public boolean isObjectExist(final Object obj) {
        LOG.traceEntry("Parameter : {}", obj);
        return LOG.traceExit(obj != null);
    }

    /**
     * Test if an object is not null.
     *
     * @param obj object to test
     * @return True when the object is null otherwise False
     */
    public boolean isObjectNotExist(final Object obj) {
        LOG.traceEntry("Parameter : {}", obj);
        return LOG.traceExit(!isObjectExist(obj));
    }

    /**
     * Adds Wadl description of the request to a method.
     *
     * @param info method description
     * @param param Request parameters
     */
    protected void addRequestDocToMethod(final MethodInfo info,
            final ParameterInfo param) {
        final RequestInfo request = new RequestInfo();
        request.getParameters().add(param);
        info.setRequest(request);
    }

    /**
     * Adds Wadl description of the request to the method.
     *
     * @param info Method description
     * @param params Request parameters
     */
    protected void addRequestDocToMethod(final MethodInfo info,
            final List<ParameterInfo> params) {
        final RequestInfo request = new RequestInfo();
        for (final ParameterInfo param : params) {
            request.getParameters().add(param);
        }
        info.setRequest(request);
    }

    /**
     * Adds Wadl description of the request to the method.
     *
     * @param info Method description
     * @param params Request parameters
     * @param rep Representation entity of the request
     */
    protected void addRequestDocToMethod(
            final MethodInfo info,
            final List<ParameterInfo> params,
            final RepresentationInfo rep) {
        addRequestDocToMethod(info, params);
        info.getRequest().getRepresentations().add(rep);
    }

    /**
     * Adds Wadl description of the response to a method.
     *
     * @param info Method description
     * @param response Response description
     */
    protected void addResponseDocToMethod(final MethodInfo info,
            final ResponseInfo response) {
        info.getResponses().add(response);
    }

    /**
     * Creates a textual explanation of the response for a given status.
     *
     * @param status HTTP Status
     * @param doc textual explanation
     * @return Response Wadl description
     */
    protected ResponseInfo createResponseDoc(final Status status,
            final String doc) {
        final ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(status);
        responseInfo.setDocumentation(doc);
        return responseInfo;
    }

    /**
     * Creates a textual explanation of the response for a given status.
     *
     * @param status HTTP status
     * @param doc textual explanation
     * @param refRepresentation reference to the representation of the response
     * @return the response Wadl description
     */
    protected ResponseInfo createResponseDoc(
            final Status status,
            final String doc,
            final String refRepresentation) {
        final ResponseInfo response = createResponseDoc(status, doc);
        final RepresentationInfo rep = new RepresentationInfo();
        rep.setReference(refRepresentation);
        response.getRepresentations().add(rep);
        return response;
    }

    /**
     * Creates a textual explanation of the response for a given status.
     *
     * @param status HTTP status
     * @param doc textual description
     * @param representation Representation of the response
     * @return the response Wadl description
     */
    protected ResponseInfo createResponseDoc(
            final Status status,
            final String doc,
            final RepresentationInfo representation) {
        final ResponseInfo response = createResponseDoc(status, doc);
        response.getRepresentations().add(representation);
        return response;
    }

    /**
     * Creates a query parameter.
     *
     * @param name query parameter name
     * @param style Style (header, template, ...)
     * @param doc textual description
     * @param isRequired optional or required
     * @param datatype data type
     * @return the query Wadl description
     */
    protected ParameterInfo createQueryParamDoc(
            final String name,
            final ParameterStyle style,
            final String doc,
            final boolean isRequired,
            final String datatype) {
        final ParameterInfo param = new ParameterInfo();
        param.setName(name);
        param.setStyle(style);
        param.setDocumentation(doc);
        param.setRequired(isRequired);
        param.setType(datatype);
        return param;
    }

    /**
     * Creates query representation.
     *
     * @param identifier representation identifier
     * @param mediaType media type
     * @param doc textual description
     * @param xmlElement XML element of the schema
     * @return Wadl element for the representation
     */
    protected RepresentationInfo createQueryRepresentationDoc(
            final String identifier,
            final MediaType mediaType,
            final String doc,
            final String xmlElement) {
        final RepresentationInfo rep = new RepresentationInfo();
        rep.setIdentifier(identifier);
        rep.setMediaType(mediaType);
        rep.setDocumentation(doc);
        rep.setXmlElement(xmlElement);
        return rep;
    }

    /**
     * projects representation
     *
     * @return Wadl representation for projects
     */
    protected RepresentationInfo stringRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("String Representation");
        docInfo.setTextContent("The representation contains a simple string.");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    /**
     * projects representation
     *
     * @return Wadl representation for projects
     */
    protected RepresentationInfo htmlRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_HTML);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("HTML Representation");
        docInfo.setTextContent("The representation contains the HTML representation.");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }
}
