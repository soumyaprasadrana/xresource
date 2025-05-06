package org.xresource.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xresource.core.model.XResourceMetadata;
import org.xresource.core.registry.XResourceMetadataRegistry;

import java.util.Base64;

/**
 * Utility to encode/decode resource URNs in the format:
 * urn:xresource:{resourceName}:{base64EncodedKeyString}
 *
 * Supports both simple and composite primary keys.
 */
public class XURNEncoder {

    private static final String URN_PREFIX = "urn:xresource:";
    private static final Pattern URN_PATTERN = Pattern.compile("^urn:xresource:([a-zA-Z0-9_-]+):([a-zA-Z0-9=+/_-]+)$");

    /**
     * Encodes a single ID as URN (simple key)
     */
    public static String encode(String resourceName, Object id) {
        Map<String, Object> keyMap = new LinkedHashMap<>();
        keyMap.put("id", id);
        return encode(resourceName, keyMap);
    }

    /**
     * Encodes a composite key map into a URN
     */
    public static String encode(String resourceName, Map<String, Object> keyMap) {
        StringBuilder builder = new StringBuilder();
        keyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    builder.append(entry.getKey())
                            .append("=")
                            .append(entry.getValue())
                            .append("&");
                });

        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1); // remove trailing '&'
        }

        String keyString = builder.toString();
        String encoded = Base64.getUrlEncoder().encodeToString(keyString.getBytes(StandardCharsets.UTF_8));
        return URN_PREFIX + resourceName + ":" + encoded;
    }

    /**
     * Decodes a URN string into a resourceName and keyMap
     */
    public static URN decode(String urn) throws URISyntaxException {
        Matcher matcher = URN_PATTERN.matcher(urn);
        if (!matcher.matches()) {
            throw new URISyntaxException(urn, "URN format is invalid");
        }

        String resourceName = matcher.group(1);
        String encoded = matcher.group(2);
        String decodedKeyString = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);

        Map<String, String> keyMap = new LinkedHashMap<>();
        for (String part : decodedKeyString.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2) {
                keyMap.put(pair[0], pair[1]);
            }
        }

        return new URN(resourceName, keyMap);
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Get URN for an entity
     */
    public static String getURN(Object entity, String resourceName, XResourceMetadata metadata) {
        try {
            Map<String, Object> keyMap = new LinkedHashMap<>();
            String primaryKeys = metadata.getPrimaryKey();
            String[] compositeKeys;
            String urn;

            if (metadata.hasCompositeKey()) {
                // Attempt to access the embedded ID getter
                Method getter = entity.getClass().getMethod("get" + capitalize(metadata.getEmbeddedKeyFieldName()));
                Object embeddedKeyObj = getter.invoke(entity);

                if (embeddedKeyObj == null) {
                    throw new IllegalStateException("Embedded key object is null");
                }

                compositeKeys = primaryKeys.split(",");
                for (String field : compositeKeys) {
                    Method emIdFieldGetter = embeddedKeyObj.getClass().getMethod("get" + capitalize(field));
                    Object embeddedKeyFieldObj = emIdFieldGetter.invoke(embeddedKeyObj);
                    keyMap.put(field, embeddedKeyFieldObj.toString());
                }
                urn = XURNEncoder.encode(resourceName, keyMap);
            } else {
                Object idValue = getFieldValue(entity, primaryKeys);
                urn = XURNEncoder.encode(resourceName, idValue);
            }

            return urn;

        } catch (Exception ex) {
            // Log and continue — do not block response
            ex.printStackTrace();
            System.err.println("Failed to generate URN for " + resourceName + ": " + ex.getMessage());
            return null;
        }
    }

    public static Object getFieldValue(Object entity, String fieldName) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access field '" + fieldName + "' in entity", e);
        }
    }

    /**
     * Container class to hold decoded URN data.
     */
    public static class URN {
        private final String resourceName;
        private final Map<String, String> keyMap;

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
            return "URN{" +
                    "resourceName='" + resourceName + '\'' +
                    ", keyMap=" + keyMap +
                    '}';
        }
    }

    // Demo
    public static void main(String[] args) throws Exception {
        // Simple key
        String urn1 = encode("component", 123);
        System.out.println("URN (simple): " + urn1);
        URN decoded1 = decode(urn1);
        System.out.println("Decoded: " + decoded1);

        // Composite key
        Map<String, Object> composite = Map.of("componentId", "comp123", "version", "v1.0.2");
        String urn2 = encode("patch", composite);
        System.out.println("URN (composite): " + urn2);
        URN decoded2 = decode(urn2);
        System.out.println("Decoded: " + decoded2);
    }
}
