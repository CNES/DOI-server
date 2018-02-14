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
package fr.cnes.doi.security;

import fr.cnes.doi.utils.spec.Requirement;

/**
 * Utility class.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_AUTO_020,reqName = Requirement.DOI_AUTO_020_NAME)
public class UtilsHeader {
    
    /**
     * The parameter to select the role of the operator when he is implied in different groups {@value #SELECTED_ROLE_PARAMETER}.
     */
    public static final String SELECTED_ROLE_PARAMETER = "selectedRole";    
    
}
