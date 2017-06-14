/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import java.util.List;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.RequestInfo;
import org.restlet.ext.wadl.ResponseInfo;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.resource.ResourceException;

/**
 * Base resource.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class BaseResource extends WadlServerResource {

    @Override
    protected void doInit() throws ResourceException {
        super.doInit(); 
    }
    
    /**
     * Adds Wadl description of the request to a method.
     * @param info method description
     * @param param Request parameters
     */
    protected void addRequestDocToMethod(final MethodInfo info, final ParameterInfo param) {
        final RequestInfo request = new RequestInfo();
        request.getParameters().add(param);
        info.setRequest(request);        
    }
    
    /**
     * Adds Wadl description of the request to the method.
     * @param info Method description
     * @param params Request parameters
     */
    protected void addRequestDocToMethod(final MethodInfo info, final List<ParameterInfo> params) {
        final RequestInfo request = new RequestInfo();
        for(ParameterInfo param:params) {
            request.getParameters().add(param);            
        }        
        info.setRequest(request);        
    }    
    
    /**
     * Adds Wadl description of the request to the method.
     * @param info Method description
     * @param params Request parameters
     * @param rep Representation entity of the request
     */
    protected void addRequestDocToMethod(final MethodInfo info, final List<ParameterInfo> params, RepresentationInfo rep) {
        addRequestDocToMethod(info, params);
        info.getRequest().getRepresentations().add(rep);       
    }     
    
    /**
     * Adds Wadl description of the response to a method.
     * @param info Method description
     * @param response Response description
     */
    protected void addResponseDocToMethod(final MethodInfo info, final ResponseInfo response) {
        info.getResponses().add(response);        
    }
    
    /**
     * Creates a textual explanation of the response for a given status.
     * @param status HTTP Status
     * @param doc textual explanation
     * @return Response Wadl description
     */
    protected ResponseInfo createResponseDoc(final Status status, final String doc) {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(status);
        responseInfo.setDocumentation(doc);       
        return responseInfo;
    }
    
    /**
     * Creates a textual explanation of the response for a given status.     
     * @param status HTTP status
     * @param doc textual explanation
     * @param refRepresentation reference to the representation of the response
     * @return the response Wadl description
     */
    protected ResponseInfo createResponseDoc(final Status status, final String doc, final String refRepresentation) {    
        ResponseInfo response = createResponseDoc(status, doc);
        RepresentationInfo rep = new RepresentationInfo();  
        rep.setReference(refRepresentation);
        response.getRepresentations().add(rep);         
        return response;
    }
    
    /**
     * Creates a textual explanation of the response for a given status.         
     * @param status HTTP status
     * @param doc textual description
     * @param representation Representation of the response
     * @return the response Wadl description
     */
    protected ResponseInfo createResponseDoc(final Status status, final String doc, final RepresentationInfo representation) {    
        ResponseInfo response = createResponseDoc(status, doc);
        response.getRepresentations().add(representation);
        return response;
    }    
    
    /**
     * Creates a query parameter.
     * @param name query parameter name
     * @param style Style (header, template, ...)
     * @param doc textual description
     * @param isRequired optional or required
     * @param datatype data type
     * @return the query Wadl description
     */
    protected ParameterInfo createQueryParamDoc(final String name, final ParameterStyle style, final String doc, boolean isRequired, final String datatype) {
        ParameterInfo param = new ParameterInfo();
        param.setName(name);
        param.setStyle(style);
        param.setDocumentation(doc);
        param.setRequired(isRequired);
        param.setType(datatype);   
        return param;
    }  
    
    /**
     * Creates query representation.
     * @param identifier representation identifier
     * @param mediaType media type
     * @param doc textual description
     * @param xmlElement XML element of the schema
     * @return
     */
    protected RepresentationInfo createQueryRepresentationDoc(final String identifier, final MediaType mediaType, final String doc, final String xmlElement) {
        RepresentationInfo rep = new RepresentationInfo();
        rep.setIdentifier(identifier);
        rep.setMediaType(mediaType);
        rep.setDocumentation(doc);
        rep.setXmlElement(xmlElement);
        return rep;
    }
        
}
