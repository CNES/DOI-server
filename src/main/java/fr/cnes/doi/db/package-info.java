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
/**
 * Provides interfaces to request external databases.
 *
 * <p>
 * Three databases are needed :
 * <ul>
 * <li>One to access to the 
 * {@link fr.cnes.doi.db.AbstractProjectSuffixDBHelper project database}</li>
 * <li>Another one to access to the 
 * {@link fr.cnes.doi.db.AbstractTokenDBHelper token database}</li>
 * <li>The last one to access to the 
 * {@link fr.cnes.doi.db.AbstractUserRoleDBHelper user/role database}</li>
 * </ul>
 * 
 * <h2>Project database</h2>
 * The project database stores the project name and its identifier. The identifier
 * is used in the DOI name to assure the unique name through the organization.
 * The DOI is built like this:<br>
 * 
 * doi://<i>prefix organization assigned by DataCite</i>/<i>suffix project assign 
 * by DOI server to a project</i>/<i>free identifier by the project</i>
 * 
 * 
 * <h2>Token database</h2>
 * The token database stores the created token in order to verify them when there 
 * are used by the user.
 * 
 * <h2>User/role database</h2>
 * The user/role database stores the users and the association role/user
 */
package fr.cnes.doi.db;
