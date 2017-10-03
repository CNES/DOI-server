/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.datacite.schema.kernel_4.Resource;
import org.datacite.schema.kernel_4.Resource.Identifier;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;

import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.utils.spec.Requirement;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.xml.sax.SAXException;

/**
 * Client to query Metadata store service at Datacite.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 * @see "https://mds.datacite.org/static/apidoc"
 */
@Requirement(
        reqId = Requirement.DOI_INTER_010,
        reqName = Requirement.DOI_INTER_010_NAME
)
public class ClientMDS extends BaseClient {

    /**
     * Metadata store service endpoint {@value #DATA_CITE_URL}.
     */
    public static final String DATA_CITE_URL = "https://mds.datacite.org";

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
    public static final String TEST_DOI_PREFIX = "10.5072";

    /**
     * DOI query parameter {@value #POST_DOI}.
     */
    public static final String POST_DOI = "doi";

    /**
     * URL query parameter {@value #POST_URL}.
     */
    public static final String POST_URL = "url";

    /**
     * Options for each context
     */
    public enum Context {

        /**
         * Development context.
         */
        DEV(true, true, DATA_CITE_TEST_URL, Level.ALL),
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
        private final String dataCiteUrl;

        Context(final boolean isTestMode, final boolean isDoiPrefix, 
                final String dataciteUrl, final Level levelLog) {
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
        public static void setLevelLog(final Context context, final Level levelLog) {
            context.setLevelLog(levelLog);
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
     */
    public ClientMDS(final Context context) {
        super(DATA_CITE_URL);
        this.context = context;
        this.testMode = this.context.hasTestMode() ? TEST_MODE : null;
        this.getClient().getLogger().setUseParentHandlers(true);
        this.getClient().getLogger().setLevel(Level.ALL);
        this.getClient().setLoggable(true);
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
     */
    public ClientMDS(final Context context, final String login, final String pwd) {
        this(context);
        this.getClient().getLogger().log(Level.FINEST, "Authentication with HTTP_BASIC : {0}/{1}", new Object[]{login, pwd});
        this.getClient().setChallengeResponse(ChallengeScheme.HTTP_BASIC, login, pwd);
    }

    /**
     * Returns the {@link TEST_MODE} or an empty parameter according to
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
        final String message = String.format("DOI %s has been renamed as %s for testing", doiName, testingPrefix);
        this.getClient().getLogger().log(Level.INFO, message);
        return testingPrefix;
    }

    /**
     * Init the client to the reference {@link DATA_CITE_URL} or
     * {@link DATA_CITE_TEST_URL}
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
    private Reference createReferenceWithDOI(final String segment, final String doiName) {
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
    private void checkInputForm(final Form form) throws IllegalArgumentException{
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
     * This request returns an URL associated with a given DOI. A 200 status is
     * an operation successful. A 204 status means no content (DOI is known to MDS, 
     * but is not minted (or not resolvable e.g. due to handle's latency) The DOI 
     * prefix may replace according to the {@link ClientMDS#context}.
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
        final String result;
        final Reference url = createReferenceWithDOI(DOI_RESOURCE, doiName);
        this.getClient().getLogger().log(Level.INFO, "GET {0}", url.toString());

        this.getClient().setReference(url);
        final Representation rep;
        try {
            rep = this.getClient().get();
            result = this.getText(rep);
            return result;
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getResponse().getEntityAsText(), ex);
        } finally {
            this.getClient().release();
        }
    }

    /**
     * This request returns a list of all DOIs for the requesting datacentre.
     * There is no guaranteed order. A 200 status is an operation successful.
     * A 204 status means no Content (no DOIs found)
     *
     * @return a list of all DOI or no content when no DOIs founds
     * @throws ClientMdsException 204 No Content - no DOIs founds
     * @see "https://mds.datacite.org/static/apidoc#tocAnchor-14"
     */
    public String getDoiCollection() throws ClientMdsException {
        final String result;
        final Reference url = createReference(DOI_RESOURCE);
        this.getClient().getLogger().log(Level.INFO, "GET {0}", url.toString());

        this.getClient().setReference(url);
        final Representation rep;
        try {
            rep = this.getClient().get();
            result = this.getText(rep);
            return result;
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getResponse().getEntityAsText(), ex);
        } finally {
            this.getClient().release();
        }
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
            final String result;
            this.checkInputForm(form);
            final Reference url = createReference(DOI_RESOURCE);
            Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "POST {0}", url.toString());
            this.getClient().setReference(url);
            String requestBody = 
                    POST_DOI + "=" + form.getFirstValue(POST_DOI) + "\n" 
                    + POST_URL + "=" + form.getFirstValue(POST_URL);
            requestBody = new String(requestBody.getBytes(
                    StandardCharsets.UTF_8), 
                    StandardCharsets.UTF_8
            );
            final Map<String,Object> requestAttributes = this.getClient().getRequestAttributes();
            requestAttributes.put("charset", StandardCharsets.UTF_8);
            requestAttributes.put("Content-Type", "text/plain");
            this.getClient().getLogger().log(Level.INFO, "POST {0} with parameters {1}", new Object[]{url, requestBody});
            final Representation rep = this.getClient().post(requestBody, MediaType.TEXT_PLAIN);
            result = getText(rep);
            return result;
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getResponse().getEntityAsText(), ex);
        } finally {
            this.getClient().release();
        }
    }

    /**
     * Parses the XML representation that implements the DATACITE schema.
     *
     * @param rep XML representation
     * @return the Resource object
     * @throws ClientMdsException Will throw when a problem happens during the
     * parsing
     */
    private Resource parseDataciteResource(final Representation rep) throws ClientMdsException {
        final Resource resource;
        try {
            final JAXBContext ctx = JAXBContext.newInstance(new Class[]{Resource.class});
            final Unmarshaller unMarshaller = ctx.createUnmarshaller();
            resource = (Resource) unMarshaller.unmarshal(rep.getStream());            
        } catch (JAXBException | IOException ex) {
            throw new ClientMdsException(Status.SERVER_ERROR_INTERNAL, ex);
        }
        return resource;
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
        Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "GET {0}", url.toString());
        this.getClient().setReference(url);
        this.getClient().getLogger().log(Level.INFO, "GET {0}", url);
        try {
            return this.getClient().get(MediaType.APPLICATION_XML);
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getResponse().getEntityAsText(), ex);
        } finally {
            this.getClient().release();
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
            final String result;
            final Identifier identifier = entity.getIdentifier();
            identifier.setValue(getDoiAccorgindToContext(identifier.getValue()));
            final Reference url = createReference(METADATA_RESOURCE);
            Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "POST {0}", url.toString());
            final OutputStream output = new OutputStream() {
                /**
                 * Output stream.
                 */
                private final StringBuilder response = new StringBuilder();

                /**
                 * Write into output stream
                 * @param b char
                 * @throws IOException  - if a problem happens
                 */
                @Override
                public void write(final int b) throws IOException {
                    this.response.append((char) b);
                }

                /**
                 * Transforms toString.
                 * @return response as String
                 */
                @Override
                public String toString() {
                    return this.response.toString();
                }
            };
            final JAXBContext jaxbContext = JAXBContext.newInstance(new Class[]{Resource.class});
            final Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, 
                    "http://datacite.org/schema/kernel-4 "
                    + "http://schema.datacite.org/meta/kernel-4/metadata.xsd");
            final Schema schema = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI
            ).newSchema(new URL("https://schema.datacite.org/meta/kernel-4.0/metadata.xsd"));
            marshaller.setSchema(schema);
            marshaller.marshal(entity, output);
            this.getClient().setReference(url);
            this.getClient().getRequestAttributes().put("charset", "UTF-8");
            final Representation response = this.getClient().post(
                    new StringRepresentation(output.toString(), MediaType.APPLICATION_XML)
            );            
            result = getText(response);
            return result;
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getResponse().getEntityAsText(), ex);
        } catch (JAXBException | SAXException | MalformedURLException ex) {
            throw new ClientMdsException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(), ex);
        } finally {
            this.getClient().release();
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
        Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "DELETE {0}", url.toString());
        this.getClient().setReference(url);
        final Representation response = this.getClient().delete();
        final Status status = this.getClient().getStatus();
        this.getClient().release();
        if (status.isSuccess()) {
            return response;
        } else {
            throw new ClientMdsException(status, status.getDescription());
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
        Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "GET {0}", url.toString());
        this.getClient().setReference(url);
        try {
            final Representation response = this.getClient().get();
            result = this.getText(response);
            return result;
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getResponse().getEntityAsText(), ex);
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
    public String createMedia(final String doiName, final Form form) throws ClientMdsException {
        final String result;
        final Reference url = createReferenceWithDOI(MEDIA_RESOURCE, doiName);
        Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "POST {0}", url.toString());
        this.getClient().setReference(url);
        final Representation entity = createEntity(form);
        try {
            final Representation response = this.getClient().post(entity, MediaType.TEXT_PLAIN);
            result = this.getText(response);
            return result;
        } catch (ResourceException ex) {
            throw new ClientMdsException(ex.getStatus(), ex.getResponse().getEntityAsText(), ex);
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

}
