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

import fr.cnes.doi.utils.spec.Requirement;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for plugins.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class Utils {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(Utils.class.getName());

    /**
     * "Static" class cannot be instantiated
     */
    private Utils() {
    }
    
    /**
     * Adds a path in the classPath.
     *
     * @param path path
     * @throws IllegalArgumentException - if path is null
     * @throws NoSuchMethodException - if a matching method is not found.
     * @throws IllegalAccessException - if this Method object is enforcing Java
     * language access control and the underlying method is inaccessible.
     * @throws InvocationTargetException - if the underlying method throws an
     * exception.
     * @throws MalformedURLException - If a protocol handler for the URL could
     * not be found, or if some other error occurred while constructing the URL
     */
    @Requirement(reqId = Requirement.DOI_ARCHI_040, reqName = Requirement.DOI_ARCHI_040_NAME)
    public static void addPath(final String path)
            throws IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, MalformedURLException {
        LOG.debug("Add path {} to plugin", path);
        if (path == null) {
            throw LOG.throwing(new IllegalArgumentException("path variable is null"));
        }
        final File pathDir = new File(path);
        if (pathDir.isDirectory()) {
            final File[] files = pathDir.listFiles();
            for (final Object file : fr.cnes.doi.utils.Utils.nullToEmpty(files)) {
                loadFileInClassPath((File) file);
            }
        }
    }

    /**
     * Adds a file in a classpath.
     *
     * @param file file
     * @throws NoSuchMethodException - if a matching method is not found.
     * @throws IllegalAccessException - if this Method object is enforcing Java
     * language access control and the underlying method is inaccessible.
     * @throws InvocationTargetException - if the underlying method throws an
     * exception.
     * @throws MalformedURLException - If a protocol handler for the URL could
     * not be found, or if some other error occurred while constructing the URL
     */
    public static void loadFileInClassPath(final File file)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException,
            MalformedURLException {
        LOG.debug("Adds file {} to classpathS", file);
        final URI uri = file.toURI();
        final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class<URLClassLoader> urlClass = URLClassLoader.class;
        final Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[]{uri.toURL()});
    }

}
