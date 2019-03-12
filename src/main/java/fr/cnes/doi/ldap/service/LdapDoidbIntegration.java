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
package fr.cnes.doi.ldap.service;

import fr.cnes.doi.exception.LDAPAccessException;
import fr.cnes.doi.exception.DOIDbException;

/**
 * Integration of the LDAP to fill the DOI users database
 * @author Jean-Christophe Malapert (Jean-Christophe.malapert@cnes.fr)
 */
public interface LdapDoidbIntegration {

    /**
     * Updates the users in DOI database from the LDAP.
     * @throws LDAPAccessException - if a problem occurs
     * @throws DOIDbException - if a problem occurs
     */
    public void updateDoiServerDataBaseFromLdap() throws LDAPAccessException, DOIDbException;

}
