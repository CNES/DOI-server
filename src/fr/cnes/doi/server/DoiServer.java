/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.server;

import fr.cnes.doi.application.DoiApplication;
import java.security.KeyStore;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.util.Series;

/**
 *
 * @author malapert
 */
public class DoiServer {

    public static void main(String[] args) throws Exception {

        Engine.setLogLevel(java.util.logging.Level.INFO);

        // Create a new Restlet component and add a HTTP and HTTPS server connector to it  
        Component component = new Component();
        //component.getServers().add(startHttpsServer(component, 443));
        component.getServers().add(startHttpServer(component, 8182));
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.HTTPS);

        // Attach the application to the component and start it  
        component.getDefaultHost().attachDefault(new DoiApplication());
        component.getLogService().setResponseLogFormat("{ciua} {cri} {ra} {m} {rp} {rq} {S} {ES} {es} {hh} {cig} {fi}");
        component.start();
    }

    private static Server startHttpServer(Component component, Integer port) throws Exception {
        return new Server(Protocol.HTTP, port, component);
    }

    private static Server startHttpsServer(Component component, Integer port) throws Exception {
        // create embedding https jetty server
        Server server = new Server(new Context(), Protocol.HTTPS, port, component);

        Series<Parameter> parameters = server.getContext().getParameters();
        parameters.add("keystore", "jks/keystore.jks");
        parameters.add("keyStorePath", "jks/keystore.jks");
        parameters.add("keyStorePassword", "xxx");
        parameters.add("keyManagerPassword", "xxx");
        parameters.add("keyPassword", "xxx");
        parameters.add("password", "xxx");
        parameters.add("keyStoreType", KeyStore.getDefaultType());
        parameters.add("tracing", "true");
        parameters.add("truststore", "jks/keystore.jks");
        parameters.add("trustStorePath", "jks/keystore.jks");
        parameters.add("trustStorePassword", "xxx");
        parameters.add("trustPassword", "xxx");
        parameters.add("trustStoreType", KeyStore.getDefaultType());
        parameters.add("allowRenegotiate", "true");
        parameters.add("type", "1");

        return server;
    }

}
// Generating a self-signed certificate
//--------------------------------------
//keytool -keystore serverKey.jks -alias server -genkey -keyalg RSA
//-keysize 2048 -dname "CN=simpson.org,OU=Simpson family,O=The Simpsons,C=US"
//-sigalg "SHA1withRSA"
//
//Note that you’ll be prompted for passwords for the keystore and the key itself. Let’s
//enter password as the example value. This certificate can then be exported as an inde-
//pendent certificate file server.crt, using this command (providing the same password):
//keytool -exportcert -keystore serverKey.jks -alias server -file serverKey.crt
//
// Generating a certificate request
//----------------------------------
//keytool -certreq -keystore serverKey.jks -alias server -file serverKey.csr
//
// Importing a trusted certificate
//----------------------------------
//After approval of the certificate request, the CA will provide a certificate file, usually in
//PEM or DER format. It needs to be imported back into the keystore to be used as a
//server certificate:
//keytool -import -keystore serverKey.jks -alias server -file serverKey.crt
//This command is also used for importing CA certificates into a special keystore that’s
//going to be used as a truststore—on the client side, for example. In this case the
//-trustcacerts options may also be required:
//keytool -import -keystore clientTrust.jks -trustcacerts -alias server -file serverKey.crt
//This trusted certificate may also be imported explicitly into your browser or used by a
//programmatic HTTPS client. This is useful if you’re deploying your own infrastruc-
//ture, or during development phases.

