/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.AbstractApplication;
import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.Requirement;

import java.util.Arrays;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * DOI resource to retrieve the landing page for a given DOI.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DoiResource extends BaseMdsResource {

    /**
     *
     */
    public static final String GET_DOI = "Get DOI";

    /**
     * DOI template.
     */
    private String doiName;

    /**
     * Init by getting the DOI name in the {@link DoiMdsApplication#DOI_TEMPLATE template URL}.
     *
     * @throws ResourceException - if a problem happens
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        LOG.traceEntry();
        setDescription("The resource can retrieve a DOI");
        this.doiName = getResourcePath().replace(DoiMdsApplication.DOI_URI + "/", "");
        //this.doiName = getAttribute(DoiMdsApplication.DOI_TEMPLATE);
        LOG.debug(this.doiName);
        LOG.traceExit();
    }

    /**
     * Returns the URL associated to a given DOI or no content. When the status is 200, the URL
     * associated to a DOI is returnes. When the status is 204, the DOI is known to MDS, but is not
     * minted (or not resolvable e.g. due to handle's latency)
     *
     * @return an URL or no content (DOI is known to MDS, but is not minted (or not resolvable e.g.
     * due to handle's latency))
     * @throws ResourceException - if an error happens <ul>
     * <li>400 Bad Request if the DOI does not contain the institution suffix</li>
     * <li>404 Not Found - DOI does not exist in DataCite</li>
     * <li>500 Internal Server Error - server internal error, try later and if problem persists
     * please contact us</li>
     * </ul>
     */
    @Requirement(reqId = Requirement.DOI_SRV_070, reqName = Requirement.DOI_SRV_070_NAME)
    @Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    @Get
    public Representation getDoi() throws ResourceException {
        LOG.traceEntry();
        final Representation result;
        checkInput(this.doiName);
        try {
            final String doi = this.getDoiApp().getClient().getDoi(this.doiName);
            if (doi != null && !doi.isEmpty()) {
                setStatus(Status.SUCCESS_OK);
            } else {
                setStatus(Status.SUCCESS_NO_CONTENT);
            }
            result = new StringRepresentation(doi, MediaType.TEXT_PLAIN);
        } catch (ClientMdsException ex) {
            if (ex.getStatus().getCode() == Status.CLIENT_ERROR_NOT_FOUND.getCode()) {
                throw LOG.throwing(new ResourceException(ex.getStatus(), ex.getMessage(), ex));
            } else {
                ((AbstractApplication) getApplication()).sendAlertWhenDataCiteFailed(ex);
                throw LOG.throwing(new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage(), ex));
            }
        }
        return LOG.traceExit(result);
    }

    /**
     * Checks if doiName is not empty and contains the institution's prefix
     *
     * @param doiName DOI name
     * @throws ResourceException 400 Bad Request if the DOI does not contain the institution suffix.
     */
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    private void checkInput(final String doiName) throws ResourceException {
        LOG.traceEntry("Parameter : {}", doiName);
        if (doiName == null || doiName.isEmpty()) {
            throw LOG.throwing(new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "doiName cannot be null or empty"
            ));
        } else if (!doiName.startsWith(DoiSettings.getInstance().getString(Consts.INIST_DOI))) {
            throw LOG.throwing(new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "the DOI"
                    + " prefix must contains the prefix of the institution"));
        } else {
            try {
                ClientMDS.checkIfAllCharsAreValid(doiName);
            } catch (IllegalArgumentException ex) {
                throw LOG.throwing(new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex));
            }
        }
        LOG.traceExit();
    }

    /**
     * DOI representation
     *
     * @return Wadl representation for a DOI
     */
    @Requirement(reqId = Requirement.DOI_DOC_010,reqName = Requirement.DOI_DOC_010_NAME)
    private RepresentationInfo doiRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("DOI Representation");
        docInfo.setTextContent("This request returns an URL associated with a given DOI.");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    /**
     * Describes the Get Method.
     *
     * @param info Wadl description
     */
    @Requirement(reqId = Requirement.DOI_DOC_010,reqName = Requirement.DOI_DOC_010_NAME)
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get the landing page related to a given DOI");
        addRequestDocToMethod(info, Arrays.asList(
                createQueryParamDoc(
                        DoiMdsApplication.DOI_TEMPLATE,
                        ParameterStyle.TEMPLATE, "DOI name", true, "xs:string"
                )
        ));

        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_OK, "Operation successful", doiRepresentation())
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SUCCESS_NO_CONTENT,
                "DOI is known to MDS, but is not minted (or not resolvable e.g. "
                + "due to handle's latency)", "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_BAD_REQUEST, "if the DOI does not contain "
                + "the institution suffix", "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.CLIENT_ERROR_NOT_FOUND, "DOI does not exist in DataCite",
                "explainRepresentation")
        );
        addResponseDocToMethod(info, createResponseDoc(
                Status.SERVER_ERROR_INTERNAL, "server internal error, try later "
                + "and if problem persists please contact us",
                "explainRepresentation")
        );
    }

}
