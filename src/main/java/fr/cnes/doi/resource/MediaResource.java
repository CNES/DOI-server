/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.client.ClientException;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/** 
 * Resource to handle to Media.
 * @author Jean-Christophe Malapert
 * https://data.datacite.org/
 */
public class MediaResource extends BaseMdsResource {
    
    public static final String GET_MEDIAS = "Get Medias";

    private String mediaName;

    @Override
    protected void doInit() throws ResourceException {   
        super.doInit();
        this.mediaName = getAttribute(DoiMdsApplication.DOI_TEMPLATE);
    }

    @Get
    public Representation getMedias() {
        getLogger().entering(getClass().getName(), "getMedias", this.mediaName);
        final Representation rep;
        String medias;
        try {
            setStatus(Status.SUCCESS_OK);
            medias = this.doiApp.getClient().getMedia(this.mediaName);
            rep = new StringRepresentation(medias, MediaType.TEXT_URI_LIST);
        } catch (ClientException ex) {
            getLogger().exiting(getClass().getName(), "getMedias", ex.getMessage());            
            throw new ResourceException(ex.getStatus(), ex.getMessage());
        }
        getLogger().exiting(getClass().getName(), "getMedias", medias);        
        return rep;
    } 
   
    
    private RepresentationInfo mediaRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);        
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Media representation");
        docInfo.setTextContent("This request returns a key-value list of media types/urls for a given DOI name");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }    

    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific media for a given DOI");

        addRequestDocToMethod(info, createQueryParamDoc(DoiMdsApplication.DOI_TEMPLATE, ParameterStyle.TEMPLATE, "DOI name", true, "xs:string"));
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", mediaRepresentation()));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "no login", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "login problem or dataset belongs to another party", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_NOT_FOUND, "DOI does not exist in our database", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));
    }     
      
}
