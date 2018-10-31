/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.utils;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utility Class.
 *
 * @author Jean-Christophe Malapert
 * @author Claire
 */
public class Utils {

    /**
     * An empty immutable {@code Object} array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * Name of the logger in console without date.
     */
    public static final String SHELL_LOGGER_NAME = "fr.cnes.doi.logging.shell";

    /**
     * Name of the logger for http requests and answers {@value #HTTP_LOGGER_NAME}.
     */
    public static final String HTTP_LOGGER_NAME = "fr.cnes.doi.logging.api";

    /**
     * Name of the logger to measure performances {@value #APP_LOGGER_NAME}.
     */
    public static final String APP_LOGGER_NAME = "fr.cnes.doi.logging.app";

    /**
     * Name of the logger to measure performances {@value #SECURITY_LOGGER_NAME}.
     */
    public static String SECURITY_LOGGER_NAME = "fr.cnes.doi.logging.security";

    /**
     * Checks whether the char sequence is empty.
     *
     * @param charSeq the char sequence
     * @return True when the char sequence is empty otherwise False
     */
    public static boolean isEmpty(final CharSequence charSeq) {
        return charSeq == null || charSeq.length() == 0;
    }

    /**
     * Checks whether the char sequence is not empty.
     *
     * @param charSeq the char sequence
     * @return True when the char sequence is not empty otherwise False
     */
    public static boolean isNotEmpty(final CharSequence charSeq) {
        return !isEmpty(charSeq);
    }

    /**
     * Returns the keys related to a value within a map.
     *
     * @param <T> the type of the key
     * @param <E> the type of the value
     * @param map map
     * @param value value to search
     * @return the keys related to a value
     */
    public static <T, E> Set<T> getKeysByValue(final Map<T, E> map,
            final E value) {
        final Set<T> keys = new HashSet<>();
        for (final Entry<T, E> entry : map.entrySet()) {
            final E entryVal = entry.getValue();
            if (entryVal.equals(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    /**
     * <p>
     * Defensive programming technique to change a {@code null} reference to an empty one.
     *
     * <p>
     * This method returns an empty array for a {@code null} input array.
     *
     * @param array the array to check for {@code null} or empty
     * @param type the class representation of the desired array
     * @param <T> the class type
     * @return the same array, {@code public static} empty array if {@code null}
     * @throws IllegalArgumentException if the type argument is null
     */
    public static <T> T[] nullToEmpty(final T[] array,
            final Class<T[]> type)
            throws IllegalArgumentException {
        if (type == null) {
            throw new IllegalArgumentException("The type must not be null");
        }

        T[] result;
        if (array == null) {
            result = type.cast(Array.newInstance(type.getComponentType(), 0));
        } else {
            result = array;
        }
        return result;
    }

    /**
     * <p>
     * Defensive programming technique to change a {@code null} reference to an empty one.
     *
     * <p>
     * This method returns an empty array for a {@code null} input array.
     *
     * <p>
     * As a memory optimizing technique an empty array passed in will be overridden with the empty
     * {@code public static} references in this class.
     *
     * @param array the array to check for {@code null} or empty
     * @return the same array, {@code public static} empty array if {@code null} or empty input
     */
    public static Object[] nullToEmpty(final Object[] array) {
        Object[] result;
        if (isEmpty(array)) {
            result = EMPTY_OBJECT_ARRAY;
        } else {
            result = array;
        }
        return result;
    }

    /**
     * <p>
     * Checks if an array of Objects is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     */
    public static boolean isEmpty(final Object[] array) {
        return getLength(array) == 0;
    }

    /**
     * <p>
     * Returns the length of the specified array. This method can deal with {@code Object} arrays
     * and with primitive arrays.
     *
     * <p>
     * If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     * Utils.getLength(null)            = 0
     * Utils.getLength([])              = 0
     * Utils.getLength([null])          = 1
     * Utils.getLength([true, false])   = 2
     * Utils.getLength([1, 2, 3])       = 3
     * Utils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     */
    public static int getLength(final Object array) throws IllegalArgumentException {
        int length;
        if (array == null) {
            length = 0;
        } else {
            length = Array.getLength(array);
        }
        return length;
    }
}
