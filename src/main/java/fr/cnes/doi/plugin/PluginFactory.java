/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.plugin;

import fr.cnes.doi.db.AbstractProjectSuffixDBHelper;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.settings.Consts;
import java.util.Map;
import fr.cnes.doi.db.AbstractUserRoleDBHelper;
import fr.cnes.doi.utils.spec.Requirement;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
    reqId = Requirement.DOI_ARCHI_030,
    reqName = Requirement.DOI_ARCHI_030_NAME      
    )
public final class PluginFactory {
    
    /**
     * Map the name of an interface to the name of a corresponding concrete
     * implementation class.
     */
    private static final Map<String, String> PLUGINS_IMPL = new ConcurrentHashMap<>();    

    /**
     * Loads the path of plugins from Settings.
     *
     * @param settings config settings
     */
    public static void init(final Map<String, String> settings) {
        final String userRealPlugin = settings.getOrDefault(Consts.PLUGIN_USER_GROUP_MGT,"");
        final String projectSuffixPlugin = settings.getOrDefault(Consts.PLUGIN_PROJECT_SUFFIX,"");
        final String tokenPlugin = settings.getOrDefault(Consts.PLUGIN_TOKEN,"");    
        final String doiPlugin = settings.getOrDefault(Consts.PLUGIN_DOI,""); 
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
     * Returns the concrete implementation of the DOI db.
     *
     * @return the plugin
     */
    public static AbstractDoiDBPluginHelper getDoi() {
        final String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_DOI);
        return (AbstractDoiDBPluginHelper) buildObject(implClassName);
    }    

    /**
     * instantiates the plugin.
     *
     * @param aClassName the plugin name
     * @return instance of aClassName
     * @throws DoiRuntimeException - if aClassName cannot be instantiated
     */
    private static Object buildObject(final String aClassName) throws DoiRuntimeException{
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
