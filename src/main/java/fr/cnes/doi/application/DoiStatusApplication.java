/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.application;

import fr.cnes.doi.settings.ProxySettings;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

/**
 * Monitors status datacite page.
 *
 * @author Jean-Christophe Malapert
 */
public class DoiStatusApplication extends BaseApplication {

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        String target = "http://status.datacite.org";
        Redirector redirector = new Redirector(getContext(), target, Redirector.MODE_SERVER_OUTBOUND);
        router.attachDefault(redirector);

        Filter authentication = new Filter() {
            @Override
            protected int doHandle(Request request, Response response) {
                request.setChallengeResponse(ProxySettings.getInstance().getProxyAuthentication());
                this.setNext(this.getContext().getClientDispatcher());
                return super.doHandle(request, response);
            }            
        };
        authentication.setNext(router);
        return authentication;
    }

}
