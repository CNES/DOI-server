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
package fr.cnes.doi.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationException;

import org.datacite.schema.kernel_4.Resource;
import org.datacite.schema.kernel_4.Resource.Identifier;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.utils.Utils;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Client to query Metadata store service at
 * <a href="https://support.datacite.org/docs/mds-2">Datacite</a>.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 * @see "https://mds.datacite.org/static/apidoc"
 */
@Requirement(reqId = Requirement.DOI_INTER_010, reqName = Requirement.DOI_INTER_010_NAME)
public class ClientMDS extends BaseClient {

    /**
     * Metadata store service endpoint {@value #DATA_CITE_URL}.
     */
    public static final String DATA_CITE_URL = "https://mds.datacite.org";

    /**
     * Metadata store mock service endpoint {@value #DATA_CITE_MOCK_URL}.
     */
    public static final String DATA_CITE_MOCK_URL = "http://localhost:" + DATACITE_MOCKSERVER_PORT;

    /**
     * Metadata store test service endpoint {@value #DATA_CITE_TEST_URL}.
     */
    public static final String DATA_CITE_TEST_URL = "https://mds.test.datacite.org";

    /**
     * DOI resource {@value #DOI_RESOURCE}.
     */
    public static final String DOI_RESOURCE = "doi";      

    /**
     * Metadata resource {@value #METADATA_RESOURCE}.
     */
    public static final String METADATA_RESOURCE = "metadata";
    
    /**
     * Metadata resource {@value #INIST_RESOURCE}.
     */
    public static final String INIST_RESOURCE = "inist";

    /**
     * Media resource {@value #MEDIA_RESOURCE}.
     */
    public static final String MEDIA_RESOURCE = "media";

    /**
     * Test mode sets to true.
     */
    public static final Parameter TEST_MODE = new Parameter("testMode", "true");

    /**
     * Test DOI prefix {@value #TEST_DOI_PREFIX}.
     */
    public static final String TEST_DOI_PREFIX = "10.80163";

    /**
     * DOI query parameter {@value #POST_DOI}.
     */
    public static final String POST_DOI = "doi";

    /**
     * URL query parameter {@value #POST_URL}.
     */
    public static final String POST_URL = "url";

    /**
     * Default XML schema for Datacite: {@value #SCHEMA_DATACITE}
     */
    public static final String SCHEMA_DATACITE = "https://schema.datacite.org/meta/kernel-4-4/metadata.xsd";

    /**
     * DataCite recommends that only the following characters are used within a
     * DOI name:
     * <ul>
     * <li>0-9</li>
     * <li>a-z</li>
     * <li>A-Z</li>
     * <li>- (dash)</li>
     * <li>. (dot)</li>
     * <li>_ (underscore)</li>
     * <li>+ (plus)</li>
     * <li>: (colon)</li>
     * <li>/ (slash)</li>
     * </ul>
     *
     * @param test DOI name to test
     * @throws IllegalArgumentException An exception is thrown when at least one
     * character is not part of 0-9a-zA-Z\\-._+:/ of a DOI name
     */
    public static void checkIfAllCharsAreValid(final String test) {
        if (!test.matches("^[0-9a-zA-Z\\-._+:/\\s]+$")) {
            throw new IllegalArgumentException("Only these characters are allowed "
                    + "0-9a-zA-Z\\-._+:/ in a DOI name");
        }
    }
    /**
     * Selected test mode.
     */
    private final Parameter testMode;
    /**
     * Context.
     */
    private final Context context;

    /**
     * Creates a client to handle DataCite server.
     *
     * There is special test prefix 10.5072 available to all datacentres. Please
     * use it for all your testing DOIs. Your real prefix should not be used for
     * test DOIs. Note that DOIs with test prefix will behave like any other
     * DOI, e.g. they can be normally resolved. They will not be exposed by
     * upcoming services like search and OAI, though. Periodically we purge *
     * all 10.5072 datasets from the system.
     *
     * <p>
     * It is important to understand that the Handle System (the technical
     * infrastructure for DOIs) is a distributed network system. The consequence
     * of this manifests is its inherent latency. For example, DOIs have TTL
     * (time to live) defaulted to 24 hours, so your changes will be visible to
     * the resolution infrastructure only when the TTL expires. Also, if you
     * create a DOI and then immediately try to update its URL, you might get
     * the error message HANDLE NOT EXISTS. This is because it takes some time
     * for the system to register a handle for a DOI.
     *
     * Each API call can have optional query parametertestMode. If set to "true"
     * or "1" the request will not change the database nor will the DOI handle
     * will be registered or updated, e.g. POST /doi?testMode=true and the
     * testing prefix will be used instead of the provided prefix
     *
     * @param context Context using
     * @throws fr.cnes.doi.exception.ClientMdsException Cannot the Datacite
     * schema
     */
    public ClientMDS(final Context context) throws ClientMdsException {
        super(context.getDataCiteUrl());
        this.context = context;
        this.testMode = this.context.hasTestMode() ? TEST_MODE : null;
    }

    /**
     * Creates a client to handle DataCite with a HTTP Basic authentication.
     *
     * All the traffic goes via HTTPS - please remember we do not support bare
     * HTTP. All the requests to this system require HTTP Basic authentication
     * header. You will get your username and password from your local DataCite
     * allocator. Each account have some constraints associated with it:
     * <ul>
     * <li>you will be allowed to mint DOIs only with prefix assigned to
     * you</li>
     * <li>you will be allowed to mint DOIs only with URLs in host domains
     * assigned to you</li>
     * <li>you might not be able to mint unlimited number of DOIs, there is a
     * quota assigned to you by your allocator (the quota can be extended or
     * lifted though)</li>
     * </ul>
     * Each API call can have optional query parametertestMode. If set to "true"
     * or "1" the request will not change the database nor will the DOI handle
     * will be registered or updated, e.g. POST /doi?testMode=true.
     *
     * @param context Context using
     * @param login Login
     * @param pwd password
     * @throws fr.cnes.doi.exception.ClientMdsException Cannot the Datacite
     * schema
     */
    public ClientMDS(final Context context,
            final String login,
            final String pwd) throws ClientMdsException {
        this(context);
        this.getLog().debug("Authentication with HTTP_BASIC : {}/{}",
                login, Utils.transformPasswordToStars(pwd));
        this.getClient().setChallengeResponse(ChallengeScheme.HTTP_BASIC, login, pwd);
    }

    /**
     * Creates a client to handle DataCite with a HTTP Basic authentication.
     *
     * @param login Login
     * @param pwd password
     * @throws fr.cnes.doi.exception.ClientMdsException Cannot the Datacite
     * schema
     */
    public ClientMDS(final String login,
            final String pwd) throws ClientMdsException {
        this(Context.PROD);
        this.getLog().debug("Authentication with HTTP_BASIC : {}/{}",
                login, Utils.transformPasswordToStars(pwd));
        this.getClient().setChallengeResponse(ChallengeScheme.HTTP_BASIC, login, pwd);
    }

    /**
     * Returns the {@link #TEST_MODE} or an empty parameter according to
     * <i>isTestMode</i>
     *
     * @return the test mode
     */
    private Parameter getTestMode() {
        return this.testMode;
    }

    /**
     * Renames the current DOI prefix by the DOI test prefix.
     *
     * @param doiName real DOI name
     * @return the renamed DOI with the test prefix
     */
    private String useTestPrefix(final String doiName) {
        final String[] split = doiName.split("/");
        split[0] = TEST_DOI_PREFIX;
        final String testingPrefix = String.join("/", split);
        final String message = String.format(
                "DOI %s has been renamed as %s for testing", doiName, testingPrefix
        );
        this.getLog().warn(message);
        return testingPrefix;
    }

    /**
     * Init the client to the reference {@link #DATA_CITE_URL} or
     * {@link #DATA_CITE_TEST_URL}
     */
    private void initReference() {
        this.getClient().setReference(this.context.getDataCiteUrl());
    }

    /**
     * Create reference. The parameter ?testMode=true is added in DEV context
     *
     * @param segment segment to add to the end point
     * @return new URL
     */
    private Reference createReference(final String segment) {
        this.initReference();
        Reference url = this.getClient().addSegment(segment);
        if (this.getTestMode() != null) {
            url = url.addQueryParameter(this.getTestMode());
        }
        return url;
    }

    /**
     * Creates the URL to query.
     *
     * @param segment segment to add to the end point service
     * @param doiName doi name
     * @return the URL to query
     */
    private Reference createReferenceWithDOI(final String segment,
            final String doiName) {
        final String requestDOI = getDoiAccorgindToContext(doiName);
        final Reference ref = createReference(segment);
        final String[] split = requestDOI.split("/");
        for (final String segmentUri : split) {
            ref.addSegment(segmentUri);
        }
        return ref;
    }

    /**
     * Returns the right DOI according to the context (DEV, POST_DEV, ...). When
     * the context has a DOI test prefix, the real DOI prefix is replaced by the
     * DOI test prefix.
     *
     * @param doiName DOI name
     * @return the right DOI
     */
    private String getDoiAccorgindToContext(final String doiName) {
        return this.context.hasDoiTestPrefix() ? useTestPrefix(doiName) : doiName;
    }

    /**
     * Checks the input parameters and specially the validity of the DOI name.
     * The real prefix is replaced by the test prefix in DEV, POST_DEV and
     * PRE_PROD context. The DOI prefix may replace according to the
     * {@link ClientMDS#context}.
     *
     * @param form query form
     * @throws IllegalArgumentException An exception is thrown when doi and url
     * are not provided or when one character at least in the DOI name is not
     * valid
     */
    private void checkInputForm(final Form form) throws IllegalArgumentException {
        final Map<String, String> map = form.getValuesMap();
        if (map.containsKey(POST_DOI) && map.containsKey(POST_URL)) {
            final String doiName = form.getFirstValue(POST_DOI);
            checkIfAllCharsAreValid(doiName);
            form.set(POST_DOI, getDoiAccorgindToContext(doiName));
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0} and {1} parameters are required",
                    POST_DOI, POST_URL)
            );
        }
    }

    /**
     * Returns the text of a response.
     *
     * @param rep Response of the server
     * @return the text of the response
     * @throws ClientMdsException An exception is thrown when cannot convert the
     * Representation to text
     */
    private String getText(final Representation rep) throws ClientMdsException {
        final String result;
        try {
            result = rep.getText();
        } catch (IOException ex) {
            throw new ClientMdsException(Status.SERVER_ERROR_INTERNAL, ex);
        }
        return result;
    }
    
    /**
     * Returns the response as a list of String of an URI.
     *
     * @param segment resource name
     * @return the response
     * @throws ClientMdsException - if an error happens when requesting
     * CrossCite
     */
    private List<String> getList(final String segment) throws ClientMdsException {
        try {
            final Reference ref = this.createReference(segment);
            this.getClient().setReference(ref);
            final Representation rep = this.getClient().get();
            final Status status = this.getClient().getStatus();
            if (status.isSuccess()) {
                final String result = rep.getText();
                return Arrays.asList(result.split("\n"));
            } else {
                throw new ClientMdsException(status, status.getDescription());
            }
        } catch (IOException | ResourceException ex) {
            throw new ClientMdsException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(), ex);
        } finally {
            this.getClient().release();
        }
    }    

    /**
     * This request returns an URL associated with a given DOI. A 200 status is
     * an operation successful. A 204 status means no content (DOI is known to
     * MDS, but is not minted (or not resolvable e.g. due to handle's latency)
     * The DOI prefix may replace according to the {@link ClientMDS#context}.
     *
     * @param doiName DOI name
     * @return an URL or no content (DOI is known to MDS, but is not minted (or
     * not resolvable e.g. due to handle's latency))
     * @throws ClientMdsException - if an error happens <ul>
     * <li>401 Unauthorized - no login</li>
     * <li>403 - login problem or dataset belongs to another party</li>
     * <li>404 Not Found - DOI does not exist in our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     * @see "https://mds.datacite.org/static/apidoc#tocAnchor-13"
     */
    public String getDoi(final String doiName) throws ClientMdsException {
        final Reference url = createReferenceWithDOI(DOI_RESOURCE, doiName);
        this.getLog().info("GET {0}", url.toString());

        this.getClient().setReference(url);
        Representation rep;
        try {
            rep = this.getClient().get();
            return (rep == null) ? "" : this.getText(rep);
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getMessage(), this.getClient().
                    getResponseEntity(), ex);
        } finally {
            this.getClient().release();
        }
    }
    
    public List<String> getDois() throws ClientMdsException {
        return getList(DOI_RESOURCE);
    }
    
    /**
     * Returns only the dois within the specified project from the search
     * result.
     *
     * @param idProject project ID
     * @return the search result
     * @throws fr.cnes.doi.exception.ClientMdsException When an error happens 
     * with Datacite
     */
    public List<String> getDois(final String idProject) throws ClientMdsException {
        final List<String> doiListFiltered = new ArrayList<>();
        for (final String doi : this.getDois()) {
            if (doi.contains(idProject)) {
                doiListFiltered.add(doi);
            }
        }
        return Collections.unmodifiableList(doiListFiltered);
    }    

    /**
     * Will mint new DOI if specified DOI doesn't exist.
     *
     * This method will attempt to update URL if you specify existing DOI.
     * Standard domains and quota restrictions check will be performed. A
     * Datacentre's doiQuotaUsed will be increased by 1. A new record in
     * Datasets will be created when 201 Created status is returned.
     *
     * The DOI prefix may replace according to the {@link ClientMDS#context}.
     *
     * @param form A form with the following attributes doi and url
     * @return short explanation of status code e.g. CREATED,
     * HANDLE_ALREADY_EXISTS etc
     * @throws ClientMdsException - if an error happens <ul>
     * <li>400 Bad Request - request body must be exactly two lines: DOI and
     * URL; wrong domain, wrong prefix</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem, quota exceeded</li>
     * <li>412 Precondition failed - metadata must be uploaded first</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     * @see "https://mds.datacite.org/static/apidoc#tocAnchor-15"
     */
    public String createDoi(final Form form) throws ClientMdsException {
        try {
            this.checkInputForm(form);
            final Reference url = createReference(DOI_RESOURCE+"/"+form.getFirstValue(POST_DOI));
            this.getLog().debug("PUT {0}", url.toString());
            final Representation rep = createRequest(url, form);
            return getText(rep);
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getMessage(), this.getClient().
                    getResponseEntity(), ex);
        } finally {
            this.getClient().release();
        }
    }

    /**
     * Creates the request and requests the DOI creation
     *
     * @param url url
     * @param form form
     * @return representation of the response
     */
    private Representation createRequest(final Reference url, final Form form) {
        this.getClient().setReference(url);
        String requestBody = POST_DOI + "=" + form.getFirstValue(POST_DOI) + "\n"
                + POST_URL + "=" + form.getFirstValue(POST_URL);
        requestBody = new String(requestBody.getBytes(
                StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        final Map<String, Object> requestAttributes = this.getClient().getRequestAttributes();
        requestAttributes.put("charset", StandardCharsets.UTF_8);
        requestAttributes.put("Content-Type", "text/plain");
        this.getLog().info("PUT {} with parameters {}", url, requestBody);
        return this.getClient().put(requestBody, MediaType.TEXT_PLAIN);
    }

    /**
     * Parses the XML representation that implements the DATACITE schema.
     *
     * @param rep XML representation
     * @return the Resource object
     * @throws ClientMdsException Will throw when a problem happens during the
     * parsing
     */
    private synchronized Resource parseDataciteResource(final Representation rep) throws
            ClientMdsException {
        final JaxbRepresentation<Resource> resource = new JaxbRepresentation<>(rep, Resource.class);
        try {
            return resource.getObject();
        } catch (IOException ex) {
            throw new ClientMdsException(Status.SERVER_ERROR_INTERNAL, ex);
        }
    }

    /**
     * This request returns the most recent version of metadata associated with
     * a given DOI. A status of 200 is an operation successful. The DOI prefix
     * may replace according to the {@link ClientMDS#context}.
     *
     * @param doiName DOI name
     * @return XML representing a dataset
     * @throws ClientMdsException - if an error happens <ul>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem or dataset belongs to another
     * party</li>
     * <li>404 Not Found - DOI does not exist in our database</li>
     * <li>410 Gone - the requested dataset was marked inactive (using DELETE
     * method)</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     */
    public Resource getMetadataAsObject(final String doiName) throws ClientMdsException {
        final Representation rep = getMetadata(doiName);
        return parseDataciteResource(rep);
    }

    /**
     * Returns the metadata based on its DOI name. A status of 200 is an
     * operation successful. The DOI prefix may replace according to the
     * {@link ClientMDS#context}.
     *
     * @param doiName DOI name
     * @return the metadata as XML
     * @throws ClientMdsException - if an error happens <ul>
     * <li>200 OK - operation successful</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem or dataset belongs to another
     * party</li>
     * <li>404 Not Found - DOI does not exist in our database</li>
     * <li>410 Gone - the requested dataset was marked inactive (using DELETE
     * method)</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     * @see "https://mds.datacite.org/static/apidoc#tocAnchor-15"
     */
    public Representation getMetadata(final String doiName) throws ClientMdsException {
        final Reference url = createReferenceWithDOI(METADATA_RESOURCE, doiName);
        this.getLog().debug("GET {}", url.toString());
        this.getClient().setReference(url);
        this.getLog().info("GET {}", url);
        try {
            return this.getClient().get(MediaType.APPLICATION_XML);
        } catch (ResourceException ex) {
            this.getClient().release();
            throw new ClientMdsException(ex.getStatus(), ex.getMessage(), this.getClient().
                    getResponseEntity(), ex);
        }
    }

    /**
     * This request stores new version of metadata. Creates metadata with 201
     * status when operation successful. The DOI prefix may replace according to
     * the {@link ClientMDS#context}.
     *
     * @param entity A valid XML
     * @return short explanation of status code e.g.
     * CREATED,HANDLE_ALREADY_EXISTS etc
     * @throws ClientMdsException - if an error happens <ul>
     * <li>400 Bad Request - invalid XML, wrong prefix</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem, quota exceeded</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     * @see "https://mds.datacite.org/static/apidoc#tocAnchor-18"
     */
    public String createMetadata(final Representation entity) throws ClientMdsException {
        final Resource resource = parseDataciteResource(entity);
        return this.createMetadata(resource);
    }

    /**
     * Creates metadata with 201 status when operation successful. The DOI
     * prefix may replace according to the {@link ClientMDS#context}.
     *
     * The method is synchronized because marshall method is not thread-safe.
     *
     * @param entity Metadata
     * @return short explanation of status code e.g. CREATED,
     * HANDLE_ALREADY_EXISTS etc
     * @throws ClientMdsException - if an error happens <ul>
     * <li>400 Bad Request - invalid XML, wrong prefix</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem, quota exceeded</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     * @see "https://mds.datacite.org/static/apidoc#tocAnchor-18"
     */
    public String createMetadata(final Resource entity) throws ClientMdsException {
        try {
            final Identifier identifier = entity.getIdentifier();
            identifier.setValue(getDoiAccorgindToContext(identifier.getValue()));
            final Reference url = createReference(METADATA_RESOURCE+"/"+identifier.getValue());
            this.getLog().debug("PUT {}", url.toString());
            final JaxbRepresentation<Resource> result = new JaxbRepresentation<>(entity);
            result.setCharacterSet(CharacterSet.UTF_8);
            result.setMediaType(MediaType.APPLICATION_XML);
            this.getClient().setReference(url);
            this.getClient().getRequestAttributes().put("Content-Type", "application/xml");
            this.getClient().getRequestAttributes().put("charset", "UTF-8");
            this.getClient().setMethod(null);
            final Representation response = this.getClient().put(result);
            return getText(response);
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getMessage(), this.getClient().
                    getResponseEntity(), ex);
        } finally {
            this.getClient().release();
        }
    }

    /**
     * Parses the metadata and returns the Resource object from DataCite.
     *
     * The method is synchronized because unmarshall method is not thread-safe.
     *
     * @param entity metadata
     * @return the Resource object from DataCite
     * @throws ValidationException When validation failed
     */
    public synchronized Resource parseMetadata(final Representation entity) throws
            ValidationException {

        try {
            final JaxbRepresentation<Resource> resourceEntity = new JaxbRepresentation<>(entity,
                    Resource.class);
            final MyValidationEventHandler validationHandler = new MyValidationEventHandler(this.
                    getClient().getLogger());
            resourceEntity.setValidationEventHandler(validationHandler);
            final Resource resource = resourceEntity.getObject();
            if (validationHandler.isValid()) {
                return resource;
            } else {
                throw new ValidationException(validationHandler.getErrorMsg());
            }
        } catch (IOException | JAXBException ex) {
            throw new ValidationException("Cannot read the metadata", ex);
        }
    }

    /**
     * This request marks a dataset as 'inactive'.
     *
     * To activate it again, POST new metadata or set the isActive-flag in the
     * user interface. A status of 200 is an operation successful. The DOI
     * prefix may replace according to the {@link ClientMDS#context}.
     *
     * @param doiName DOI name
     * @return XML representing a dataset
     * @throws ClientMdsException - if an error happens <ul>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem or dataset belongs to another
     * party</li>
     * <li>404 Not Found - DOI does not exist in our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     * @see "https://mds.datacite.org/static/apidoc#tocAnchor-19"
     */
    public Resource deleteMetadataDoiAsObject(final String doiName) throws ClientMdsException {
        final Representation rep = this.deleteMetadata(doiName);
        return parseDataciteResource(rep);
    }

    /**
     * This request marks a dataset as 'inactive'.
     *
     * To activate it again, POST new metadata or set the isActive-flag in the
     * user interface. A status of 200 is an operation successful The DOI prefix
     * may replace according to the {@link ClientMDS#context}.
     *
     * @param doiName DOI name
     * @return the deleted metadata
     * @throws ClientMdsException - if an error happens <ul>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem or dataset belongs to another
     * party</li>
     * <li>404 Not Found - DOI does not exist in our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     * @see "https://mds.datacite.org/static/apidoc#tocAnchor-19"
     */
    public Representation deleteMetadata(final String doiName) throws ClientMdsException {
        final Reference url = createReferenceWithDOI(METADATA_RESOURCE, doiName);
        this.getLog().debug("DELETE {}", url.toString());
        this.getClient().setReference(url);
        try {
            return this.getClient().delete();
        } catch (ResourceException ex) {
            this.getClient().release();
            throw new ClientMdsException(ex.getStatus(), ex.getMessage(), this.getClient().
                    getResponseEntity(), ex);
        }
    }

    /**
     * This request returns list of pairs of media type and URLs associated with
     * a given DOI. A status of 200 is an operation successful. The DOI prefix
     * may replace according to the {@link ClientMDS#context}.
     *
     * @param doiName DOI name
     * @return list of pairs of media type and URLs
     * @throws ClientMdsException - if an error happens <ul>
     * <li>401 Unauthorized - no login</li>
     * <li>403 login problem or dataset belongs to another party</li>
     * <li>404 Not Found - No media attached to the DOI or DOI does not exist in
     * our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     * @see "https://mds.datacite.org/static/apidoc#tocAnchor-21"
     */
    public String getMedia(final String doiName) throws ClientMdsException {
        final String result;
        final Reference url = createReferenceWithDOI(MEDIA_RESOURCE, doiName);
        this.getLog().debug("GET {}", url.toString());
        this.getClient().setReference(url);
        try {
            final Representation response = this.getClient().get();
            result = this.getText(response);
            return result;
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getMessage(), this.getClient().
                    getResponseEntity(), ex);
        } finally {
            this.getClient().release();
        }
    }

    /**
     * Will add/update media type/urls pairs to a DOI. A status of 200 is an
     * operation successful.Standard domain restrictions check will be
     * performed. The DOI prefix may replace according to the
     * {@link ClientMDS#context}.
     *
     * @param doiName DOI identifier
     * @param form Multiple lines in the following format{mime-type}={url} where
     * {mime-type} and {url} have to be replaced by your mime type and URL,
     * UFT-8 encoded.
     * @return short explanation of status code
     * @throws ClientMdsException - if an error happens <ul>
     * <li>400 Bad Request - one or more of the specified mime-types or urls are
     * invalid (e.g. non supported mime-type, not allowed url domain, etc.)</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     * @see "https://mds.datacite.org/static/apidoc#tocAnchor-22"
     */
    public String createMedia(final String doiName,
            final Form form) throws ClientMdsException {
        final String result;
        final Reference url = createReferenceWithDOI(MEDIA_RESOURCE, doiName);
        this.getLog().debug("POST {}", url.toString());
        this.getClient().setReference(url);
        final Representation entity = createEntity(form);
        try {
            final Representation response = this.getClient().post(entity, MediaType.TEXT_PLAIN);
            result = this.getText(response);
            return result;
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getMessage(), this.getClient().
                    getResponseEntity(), ex);
        } finally {
            this.getClient().release();
        }
    }

    /**
     * Creates an entity based on the form. The form contains a set of
     * mime-type/url
     *
     * @param mediaForm form
     * @return Text entity
     */
    private Representation createEntity(final Form mediaForm) {
        final Iterator<Parameter> iter = mediaForm.iterator();
        StringBuilder entity = new StringBuilder();
        while (iter.hasNext()) {
            final Parameter param = iter.next();
            final String mimeType = param.getName();
            final String url = param.getValue();
            entity = entity.append(mimeType).append("=").append(url).append("\n");
        }
        return new StringRepresentation(
                entity.toString(),
                MediaType.TEXT_PLAIN,
                Language.ENGLISH,
                CharacterSet.UTF_8
        );
    }

    /**
     * Metadata Validation.
     */
    @Requirement(reqId = Requirement.DOI_ARCHI_020, reqName = Requirement.DOI_ARCHI_020_NAME)
    private static class MyValidationEventHandler implements ValidationEventHandler {

        /**
         * Logger.
         */
        private final java.util.logging.Logger logger;

        /**
         * Indicates if an error was happening.
         */
        private boolean hasError = false;

        /**
         * Error message.
         */
        private String errorMsg = null;

        /**
         * Validation handler
         *
         * @param logger logger
         */
        MyValidationEventHandler(final java.util.logging.Logger logger) {
            this.logger = logger;
        }

        /**
         * Handles event
         *
         * @param event event
         * @return True
         */
        @Override
        public boolean handleEvent(final ValidationEvent event) {
            final StringBuilder stringBuilder = new StringBuilder("\nEVENT");
            stringBuilder.append("SEVERITY:  ").append(event.getSeverity()).append("\n");
            stringBuilder.append("MESSAGE:  ").append(event.getMessage()).append("\n");
            stringBuilder.append("LINKED EXCEPTION:  ").append(event.getLinkedException()).append(
                    "\n");
            stringBuilder.append("LOCATOR\n");
            stringBuilder.append("    LINE NUMBER:  ").append(event.getLocator().getLineNumber()).
                    append("\n");
            stringBuilder.append("    COLUMN NUMBER:  ").
                    append(event.getLocator().getColumnNumber()).append("\n");
            stringBuilder.append("    OFFSET:  ").append(event.getLocator().getOffset()).
                    append("\n");
            stringBuilder.append("    OBJECT:  ").append(event.getLocator().getObject()).
                    append("\n");
            stringBuilder.append("    NODE:  ").append(event.getLocator().getNode()).append("\n");
            stringBuilder.append("    URL  ").append(event.getLocator().getURL()).append("\n");
            this.errorMsg = stringBuilder.toString();
            this.logger.info(this.errorMsg);
            this.hasError = true;
            return true;
        }

        /**
         * Returns true when metadata is valid against the schema otherwise
         * false.
         *
         * @return true when metadata is valid against the schema otherwise
         * false
         */
        public boolean isValid() {
            return !this.isNotValid();

        }

        /**
         * Returns true when metadata is not valid against the schema otherwise
         * false.
         *
         * @return true when metadata is not valid against the schema otherwise
         * false
         */
        public boolean isNotValid() {
            return this.hasError;
        }

        /**
         * Returns the errorMsg or null when no error message.
         *
         * @return the errorMsg or null when no error message
         */
        public String getErrorMsg() {
            return this.errorMsg;
        }
    }

    /**
     * Datacite API.
     */
    public enum DATACITE_API_RESPONSE {
        /**
         * Get/Delete successfully a DOI or a media. SUCCESS_OK is used as
         * status
         */
        SUCCESS(Status.SUCCESS_OK, "Operation successful"),
        /**
         * Create successfully a DOI. SUCCESS_CREATED is as used as status.
         */
        SUCESS_CREATED(Status.SUCCESS_CREATED, "Operation successful"),
        /**
         * Get a DOI without metadata. SUCCESS_NO_CONTENT is used as status
         */
        SUCCESS_NO_CONTENT(Status.SUCCESS_NO_CONTENT,
                " the DOI is known to DataCite Metadata Store (MDS), but no metadata have been registered"),
        /**
         * Fail to create a media or the metadata. CLIENT_ERROR_BAD_REQUEST is
         * used as status
         */
        BAD_REQUEST(Status.CLIENT_ERROR_BAD_REQUEST,
                "invalid XML, wrong prefix or request body must be exactly two lines: DOI and URL; wrong domain, wrong prefix"),
        /**
         * Fail to authorize the user to create/delete a DOI.
         * CLIENT_ERROR_UNAUTHORIZED is used as status
         */
        UNAUTHORIZED(Status.CLIENT_ERROR_UNAUTHORIZED, "no login"),
        /**
         * Fail to create/delete media/metadata/Landing page.
         * CLIENT_ERROR_FORBIDDEN is used as status
         */
        FORBIDDEN(Status.CLIENT_ERROR_FORBIDDEN,
                "login problem, wrong prefix, permission problem or dataset belongs to another party"),
        /**
         * Fail to get the DOI. CLIENT_ERROR_NOT_FOUND is used as status
         */
        DOI_NOT_FOUND(Status.CLIENT_ERROR_NOT_FOUND, "DOI does not exist in our database"),
        /**
         * Get an inactive DOI. CLIENT_ERROR_GONE is used as status
         */
        DOI_INACTIVE(Status.CLIENT_ERROR_GONE,
                "the requested dataset was marked inactive (using DELETE method)"),
        /**
         * Fail to create a DOI because metadata must be uploaded first.
         * CLIENT_ERROR_PRECONDITION_FAILED is used as status
         */
        PROCESS_ERROR(Status.CLIENT_ERROR_PRECONDITION_FAILED, "metadata must be uploaded first"),
        /**
         * Internal server Error. INTERNAL_SERVER_ERROR is used as status.
         */
        INTERNAL_SERVER_ERROR(Status.SERVER_ERROR_INTERNAL, "Internal server error");

        /**
         * HTTP status.
         */
        private final Status status;

        /**
         * message.
         */
        private final String message;

        /**
         * Creates enumeration
         *
         * @param status status
         * @param message message
         */
        DATACITE_API_RESPONSE(final Status status, final String message) {
            this.status = status;
            this.message = message;
        }

        /**
         * Returns the status
         *
         * @return the status
         */
        public Status getStatus() {
            return this.status;
        }

        /**
         * Returns the short message
         *
         * @return the short message
         */
        public String getShortMessage() {
            return this.message;
        }

        /**
         * Returns the message for a specific Status.
         *
         * @param statusToFind status to search
         * @return the message or empty string
         */
        public static String getMessageFromStatus(final Status statusToFind) {
            String result = "";
            final int codeToFind = statusToFind.getCode();
            final DATACITE_API_RESPONSE[] values = DATACITE_API_RESPONSE.values();
            for (int i = 0; i <= values.length; i++) {
                final DATACITE_API_RESPONSE value = values[i];
                final int codeValue = value.getStatus().getCode();
                if (codeValue == codeToFind) {
                    result = value.message;
                    break;
                }
            }
            return result;
        }
    }

    /**
     * Options for each context
     */
    public enum Context {

        /**
         * Development context.
         */
        DEV(false, true, DATA_CITE_MOCK_URL, Level.ALL),
        /**
         * Post development context.
         */
        POST_DEV(false, true, DATA_CITE_TEST_URL, Level.ALL),
        /**
         * Pre production context.
         */
        PRE_PROD(false, true, DATA_CITE_URL, Level.FINE),
        /**
         * Production context.
         */
        PROD(false, false, DATA_CITE_URL, Level.INFO);

        /**
         * Each API call can have optional query parametertestMode. If set to
         * "true" or "1" the request will not change the database nor will the
         * DOI handle will be registered or updated, e.g. POST
         * /doi?testMode=true and the testing prefix will be used instead of the
         * provided prefix
         */
        private final boolean isTestMode;

        /**
         * There is special test prefix 10.5072 available to all datacentres.
         * Please use it for all your testing DOIs. Your real prefix should not
         * be used for test DOIs. Note that DOIs with test prefix will behave
         * like any other DOI, e.g. they can be normally resolved. They will not
         * be exposed by upcoming services like search and OAI, though.
         * Periodically we purge all 10.5072 datasets from the system.
         */
        private final boolean isDoiPrefix;

        /**
         * Level log.
         */
        private Level levelLog;

        /**
         * DataCite URL.
         */
        private String dataCiteUrl;

        Context(final boolean isTestMode,
                final boolean isDoiPrefix,
                final String dataciteUrl,
                final Level levelLog) {
            this.isTestMode = isTestMode;
            this.isDoiPrefix = isDoiPrefix;
            this.dataCiteUrl = dataciteUrl;
            this.levelLog = levelLog;
        }

        /**
         * Returns true when the context has a DOI dev.
         *
         * @return True when the context has a DOI dev
         */
        public boolean hasDoiTestPrefix() {
            return this.isDoiPrefix;
        }

        /**
         * Returns true when the context must not register data in DataCite
         *
         * @return true when the context must not register data in DataCite
         */
        public boolean hasTestMode() {
            return this.isTestMode;
        }

        /**
         * Returns the log level.
         *
         * @return the log level
         */
        public Level getLevelLog() {
            return this.levelLog;
        }

        /**
         * Returns the service end point.
         *
         * @return the service end point
         */
        public String getDataCiteUrl() {
            return this.dataCiteUrl;
        }

        /**
         * Sets the DataCite URL for the context
         *
         * @param dataCiteUrl DataCite URL
         */
        private void setDataCiteURl(final String dataCiteUrl) {
            this.dataCiteUrl = dataCiteUrl;
        }

        /**
         * Sets the level log for the context
         *
         * @param levelLog level log
         */
        private void setLevelLog(final Level levelLog) {
            this.levelLog = levelLog;
        }

        /**
         * Sets the level log for a given context
         *
         * @param context the context
         * @param levelLog the level log
         */
        public static void setLevelLog(final Context context,
                final Level levelLog) {
            context.setLevelLog(levelLog);
        }

        /**
         * Sets the DataCite URL for a given context
         *
         * @param context the context
         * @param dataCiteUrl the DataCite URL
         */
        public static void setDataCiteUrl(final Context context,
                final String dataCiteUrl) {
            context.setDataCiteURl(dataCiteUrl);
        }

    }

}
