/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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
 * Provides resources for cross cite application.
 * 
 * This package contains :
 * <ul>
 * <li>The {@link fr.cnes.doi.resource.citation.LanguageCitationResource resource} 
 * to get the list of possible languages</li>
 * <li>The {@link fr.cnes.doi.resource.citation.StyleCitationResource resource} 
 * to get the list of possible styles</li>
 * <li>The {@link fr.cnes.doi.resource.citation.FormatCitationResource resource} 
 * to get the citation</li>
 * </ul>
 * 
 * The Citation Formatter takes the metadata description of a DOI and uses the 
 * information to build a citation following more than 5,000 citation styles 
 * made available by the <a href="http://citationstyles.org/">citationstyles.org</a> project.
 */
package fr.cnes.doi.resource.citation;
