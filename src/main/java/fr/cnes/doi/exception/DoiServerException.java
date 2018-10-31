/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package fr.cnes.doi.exception;

import fr.cnes.doi.application.AbstractApplication;
import fr.cnes.doi.application.DoiMdsApplication.API_MDS;
import fr.cnes.doi.client.ClientMDS.DATACITE_API_RESPONSE;
import org.restlet.Application;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * General Server Exception
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class DoiServerException extends ResourceException {

    /**
     * Creates an exception based on the app and the error coming from Doi-server.
     *
     * @param app MDS application
     * @param error MDS error
     */
    public DoiServerException(final Application app,
            API_MDS error) {
        super(error.getStatus().getCode(), error.getShortMessage());
    }

    /**
     * Creates an exception based on the app and the error coming from Doi-server.
     *
     * @param app MDS application
     * @param error MDS error
     * @param ex Exception
     */
    public DoiServerException(final Application app,
            API_MDS error,
            Exception ex) {
        super(error.getStatus(), error.getShortMessage(), ex);
    }

    /**
     * Creates an exception based on the app and the error coming from Doi-server.
     *
     * @param app MDS application
     * @param error MDS error
     * @param description Description
     */
    public DoiServerException(final Application app,
            API_MDS error,
            final String description) {
        super(error.getStatus().getCode(), error.getShortMessage(), description);
    }

    /**
     * Creates an exception based on the app and the error coming from Doi-server.
     *
     * @param app MDS application
     * @param error MDS error
     * @param description Description
     * @param cause Exception
     */
    public DoiServerException(final Application app,
            API_MDS error,
            final String description,
            final Throwable cause) {
        super(error.getStatus().getCode(), cause, error.getShortMessage(), description);
    }

    /**
     * Creates an exception based on the app and the error coming from Doi-server.
     *
     * @param app MDS application
     * @param error Datacite error
     */
    public DoiServerException(final Application app,
            DATACITE_API_RESPONSE error) {
        super(error.getStatus().getCode(), error.getShortMessage());
        if (DATACITE_API_RESPONSE.INTERNAL_SERVER_ERROR.getStatus().getCode() == error.getStatus().
                getCode()) {
            sendAlert(app);
        }
    }

    /**
     * Creates an exception based on the app and the error coming from Doi-server.
     *
     * @param app MDS application
     * @param error Datacite error
     * @param ex Exception
     */
    public DoiServerException(final Application app,
            DATACITE_API_RESPONSE error,
            Exception ex) {
        super(error.getStatus(), error.getShortMessage(), ex);
        if (DATACITE_API_RESPONSE.INTERNAL_SERVER_ERROR.getStatus().getCode() == error.getStatus().
                getCode()) {
            sendAlert(app);
        }
    }

    /**
     * Creates an exception based on the app and the error coming from Doi-server.
     *
     * @param app MDS application
     * @param error Datacite error
     * @param description description
     */
    public DoiServerException(final Application app,
            DATACITE_API_RESPONSE error,
            final String description) {
        super(error.getStatus().getCode(), error.getShortMessage(), description);
        if (DATACITE_API_RESPONSE.INTERNAL_SERVER_ERROR.getStatus().getCode() == error.getStatus().
                getCode()) {
            sendAlert(app);
        }
    }

    /**
     * Creates an exception based on the app and the error coming from Doi-server.
     *
     * @param app MDS application
     * @param error Datacite error
     * @param description description
     * @param cause Exception
     */
    public DoiServerException(final Application app,
            DATACITE_API_RESPONSE error,
            final String description,
            final Throwable cause) {
        super(error.getStatus().getCode(), cause, error.getShortMessage(), description);
        if (DATACITE_API_RESPONSE.INTERNAL_SERVER_ERROR.getStatus().getCode() == error.getStatus().
                getCode()) {
            sendAlert(app);
        }
    }

    /**
     * Creates an exception based on the app and the error coming from Doi-server.
     *
     * @param app MDS application
     * @param status Status
     * @param description description
     * @param cause Exception
     */
    public DoiServerException(final Application app,
            final Status status,
            final String description,
            final Throwable cause) {
        super(status, description, cause);
        sendAlert(app);
    }

    /**
     * Creates an exception based on the app and the error coming from Doi-server.
     *
     * @param app MDS application
     * @param status Status
     * @param description description
     */
    public DoiServerException(final Application app,
            final Status status,
            final String description) {
        super(status, description);
    }

    /**
     * Sends the alert to the administrator.
     *
     * @param app Application
     */
    private void sendAlert(final Application app) {
        ((AbstractApplication) app).sendAlertWhenDataCiteFailed(this);
    }

}
