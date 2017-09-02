/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.plugin;

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.settings.Consts;
import java.util.LinkedHashMap;
import java.util.Map;
import fr.cnes.doi.security.UserGroupMngtPlugin;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class PluginFactory {

    /**
     * Loads the path of plugins from Settings.
     * @param settings config settings
     */
    public static void init(Map<String, String> settings) {        
        String userRealPlugin = settings.get(Consts.PLUGIN_USER_GROUP_MGT);
        PLUGINS_IMPL.put(Consts.PLUGIN_USER_GROUP_MGT, userRealPlugin);
    }
     
    /**
     * Return the concrete implementation of the user management interface.
     * @return the plugin
     */
    public static UserGroupMngtPlugin getUserManagement() {
        String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_USER_GROUP_MGT);
        UserGroupMngtPlugin result = (UserGroupMngtPlugin) buildObject(implClassName);
        return result;
    }

    /**
     * Map the name of an interface to the name of a corresponding concrete
     * implementation class.
     */
    private static final Map<String, String> PLUGINS_IMPL = new LinkedHashMap<>();

    /**
     * instantiates the plugin.
     * @param aClassName the plugin name
     * @return 
     */
    private static Object buildObject(final String aClassName) {
        Object result = null;
        try {
            Class implClass = Class.forName(aClassName);
            result = implClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new DoiRuntimeException(ex);
        }
        return result;
    }

}
