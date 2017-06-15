/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.datacite.schema.kernel_4.Resource;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Client to query Metadata store service at Datacite. 
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 * @see "https://datacite.readme.io/v1.0/docs/api"
 */
public class ClientMDS {

    /**
     * Metadata store service endpoint.
     */
    public static final String DATA_CITE_URL = "https://mds.datacite.org";

    /**
     * Metadata store test service endpoint.
     */
    public static final String DATA_CITE_TEST_URL = "https://mds.test.datacite.org";

    /**
     * DOI resource.
     */
    public static final String DOI_RESOURCE = "doi";

    /**
     * Metadata resource.
     */
    public static final String METADATA_RESOURCE = "metadata";

    /**
     * Media resource.
     */
    public static final String MEDIA_RESOURCE = "media";

    /**
     *
     */
    public static final Parameter TEST_MODE = new Parameter("testMode", "true");

    /**
     * Test DOI prefix.
     */
    public static final String TEST_DOI_PREFIX = "10.5072";

    /**
     * DOI query parameter.
     */
    public static final String POST_DOI = "doi";

    /**
     * URL query parameter.
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
         * Each API call can have optional query parametertestMode. 
         * If set to "true" or "1" the request will not change the database nor 
         * will the DOI handle will be registered or updated, e.g. POST
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

        Context(boolean isTestMode, boolean isDoiPrefix, final String dataciteUrl, final Level levelLog) {
            this.isTestMode = isTestMode;
            this.isDoiPrefix = isDoiPrefix;
            this.dataCiteUrl = dataciteUrl;
            this.levelLog = levelLog;
        }

        /**
         * Returns true when the context has a DOI dev.
         * @return True when the context has a DOI dev
         */
        public boolean hasDoiTestPrefix() {
            return this.isDoiPrefix;
        }

        /**
         * Returns true when the context must not register data in DataCite
         * @return true when the context must not register data in DataCite
         */
        public boolean hasTestMode() {
            return this.isTestMode;
        }

        /**
         * Returns the log level.
         * @return the log level
         */
        public Level getLevelLog() {
            return this.levelLog;
        }
        
        /**
         * Returns the service end point.
         * @return the service end point
         */
        public String getDataCiteUrl() {
            return this.dataCiteUrl;
        }

        /**
         * Sets the level log for the context
         * @param levelLog 
         */
        private void setLevelLog(final Level levelLog) {
            this.levelLog = levelLog;
        }

        /**
         * Sets the level log for a given context
         * @param context the context
         * @param levelLog  the level log
         */
        public static void setLevelLog(Context context, Level levelLog) {
            context.setLevelLog(levelLog);
        }
              
    }

    private final ClientResource client = new ClientResource(DATA_CITE_URL);
    private final Parameter testMode;
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
        this.context = context;
        this.testMode = this.context.hasTestMode() ? TEST_MODE : null;
        this.client.getLogger().setUseParentHandlers(true);        
        this.client.getLogger().setLevel(Level.ALL);        
        this.client.setLoggable(true);
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
        this.client.getLogger().log(Level.FINEST, "Authentication with HTTP_BASIC : {0}/{1}", new Object[]{login, pwd});
        this.client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, login, pwd);
    }
    
    /**
     * Sets Proxy authentication.
     * @param authentication authentication
     */
    public void setProxyAuthentication(final ChallengeResponse authentication) {        
       this.client.getLogger().log(Level.FINEST,"Proxy Authentication :\n"
               + "- login:{0}"+"\n"+"- pwd:{1}\n", new Object[]{authentication.getIdentifier(), authentication.getSecret()});
       this.client.setProxyChallengeResponse(authentication);
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
     * Renames the current DOI prefix by the testing DOI prefix
     *
     * @param doiName
     * @return the renamed prefix
     */
    private String useTestPrefix(final String doiName) {
        String[] split = doiName.split("/");
        split[0] = TEST_DOI_PREFIX;
        String testingPrefix = String.join("/", split);
        String message = String.format("DOI %s has been renamed as %s for testing", doiName, testingPrefix);
        this.client.getLogger().log(Level.INFO, message);
        return testingPrefix;
    }

    /**
     * Init the client to the reference {@link DATA_CITE_URL} or {@link DATA_CITE_TEST_URL}
     */
    private void initReference() {
        this.client.setReference(this.context.getDataCiteUrl());
    }

    /**
     * Create reference
     * @param segment segment to add to the end point
     * @return new URL
     */
    private Reference createReference(final String segment) {
        this.initReference();
        Reference url = this.client.addSegment(segment);
        if(this.getTestMode() != null) {
            url = url.addQueryParameter(this.getTestMode());
        }
        return url;
    }

    /**
     * Creates the URL to query.
     * @param segment segment to add to the end point service
     * @param doiName doi name
     * @return the URL to query
     */
    private Reference createReferenceWithDOI(final String segment, final String doiName) {
        String requestDOI = this.context.hasDoiTestPrefix() ? useTestPrefix(doiName) : doiName;
        Reference ref = createReference(segment);
        String[] split = requestDOI.split("/");
        for (String segmentUri : split) {
            ref.addSegment(segmentUri);
        }
        return ref;
    }

    /**
     * Returns the text of a response.
     *
     * @param rep Response of the server
     * @param status Status of the response
     * @return the text of the response
     * @throws ClientMdsException An exception is thrown for a status!=2xx
     */
    private String getText(final Representation rep, final Status status) throws ClientMdsException {
        String result;
        try {
            result = rep.getText();
        } catch (IOException ex) {
            throw new ClientMdsException(Status.SERVER_ERROR_INTERNAL, ex);
        }
        if (!status.isSuccess()) {
            throw new ClientMdsException(status, result);
        }
        return result;
    }

    /**
     * Checks the input parameters and specially the validity of the DOI name. 
     * @param form query form
     */
    private void checkInputForm(final Form form) {
        Map<String, String> map = form.getValuesMap();
        if (map.containsKey(POST_DOI) && map.containsKey(POST_URL)) {
            final String doiName = form.getFirstValue(POST_DOI);
            checkIfAllCharsAreValid(doiName);
            if (this.context.hasDoiTestPrefix()) {
                form.set(POST_DOI, useTestPrefix(doiName));
            }
        } else {
            throw new IllegalArgumentException(POST_DOI+" and "+POST_URL+" parameters are required");
        }
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
     *</ul>
     * @param test DOI name to test
     */
    private void checkIfAllCharsAreValid(String test) {
        if (!test.matches("^[0-9a-zA-Z\\-._+:/\\s]+$")) {
            throw new IllegalArgumentException("Only these characters are allowed 0-9a-zA-Z\\-._+:/ in a DOI name");
        }
    }

    /**
     * This request returns an URL associated with a given DOI.
     *
     * The different status:
     * <ul>
     * <li>200 OK - operation successful</li>
     * <li>204 No Content - DOI is known to MDS, but is not minted (or not
     * resolvable e.g. due to handle's latency)</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 - login problem or dataset belongs to another party</li>
     * <li>404 Not Found - DOI does not exist in our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     *
     * @param doiName DOI name
     * @return an URL or no content (DOI is known to MDS, but is not minted (or
     * not resolvable e.g. due to handle's latency))
     * @throws ClientMdsException An exception is thrown for a status!=2xx
     */
    public String getDoi(final String doiName) throws ClientMdsException {
        String result = null;
        Reference url = createReferenceWithDOI(DOI_RESOURCE, doiName);
        this.client.getLogger().log(Level.INFO, "GET {0}", url.toString());

        this.client.setReference(url);
        Representation rep = this.client.get();
        Status responseStatus = this.client.getStatus();

        try {
            this.getText(rep, responseStatus);
        } finally {
            client.release();
        }
        return result;
    }

    /**
     * This request returns a list of all DOIs for the requesting datacentre.
     *
     * There is no guaranteed order. The different status:
     * <ul>
     * <li>200 OK - operation successful</li>
     * <li>204 No Content - no DOIs founds</li>
     * </ul>
     *
     * @return a list of all DOI or no content when no DOIs founds
     * @throws ClientMdsException An exception is thrown for a status!=2xx
     */
    public String getDoiCollection() throws ClientMdsException {
        String result = null;
        Reference url = createReference(DOI_RESOURCE);
        this.client.getLogger().log(Level.INFO, "GET {0}", url.toString());

        this.client.setReference(url);
        Representation rep = this.client.get();
        Status responseStatus = this.client.getStatus();
        try {
            this.getText(rep, responseStatus);
        } finally {
            client.release();
        }
        return result;
    }        

    /**
     * Will mint new DOI if specified DOI doesn't exist.
     *
     * This method will attempt to update URL if you specify existing DOI.
     * Standard domains and quota restrictions check will be performed. A
     * Datacentre's doiQuotaUsed will be increased by 1. A new record in
     * Datasets will be created.
     *
     * <p>
     * The different status:
     * <ul>
     * <li>201 Created - operation successful</li>
     * <li>400 Bad Request - request body must be exactly two lines: DOI and
     * URL; wrong domain, wrong prefix</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem, quota exceeded</li>
     * <li>412 Precondition failed - metadata must be uploaded first</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     *
     * @param form A form with the following attributes doi and url
     * @return short explanation of status code e.g. CREATED,
     * HANDLE_ALREADY_EXISTS etc
     * @throws ClientMdsException Will throw an exception when status!=2xx
     */
    public String createDoi(final Form form) throws ClientMdsException {
        try {            
            String result;
            this.checkInputForm(form);
            Reference url = createReference(DOI_RESOURCE);
            url = url.addQueryParameter(this.getTestMode());
            Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "POST {0}", url.toString());
            this.client.setReference(url);
            String requestBody = POST_DOI+"="+form.getFirstValue(POST_DOI)+"\n"+POST_URL+"="+form.getFirstValue(POST_URL);
            requestBody = new String(requestBody.getBytes("UTF-8"), "UTF-8");
            this.client.getRequestAttributes().put("charset","UTF-8");
            this.client.getLogger().log(Level.INFO, "POST {0} with parameters {1}", new Object[]{url, requestBody});
            Representation rep = this.client.post(requestBody, MediaType.TEXT_PLAIN);
            Status responseStatus = this.client.getStatus();           
            result = getText(rep, responseStatus);            
            return result;
        }   catch (UnsupportedEncodingException ex) {
            throw new ClientMdsException(Status.SERVER_ERROR_INTERNAL, ex);
        } finally {
                this.client.release();
        }
    }
    
    /**
     * Parses the XML representation that implements the DATACITE schema.
     * @param rep XML representation
     * @return the Resource object
     * @throws ClientMdsException Will throw when a problem happens during the parsing
     */
    private Resource parseDataciteResource(final Representation rep) throws ClientMdsException {
        final Resource resource;
        try {
            final String result = getText(rep, Status.SUCCESS_OK);
            JAXBContext ctx = JAXBContext.newInstance(new Class[]{org.datacite.schema.kernel_4.Resource.class});
            Unmarshaller um = ctx.createUnmarshaller();
            JAXBElement<Resource> jaxbResource = (JAXBElement<Resource>) um.unmarshal(new ByteArrayInputStream(result.getBytes()));
            resource = jaxbResource.getValue();
        } catch (JAXBException ex) {
            throw new ClientMdsException(Status.SERVER_ERROR_INTERNAL, ex);
        }    
        return resource;
    }

    /**
     * This request returns the most recent version of metadata associated with
     * a given DOI.
     *
     * The different status:
     * <ul>
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
     *
     * @param doiName DOI name
     * @return XML representing a dataset
     * @throws ClientMdsException Will throw an exception when status != 2xx
     */
    public Resource getMetadataAsObject(final String doiName) throws ClientMdsException {        
        final Representation rep = getMetadata(doiName);
        return parseDataciteResource(rep);
    }
    
    /**
     * Returns the metadata based on its DOI name.
     * The different responses are the followings : 
     * <ul>
     * <li>200 OK - operation successful</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem or dataset belongs to another party</li>
     * <li>404 Not Found - DOI does not exist in our database</li>
     * <li>410 Gone - the requested dataset was marked inactive (using DELETE method)</li>
     * <li>500 Internal Server Error - server internal error, try later and if problem persists please contact us</li>
     * </ul>
     * @param doiName DOI name
     * @return the metadata as XML
     * @throws ClientMdsException Will throw when a problem happens with datacite
     */
    public Representation getMetadata(final String doiName) throws ClientMdsException {
        Reference url = createReferenceWithDOI(METADATA_RESOURCE, doiName);
        Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "GET {0}", url.toString());
        this.client.setReference(url);
        this.client.getLogger().log(Level.INFO, "GET {0}", url);
        Representation rep = this.client.get(MediaType.APPLICATION_XML);
        Status status = this.client.getStatus();
        client.release();
        if(status.isSuccess()) {
            return rep;
        } else {
            throw new ClientMdsException(status, status.getDescription());
        }        
     }

    /**
     * This request stores new version of metadata.
     *
     * The different status:
     * <ul>
     * <li>201 Created - operation successful</li>
     * <li>400 Bad Request - invalid XML, wrong prefix</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem, quota exceeded</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     *
     * @param entity A valid XML
     * @return short explanation of status code e.g. CREATED,
     * HANDLE_ALREADY_EXISTS etc
     * @throws ClientMdsException Will throw an error when a problem happens with DataCite
     */
    public String createMetadata(final Representation entity) throws ClientMdsException {
        String result = null;
        Reference url = createReference(METADATA_RESOURCE);
        Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "POST {0}", url.toString());
        this.client.setReference(url);
        Representation response = this.client.post(entity);
        Status responseStatus = this.client.getStatus();
        try {
            result = getText(response, responseStatus);
        } finally {
            this.client.release();
        }
        return result;
    }
    
    /**
     * Creates metadata.
     * The different status:
     * <ul>
     * <li>201 Created - operation successful</li>
     * <li>400 Bad Request - invalid XML, wrong prefix</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem, quota exceeded</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>     
     * @param entity Metadata
     * @return short explanation of status code e.g. CREATED, HANDLE_ALREADY_EXISTS etc
     * @throws ClientMdsException Will throw an exception for status != 2xx
     */
    public String createMetadata(final Resource entity) throws ClientMdsException {
        String result = null;
        Reference url = createReference(METADATA_RESOURCE);
        Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "POST {0}", url.toString());
        this.client.setReference(url);
        this.client.getRequestAttributes().put("charset","UTF-8");
        Representation response = this.client.post(entity, MediaType.APPLICATION_XML);        
        Status responseStatus = this.client.getStatus();
        try {
            result = getText(response, responseStatus);
        } finally {
            this.client.release();
        }
        return result;
    }    

    /**
     * This request marks a dataset as 'inactive'.
     *
     * To activate it again, POST new metadata or set the isActive-flag in the
     * user interface. The different status:
     * <ul>
     * <li>200 OK - operation successful: dataset deactivated</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem or dataset belongs to another
     * party</li>
     * <li>404 Not Found - DOI does not exist in our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     *
     * @param doiName DOI name
     * @return XML representing a dataset
     * @throws ClientMdsException Will throw an exception for status != 2xx
     */
    public Resource deleteMetadataDoiAsObject(final String doiName) throws ClientMdsException {
        Representation rep = this.getMetadata(doiName);
        return parseDataciteResource(rep);
    }
    
    /**
     * This request marks a dataset as 'inactive'.
     *
     * To activate it again, POST new metadata or set the isActive-flag in the
     * user interface. The different status:
     * <ul>
     * <li>200 OK - operation successful: dataset deactivated</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem or dataset belongs to another
     * party</li>
     * <li>404 Not Found - DOI does not exist in our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     * @param doiName DOI name
     * @return the deleted metadata
     * @throws ClientMdsException throw an error when a problem happens with DataCite
     */
    public Representation deleteMetadata(final String doiName) throws ClientMdsException {
        Reference url = createReferenceWithDOI(METADATA_RESOURCE, doiName);
        Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "DELETE {0}", url.toString());
        this.client.setReference(url);
        Representation response = this.client.delete();
        Status status = this.client.getStatus();
        this.client.release();
        if(status.isSuccess()) {
            return response;
        } else {
            throw new ClientMdsException(status, status.getDescription());
        }
    }

    /**
     * This request returns list of pairs of media type and URLs associated with
     * a given DOI.
     *
     * The difference status:
     * <ul>
     * <li>200 OK - operation successful</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 login problem or dataset belongs to another party</li>
     * <li>404 Not Found - No media attached to the DOI or DOI does not exist in
     * our database</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     *
     * @param doiName DOI name
     * @return list of pairs of media type and URLs
     * @throws ClientMdsException Will throw an exception for status != 2xx
     */
    public String getMedia(final String doiName) throws ClientMdsException {
        String result;
        Reference url = createReferenceWithDOI(MEDIA_RESOURCE, doiName);
        Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "GET {0}", url.toString());
        this.client.setReference(url);
        Representation response = this.client.delete();
        Status responseStatus = this.client.getStatus();
        try {
            result = this.getText(response, responseStatus);
        } finally {
            this.client.release();
        }
        return result;
    }

    /**
     * Will add/update media type/urls pairs to a DOI.
     *
     * Standard domain restrictions check will be performed. The different
     * status:
     * <ul>
     * <li>200 OK - operation successful</li>
     * <li>400 Bad Request - one or more of the specified mime-types or urls are
     * invalid (e.g. non supported mime-type, not allowed url domain, etc.)</li>
     * <li>401 Unauthorized - no login</li>
     * <li>403 Forbidden - login problem</li>
     * <li>500 Internal Server Error - server internal error, try later and if
     * problem persists please contact us</li>
     * </ul>
     *
     * @param form Multiple lines in the following format{mime-type}={url} where
     * {mime-type} and {url} have to be replaced by your mime type and URL,
     * UFT-8 encoded.
     * @return short explanation of status code
     * @throws ClientMdsException Will throw an exception for status != 2xx
     */
    public String createMedia(final Form form) throws ClientMdsException {
        String result;
        Reference url = createReference(METADATA_RESOURCE);
        Engine.getLogger(ClientMDS.class.getName()).log(Level.FINE, "POST {0}", url.toString());
        this.client.setReference(url);
        Representation response = this.client.post(form);
        Status responseStatus = this.client.getStatus();
        try {
            result = this.getText(response, responseStatus);
        } finally {
            this.client.release();
        }
        return result;
    }

}
