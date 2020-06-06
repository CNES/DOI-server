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
package fr.cnes.doi.db;

import fr.cnes.doi.exception.DOIDbException;
import java.util.List;

import fr.cnes.doi.utils.spec.Requirement;

/**
 * Interface for handling the token database.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_INTER_040, reqName = Requirement.DOI_INTER_040_NAME)
public abstract class AbstractTokenDBHelper {

    /**
     * Adds a token in the database
     *
     * @param jwt a token
     * @return True when the token is added to the database otherwise False
     */
    public abstract boolean addToken(String jwt);

    /**
     * Deletes a token from the database.
     *
     * @param jwt the token
     * @return True when the token is deletes otherwise false
     */
    public abstract boolean deleteToken(String jwt);

    /**
     * Tests if the token exists in the database.
     *
     * @param jwt the token
     * @return True when the token exists in the database otherwise False
     */
    public abstract boolean isExist(String jwt);

    /**
     * Return the token list from database.
     *
     * @return the list of tokens
     * @throws fr.cnes.doi.exception.DOIDbException when an error occurs
     */
    public abstract List<String> getTokens() throws DOIDbException;
}
