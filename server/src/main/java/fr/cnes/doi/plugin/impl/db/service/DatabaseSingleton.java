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
import java.util.Map;

/**
 * Database singleton for accessing to the DOI database.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class DatabaseSingleton {

    /**
     * DOI db access.
     */
    private final DOIDbDataAccessService das;
    
    /**
     * Returns the instance
     *
     * @return the instance
     */
    public static DatabaseSingleton getInstance() {
        return DatabaseSingletonHolder.INSTANCE;
    }

    /**
     * Initialize the DOI singleton.
     */
    private DatabaseSingleton() {
        this.das = new DOIDbDataAccessServiceImpl();
    }

    /**
     * Initialize the DOI database.
     *
     * @param dbUrl database URL
     * @param dbUser database user
     * @param dbPwd database password
     * @param options database options
     */
    public void init(final String dbUrl, final String dbUser, final String dbPwd,
            final Map<String, Integer> options) {
        ((DOIDbDataAccessServiceImpl) this.das).init(dbUrl, dbUser, dbPwd, options);
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
