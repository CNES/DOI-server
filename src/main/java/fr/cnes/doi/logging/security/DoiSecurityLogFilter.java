/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.logging.security;

import fr.cnes.doi.utils.Utils;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.routing.Filter;
import org.restlet.security.Role;

/**
 *
 * @author malapert
 */
@Requirement(reqId = Requirement.DOI_ARCHI_020, reqName = Requirement.DOI_ARCHI_020_NAME)
public class DoiSecurityLogFilter extends Filter {

    /**
     * The name of the logger to use.
     */
    private final String loggerName;

    /**
     * Instantiates a new sitools log filter.
     *
     * @param loggerName the name of the logger to use
     */
    public DoiSecurityLogFilter(final String loggerName) {
        super();
        this.loggerName = loggerName;
    }

    /**
     * Allows filtering after processing by the next Restlet. Does nothing by default.
     *
     * @param request request
     * @param response response
     * @see org.restlet.routing.Filter#afterHandle(org.restlet.Request, org.restlet.Response)
     */
    @Override
    protected void afterHandle(final Request request,
            final Response response) {
        super.afterHandle(request, response);

        final String targetUri = request.getResourceRef().getIdentifier();
        final String method = request.getMethod().getName();
        final ClientInfo clientInfo = request.getClientInfo();
        final String upStreamIp = clientInfo.getUpstreamAddress();
        if (request.getClientInfo().isAuthenticated()) {
            final String authenticationMethod = request.getChallengeResponse().getScheme().
                    getTechnicalName();
            final String identifier = request.getChallengeResponse().getIdentifier();
            final String profiles = computeProfiles(clientInfo);
            LogManager.getLogger(Utils.SECURITY_LOGGER_NAME).info(
                    "User: " + identifier + "\tProfile(s): " + profiles + "\t - "
                    + "[" + upStreamIp + "] - [" + authenticationMethod + "] "
                    + method + " " + targetUri + " " + response.
                            getStatus().getCode() + " - " + clientInfo.getAgent());
        }
    }

    /**
     * Represents the list of roles as a single string.
     *
     * @param clientInfo Client information
     * @return the roles
     */
    private String computeProfiles(final ClientInfo clientInfo) {
        final List<Role> roles = clientInfo.getRoles();
        final Collection<String> rolesStr = new ArrayList<>();
        for (final Role role : roles) {
            rolesStr.add(role.getName());
        }
        return String.join(",", rolesStr);
    }
}
