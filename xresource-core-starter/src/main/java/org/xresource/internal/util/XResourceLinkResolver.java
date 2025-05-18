package org.xresource.internal.util;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import org.springframework.beans.factory.annotation.Value;
import org.xresource.core.logging.XLogger;
import org.xresource.core.util.XUtils;
import org.xresource.internal.models.XResourceMetadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xresource.internal.config.XResourceConfigProperties.API_BASE_PATH;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XResourceLinkResolver {

    @Value(API_BASE_PATH)
    private String baseAPIURL;

    private static final XLogger log = XLogger.forClass(XResourceLinkResolver.class);

    public String buildResourceLink(Object entity, XResourceMetadata metadata, String baseUrl, String fieldName) {
        String urn = XURNEncoder.getURN(entity, metadata.getResourceName(), metadata);
        String resourceURI = baseUrl + baseAPIURL + "/" + metadata.getResourceName() + "/" + urn + "/" + fieldName;

        return resourceURI;
    }

    public String buildResourceLink(Object entity, String baseUrl) {
        try {
            Object idValue = extractEntityId(entity.getClass(), entity);
            if (idValue == null)
                return null;

            String resourceName = entity.getClass().getSimpleName().toLowerCase();
            return baseUrl + baseAPIURL + "/" + resourceName + "/" + idValue;

        } catch (Exception e) {
            return null;
        }
    }

    public String buildResourceLinkFromJoinColumn(Object parent, Field field, String baseUrl) {
        try {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (joinColumn == null || joinColumn.name().isEmpty())
                return null;

            String refTable = field.getType().getAnnotation(jakarta.persistence.Table.class).name();

            return baseUrl + baseAPIURL + "/" + refTable + "/" + "";

        } catch (Exception e) {
            return null;
        }
    }

    private Object extractEntityId(Class<?> entityClass, Object instance) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                try {
                    return field.get(instance);
                } catch (IllegalAccessException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Injects a permalink and URN into the given JSON node for the specified
     * entity.
     *
     * @param node         The JSON node to augment.
     * @param entity       The entity from which to extract ID fields.
     * @param resourceName The name of the resource (usually the entity/table name).
     * @param metadata     Metadata describing the entity and its primary key(s).
     * @param baseUrl      The base URL to use for generating the permalink.
     */
    public void injectPermalink(ObjectNode node, Object entity, String resourceName, XResourceMetadata metadata,
            String baseUrl) {
        log.enter("injectPermalink(entity=%s, resourceName=%s, baseUrl=%s)", entity, resourceName, baseUrl);
        try {
            Map<String, Object> keyMap = new LinkedHashMap<>();
            String primaryKeys = metadata.getPrimaryKey();
            String[] compositeKeys;
            String urn;

            if (metadata.hasCompositeKey()) {
                log.debug("Handling composite key for resource: %s", resourceName);
                Method getter = entity.getClass()
                        .getMethod("get" + XUtils.capitalize(metadata.getEmbeddedKeyFieldName()));
                Object embeddedKeyObj = getter.invoke(entity);

                if (embeddedKeyObj == null) {
                    throw new IllegalStateException("Embedded key object is null");
                }

                compositeKeys = primaryKeys.split(",");
                for (String field : compositeKeys) {
                    Method emIdFieldGetter = embeddedKeyObj.getClass().getMethod("get" + XUtils.capitalize(field));
                    Object embeddedKeyFieldObj = emIdFieldGetter.invoke(embeddedKeyObj);
                    keyMap.put(field, embeddedKeyFieldObj.toString());
                }
                urn = XURNEncoder.encode(resourceName, keyMap);
            } else {
                log.debug("Handling simple key for resource: %s", resourceName);
                Object idValue = XURNEncoder.getFieldValue(entity, primaryKeys);
                urn = XURNEncoder.encode(resourceName, idValue);
            }

            String permalink = baseUrl + baseAPIURL + "/" + resourceName + "/" + urn;
            log.debug("Generated permalink: %s", permalink);

            node.put("permalink", permalink);
            node.put("urn", urn);
        } catch (Exception ex) {
            log.error("Failed to generate permalink for %s: %s", resourceName, ex.getMessage(), ex);
            node.put("permalink", (String) null);
            node.put("urn", (String) null);
        }
        log.exit("injectPermalink completed for resource: %s", resourceName);
    }

    /**
     * Injects a permalink and URN into the given JSON node for the specified
     * entity.
     *
     * @param node         The JSON node to augment.
     * @param entity       The entity from which to extract ID fields.
     * @param resourceName The name of the resource (usually the entity/table name).
     * @param metadata     Metadata describing the entity and its primary key(s).
     * @param baseUrl      The base URL to use for generating the permalink.
     */
    public void injectXActions(ObjectNode node, Object entity, String resourceName, XResourceMetadata metadata,
            String baseUrl, ObjectMapper objectMapper) {
        log.enter("injectPermalink(entity=%s, resourceName=%s, baseUrl=%s)", entity, resourceName, baseUrl);
        try {
            String urn = XURNEncoder.getURN(entity, metadata.getResourceName(), metadata);

            List<Map<String, Map<String, Object>>> actions = new ArrayList<Map<String, Map<String, Object>>>();

            metadata.getXActionsMap().values().stream().forEach((action) -> {
                actions.add(Map.of(action.name(),
                        Map.of("_href",
                                baseUrl + baseAPIURL + "/" + resourceName + "/" + urn + "/" + "actions" + "/"
                                        + action.name(),
                                "_meta", Map.of(
                                        "type", action.type()))));
            });

            metadata.getAllXFieldActions().values().stream().forEach((actionMap) -> {
                actionMap.forEach((name, action) -> {
                    actions.add(Map.of(action.name(),
                            Map.of("_href",
                                    baseUrl + baseAPIURL + "/" + resourceName + "/" + urn + "/" + "actions" + "/"
                                            + action.name(),
                                    "_meta", Map.of(
                                            "type", "POST"))));
                });
            });
            if (!actions.isEmpty())
                node.put("_actions", objectMapper.valueToTree(actions));
        } catch (Exception ex) {
            log.error("Failed to generate permalink for %s: %s", resourceName, ex.getMessage(), ex);
            node.put("permalink", (String) null);
            node.put("urn", (String) null);
        }
        log.exit("injectPermalink completed for resource: %s", resourceName);
    }

}
