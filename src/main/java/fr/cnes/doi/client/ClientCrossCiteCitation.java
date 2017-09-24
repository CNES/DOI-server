/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cnes.doi.exception.ClientCrossCiteException;
import fr.cnes.doi.utils.spec.Requirement;

import java.io.IOException;
import java.util.List;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

/**
 * Client to query the citation service.
 *
 * @author Jean-Christophe Malapert (Jean-Christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_INTER_020,
        reqName = Requirement.DOI_INTER_020_NAME
)
public class ClientCrossCiteCitation extends BaseClient {

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
     * Empty constructor.
     */
    public ClientCrossCiteCitation() {
        super(CROSS_CITE_URL);
    }

    /**
     * Init the endpoint.
     */
    protected void init() {
        this.client.setReference(new Reference(CROSS_CITE_URL));
    }

    /**
     * Returns the response as a list of String of an URI.
     *
     * @param segment resource name
     * @return the response
     */
    private List<String> getList(final String segment) throws ClientCrossCiteException {
        try {
            Reference ref = client.addSegment(segment);
            client.setReference(ref);
            Representation rep = client.get();
            Status status = client.getStatus();
            if (status.isSuccess()) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(rep.getStream(), List.class);
            } else {                
                throw new ClientCrossCiteException(status, status.getDescription());
            }
        } catch (IOException | ResourceException ex) {
            throw new ClientCrossCiteException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } finally {
            client.release();
        }
    }

    /**
     * Returns styles
     *
     * @return list of possible styles
     * @throws fr.cnes.doi.exception.ClientCrossCiteException Will thrown an
     * Exception when a problem happens during the request to Cross Cite
     */
    public List<String> getStyles() throws ClientCrossCiteException {
        init();
        return getList(STYLE_URI);
    }

    /**
     * Returns languages
     *
     * @return List of possible languages
     * @throws fr.cnes.doi.exception.ClientCrossCiteException Will thrown an
     * Exception when a problem happens during the request to Cross Cite
     */
    public List<String> getLanguages() throws ClientCrossCiteException {
        init();
        return getList(LOCALE_URI);
    }

    /**
     * Returns the citation of a DOI based on the selected style and language.
     *
     * @param doiName DOI name
     * @param style Selected style to format the citation
     * @param language Selected language to format the citation
     * @return The formatted citation
     * @throws fr.cnes.doi.exception.ClientCrossCiteException Will thrown an
     * Exception when a problem happens during the request to Cross Cite
     */
    public String getFormat(final String doiName, final String style, final String language) throws ClientCrossCiteException {
        init();
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
                throw new ClientCrossCiteException(status, status.getDescription());
            }
            return result;
        } catch (IOException ex) {
            throw new ClientCrossCiteException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ResourceException ex) {
            throw new ClientCrossCiteException(ex.getStatus(), ex.getMessage());
        } finally {
            client.release();
        }
    }
}
