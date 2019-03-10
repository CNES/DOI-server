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

import fr.cnes.doi.db.AbstractProjectSuffixDBHelper;
import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(reqId = Requirement.DOI_ARCHI_030, reqName = Requirement.DOI_ARCHI_030_NAME)
public final class PluginFactory {
    
    /**
     * Map the name of an interface to the name of a corresponding concrete
     * implementation class.
     */
    private static final Map<String, String> PLUGINS_IMPL = new ConcurrentHashMap<>();

    /**
     * "Static" class cannot be instantiated
     */
    private PluginFactory() {
    }

    /**
     * Loads the path of plugins from Settings.
     *
     * @param settings
     *            config settings
     */
    public static void init(final Map<String, String> settings) {
	final String userRealPlugin = settings.getOrDefault(Consts.PLUGIN_USER_GROUP_MGT, "");
	final String projectSuffixPlugin = settings.getOrDefault(Consts.PLUGIN_PROJECT_SUFFIX, "");
	final String tokenPlugin = settings.getOrDefault(Consts.PLUGIN_TOKEN, "");
	final String doiPlugin = settings.getOrDefault(Consts.PLUGIN_DOI, "");
	PLUGINS_IMPL.put(Consts.PLUGIN_USER_GROUP_MGT, userRealPlugin);
	PLUGINS_IMPL.put(Consts.PLUGIN_PROJECT_SUFFIX, projectSuffixPlugin);
	PLUGINS_IMPL.put(Consts.PLUGIN_TOKEN, tokenPlugin);
	PLUGINS_IMPL.put(Consts.PLUGIN_DOI, doiPlugin);
    }

    /**
     * Returns the concrete implementation of the user management interface.
     *
     * @return the plugin
     */
    public static AbstractUserRoleDBHelper getUserManagement() {
	final String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_USER_GROUP_MGT);
	return (AbstractUserRoleDBHelper) buildObject(implClassName);
    }

    /**
     * Returns the concrete implementation of the suffix project db.
     *
     * @return the plugin
     */
    public static AbstractProjectSuffixDBHelper getProjectSuffix() {
	final String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_PROJECT_SUFFIX);
	return (AbstractProjectSuffixDBHelper) buildObject(implClassName);
    }

    /**
     * Returns the concrete implementation of the token db.
     *
     * @return the plugin
     */
    public static AbstractTokenDBPluginHelper getToken() {
	final String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_TOKEN);
	return (AbstractTokenDBPluginHelper) buildObject(implClassName);
    }

    /**
     * instantiates the plugin.
     *
     * @param aClassName
     *            the plugin name
     * @return instance of aClassName
     * @throws DoiRuntimeException
     *             - if aClassName cannot be instantiated
     */
    private static Object buildObject(final String aClassName) throws DoiRuntimeException {
	final Object result;
	try {
	    final Class implClass = Class.forName(aClassName);
	    result = implClass.newInstance();
	} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
	    throw new DoiRuntimeException(ex);
	}
	return result;
    }

}
