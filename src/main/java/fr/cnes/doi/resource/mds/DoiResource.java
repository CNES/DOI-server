/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.utils.Requirement;

import java.util.Arrays;
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
 * DOI resource to retrieve a given DOI.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DoiResource extends BaseMdsResource {

    public static final String GET_DOI = "Get DOI";    
    
    /**
     * DOI template.
     */
    private String doiName;        

    /**
     * Init by getting the DOI name.
     * @throws ResourceException
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setDescription("The resource can retrieve a DOI");                        
        this.doiName = getAttribute(DoiMdsApplication.DOI_TEMPLATE); 
    }
    
    /**
     * Returns a DOI.
     * This request returns an URL associated with a given DOI. The different status:
     * <ul>
     * <li>200 OK - operation successful</li>
     * <li>204 No Content - DOI is known to MDS, but is not minted (or not resolvable e.g. due to handle's latency)</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 - login problem or dataset belongs to another party</li>
     * <li>404 Not Found - DOI does not exist in our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if problem persists please contact us</li>
     * </ul>
     * @return an URL or no content (DOI is known to MDS, but is not minted 
     * (or not resolvable e.g. due to handle's latency))
     * @throws ResourceException Will be thrown when an error happens
     */
    @Requirement(
            reqId = "DOI_SRV_070",
            reqName = "Récupération de l'URL"
    )     
    @Get
    public Representation getDoi() throws ResourceException {
        getLogger().entering(getClass().getName(), "getDoi", this.doiName);
        try {
            String doi = this.doiApp.getClient().getDoi(this.doiName);
            if(doi != null && !doi.isEmpty()) {
                setStatus(Status.SUCCESS_OK);
            } else {
                setStatus(Status.SUCCESS_NO_CONTENT);
            }
            getLogger().exiting(getClass().getName(), "getDoi", doi);
            return new StringRepresentation(doi, MediaType.TEXT_PLAIN);          
        } catch (ClientMdsException ex) {
            getLogger().exiting(getClass().getName(), "getDoi", ex.getDetailMessage());
            throw new ResourceException(ex.getStatus(), ex.getDetailMessage());
        }
    }    
    
    /**
     * DOI representation
     * @return Wadl representation for a DOI
     */
    private RepresentationInfo doiRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);        
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("DOI Representation");
        docInfo.setTextContent("This request returns an URL associated with a given DOI.");
        repInfo.setDocumentation(docInfo);        
        return repInfo;
    }   
    
    /**
     * Describes the Get Method.
     * @param info Wadl description
     */
    @Requirement(
            reqId = "DOI_DOC_010",
            reqName = "Documentation des interfaces"
    )      
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific DOI"); 
        addRequestDocToMethod(info, Arrays.asList(
                createQueryParamDoc(DoiMdsApplication.DOI_TEMPLATE, ParameterStyle.TEMPLATE, "DOI name", true, "xs:string")
        ));

        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_OK, "Operation successful", doiRepresentation()));        
        addResponseDocToMethod(info, createResponseDoc(Status.SUCCESS_NO_CONTENT, "DOI is known to MDS, but is not minted (or not resolvable e.g. due to handle's latency)", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_UNAUTHORIZED, "no login", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_FORBIDDEN, "login problem or dataset belongs to another party", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.CLIENT_ERROR_NOT_FOUND, "DOI does not exist in our database", "explainRepresentation"));
        addResponseDocToMethod(info, createResponseDoc(Status.SERVER_ERROR_INTERNAL, "server internal error, try later and if problem persists please contact us", "explainRepresentation"));
    }     
  
}
