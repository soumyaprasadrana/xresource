package org.xresource.core.util;

import java.lang.reflect.Field;

import com.fasterxml.jackson.databind.ObjectMapper;

public class XFieldValueResolver {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Object resolveValue(Field field, String stringValue) {
        Class<?> fieldType = field.getType();

        try {
            if (fieldType.equals(String.class)) {
                return stringValue;
            } else if (fieldType.isPrimitive() || Number.class.isAssignableFrom(fieldType)
                    || Boolean.class.isAssignableFrom(fieldType)) {
                return convertPrimitive(fieldType, stringValue);
            } else if (fieldType.isEnum()) {
                return Enum.valueOf((Class<Enum>) fieldType, stringValue);
            } else {
                // assume it's a complex object or another entity
                return objectMapper.readValue(stringValue, fieldType);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert value '" + stringValue +
                    "' to type " + fieldType.getName(), e);
        }
    }

    private static Object convertPrimitive(Class<?> type, String value) {
        if (type == int.class || type == Integer.class)
            return Integer.parseInt(value);
        if (type == long.class || type == Long.class)
            return Long.parseLong(value);
        if (type == boolean.class || type == Boolean.class)
            return Boolean.parseBoolean(value);
        if (type == double.class || type == Double.class)
            return Double.parseDouble(value);
        if (type == float.class || type == Float.class)
            return Float.parseFloat(value);
        // extend as needed
        throw new IllegalArgumentException("Unsupported primitive type: " + type);
    }

}
