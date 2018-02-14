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
package fr.cnes.doi.services;


/**
 * Monitoring record containing:
 * <ul>
 * <li>Description of the record</li>
 * <li>Average</li>
 * <li>Nb total of recorded values (to compute the average)</li>
 * </ul>
 *
 * @author Claire
 *
 */
public class DoiMonitoringRecord {

    /**
     * Description (name) of the service to record *
     */
    private String description;

    /**
     * Current average time to access this service *
     */
    private float average = 0.0f;

    /**
     * Nb of values to compute the average *
     */
    private int nbAccess = 0;

    /**
     * Constructor.
     *
     * @param description Service description
     * @param average Average speed of the request
     * @param nbAccess Number of access
     */
    public DoiMonitoringRecord(final String description, final float average, final int nbAccess) {
        super();
        this.description = description;
        this.average = average;
        this.nbAccess = nbAccess;
    }

    /**
     * Returns the service description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the service description.
     *
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the average speed of the request.
     *
     * @return the average
     */
    public float getAverage() {
        return average;
    }

    /**
     * Sets the average speed of the request.
     *
     * @param average the average to set
     */
    public void setAverage(final float average) {
        this.average = average;
    }

    /**
     * Returns the number of access.
     *
     * @return the nbAccess
     */
    public int getNbAccess() {
        return nbAccess;
    }

    /**
     * Sets the number of access.
     *
     * @param nbAccess the nbAccess to set
     */
    public void setNbAccess(final int nbAccess) {
        this.nbAccess = nbAccess;
    }

}
