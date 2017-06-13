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
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class BaseResource extends WadlServerResource {

    @Override
    protected void doInit() throws ResourceException {
        super.doInit(); 
    }
    
    /**
     *
     * @param info
     * @param param
     */
    protected void addRequestDocToMethod(final MethodInfo info, final ParameterInfo param) {
        final RequestInfo request = new RequestInfo();
        request.getParameters().add(param);
        info.setRequest(request);        
    }
    
    /**
     *
     * @param info
     * @param params
     */
    protected void addRequestDocToMethod(final MethodInfo info, final List<ParameterInfo> params) {
        final RequestInfo request = new RequestInfo();
        for(ParameterInfo param:params) {
            request.getParameters().add(param);            
        }        
        info.setRequest(request);        
    }    
    
    /**
     *
     * @param info
     * @param params
     * @param rep
     */
    protected void addRequestDocToMethod(final MethodInfo info, final List<ParameterInfo> params, RepresentationInfo rep) {
        addRequestDocToMethod(info, params);
        info.getRequest().getRepresentations().add(rep);       
    }     
    
    /**
     *
     * @param info
     * @param response
     */
    protected void addResponseDocToMethod(final MethodInfo info, final ResponseInfo response) {
        info.getResponses().add(response);        
    }
    
    /**
     *
     * @param status
     * @param doc
     * @return
     */
    protected ResponseInfo createResponseDoc(final Status status, final String doc) {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(status);
        responseInfo.setDocumentation(doc);       
        return responseInfo;
    }
    
    /**
     *
     * @param status
     * @param doc
     * @param refRepresentation
     * @return
     */
    protected ResponseInfo createResponseDoc(final Status status, final String doc, final String refRepresentation) {    
        ResponseInfo response = createResponseDoc(status, doc);
        RepresentationInfo rep = new RepresentationInfo();  
        rep.setReference(refRepresentation);
        response.getRepresentations().add(rep);         
        return response;
    }
    
    /**
     *
     * @param status
     * @param doc
     * @param refRepresentation
     * @return
     */
    protected ResponseInfo createResponseDoc(final Status status, final String doc, final RepresentationInfo refRepresentation) {    
        ResponseInfo response = createResponseDoc(status, doc);
        response.getRepresentations().add(refRepresentation);
        return response;
    }    
    
    /**
     *
     * @param name
     * @param style
     * @param doc
     * @param isRequired
     * @param datatype
     * @return
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
     *
     * @param identifier
     * @param mediaType
     * @param doc
     * @param xmlElement
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
