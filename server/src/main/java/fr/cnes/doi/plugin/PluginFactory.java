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
import fr.cnes.doi.security.RoleAuthorizer;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.utils.spec.Requirement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
     * Settings.
     */
    private static final Map<String, String> SETTINGS = new ConcurrentHashMap<>();

    /**
     * Stores the instances of plugin.
     */
    private static final Map<String, Object> CONFIG = new ConcurrentHashMap<>();

    /**
     * Loads the path of plugins from Settings.
     *
     * @param settings config settings
     */
    public static void init(final Map<String, String> settings) {
        final String userRealPlugin = settings.getOrDefault(Consts.PLUGIN_USER_GROUP_MGT, "");
        final String projectSuffixPlugin = settings.getOrDefault(Consts.PLUGIN_PROJECT_SUFFIX, "");
        final String tokenPlugin = settings.getOrDefault(Consts.PLUGIN_TOKEN, "");
        final String authPlugin = settings.getOrDefault(Consts.PLUGIN_AUTHENTICATION, "");
        PLUGINS_IMPL.put(Consts.PLUGIN_USER_GROUP_MGT, userRealPlugin);
        PLUGINS_IMPL.put(Consts.PLUGIN_PROJECT_SUFFIX, projectSuffixPlugin);
        PLUGINS_IMPL.put(Consts.PLUGIN_TOKEN, tokenPlugin);
        PLUGINS_IMPL.put(Consts.PLUGIN_AUTHENTICATION, authPlugin);
        SETTINGS.putAll(settings);
    }

    /**
     * Returns the concrete implementation of the user management interface.
     *
     * @return the plugin
     */
    public static AbstractUserRolePluginHelper getUserManagement() {
        final String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_USER_GROUP_MGT);
        final AbstractUserRolePluginHelper plugin = getPlugin(implClassName);
        if (!plugin.isConfigured()) {
            plugin.setConfiguration(SETTINGS);
            plugin.initConnection();
        }
        return plugin;
    }

    /**
     * Returns the concrete implementation of the suffix project db.
     *
     * @return the plugin
     */
    public static AbstractProjectSuffixPluginHelper getProjectSuffix() {
        final String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_PROJECT_SUFFIX);
        final AbstractProjectSuffixPluginHelper plugin = getPlugin(implClassName);
        if (!plugin.isConfigured()) {
            plugin.setConfiguration(SETTINGS);
            plugin.initConnection();
            //plugin.addObserver(RoleAuthorizer.getInstance());
        }
        return plugin;
    }

    /**
     * Returns the concrete implementation of the token db.
     *
     * @return the plugin
     */
    public static AbstractTokenDBPluginHelper getToken() {
        final String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_TOKEN);
        final AbstractTokenDBPluginHelper plugin = getPlugin(implClassName);
        if (!plugin.isConfigured()) {
            plugin.setConfiguration(SETTINGS);
            plugin.initConnection();
        }
        return plugin;
    }

    /**
     * Returns the concrete implementation of the authentication system.
     *
     * @return the plugin
     */
    public static AbstractAuthenticationPluginHelper getAuthenticationSystem() {
        final String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_AUTHENTICATION);
        final AbstractAuthenticationPluginHelper plugin = getPlugin(implClassName);
        if (!plugin.isConfigured()) {
            plugin.setConfiguration(SETTINGS);
            plugin.initConnection();
        }
        return plugin;
    }

    /**
     * Checks if the key is a password in the class keywordClassName related to
     * the configuration file
     *
     * @param keywordClassName plugin related to the configuration file
     * @param key keyword
     * @return True when key is a password otherwise false
     * @throws DoiRuntimeException When an error occurs
     */
    public static boolean isPassword(final String keywordClassName, final String key) throws
            DoiRuntimeException {
        try {
            final String implClassName = PLUGINS_IMPL.get(keywordClassName);
            final Class implClass = Class.forName(implClassName);
            final Method method = implClass.getMethod("isPassword", String.class);
            final Object obj = method.invoke(null, key);
            return Boolean.getBoolean(String.valueOf(obj));
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new DoiRuntimeException(ex);
        }
    }

    /**
     * Returns the plugin according to its implementation class.
     *
     * @param <T> the plugin
     * @param implClassName implementation class
     * @return the plugin
     */
    private static <T> T getPlugin(final String implClassName) {
        final T plugin;
        if (CONFIG.get(implClassName) == null) {
            try {
                final Class impClass = Class.forName(implClassName);
                plugin = (T) impClass.cast(buildObject(implClassName));
                CONFIG.put(implClassName, plugin);
            } catch (ClassNotFoundException ex) {
                throw new DoiRuntimeException(ex);
            }
        } else {
            plugin = (T) CONFIG.get(implClassName);
        }
        return plugin;
    }

    /**
     * instantiates the plugin.
     *
     * @param aClassName the plugin name
     * @return instance of aClassName
     * @throws DoiRuntimeException - if aClassName cannot be instantiated
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

    /**
     * "Static" class cannot be instantiated
     */
    private PluginFactory() {
    }

}
