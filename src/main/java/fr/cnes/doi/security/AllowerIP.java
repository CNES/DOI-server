/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.text.MessageFormat;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArraySet;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class AllowerIP extends org.restlet.routing.Filter {
    
    /**
     * Class name.
     */
    private static final String CLASS_NAME = AllowerIP.class.getName();

    /**
     * Localhost in IPv6.
     */
    public static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    /**
     * Localhost in IPv4.
     */
    public static final String LOCALHOST_IPV4 = "127.0.0.1";

    /**
     * List of allowed IPs addresses.
     */
    private final Set<String> allowedAddresses;

    /**
     * Constructor.
     *
     * @param context context
     */
    public AllowerIP(final Context context) {
        super(context);
        getLogger().entering(CLASS_NAME, "Constructor");
        this.allowedAddresses = new CopyOnWriteArraySet<>();
        this.allowedAddresses.add(LOCALHOST_IPV6);
        this.allowedAddresses.add(LOCALHOST_IPV4);
        addCustomIP(allowedAddresses);
        getLogger().exiting(CLASS_NAME, "Constructor");
    }

    /**
     * Adds custom IPs based on the settings.
     *
     * @param allowedAddresses the new allowed addresses
     */
    private void addCustomIP(final Set<String> allowedAddresses) {
        getLogger().entering(CLASS_NAME, "addCustomIP", allowedAddresses);
        final String ips = DoiSettings.getInstance().getString(Consts.ADMIN_IP_ALLOWER);
        if (ips != null) {
            final StringTokenizer tokenizer = new StringTokenizer(ips, "|");
            while (tokenizer.hasMoreTokens()) {
                final String newIP = tokenizer.nextToken();
                getLogger().info(MessageFormat.format("Adds this IP {0} for allowing the access to the amdinistration application", newIP));
                allowedAddresses.add(newIP);
            }
        }
        getLogger().exiting(CLASS_NAME, "addCustomIP");
    }

    /**
     * Before Handler.
     * @param request request
     * @param response response
     * @return beforeHandler 
     */
    @Override
    protected int beforeHandle(final Request request, final Response response) {
        int result = STOP;
        final String ipClient = request.getClientInfo().getAddress();
        if (getAllowedAddresses().contains(ipClient)) {
            result = CONTINUE;
        } else {
            getLogger().warning(MessageFormat.format("You IP address {0} was blocked", ipClient));
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Your IP address was blocked");
        }
        return result;
    }

    /**
     * Returns the allowed addresses.
     *
     * @return the allowed addresses
     */
    public Set<String> getAllowedAddresses() {
        return allowedAddresses;
    }

}
