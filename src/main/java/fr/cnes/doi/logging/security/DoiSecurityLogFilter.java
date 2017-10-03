/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging.security;

import fr.cnes.doi.utils.spec.CoverageAnnotation;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.LogRecord;
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
@Requirement(
        reqId = Requirement.DOI_ARCHI_020,
        reqName = Requirement.DOI_ARCHI_020_NAME
)
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
     * @param request request
     * @param response response
     * @see org.restlet.routing.Filter#afterHandle(org.restlet.Request, org.restlet.Response)
     */
    @Override
    protected void afterHandle(final Request request, final Response response) {
        super.afterHandle(request, response);
        final Object logRecordObj = response.getAttributes().get("LOG_RECORD");
        if (logRecordObj != null) {

            final ClientInfo clientInfo = request.getClientInfo();
            String user = null;
            StringBuffer profile = new StringBuffer();
            if (clientInfo != null && clientInfo.getUser() != null) {
                user = clientInfo.getUser().getIdentifier();                
                final List<Role> roles = clientInfo.getRoles();
                final Set<String> rolesStr = new HashSet<>();
                for (final Role role : roles) {
                    rolesStr.add(role.getName());
                }
                //profile += Joiner.on(",").join(rolesStr);
                profile = profile.append(",").append(rolesStr);
            }

            final LogRecord logRecord = (LogRecord) logRecordObj;
            logRecord.setMessage("User: " + user + "\tProfile: " + profile + "\t" + logRecord.getMessage());            
            LogManager.getLogger(loggerName).info(logRecord);
            //final Logger logger = Engine.getLogger(loggerName);
            //logger.log(logRecord);

        }
    }
}
