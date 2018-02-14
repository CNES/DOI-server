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
 * Provides classes to create the server and start it.
 * 
 * <p>
 * The architecture of the DOI server is composed of the following elements:
 * 
 * <ul>
 * <li>A HTTP server</li>
 * <li>A HTTPS server</li>
 * <li>Some singletons classes for accessing to the DOI configuration file</li>
 * <li>{@link fr.cnes.doi.application.AdminApplication Administration application}</li>
 * <li>{@link fr.cnes.doi.application.DoiCrossCiteApplication CrossCite application}</li>
 * <li>{@link fr.cnes.doi.application.DoiMdsApplication DOI MDS application}</li>
 * </ul>
 * 
 * <img src="{@docRoot}/doc-files/server.png" alt="Server overview">
 * 
 * A user can basically query DataCite for getting, creating, updating, deleting and searching a DOI.
 * For this, he should use the web services of each application or the web page available in the
 * administration application.
 * 
 * According to the web service, the authentication/authorization system must be different. The different
 * system could be one of the following or all of them in the same time with some of them optional :
 * <ul>
 * <li>An authentication based on Token</li>
 * <li>An authentication based on simple login/password</li>
 * <li>An IP filtering</li>
 * <li>An authorization by role</li>
 * </ul>
 */
package fr.cnes.doi.server;
