/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import fr.cnes.doi.resource.DoiResource;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Extractor;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;

/**
 *
 * @author malapert
 */
public class DoiApplication extends Application {
    
    public static final String DATA_CITE_URL = "https://mds.datacite.org";
    public static final String DOI_RESOURCE = "/doi";
    public static final String METADATA_RESOURCE = "/metadata";
    public static final String MEDIA_RESOURCE = "/media";

    @Override
    public Restlet createInboundRoot() {
//        Router router = new Router(getContext());
//
//        router.attach("/register/{user}", DoiResource.class);
//        ChallengeAuthenticator authenticator = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "My Realm");
//        MapVerifier verifier = new MapVerifier();
//        verifier.getLocalSecrets().put("test", "pwd".toCharArray());
//        authenticator.setVerifier(verifier);
//        authenticator.setNext(router);
//
//        return authenticator;
        Router router = new Router(getContext());
        String target = "http://www.google.com/search?q=site:mysite.org+{keywords}";
        Redirector redirector = new Redirector(getContext(), target, Redirector.MODE_SERVER_OUTBOUND);
        Extractor extractor = new Extractor(getContext(), redirector);
        extractor.extractFromQuery("keywords", "kwd", true);
        router.attach("/search", extractor);
        return router;
    }
}
