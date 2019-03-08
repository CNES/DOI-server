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
import static fr.cnes.doi.security.UtilsHeader.SELECTED_ROLE_PARAMETER;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.Arrays;
import org.apache.logging.log4j.Level;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * Resource to handle the Media.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class MediaResource extends BaseMdsResource {

    /**
     * DOI parsed from the URL.
     */
    private volatile String mediaName;

    /**
     * Init by getting the media name.
     *
     * @throws DoiServerException - if a problem happens
     */
    @Override
    protected void doInit() throws DoiServerException {
        super.doInit();
        LOG.traceEntry();
        this.mediaName = getResourcePath().replace(DoiMdsApplication.MEDIA_URI + "/", "");
        LOG.debug(this.mediaName);
        LOG.traceExit();
    }

    /**
     * Returns the media related to a DOI. This request returns list of pairs of media type and URLs
     * associated with a given DOI when 200 status is returned (operation successful).
     *
     * @return the media related to a DOI
     * @throws DoiServerException - if the response is not a success
     * <ul>
     * <li>{@link DATACITE_API_RESPONSE#DOI_NOT_FOUND}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * </ul>
     */
    @Requirement(reqId = Requirement.DOI_SRV_090, reqName = Requirement.DOI_SRV_090_NAME)
    @Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
    @Get
    public Representation getMedias() throws DoiServerException {
        LOG.traceEntry();
        final Representation rep;
        final String medias;
        try {
            setStatus(Status.SUCCESS_OK);
            medias = this.getDoiApp().getClient().getMedia(this.mediaName);
            rep = new StringRepresentation(medias, MediaType.TEXT_URI_LIST);
        } catch (ClientMdsException ex) {
            if (ex.getStatus().getCode() == Status.CLIENT_ERROR_NOT_FOUND.getCode()) {
                throw LOG.throwing(
                        Level.DEBUG,
                        new DoiServerException(getApplication(), DATACITE_API_RESPONSE.DOI_NOT_FOUND,
                                ex)
                );
            } else {
                throw LOG.throwing(
                        Level.DEBUG,
                        new DoiServerException(getApplication(), API_MDS.DATACITE_PROBLEM, ex)
                );
            }
        }

        return LOG.traceExit(rep);
    }

    /**
     * Creates a media related to an URL for a given DOI. Will add/update media type/urls pairs to a
     * DOI. Standard domain restrictions check will be performed. 200 status is returned when the
     * operation is successful.
     *
     * @param mediaForm Form
     * @return short explanation of status code
     * @throws DoiServerException - if the response is not a success :
     * <ul>
     * <li>{@link DATACITE_API_RESPONSE#BAD_REQUEST}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
     * <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li>
     * </ul>
     */
    @Requirement(reqId = Requirement.DOI_SRV_080, reqName = Requirement.DOI_SRV_080_NAME)
    @Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    @Requirement(reqId = Requirement.DOI_AUTO_020, reqName = Requirement.DOI_AUTO_020_NAME)
    @Requirement(reqId = Requirement.DOI_AUTO_030, reqName = Requirement.DOI_AUTO_030_NAME)
    @Post
    public Representation createMedia(final Form mediaForm) throws DoiServerException {
        LOG.traceEntry("Parameter : {}", mediaForm);
        checkInputs(this.mediaName, mediaForm);
        final String result;
        try {
            setStatus(Status.SUCCESS_OK);
            final String selectedRole = extractSelectedRoleFromRequestIfExists();
            checkPermission(this.mediaName, selectedRole);
            result = this.getDoiApp().getClient().createMedia(this.mediaName, mediaForm);
        } catch (ClientMdsException ex) {
            if (ex.getStatus().getCode() == Status.CLIENT_ERROR_BAD_REQUEST.getCode()) {
                throw LOG.throwing(
                        Level.DEBUG,
                        new DoiServerException(getApplication(), DATACITE_API_RESPONSE.BAD_REQUEST,
                                ex)
                );
            } else {
                throw LOG.throwing(
                        Level.DEBUG,
                        new DoiServerException(getApplication(), API_MDS.DATACITE_PROBLEM, ex)
                );
            }
        }
        return LOG.traceExit(new StringRepresentation(result));
    }

    /**
     * Checks input parameters
     *
     * @param doi DOI number
     * @param mediaForm the parameters
     * @throws DoiServerException - 400 Bad Request if DOI_PARAMETER is not set
     */
    @Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
    private void checkInputs(final String doi,
            final Form mediaForm) throws DoiServerException {
        LOG.traceEntry("Parameters : {} and {}", doi, mediaForm);
        final StringBuilder errorMsg = new StringBuilder();
        if (doi == null || doi.isEmpty() || !doi.startsWith(DoiSettings.getInstance().getString(
                Consts.INIST_DOI))) {
            errorMsg.append(DOI_PARAMETER).append(" value is not set.");
        } else {
            try {
                ClientMDS.checkIfAllCharsAreValid(doi);
            } catch (IllegalArgumentException ex) {
                errorMsg.append(DOI_PARAMETER).append(" no valid syntax.");
            }
        }
        if (errorMsg.length() == 0) {
            LOG.debug("The form is valid");
        } else {
            throw LOG.throwing(
                    Level.DEBUG,
                    new DoiServerException(getApplication(), API_MDS.MEDIA_VALIDATION, errorMsg.
                            toString())
            );
        }
        LOG.traceExit();
    }

    /**
     * Media representation.
     *
     * @return Wadl description for a Media representation
     */
    private RepresentationInfo mediaRepresentation() {
        final RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setMediaType(MediaType.TEXT_PLAIN);
        final DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Media representation");
        docInfo.setTextContent("This request returns a key-value list of media "
                + "types/urls for a given DOI name");
        repInfo.setDocumentation(docInfo);
        return repInfo;
    }

    /**
     * Describes the GET method. The different representations are the followings:
     * <ul>
     * <li>{@link DATACITE_API_RESPONSE#SUCCESS}</li>
     * <li>{@link DATACITE_API_RESPONSE#DOI_NOT_FOUND}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * </ul>
     *
     * @param info Wadl description for a GET method
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    @Override
    protected final void describeGet(final MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get a specific media for a given DOI");

        addRequestDocToMethod(info, createQueryParamDoc(
                DoiMdsApplication.DOI_TEMPLATE, ParameterStyle.TEMPLATE,
                "DOI name", true, "xs:string")
        );
        addResponseDocToMethod(info, createResponseDoc(
                DATACITE_API_RESPONSE.SUCCESS.getStatus(),
                DATACITE_API_RESPONSE.SUCCESS.getShortMessage(),
                mediaRepresentation())
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

    /**
     * Describes the POST method. The different representations are the followings:
     * <ul>
     * <li>{@link DATACITE_API_RESPONSE#SUCCESS}</li>
     * <li>{@link API_MDS#MEDIA_VALIDATION}</li>
     * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
     * <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
     * <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
     * <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li>
     * </ul>
     *
     * @param info Wadl description for describing POST method
     */
    @Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
    @Override
    protected final void describePost(final MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation(
                "POST will add/update media type/urls pairs to a DOI. Standard domain restrictions check will be performed.");
        final ParameterInfo param = new ParameterInfo();
        param.setName("{mediaType}");
        param.setStyle(ParameterStyle.PLAIN);
        param.setRequired(false);
        param.setType("xs:string");
        param.setFixed("{url}");
        param.setRepeating(true);
        param.setDocumentation("(key/value) = (mediaType/url)");
        final RepresentationInfo rep = new RepresentationInfo(MediaType.APPLICATION_WWW_FORM);
        rep.getParameters().add(param);

        addRequestDocToMethod(info,
                Arrays.asList(createQueryParamDoc(SELECTED_ROLE_PARAMETER, ParameterStyle.HEADER,
                        "A user can select one role when he is associated to several roles", false,
                        "xs:string")),
                rep);
        addResponseDocToMethod(info, createResponseDoc(
                DATACITE_API_RESPONSE.SUCCESS.getStatus(),
                DATACITE_API_RESPONSE.SUCCESS.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.MEDIA_VALIDATION.getStatus(),
                API_MDS.MEDIA_VALIDATION.getShortMessage(),
                "explainRepresentationID")
        );
        addResponseDocToMethod(info, createResponseDoc(
                API_MDS.DATACITE_PROBLEM.getStatus(),
                API_MDS.DATACITE_PROBLEM.getShortMessage(),
                "explainRepresentationID")
        );
        super.describePost(info);
    }
}
