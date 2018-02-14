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
package fr.cnes.doi.security;

import fr.cnes.doi.logging.business.JsonMessage;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * IP filtering
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class AllowerIP extends org.restlet.routing.Filter {
    
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(AllowerIP.class.getName());

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
        LOG.traceEntry();
        this.allowedAddresses = new CopyOnWriteArraySet<>();
        this.allowedAddresses.add(LOCALHOST_IPV6);
        this.allowedAddresses.add(LOCALHOST_IPV4);
        addCustomIP(allowedAddresses);
        LOG.traceExit();
    }

    /**
     * Adds custom IPs based on the settings.
     *
     * @param allowedAddresses the new allowed addresses
     */
    private void addCustomIP(final Set<String> allowedAddresses) {
        LOG.traceEntry("Parameter : {}",allowedAddresses);
        final String ips = DoiSettings.getInstance().getString(Consts.ADMIN_IP_ALLOWER);
        if (ips != null) {
            final StringTokenizer tokenizer = new StringTokenizer(ips, "|");
            while (tokenizer.hasMoreTokens()) {
                final String newIP = tokenizer.nextToken();
                LOG.info("Adds this IP {} for allowing the access "
                        + "to the amdinistration application", newIP);
                allowedAddresses.add(newIP);
            }
        }
        LOG.traceExit();
    }

    /**
     * Before Handler.
     * @param request request
     * @param response response
     * @return beforeHandler 
     */
    @Override
    protected int beforeHandle(final Request request, final Response response) {
        LOG.traceEntry(new JsonMessage(request));
        int result = STOP;
        final String ipClient = request.getClientInfo().getAddress();
        if (getAllowedAddresses().contains(ipClient)) {
            result = CONTINUE;
        } else {
            LOG.info("You IP address {} was blocked", ipClient);
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Your IP address was blocked");
        }
        return LOG.traceExit(result);
    }

    /**
     * Returns the allowed addresses.
     *
     * @return the allowed addresses
     */
    public Set<String> getAllowedAddresses() {
        LOG.traceEntry();
        return LOG.traceExit(allowedAddresses);
    }

}
