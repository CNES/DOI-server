/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//TODO
// Créer singleton pour DB token
// Extraire la bd de UniqueFileName et le mettre dans un package DB avec DB token
// Vérifier les authentification et authorisations
package fr.cnes.doi.application;

import fr.cnes.doi.settings.ProxySettings;
import fr.cnes.doi.utils.Requirement;
import fr.cnes.doi.utils.Utils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.service.CorsService;


/**
 * Creates a base application by retrieving the proxy settings.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = "DOI_DOC_010",
        reqName = "Documentation des interfaces"
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
     * Logger.
     */
    public static final Logger LOGGER = Utils.getAppLogger();
    
    /**
     * Proxy settings.
     */
    protected ProxySettings proxySettings;
    
    /**
     * This constructor creates an instance of proxySettings.
     */
    public BaseApplication() {                
        this.proxySettings = ProxySettings.getInstance();
    }
    
    /**
     * Defines CORS service.
     * @param corsOrigin IP that can access to the service
     * @param corsCredentials credentials allowed
     * @return CORS service
     */
    protected final CorsService createCoreService(final Set corsOrigin, final boolean corsCredentials) {        
        LOGGER.entering(BaseApplication.class.getName(), "createCoreService");

        CorsService corsService = new CorsService();
        LOGGER.log(Level.INFO, "Allows all origins {0}", corsOrigin);        
        corsService.setAllowedOrigins(corsOrigin);
        LOGGER.log(Level.INFO, "Allows Credientials {0}", corsCredentials);
        corsService.setAllowedCredentials(corsCredentials);

        LOGGER.exiting(BaseApplication.class.getName(), "createCoreService", corsService);
        return corsService;
    } 

    /**
     * Creates the authenticator. Creates the user, role and mapping user/role.
     *
     * @return Authenticator based on a challenge scheme
     */
    @Requirement(
            reqId = "DOI_AUTH_040",
            reqName = "Association des projets"
    )
    protected ChallengeAuthenticator createAuthenticator() {
        LOGGER.entering(DoiMdsApplication.class.getName(), "createAuthenticator");

        ChallengeAuthenticator guard = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "realm");
        
        guard.setVerifier(this.getContext().getDefaultVerifier());
        guard.setEnroler(this.getContext().getDefaultEnroler());        
        
        LOGGER.exiting(DoiMdsApplication.class.getName(), "createAuthenticator", guard);

        return guard;
    }    
    
}
