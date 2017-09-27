/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.plugin;

import fr.cnes.doi.utils.spec.Requirement;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for plugins.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class Utils {

    private static final Logger LOGGER = fr.cnes.doi.utils.Utils.getAppLogger();

    /**
     * Adds a path in the classPath.
     *
     * @param path path
     * @throws Exception
     */
    @Requirement(
            reqId = Requirement.DOI_ARCHI_040,
            reqName = Requirement.DOI_ARCHI_040_NAME
    )
    public static void addPath(String path) throws Exception {
        LOGGER.fine(String.format("Add path %s to plugin", path));
        if(path == null) {
            throw new NullPointerException("path variable is null");
        }
        File f = new File(path);
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (Object file : fr.cnes.doi.utils.Utils.nullToEmpty(files)) {
                loadFileInClassPath((File)file);
            }
        }
    }

    /**
     * Adds a file in a classpath.
     *
     * @param f file
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws MalformedURLException
     */
    public static void loadFileInClassPath(File f) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
        LOGGER.log(Level.FINE, "Adds file {0} to classpathS", f);
        URI u = f.toURI();
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> urlClass = URLClassLoader.class;
        Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[]{u.toURL()});
    }

}
