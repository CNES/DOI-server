/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.plugin;

import fr.cnes.doi.db.ProjectSuffixDBHelper;
import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.settings.Consts;
import java.util.LinkedHashMap;
import java.util.Map;
import fr.cnes.doi.db.UserRoleDBHelper;
import fr.cnes.doi.utils.spec.Requirement;

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
     * Loads the path of plugins from Settings.
     *
     * @param settings config settings
     */
    public static void init(Map<String, String> settings) {
        String userRealPlugin = settings.get(Consts.PLUGIN_USER_GROUP_MGT);
        String projectSuffixPlugin = settings.get(Consts.PLUGIN_PROJECT_SUFFIX);
        String tokenPlugin = settings.get(Consts.PLUGIN_TOKEN);
        PLUGINS_IMPL.put(Consts.PLUGIN_USER_GROUP_MGT, userRealPlugin);
        PLUGINS_IMPL.put(Consts.PLUGIN_PROJECT_SUFFIX, projectSuffixPlugin);
        PLUGINS_IMPL.put(Consts.PLUGIN_TOKEN, tokenPlugin);
    }

    /**
     * Returns the concrete implementation of the user management interface.
     *
     * @return the plugin
     */
    public static UserRoleDBHelper getUserManagement() {
        String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_USER_GROUP_MGT);
        UserRoleDBHelper result = (UserRoleDBHelper) buildObject(implClassName);
        return result;
    }

    /**
     * Returns the concrete implementation of the suffix project db.
     *
     * @return the plugin
     */
    public static ProjectSuffixDBHelper getProjectSuffix() {
        String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_PROJECT_SUFFIX);
        ProjectSuffixDBHelper result = (ProjectSuffixDBHelper) buildObject(implClassName);
        return result;
    }

    /**
     * Returns the concrete implementation of the token db.
     *
     * @return the plugin
     */
    public static TokenDBPluginHelper getToken() {
        String implClassName = PLUGINS_IMPL.get(Consts.PLUGIN_TOKEN);
        TokenDBPluginHelper result = (TokenDBPluginHelper) buildObject(implClassName);
        return result;
    }

    /**
     * Map the name of an interface to the name of a corresponding concrete
     * implementation class.
     */
    private static final Map<String, String> PLUGINS_IMPL = new LinkedHashMap<>();

    /**
     * instantiates the plugin.
     *
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
