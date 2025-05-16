package org.xresource.core.util;

import java.lang.reflect.Field;

import org.xresource.core.logging.XLogger;

public class XUtils {

    private static final XLogger log = XLogger.forClass(XUtils.class);

    /**
     * Capitalizes the first character of the input string.
     *
     * @param str The input string to capitalize.
     * @return The capitalized string. Returns the input string if null or empty.
     */
    public static String capitalize(String str) {
        log.enter("capitalize(%s)", str);
        if (str == null || str.isEmpty()) {
            log.debug("String is null or empty, returning original.");
            log.exit("capitalize -> %s", str);
            return str;
        }
        String result = str.substring(0, 1).toUpperCase() + str.substring(1);
        log.exit("capitalize -> %s", result);
        return result;
    }

    /**
     * Safely finds a declared field from a class or its superclasses.
     *
     * @param clazz     The class to search.
     * @param fieldName The name of the field.
     * @return The Field object if found, or null otherwise.
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        log.enter("findField(clazz=%s, fieldName=%s)", clazz, fieldName);
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                log.exit("findField -> %s", field);
                return field;
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        log.debug("Field %s not found in class hierarchy", fieldName);
        log.exit("findField -> not found");
        return null;
    }

}
