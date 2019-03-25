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
package fr.cnes.doi.plugin;

import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Plugin to define users and groups of the system.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_ARCHI_030, reqName = Requirement.DOI_ARCHI_030_NAME)
public abstract class AbstractUserRolePluginHelper
        extends AbstractUserRoleDBHelper implements PluginMetadata, PluginConfiguration {

    /**
     * Empty constructor.
     */
    protected AbstractUserRolePluginHelper() {
        super();
    }

}
