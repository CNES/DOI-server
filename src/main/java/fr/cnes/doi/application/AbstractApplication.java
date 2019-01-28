/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.application;

import fr.cnes.doi.services.CnesStatusService;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.settings.EmailSettings;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.ext.wadl.WadlCnesRepresentation;
import org.restlet.representation.Representation;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.service.CorsService;

/**
 * Creates a base application by setting both the CORS, the DOI status page, the proxy and the DOI.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
public abstract class AbstractApplication extends WadlApplication {

    /**
     * Default value of 'Access-Control-Allow-Origin' header.
     */
    public static final Set DEFAULT_CORS_ORIGIN = Collections.unmodifiableSet(
            new HashSet(Arrays.asList("*"))
    );

    /**
     * If true, adds 'Access-Control-Allow-Credentials' header.
     */
    public static final boolean DEFAULT_CORS_CREDENTIALS = true;

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(AbstractApplication.class.getName());

    /**
     * DOI settings.
     */
    private final DoiSettings config;

    /**
     * This constructor creates an instance of proxySettings and doiSettings. By creating the
     * instance, the constructor creates :
     * <ul>
     * <li>the CORS service using the default value {@link AbstractApplication#DEFAULT_CORS_ORIGIN}
     * and {@link AbstractApplication#DEFAULT_CORS_CREDENTIALS}</li>
     * <li>the custom status page</li>
     * </ul>
     *
     * @see <a href="https://en.wikipedia.org/wiki/Cross-origin_resource_sharing">CORS</a>
     * @see AbstractApplication#createCoreService
     */
    public AbstractApplication() {
        this.config = DoiSettings.getInstance();
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
    protected final CorsService createCoreService(final Set corsOrigin,
            final boolean corsCredentials) {
        LOG.traceEntry();
        final CorsService corsService = new CorsService();
        LOG.info("Allows all origins {}", corsOrigin);
        corsService.setAllowedOrigins(corsOrigin);
        LOG.info("Allows Credientials {}", corsCredentials);
        corsService.setAllowedCredentials(corsCredentials);

        return LOG.traceExit(corsService);
    }

    /**
     * Creates the authenticator based on a HTTP basic. Creates the user, role and mapping
     * user/role.
     *
     * @return Authenticator based on a challenge scheme
     */
    @Requirement(reqId = Requirement.DOI_AUTH_010, reqName = Requirement.DOI_AUTH_010_NAME)
    protected ChallengeAuthenticator createAuthenticator() {
        LOG.traceEntry();
        final ChallengeAuthenticator guard = new ChallengeAuthenticator(
                getContext(), ChallengeScheme.HTTP_BASIC, "realm");

        guard.setVerifier(this.getContext().getDefaultVerifier());
        guard.setEnroler(this.getContext().getDefaultEnroler());

        return LOG.traceExit(guard);
    }

    /**
     * Creates HTML representation of the WADL.
     *
     * @param applicationInfo Application description
     * @return the HTML representation of the WADL
     */
    @Override
    protected Representation createHtmlRepresentation(final ApplicationInfo applicationInfo) {
        final WadlCnesRepresentation wadl = new WadlCnesRepresentation(applicationInfo);
        return wadl.getHtmlRepresentation();
    }

    /**
     * Sends alert as an email when DataCite failed.
     *
     * @param exception error message to send
     */
    public void sendAlertWhenDataCiteFailed(final Exception exception) {
        LOG.traceEntry("Parameters : {}", exception);
        final String subject = "Datacite problem";
        final String message = "Dear administrator, an error has been detected"
                + " coming from Datacite, please look to the Service status\n" + exception;
        EmailSettings.getInstance().sendMessage(subject, message);
    }

    /**
     * Returns the configuration of the DOI server.
     *
     * @return the configuration of the DOI server.
     */
    protected final DoiSettings getConfig() {
        LOG.traceEntry();
        return LOG.traceExit(config);
    }

    /**
     * Returns the logger.
     *
     * @return the logger
     */
    public Logger getLog() {
        return LOG;
    }

}
