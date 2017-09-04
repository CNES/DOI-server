/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils;

import java.util.logging.Logger;

import fr.cnes.doi.logging.shell.ShellHandler;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility Class.
 *
 * @author Jean-Christophe Malapert
 * @author Claire
 */
public class Utils {      

    /**
     * Name of the logger in console without date.
     */
    public static final String SHELL_LOGGER_NAME = "fr.cnes.doi.logging.shell";

    /**
     * Name of the logger for http requests and answers.
     */
    public static final String HTTP_LOGGER_NAME = "fr.cnes.doi.logging.api";

    /**
     * Name of the logger for applicative logs.
     */
    public static final String APP_LOGGER_NAME = "fr.cnes.doi.logging.app";

    /**
     * Logger for applicative logs.
     */
    private static Logger appLogger;

    /**
     * Logger in the console to display msg info like help.
     */
    private static Logger shellLogger;

    /**
     * Checks whether the char sequence is empty.
     *
     * @param cs the char sequence
     * @return True when the char sequence is empty otherwise False
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Checks whether the char sequence is not empty.
     *
     * @param cs the char sequence
     * @return True when the char sequence is not empty otherwise False
     */
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * Gets the shell logger to log in the console without date
     *
     * @return the logger
     */
    public static Logger getShellLogger() {

        if (shellLogger == null) {
            shellLogger = Logger.getLogger(SHELL_LOGGER_NAME);
            shellLogger.addHandler(new ShellHandler());
            shellLogger.setUseParentHandlers(false);
        }
        return shellLogger;
    }

    /**
     * Gets the application logger to log in the specific file for applicative
     * messages
     *
     * @return the logger
     */
    public static Logger getAppLogger() {
        if (appLogger == null) {
            appLogger = Logger.getLogger(APP_LOGGER_NAME);
        }
        return appLogger;
    }

    /**
     * Returns the keys related to a value within a map.
     * @param <T> the type of the key
     * @param <E> the type of the value
     * @param map map
     * @param value value to search
     * @return the keys related to a value
     */
    public static <T, E> Set<T> getKeysByValue(final Map<T, E> map, final E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }    
}
