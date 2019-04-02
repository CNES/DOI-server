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

import fr.cnes.doi.exception.DoiRuntimeException;

/**
 * Provides validation for plugin configuration.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public interface PluginConfiguration {

    /**
     * Validates the parameters in the configuration file.
     *
     * @return the error messages.
     */
    public StringBuilder validate();
    

    /**
     * Sets the configuration.
     *
     * @param configuration configuration parameters
     */
    public void setConfiguration(final Object configuration);    

    /**
     * Checks if the plugin is already configured
     *
     * @return True when the plugin is already configured otherwise false
     */
    public boolean isConfigured();
    
    /**
     * Inits the connection
     * @throws DoiRuntimeException When an connection error happens
     */
    public void initConnection() throws DoiRuntimeException;

    /**
     * Release the plugin.
     */
    public void release();

}
