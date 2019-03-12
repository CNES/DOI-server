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
package fr.cnes.doi.ldap.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.exception.LDAPAccessException;
import fr.cnes.doi.ldap.impl.LdapDoidbIntegrationImpl;
import fr.cnes.doi.ldap.service.LdapDoidbIntegration;
import fr.cnes.doi.exception.DOIDbException;

/**
 * Updates the DOI database from the LDAP.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class DOIUsersUpdate implements Runnable {

    /**
     * ldap and doidb integration service.
     */
    private final LdapDoidbIntegration service = new LdapDoidbIntegrationImpl();

    /**
     * logger.
     */
    private final Logger LOG = LogManager.getLogger(DOIUsersUpdate.class.getName());

    /**
     * {@inheritDoc }
     */
    @Override
    public void run() {
        LOG.info("executing task that updates database from ldap !");
        try {
            service.updateDoiServerDataBaseFromLdap();
        } catch (LDAPAccessException | DOIDbException e) {
            LOG.error("error occured when calling DOIUsersUpdate job", e);
        }

    }
}
