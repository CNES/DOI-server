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
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.application.DoiMdsApplication;
import fr.cnes.doi.application.DoiMdsApplication.API_MDS;
import fr.cnes.doi.client.ClientMDS;
import fr.cnes.doi.client.ClientMDS.DATACITE_API_RESPONSE;
import fr.cnes.doi.exception.ClientMdsException;
import fr.cnes.doi.exception.DoiServerException;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.Arrays;
import org.apache.logging.log4j.Level;
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

/**
 * DOI resource to retrieve the landing page for a given DOI.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DoiResource extends BaseMdsResource {

    /**
     * Function of this resource {@value #GET_DOI}.
     */
    public static final String GET_DOI = "Get DOI";

    /**
     * DOI template.
     */
    private volatile String doiName;

    /**
     * Init by getting the DOI name in the
     * {@link DoiMdsApplication#DOI_TEMPLATE template URL}.
     *
     * @throws DoiServerException - if a problem happens
     */
    @Override
    protected void doInit() throws DoiServerException {
        super.doInit();
        LOG.traceEntry();
        setDescription("The resource can retrieve a DOI");
        this.doiName = getAttributePath(DoiMdsApplication.DOI_URI);
        LOG.debug(this.doiName);
        LOG.traceExit();
    }

    /**
     * Returns the URL associated to a given DOI or no content. When the status
     * is 200, the URL associated to a DOI is returnes. When the status is 204,
     * the DOI is known to MDS, but is not minted (or not resolvable e.g. due to
     * handle's latency)
     *
     * @return an URL or no content (DOI is known to MDS, but is not minted (or
     * not resolvable e.g. due to handle's latency))
     * @throws DoiServerException - if the response is not a success
     * <ul>
     * <li>{@link DATACITE_API_RESPONSE#DOI_NOT_FOUND}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * <li>{@link API_MDS#DOI_VALIDATION}</li>
     * </ul>
     */
    @Requirement(reqId = Requirement.DOI_SRV_070, reqName = Requirement.DOI_SRV_070_NAME)
    @Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    @Get
    public Representation getDoi() throws DoiServerException {
        LOG.traceEntry();
        final Representation result;
        checkInput(this.doiName);
        try {
            final String doi = this.getDoiApp().getClient().getDoi(this.doiName);
            if (doi == null || doi.isEmpty()) {
                setStatus(Status.SUCCESS_NO_CONTENT);
            } else {
                setStatus(Status.SUCCESS_OK);
            }
            result = new StringRepresentation(doi, MediaType.TEXT_PLAIN);
        } catch (ClientMdsException ex) {
            if (ex.getStatus().getCode() == Status.CLIENT_ERROR_NOT_FOUND.getCode()) {
                throw LOG.throwing(
                        Level.ERROR,
                        new DoiServerException(getApplication(), DATACITE_API_RESPONSE.DOI_NOT_FOUND,
                                ex)
                );
            } else {
                throw LOG.throwing(
                        Level.ERROR,
                        new DoiServerException(getApplication(), API_MDS.DATACITE_PROBLEM, ex)
                );
            }
        }
        return LOG.traceExit(result);
    }

    /**
     * Checks if doiName is not empty and contains the institution's prefix
     *
     * @param doiName DOI name
     * @throws DoiServerException 400 Bad Request if the DOI does not contain
     * the institution suffix.
     */
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    private void checkInput(final String doiName) throws DoiServerException {
        LOG.traceEntry("Parameter : {}", doiName);
        if (doiName == null || doiName.isEmpty()) {
            throw LOG.throwing(
                    Level.ERROR,
                    new DoiServerException(getApplication(), API_MDS.DOI_VALIDATION,
                            "DoiName must be set.")
            );
        } else if (doiName.startsWith(DoiSettings.getInstance().getString(Consts.INIST_DOI))) {
            try {
                ClientMDS.checkIfAllCharsAreValid(doiName);
            } catch (IllegalArgumentException ex) {
                throw LOG.throwing(
                        Level.ERROR,
                        new DoiServerException(getApplication(), API_MDS.DOI_VALIDATION, ex)
                );
            }
        } else {
            throw LOG.throwing(
                    Level.ERROR,
                    new DoiServerException(getApplication(), API_MDS.DOI_VALIDATION, "the DOI"
                            + " prefix must contains the prefix of the institution")
            );
        }
        LOG.traceExit();
    }

    /**
     * DOI representation
     *
     * @return Wadl representation for a DOI
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
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
     * Describes the Get Method. The different representations are the
     * followings:
     * <ul>
     * <li>{@link DATACITE_API_RESPONSE#SUCCESS}</li>
     * <li>{@link DATACITE_API_RESPONSE#SUCCESS_NO_CONTENT}</li>
     * <li>{@link API_MDS#DOI_VALIDATION}</li>
     * <li>{@link DATACITE_API_RESPONSE#DOI_NOT_FOUND}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * </ul>
     *
     * @param info Wadl description
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
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
                DATACITE_API_RESPONSE.SUCCESS.getStatus(),
                DATACITE_API_RESPONSE.SUCCESS.getShortMessage(),
                doiRepresentation())
        );
        addResponseDocToMethod(info, createResponseDoc(
                DATACITE_API_RESPONSE.SUCCESS_NO_CONTENT.getStatus(),
                DATACITE_API_RESPONSE.SUCCESS_NO_CONTENT.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.DOI_VALIDATION.getStatus(),
                API_MDS.DOI_VALIDATION.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                DATACITE_API_RESPONSE.DOI_NOT_FOUND.getStatus(),
                DATACITE_API_RESPONSE.DOI_NOT_FOUND.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.DATACITE_PROBLEM.getStatus(),
                API_MDS.DATACITE_PROBLEM.getShortMessage(),
                "explainRepresentationID")
        );
    }

}
