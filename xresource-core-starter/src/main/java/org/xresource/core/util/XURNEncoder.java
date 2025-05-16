package org.xresource.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xresource.core.model.XResourceMetadata;

/**
 * Utility class for encoding and decoding URNs (Uniform Resource Names)
 * using a compact Base62 format. A URN identifies a resource by combining
 * the resource name and its key(s) into a unique string format:
 *
 * <pre>
 *     uxr:{resource}:{base62EncodedKeyString}
 * </pre>
 */
public class XURNEncoder {

    private static final String URN_PREFIX = "uxr:";
    private static final Pattern URN_PATTERN = Pattern.compile("^uxr:([a-zA-Z0-9_-]+):([a-zA-Z0-9]+)$");
    private static final char[] BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            .toCharArray();

    // ------------------- PUBLIC ENCODE -------------------

    /**
     * Encodes a simple (single-key) URN.
     *
     * @param resourceName the name of the resource
     * @param id           the ID of the resource
     * @return encoded URN string
     */
    public static String encode(String resourceName, Object id) {
        Map<String, Object> keyMap = new LinkedHashMap<>();
        keyMap.put("id", id);
        return encode(resourceName, keyMap);
    }

    /**
     * Encodes a composite-key URN.
     *
     * @param resourceName the name of the resource
     * @param keyMap       a map of key field names and their values
     * @return encoded URN string
     */
    public static String encode(String resourceName, Map<String, Object> keyMap) {
        StringBuilder sb = new StringBuilder();
        keyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sb.append(entry.getKey()).append('=').append(entry.getValue()).append('&'));

        if (sb.length() > 0)
            sb.setLength(sb.length() - 1); // trim trailing '&'
        byte[] utf8Bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String encoded = encodeBase62(utf8Bytes);
        return URN_PREFIX + resourceName + ":" + encoded;
    }

    // ------------------- PUBLIC DECODE -------------------

    /**
     * Decodes a URN into its resource name and key map.
     *
     * @param urn the URN string to decode
     * @return URN object containing resource name and key-value map
     * @throws URISyntaxException if the URN is invalid
     */
    public static URN decode(String urn) throws URISyntaxException {
        Matcher matcher = URN_PATTERN.matcher(urn);
        if (!matcher.matches()) {
            throw new URISyntaxException(urn, "URN format is invalid");
        }

        String resourceName = matcher.group(1);
        String base62Part = matcher.group(2);
        byte[] decodedBytes = decodeBase62(base62Part);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

        Map<String, String> keyMap = new LinkedHashMap<>();
        for (String part : decodedString.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2) {
                keyMap.put(pair[0], pair[1]);
            }
        }

        return new URN(resourceName, keyMap);
    }

    // ------------------- BASE62 ENCODING -------------------

    /**
     * Encodes a byte array into a Base62 string.
     *
     * @param data byte array to encode
     * @return Base62 string
     */
    private static String encodeBase62(byte[] data) {
        BigInteger num = new BigInteger(1, data);
        StringBuilder sb = new StringBuilder();
        while (num.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = num.divideAndRemainder(BigInteger.valueOf(62));
            sb.append(BASE62_CHARS[divRem[1].intValue()]);
            num = divRem[0];
        }
        return sb.reverse().toString();
    }

    /**
     * Decodes a Base62 string into a byte array.
     *
     * @param base62 Base62-encoded string
     * @return decoded byte array
     */
    private static byte[] decodeBase62(String base62) {
        BigInteger num = BigInteger.ZERO;
        for (char c : base62.toCharArray()) {
            int index = new String(BASE62_CHARS).indexOf(c);
            if (index < 0)
                throw new IllegalArgumentException("Invalid base62 character: " + c);
            num = num.multiply(BigInteger.valueOf(62)).add(BigInteger.valueOf(index));
        }
        byte[] bytes = num.toByteArray();
        return (bytes.length > 0 && bytes[0] == 0) ? Arrays.copyOfRange(bytes, 1, bytes.length) : bytes;
    }

    // ------------------- REFLECTION + UTILS -------------------

    /**
     * Generates a URN for an entity object using resource metadata.
     *
     * @param entity       the entity instance
     * @param resourceName the name of the resource
     * @param metadata     metadata describing the entity's keys
     * @return encoded URN string or null on error
     */
    public static String getURN(Object entity, String resourceName, XResourceMetadata metadata) {
        try {
            Map<String, Object> keyMap = new LinkedHashMap<>();
            String primaryKeys = metadata.getPrimaryKey();
            String[] compositeKeys;

            if (metadata.hasCompositeKey()) {
                Method getter = entity.getClass().getMethod("get" + capitalize(metadata.getEmbeddedKeyFieldName()));
                Object embeddedKeyObj = getter.invoke(entity);
                if (embeddedKeyObj == null)
                    throw new IllegalStateException("Embedded key object is null");

                compositeKeys = primaryKeys.split(",");
                for (String field : compositeKeys) {
                    Method emIdFieldGetter = embeddedKeyObj.getClass().getMethod("get" + capitalize(field));
                    Object embeddedKeyFieldObj = emIdFieldGetter.invoke(embeddedKeyObj);
                    keyMap.put(field, embeddedKeyFieldObj.toString());
                }

                return encode(resourceName, keyMap);
            } else {
                Object idValue = getFieldValue(entity, primaryKeys);
                return encode(resourceName, idValue);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves the value of a field using reflection.
     *
     * @param entity    object instance
     * @param fieldName field to access
     * @return field value
     */
    public static Object getFieldValue(Object entity, String fieldName) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access field '" + fieldName + "'", e);
        }
    }

    /**
     * Capitalizes the first character of a string.
     *
     * @param str input string
     * @return capitalized string
     */
    public static String capitalize(String str) {
        return (str == null || str.isEmpty()) ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // ------------------- URN RECORD -------------------

    /**
     * Represents a decoded URN containing the resource name and key map.
     */
    public static class URN {
        private final String resourceName;
        private final Map<String, String> keyMap;

        /**
         * Constructs a URN instance.
         *
         * @param resourceName the name of the resource
         * @param keyMap       the key-value pairs of the identifier
         */
        public URN(String resourceName, Map<String, String> keyMap) {
            this.resourceName = resourceName;
            this.keyMap = keyMap;
        }

        public String getResourceName() {
            return resourceName;
        }

        public Map<String, String> getKeyMap() {
            return keyMap;
        }

        @Override
        public String toString() {
            return "URN{resourceName='" + resourceName + "', keyMap=" + keyMap + '}';
        }
    }

    // ------------------- TEST MAIN -------------------

    /**
     * Demonstrates simple and composite URN encoding/decoding.
     */
    public static void main(String[] args) throws Exception {
        String urn1 = encode("component", 123);
        System.out.println("URN (simple): " + urn1);
        System.out.println("Decoded: " + decode(urn1));

        Map<String, Object> composite = Map.of("componentId", "comp123", "version", "v1.0.2");
        String urn2 = encode("patch", composite);
        System.out.println("URN (composite): " + urn2);
        System.out.println("Decoded: " + decode(urn2));
    }
}
