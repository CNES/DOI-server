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
    public AllowerIP(Context context) {
        super(context);
        getLogger().entering(this.getClass().getName(), "Constructor");
        this.allowedAddresses = new CopyOnWriteArraySet<>();
        this.allowedAddresses.add(LOCALHOST_IPV6);
        this.allowedAddresses.add(LOCALHOST_IPV4);
        addCustomIP(allowedAddresses);
        getLogger().exiting(this.getClass().getName(), "Constructor");
    }

    /**
     * Adds custom IPs based on the settings.
     *
     * @param allowedAddresses the new allowed addresses
     */
    private void addCustomIP(final Set<String> allowedAddresses) {
        getLogger().entering(this.getClass().getName(), "addCustomIP", allowedAddresses);
        String ips = DoiSettings.getInstance().getString(Consts.ADMIN_IP_ALLOWER);
        if (ips != null) {
            StringTokenizer tokenizer = new StringTokenizer(ips, "|");
            while (tokenizer.hasMoreTokens()) {
                String newIP = tokenizer.nextToken();
                getLogger().info(MessageFormat.format("Adds this IP {0} for allowing the access to the amdinistration application", newIP));
                allowedAddresses.add(newIP);
            }
        }
        getLogger().exiting(this.getClass().getName(), "addCustomIP");
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        int result = STOP;
        String ipClient = request.getClientInfo().getAddress();
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
