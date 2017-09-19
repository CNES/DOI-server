/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import fr.cnes.doi.services.CnesStatusService;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.settings.EmailSettings;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.service.CorsService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import fr.cnes.doi.settings.ProxySettings;
import fr.cnes.doi.utils.spec.Requirement;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.WadlCnesRepresentation;
import org.restlet.representation.Representation;

/**
 * Creates a base application by retrieving the proxy settings.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = Requirement.DOI_DOC_010,
        reqName = Requirement.DOI_DOC_010_NAME
)
public class BaseApplication extends WadlApplication {

    /**
     * Default value of 'Access-Control-Allow-Origin' header.
     */
    public static final Set DEFAULT_CORS_ORIGIN = new HashSet(Arrays.asList("*"));

    /**
     * If true, adds 'Access-Control-Allow-Credentials' header.
     */
    public static final boolean DEFAULT_CORS_CREDENTIALS = true;

    /**
     * Instance of configuration settings.
     */
    protected final DoiSettings config;

    /**
     * Proxy settings.
     */
    protected final ProxySettings proxySettings;

    /**
     * This constructor creates an instance of proxySettings. By creating the
     * instance, the constructor creates :
     * <ul>
     * <li>the CORS service using the default value
     * {@link BaseApplication#DEFAULT_CORS_ORIGIN} and
     * {@link BaseApplication#DEFAULT_CORS_CREDENTIALS}</li>
     * <li>the custom status page</li>
     * </ul>
     *
     * @see
     * <a href="https://en.wikipedia.org/wiki/Cross-origin_resource_sharing">CORS</a>
     * @see BaseApplication#createCoreService
     */
    public BaseApplication() {
        this.config = DoiSettings.getInstance();
        this.proxySettings = ProxySettings.getInstance();
        getServices().add(this.createCoreService(DEFAULT_CORS_ORIGIN, DEFAULT_CORS_CREDENTIALS));
        setStatusService(new CnesStatusService());
        setOwner("Centre National d'Etudes Spatiales (CNES)");
        setAuthor("Jean-Christophe Malapert (DNO/ISA/VIP)");
    }

    /**
     * Defines the CORS service.
     *
     * @param corsOrigin IP that can access to the service
     * @param corsCredentials credentials allowed
     * @return CORS service
     */
    protected final CorsService createCoreService(final Set corsOrigin, final boolean corsCredentials) {
        getLogger().entering(BaseApplication.class.getName(), "createCoreService");

        CorsService corsService = new CorsService();
        getLogger().log(Level.INFO, "Allows all origins {0}", corsOrigin);
        corsService.setAllowedOrigins(corsOrigin);
        getLogger().log(Level.INFO, "Allows Credientials {0}", corsCredentials);
        corsService.setAllowedCredentials(corsCredentials);

        getLogger().exiting(BaseApplication.class.getName(), "createCoreService", corsService);

        return corsService;
    }

    /**
     * Creates the authenticator. Creates the user, role and mapping user/role.
     *
     * @return Authenticator based on a challenge scheme
     */
    @Requirement(
            reqId = Requirement.DOI_AUTH_010,
            reqName = Requirement.DOI_AUTH_010_NAME
    )
    protected ChallengeAuthenticator createAuthenticator() {
        getLogger().entering(DoiMdsApplication.class.getName(), "createAuthenticator");

        ChallengeAuthenticator guard = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "realm");

        guard.setVerifier(this.getContext().getDefaultVerifier());
        guard.setEnroler(this.getContext().getDefaultEnroler());

        getLogger().exiting(DoiMdsApplication.class.getName(), "createAuthenticator", guard);

        return guard;
    }

    /**
     * Creates HTML representation of the WADL.
     *
     * @param applicationInfo Application description
     * @return the HTML representation of the WADL
     */
    @Override
    protected Representation createHtmlRepresentation(ApplicationInfo applicationInfo) {
        WadlCnesRepresentation wadl = new WadlCnesRepresentation(applicationInfo);
        return wadl.getHtmlRepresentation();
    }

    public void sendAlertWhenDataCiteFailed(final Exception ex) {
        String subject = "Datacite problem";
        String message = "Dear administrator, an error has been detected"
                + " coming from Datacite, please look to the Service status\n" + ex;
        EmailSettings.getInstance().sendMessage(subject, message);
    }

}
