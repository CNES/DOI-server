/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 *
 * @author malapert
 */
public class ClientCrossCiteCitation {
    
    /**
     * Service end point.
     */
    public static final String CROSS_CITE_URL = "http://citation.crosscite.org";
    /**
     * Resource to get styles.
     */
    public static final String STYLE_URI = "/styles";
    /**
     * Resource to get locales.
     */
    public static final String LOCALE_URI = "/locales";        
    /**
     * Resource to get format.
     */
    public static final String FORMAT_URI = "/format";
    
    /**
     * HTTP Client to request CrossCite.
     */
    private final ClientResource client = new ClientResource(CROSS_CITE_URL);
    
    /**
     * Empty constructor.
     */
    public ClientCrossCiteCitation() {
        
    }
    
    /**
     * Sets Proxy authentication.
     * @param authentication authentication
     */
    public void setProxyAuthentication(final ChallengeResponse authentication) {
        client.setProxyChallengeResponse(authentication);
    }
    
    /**
     * Returns the response as a list of String of an URI.
     * @param segment resource name
     * @return the response
     */
    private List<String> getList(final String segment) {
        try {
            Reference ref = client.addSegment(segment);
            client.setReference(ref);
            Representation rep = client.get();
            Status status = client.getStatus();
            if(status.isSuccess()) {                
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(rep.getStream() , List.class);             
            } else {
                throw new ResourceException(status, status.getDescription());
            }
        } catch (IOException ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } finally {
            client.release();
        }        
    }
    
    /**
     * Returns styles
     * @return list of possible styles
     */
    public List<String> getStyles() {
        return getList(STYLE_URI);
    }
    
    /**
     * Returns languages
     * @return List of possible languages
     */
    public List<String> getLanguages() {
        return getList(LOCALE_URI);
    }
    
    /**
     * Returns the citation of a DOI based on the selected style and language.
     * @param doiName DOI name
     * @param style Selected style to format the citation
     * @param language Selected language to format the citation
     * @return The formatted citation
     */
    public String getFormat(final String doiName, final String style, final String language) {
        String result;
        try {
            Reference ref = client.addSegment(FORMAT_URI);
            ref = ref.addQueryParameter("doi", doiName);
            ref = ref.addQueryParameter("style", style);
            ref = ref.addQueryParameter("lang", language);
            client.setReference(ref);
            Representation rep = client.get();
            Status status = client.getStatus();
            if (status.isSuccess()) {
                result = rep.getText();
            } else {
                client.release();
                throw new ResourceException(status, status.getDescription());
            }
            return result;
        } catch (IOException ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } finally {
            client.release();
        }
    }    
}
