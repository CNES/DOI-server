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
package fr.cnes.doi.plugin.impl.db.service;

import fr.cnes.doi.plugin.impl.db.impl.DOIDbDataAccessServiceImpl;

/**
 * Database singleton for accessing to the LDAP and DOI database.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class DatabaseSingleton {

    /**
     * Returns the instance
     *
     * @return the instance
     */
    public static DatabaseSingleton getInstance() {
        return DatabaseSingletonHolder.INSTANCE;
    }

    /**
     * DOI db access.
     */
    private final DOIDbDataAccessService das;

    /**
     * Initialize the LDAP and DOI database.
     */
    private DatabaseSingleton() {
        this.das = new DOIDbDataAccessServiceImpl();
    }

    /**
     * Returns the database access.
     *
     * @return the database access.
     */
    public DOIDbDataAccessService getDatabaseAccess() {
        return this.das;
    }

    /**
     * Holder
     */
    private static class DatabaseSingletonHolder {

        /**
         * Database singleton Unique Instance not pre-initiliaze
         */
        private static final DatabaseSingleton INSTANCE = new DatabaseSingleton();
    }
}
