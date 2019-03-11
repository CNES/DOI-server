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
package fr.cnes.doi.db.model;

/**
 * Data model for a DOI project.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DOIProject {

    /**
     * Suffix of the project name.
     */
    private int suffix;

    /**
     * project name.
     */
    private String projectname;

    /**
     * Returns the suffix.
     * @return the suffix
     */
    public int getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix.
     * @param suffix 
     */
    public void setSuffix(final int suffix) {
        this.suffix = suffix;
    }

    /**
     * Returns the project name.
     * @return the project name
     */
    public String getProjectname() {
        return projectname;
    }

    /**
     * Sets the project name.
     * @param projectname  the project name
     */
    public void setProjectname(final String projectname) {
        this.projectname = projectname;
    }

    /**
     * Tests a doiProject is equal to this one. 
     * @param doiProject doi project
     * @return True when there are equals otherwise False
     */
    public Boolean isEqualTo(final DOIProject doiProject) {
        return (this.suffix == doiProject.getSuffix())
                && this.projectname.equals(doiProject.getProjectname());
    }

}
