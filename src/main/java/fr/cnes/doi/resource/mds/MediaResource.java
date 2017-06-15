/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.client.ClientMdsException;
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
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MediaResource extends BaseMdsResource {    

    private String mediaName;

    /**
     * Init by getting the media name.
     * @throws ResourceException
     */
    @Override
    protected void doInit() throws ResourceException {   
        super.doInit();
        this.mediaName = getAttribute(DoiMdsApplication.DOI_TEMPLATE);
    }

    /**
     * Returns the media related to a DOI.
     * This request returns list of pairs of media type and URLs associated with
     * a given DOI. The difference status:
     * <ul>
     * <li>200 OK - operation successful</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 login problem or dataset belongs to another party</li>
     * <li>404 Not Found - No media attached to the DOI or DOI does not exist in our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if problem persists please contact us</li>
     * </ul>
     * @return the media related to a DOI
     * @throws ResourceException Will be thrown when an error happens          
     */
    @Get
    public Representation getMedias() {
        getLogger().entering(getClass().getName(), "getMedias", this.mediaName);
        
        final Representation rep;
        String medias;
        try {
            setStatus(Status.SUCCESS_OK);
            medias = this.doiApp.getClient().getMedia(this.mediaName);
            rep = new StringRepresentation(medias, MediaType.TEXT_URI_LIST);
        } catch (ClientMdsException ex) {
            getLogger().exiting(getClass().getName(), "getMedias", ex.getDetailMessage());            
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
        
        getLogger().exiting(getClass().getName(), "getMedias", medias);        
        return rep;
    } 
   
    /**
     * Media representation.
     * @return Wadl description for a Media representation
     */
    private RepresentationInfo mediaRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);        
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Media representation");
        docInfo.setTextContent("This request returns a key-value list of media types/urls for a given DOI name");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }    

    /**
     * Describes the GET method.
     * @param info Wadl description for a GET method
     */
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
