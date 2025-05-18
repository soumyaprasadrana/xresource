package org.xresource.internal.openapi;

import org.springframework.security.core.context.SecurityContextHolder;
import org.xresource.core.annotations.AccessLevel;
import org.xresource.core.annotations.XAction;
import org.xresource.core.annotations.XForceAllowResourceRef;
import org.xresource.core.annotations.XQuery;
import org.xresource.core.logging.XLogger;
import org.xresource.core.service.XResourceService;
import org.xresource.internal.auth.XRoleBasedAccessEvaluator;
import org.xresource.internal.models.XFieldMetadata;
import org.xresource.internal.models.XResourceMetadata;
import org.xresource.internal.registry.XResourceMetadataRegistry;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class XOpenApiGenerator {

    private static final XLogger log = XLogger.forClass(XOpenApiGenerator.class);

    private final XResourceMetadataRegistry registry;
    private final String basePath;
    private final XRoleBasedAccessEvaluator xRoleBasedAccessEvaluator;
    private final XResourceService xResourceService;
    private final XOpenApiAuthProperties authProps;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String, Object> generateOpenApiSpec() {
        Map<String, Object> openApi = new LinkedHashMap<>();

        openApi.put("openapi", "3.0.0");
        openApi.put("info", Map.of(
                "title", "XResource OpenAPI",
                "version", "1.0.0"));

        Map<String, Object> paths = new TreeMap<>();
        List<Map<String, Object>> tags = new ArrayList<>();
        Map<String, Object> schemas = new TreeMap<>();

        registry.getRegistry().forEach((resourceName, metadata) -> {
            Class<?> repoClazz = metadata.getRepositoryClass();
            Class<?> entityClazz = metadata.getEntityClass();
            if (repoClazz.isAnnotationPresent(Hidden.class) || entityClazz.isAnnotationPresent(Hidden.class)) {
                return;
            }
            List<Map<String, Object>> endpoints = generateEndpointsForResource(resourceName, metadata, basePath);
            tags.add(Map.of("name", resourceName, "description", "CRUD Endpoints for " + resourceName));

            for (Map<String, Object> ep : endpoints) {
                String path = ep.get("path").toString().replace("{resourceName}", resourceName);
                String method = ep.get("method").toString().toLowerCase();
                Map<String, Object> operation = new LinkedHashMap<>();
                operation.put("summary", ep.get("summary"));
                operation.put("tags", List.of(resourceName));
                operation.put("parameters", ep.get("parameters"));
                operation.put("responses", Map.of("200", Map.of("description", "Success")));

                if (authProps.isEnabled()) {
                    if (authProps.getResourceSecurity().get(resourceName) != null) {
                        String securitySchemeName = authProps.getResourceSecurity().get(resourceName);
                        operation.put("security", List.of(Map.of(securitySchemeName, new ArrayList<>())));
                    } else {
                        String securitySchemeName = authProps.getResourceSecurity().getOrDefault("default", "jwt");
                        operation.put("security", List.of(Map.of(securitySchemeName, new ArrayList<>())));
                    }
                }

                if (method.equals("post")) {
                    operation.put("requestBody", ep.get("requestBody"));
                }

                paths.computeIfAbsent(path, k -> new LinkedHashMap<>());
                ((Map<String, Object>) paths.get(path)).put(method, operation);

                Map<String, Object> schema = generateOpenApiSchema(resourceName, false);
                schemas.put(resourceName, schema);
            }
        });

        registry.getEmbeddedResourceRegistry().forEach((resourceName, metadata) -> {
            Map<String, Object> schema = generateOpenApiSchema(resourceName, true);
            schemas.put(resourceName, schema);
        });

        openApi.put("paths", paths);
        openApi.put("tags", tags);

        Map<String, Object> componentsMap = new HashMap<>();
        componentsMap.put("schemas", schemas);

        if (authProps.isEnabled() && !authProps.getSecuritySchemes().isEmpty()) {
            Map<String, Object> securitySchemes = new LinkedHashMap<>();

            for (Map.Entry<String, XOpenApiAuthProperties.SecuritySchemeDefinition> entry : authProps
                    .getSecuritySchemes().entrySet()) {
                String schemeName = entry.getKey();
                XOpenApiAuthProperties.SecuritySchemeDefinition def = entry.getValue();

                Map<String, Object> scheme = new LinkedHashMap<>();
                scheme.put("type", def.getType());

                if ("http".equals(def.getType())) {
                    scheme.put("scheme", def.getScheme());
                    if (def.getBearerFormat() != null) {
                        scheme.put("bearerFormat", def.getBearerFormat());
                    }
                } else if ("oauth2".equals(def.getType())) {
                    Map<String, Object> flows = new LinkedHashMap<>();
                    for (Map.Entry<String, XOpenApiAuthProperties.OAuthFlowDefinition> flowEntry : def.getFlows()
                            .entrySet()) {
                        XOpenApiAuthProperties.OAuthFlowDefinition flowDef = flowEntry.getValue();
                        Map<String, Object> flow = new LinkedHashMap<>();
                        flow.put("authorizationUrl", flowDef.getAuthorizationUrl());
                        flow.put("tokenUrl", flowDef.getTokenUrl());
                        flow.put("scopes", flowDef.getScopes());
                        flows.put(flowEntry.getKey(), flow);
                    }
                    scheme.put("flows", flows);
                } else if ("apiKey".equals(def.getType())) {
                    if (def.getName() != null) {
                        scheme.put("name", def.getName());
                    }
                    if (def.getIn() != null) {
                        scheme.put("in", def.getIn());
                    }
                }

                securitySchemes.put(schemeName, scheme);
            }

            componentsMap.put("securitySchemes", securitySchemes);

        }
        openApi.put("components", componentsMap);

        return openApi;
    }

    private List<Map<String, Object>> generateEndpointsForResource(String resourceName,
            XResourceMetadata metadata, String baseApiPath) {
        List<Map<String, Object>> openApiEndpoints = new ArrayList<>();

        // --- Queries (GET only)
        for (Map.Entry<String, XQuery> qEntry : metadata.getXQueriesMap().entrySet()) {
            String queryName = qEntry.getKey();
            XQuery query = qEntry.getValue();

            List<Map<String, Object>> queryParams = new ArrayList<>();
            // Standard filters
            queryParams.add(queryParam("page", "integer", "Page number (0-based index)"));
            queryParams.add(queryParam("size", "integer", "Number of records per page"));
            queryParams.add(queryParam("sortBy", "string", "Field name to sort by"));
            queryParams.add(queryParam("direction", "string", "Sort direction: 'asc' or 'desc'"));
            queryParams.add(queryParam("foreignKeys", "string", "Comma-separated foreign key fields to expand"));
            queryParams.add(queryParam("xQueryParams", "string",
                    "JSON string containing query parameters. Example: { \"email\": \"user@example.com\" }"));

            openApiEndpoints.add(buildEndpoint(baseApiPath, "GET", "/{resourceName}/query/" + queryName,
                    "Run named query: " + queryName, queryParams));
        }

        // --- Actions (GET, POST, PUT, DELETE)
        for (Map.Entry<String, XAction> aEntry : metadata.getXActionsMap().entrySet()) {
            String actionName = aEntry.getKey();
            XAction action = aEntry.getValue();

            String method = Optional.ofNullable(action.type()).get().toString().toUpperCase();
            String actionSummary = "Execute " + method + " action: " + actionName;

            List<Map<String, Object>> actionParams = new ArrayList<>();
            actionParams.add(pathParam("id", "string"));

            openApiEndpoints.add(
                    buildEndpoint(
                            baseApiPath, method, "/{resourceName}/{id}/actions/" + actionName, actionSummary,
                            actionParams));
        }

        // --- Standard CRUD endpoints
        List<Map<String, Object>> baseParams = List.of(
                queryParam("page", "integer", "Page number (0-based index)"),
                queryParam("size", "integer", "Number of items per page"),
                queryParam("sortBy", "string", "Field name to sort the results by"),
                queryParam("direction", "string", "Sort direction: 'asc' or 'desc'"),
                queryParam("foreignKeys", "string",
                        "Comma-separated list of foreign key fields to expand in the result"));
        openApiEndpoints.add(buildEndpoint(baseApiPath, "GET", "/{resourceName}", "Find all records", baseParams));
        openApiEndpoints.add(buildEndpoint(baseApiPath, "GET", "/{resourceName}/{id}", "Get record by ID", List.of(
                pathParam("id", "string"),
                queryParam("foreignKeys", "string",
                        "Comma-separated list of foreign key fields to expand in the result"))));
        openApiEndpoints.add(
                buildEndpointWithRequestBody(baseApiPath, "POST", "/{resourceName}", "Create new record", List.of(),
                        resourceName));
        openApiEndpoints.add(buildEndpoint(baseApiPath, "PUT", "/{resourceName}/{id}", "Update record", List.of(
                pathParam("id", "string"),
                queryParam("foreignKeys", "string",
                        "Comma-separated list of foreign key fields to expand in the result"))));
        openApiEndpoints.add(buildEndpoint(baseApiPath, "DELETE", "/{resourceName}/{id}", "Delete record", List.of(
                pathParam("id", "string"))));

        // --- JSON Form endpoint
        openApiEndpoints.add(buildEndpoint(baseApiPath, "GET", "/jsonform/{resourceName}", "Generate JSON form",
                List.of(
                        queryParam("fieldsCsv", "string",
                                "Comma-separated list of field names to include in the JSON form. Use to generate form only for specific fields."))));

        // --- Field by ID
        openApiEndpoints
                .add(buildEndpoint(baseApiPath, "GET", "/{resourceName}/{id}/{field}", "Get field value from record",
                        List.of(
                                pathParam("id", "string"),
                                pathParam("field", "string"),
                                queryParam("foreignKeys", "string",
                                        "Comma-separated list of foreign key fields to expand in the result"))));

        return openApiEndpoints;
    }

    private Map<String, Object> buildEndpoint(String baseAPIPath, String method, String path, String summary,
            List<Map<String, Object>> params) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("method", method);
        map.put("path", baseAPIPath + path);
        map.put("summary", summary);
        map.put("parameters", params);
        return map;
    }

    private Map<String, Object> buildEndpointWithRequestBody(
            String baseAPIPath,
            String method,
            String path,
            String summary,
            List<Map<String, Object>> params,
            String resourceName) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("method", method);
        map.put("path", baseAPIPath + path);
        map.put("summary", summary);
        map.put("parameters", params);

        if (resourceName != null) {

            List<Map<String, Object>> formFields = xResourceService.generateJsonForm(resourceName, null);
            map.put("requestBody", buildOpenApiRequestBody(formFields));
        }

        return map;
    }

    private Map<String, Object> queryParam(String name, String type, String description) {
        return Map.of(
                "in", "query",
                "name", name,
                "required", false,
                "schema", Map.of("type", type),
                "description", description);
    }

    private Map<String, Object> requiredQueryParam(String name, String type) {
        return Map.of(
                "in", "query",
                "name", name,
                "required", true,
                "schema", Map.of("type", type));
    }

    private Map<String, Object> pathParam(String name, String type) {
        return Map.of(
                "in", "path",
                "name", name,
                "required", true,
                "schema", Map.of("type", type));
    }

    private Map<String, Object> generateOpenApiSchema(String resourceName, boolean isExposedForEmbbededReferencesOnly) {
        XResourceMetadata metadata = isExposedForEmbbededReferencesOnly ? registry.getEmbeddedResource(resourceName)
                : registry.get(resourceName);

        Map<String, XFieldMetadata> fieldsMeta = metadata.getFields();
        Map<String, XFieldMetadata> embeddedIdClassFieldsMeta = metadata.getEmbeddedIdClassFields();

        // Collect all eligible fields
        Set<String> fieldNames = new LinkedHashSet<>();
        fieldsMeta.values().stream()
                .map(XFieldMetadata::getName)
                .forEach(fieldNames::add);

        if (metadata.hasCompositeKey()) {
            fieldNames.add(metadata.getPrimaryKey());
        }

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (String fieldName : fieldNames) {
            XFieldMetadata meta = fieldsMeta.getOrDefault(fieldName, embeddedIdClassFieldsMeta.get(fieldName));
            if (meta == null)
                continue;

            Field field = meta.getField();
            if (field.isAnnotationPresent(Hidden.class))
                continue;

            List<String> roles = getCurrentUserRoles();
            AccessLevel level = xRoleBasedAccessEvaluator.getFieldEffectiveAccess(roles, meta);
            if (level == AccessLevel.NONE)
                continue;

            if (field.isAnnotationPresent(JsonBackReference.class) || field.isAnnotationPresent(JsonIgnore.class)) {
                if (field.isAnnotationPresent(XForceAllowResourceRef.class)) {
                    // ignore
                } else
                    continue;
            }

            Map<String, Object> prop = new LinkedHashMap<>();

            if (meta.isForeignKey()) {
                prop.put("$ref", "#/components/schemas/" + meta.getForeignKeyRefTable());
                properties.put(fieldName, prop);
                continue;
            }
            // Fallback for if a field is an entity type field and backed with only OneToOne
            // then return the mappedby field as this will be the foreign key table
            if (xResourceService.isInverse(field)) {
                String table = xResourceService.resolveInverseOneToOneOrOneToManyForeignTable(field);
                if (table != null) {
                    prop.put("$ref", "#/components/schemas/" + table);
                    properties.put(fieldName, prop);
                    continue;
                }
            }
            // Map Java types to OpenAPI types (you can enhance this if needed)
            prop.put("type", resolveOpenApiType(meta.getType()));
            if (meta.getFormat() != null)
                prop.put("format", meta.getFormat());
            if (meta.getDescription() != null)
                prop.put("description", meta.getDescription());
            if (meta.getDefaultValue() != null)
                prop.put("default", meta.getDefaultValue());
            if (meta.getEnumValues() != null && !meta.getEnumValues().isEmpty()) {
                prop.put("enum", meta.getEnumValues());
            }

            if (meta.isRequired()) {
                required.add(fieldName);
            }

            properties.put(fieldName, prop);
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }

        return schema;
    }

    /**
     * Retrieves the list of roles associated with the currently authenticated user.
     *
     * @return List of role names as strings.
     */
    private List<String> getCurrentUserRoles() {
        log.enter("getCurrentUserRoles");
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());
        log.debug("Retrieved roles: %s", roles);
        log.exit("getCurrentUserRoles -> %s", roles);
        return roles;
    }

    private String resolveOpenApiType(String fieldType) {
        switch (fieldType.toLowerCase()) {
            case "string":
            case "text":
            case "uuid":
            case "date":
            case "datetime":
                return "string";
            case "int":
            case "integer":
            case "long":
                return "integer";
            case "float":
            case "double":
            case "decimal":
                return "number";
            case "boolean":
            case "bool":
                return "boolean";
            case "array":
                return "array";
            case "object":
                return "object";
            default:
                return "string"; // fallback
        }
    }

    public Map<String, Object> buildOpenApiRequestBody(List<Map<String, Object>> formFields) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> requiredFields = new ArrayList<>();

        for (Map<String, Object> field : formFields) {
            String name = (String) field.get("name");
            String type = (String) field.getOrDefault("type", "string");
            boolean required = Boolean.TRUE.equals(field.get("required"));
            String format = (String) field.get("format");
            List<?> enumValues = (List<?>) field.get("enumValues");
            String description = (String) field.get("description");
            Object defaultValue = field.get("defaultValue");

            Map<String, Object> propSchema = new LinkedHashMap<>();
            propSchema.put("type", type);

            if (format != null && !format.isEmpty()) {
                propSchema.put("format", format);
            }
            if (enumValues != null) {
                propSchema.put("enum", enumValues);
            }
            if (description != null) {
                propSchema.put("description", description);
            }
            if (defaultValue != null) {
                propSchema.put("default", defaultValue);
            }

            // Handle object fields (foreign key or nested DTOs)
            if ("object".equals(type)) {
                // Check for nested foreign key scenario
                if (Boolean.TRUE.equals(field.get("isNestedObject")) && field.get("nestedObjectKeyField") != null) {
                    String nestedKey = (String) field.get("nestedObjectKeyField");
                    boolean nestedRequired = Boolean.TRUE.equals(field.get("nestedObjectKeyFieldIsRequired"));

                    Map<String, Object> nestedProperties = new LinkedHashMap<>();
                    nestedProperties.put(nestedKey, Map.of("type", "string"));

                    propSchema.put("properties", nestedProperties);
                    if (nestedRequired) {
                        propSchema.put("required", List.of(nestedKey));
                    }
                }

                // Check for full nested object form
                if (field.containsKey("nestedObjectForm")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> nestedForm = (List<Map<String, Object>>) field.get("nestedObjectForm");

                    Map<String, Object> nestedSchema = buildOpenApiRequestBody(nestedForm);
                    Map<String, Object> nestedObjectProperties = extractPropertiesFromSchema(nestedSchema,
                            "properties");
                    if (!nestedObjectProperties.isEmpty()) {
                        propSchema.put("properties", nestedObjectProperties);
                    }
                    Map<String, Object> nestedObjectRequired = extractPropertiesFromSchema(nestedSchema,
                            "required");
                    if (!nestedObjectProperties.isEmpty()) {
                        propSchema.put("required", nestedObjectRequired.get("array"));
                    }

                }
            }

            properties.put(name, propSchema);
            if (required) {
                requiredFields.add(name);
            }
        }

        schema.put("properties", properties);
        if (!requiredFields.isEmpty()) {
            schema.put("required", requiredFields);
        }

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("application/json", Map.of("schema", schema));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("required", true);
        requestBody.put("content", content);

        return requestBody;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> extractPropertiesFromSchema(Map<String, Object> nestedSchema, String propName) {
        if (nestedSchema == null)
            return Collections.emptyMap();

        Object content = nestedSchema.get("content");
        if (!(content instanceof Map))
            return Collections.emptyMap();

        Object appJson = ((Map<?, ?>) content).get("application/json");
        if (!(appJson instanceof Map))
            return Collections.emptyMap();

        Object schema = ((Map<?, ?>) appJson).get("schema");
        if (!(schema instanceof Map))
            return Collections.emptyMap();

        Object properties = ((Map<?, ?>) schema).get(propName);
        if (properties instanceof Map) {
            return (Map<String, Object>) properties;
        }
        if (properties instanceof ArrayList) {
            return Map.of("array", properties);
        }

        return Collections.emptyMap();
    }
}
