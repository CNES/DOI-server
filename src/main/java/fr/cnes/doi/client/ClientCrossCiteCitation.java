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
    
    public static final String CROSS_CITE_URL = "http://citation.crosscite.org";
    public static final String STYLE_URI = "/styles";
    public static final String LOCALE_URI = "/locales";        
    public static final String FORMAT_URI = "/format";
    
    private final ClientResource client = new ClientResource(CROSS_CITE_URL);
    
    public ClientCrossCiteCitation() {
        
    }
    
    public void setProxyAuthentication(final ChallengeResponse authentication) {
        client.setProxyChallengeResponse(authentication);
    }
    
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
    
    public List<String> getStyles() {
        return getList(STYLE_URI);
    }
    
    public List<String> getLanguages() {
        return getList(LOCALE_URI);
    }
    
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
