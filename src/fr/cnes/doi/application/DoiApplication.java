/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import org.restlet.Restlet;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

/**
 *
 * @author Jean-Christophe Malapert
 * https://search.datacite.org/help.html
 */
public class DoiApplication extends WadlApplication {
    
    public static final String DATA_CITE_URL = "https://mds.datacite.org";
    public static final String DOI_RESOURCE = "/doi";
    public static final String METADATA_RESOURCE = "/metadata";
    public static final String MEDIA_RESOURCE = "/media";
    
    public DoiApplication() {
        super();
        setAuthor("Jean-Christophe Malapert (DNO/ISA/VIP)");
        setName("Digital Object Identifier Application");
        setOwner("CNES");       
    }

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
        String target = "http://status.datacite.org";
        Redirector redirector = new Redirector(getContext(), target, Redirector.MODE_SERVER_OUTBOUND);        
        router.attach("/status", redirector);
        return router;
    }
    
    
}
