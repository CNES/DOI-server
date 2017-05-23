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
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;

/**
 *
 * @author malapert
 */
public class DoiApplication extends Application {

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        router.attach("/register/{user}", DoiResource.class);
        ChallengeAuthenticator authenticator = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "My Realm");
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("test", "pwd".toCharArray());
        authenticator.setVerifier(verifier);
        authenticator.setNext(router);

        return authenticator;
    }
}
