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
 * DOI user model
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DOIUser {

    /**
     * User name.
     */
    private String username;

    /**
     * Indicates if the user is admin.
     */
    private Boolean admin;

    /**
     * Email of the user name.
     */
    private String email;

    /**
     * Returns the user name.
     *
     * @return the user name
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user name.
     *
     * @param username user name.
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Returns True when the user is administrator otherwise False.
     *
     * @return True when the user is administrator otherwise False
     */
    public Boolean isAdmin() {
        return admin;
    }

    /**
     * Sets an administrator as admin.
     *
     * @param isAdmin True when the user is administrator otherwise False
     */
    public void setAdmin(final Boolean isAdmin) {
        this.admin = isAdmin;
    }

    /**
     * Sets the email of the user.
     *
     * @param email email
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Returns the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Tests if this object is equal to testuser.
     *
     * @param testuser DOIUser to test
     * @return True when testuser is the same à this DOIUser
     */
    public Boolean isEqualTo(final DOIUser testuser) {
        return this.username.equals(testuser.getUsername())
                && this.admin.equals(testuser.isAdmin())
                && this.email.equals(testuser.getEmail());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getUsername() + "_" + this.getEmail();
    }
}
