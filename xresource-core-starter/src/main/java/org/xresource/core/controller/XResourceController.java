package org.xresource.core.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.xresource.core.actions.XActionExecutor;
import org.xresource.core.annotation.XAction;
import org.xresource.core.annotation.XQuery;
import org.xresource.core.exception.ResourceNotFoundException;
import org.xresource.core.model.ForeignKeyTree;
import org.xresource.core.model.XFieldMetadata;
import org.xresource.core.model.XResourceMetadata;
import org.xresource.core.query.XQueryContextProvider;
import org.xresource.core.query.XQueryExecutor;
import org.xresource.core.registry.XResourceMetadataRegistry;
import org.xresource.core.service.XResourceService;
import org.xresource.core.util.ForeignKeyParser;
import org.xresource.core.util.XResourceLogger;
import org.xresource.core.util.XURNEncoder;
import org.xresource.core.util.XURNEncoder.URN;
import org.xresource.core.auth.XAccessManager;
import org.xresource.core.validation.ValidationContext;

import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${xresource.api.base-path:/api/resource}")
public class XResourceController {

    @Autowired
    private XResourceService service;

    @Autowired
    private XResourceMetadataRegistry registry;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private XAccessManager xAccessManager;

    @Autowired
    XQueryExecutor queryExecutor;

    @Autowired
    XQueryContextProvider contextProvider;

    @Autowired
    XActionExecutor xActionExecutor;

    @GetMapping("/{resourceName}")
    public ResponseEntity<?> findAll(
            @PathVariable String resourceName,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String foreignKeys,
            HttpServletRequest request) {
        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xAccessManager.checkReadAccess(roles, metadata, resourceName);

        boolean isPaged = page != null || size != null || sortBy != null || direction != null;
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();
        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();

        if (isPaged) {
            int pg = page != null ? page : 0;
            int sz = size != null ? size : 10;
            String sortField = sortBy != null ? sortBy : null;
            String dir = direction != null ? direction : "asc";

            Page<Object> paged = service.findPaged(resourceName, pg, sz, sortField, dir);
            List<ObjectNode> results = paged.getContent().stream()
                    .map(entity -> {
                        ObjectNode node = fkTree.isEmpty()
                                ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                                : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
                        injectPermalink(node, entity, resourceName, metadata, baseUrl);
                        return node;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("data", results);
            response.put("page", pg);
            response.put("size", sz);
            response.put("totalElements", paged.getTotalElements());
            response.put("totalPages", paged.getTotalPages());

            XResourceLogger.debug("Paged fetch: {} records from {}", results.size(), resourceName);
            return ResponseEntity.ok(response);
        } else {
            List<Object> all = service.findAll(resourceName);
            List<ObjectNode> results = all.stream()
                    .map(entity -> {
                        ObjectNode node = fkTree.isEmpty()
                                ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                                : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
                        injectPermalink(node, entity, resourceName, metadata, baseUrl);
                        return node;
                    })
                    .collect(Collectors.toList());

            XResourceLogger.debug("Unpaged fetch: {} records from {}", results.size(), resourceName);
            return ResponseEntity.ok(Map.of("data", results));
        }
    }

    @GetMapping("/{resourceName}/query/{queryName}")
    public ResponseEntity<?> executeXQuery(
            @PathVariable String resourceName,
            @PathVariable String queryName,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String foreignKeys,
            @RequestParam(required = false, name = "queryFilters") String queryFiltersJson,
            HttpServletRequest request) {
        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xAccessManager.checkReadAccess(roles, metadata, resourceName);

        boolean isPaged = page != null || size != null || sortBy != null || direction != null;
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();

        Map<String, String> dynamicFilters = parseDynamicFilters(queryFiltersJson); // utility method
        Map<String, Object> userContext = service.getCurrentUserContext();
        Map<String, Object> context = contextProvider.buildContext(userContext, dynamicFilters);

        XQuery xQuery = metadata.getXQuery(queryName)
                .orElseThrow(() -> new RuntimeException("Query not found: " + queryName));

        if (isPaged) {
            int pg = page != null ? page : 0;
            int sz = size != null ? size : 10;
            String sortField = sortBy != null ? sortBy : null;
            String dir = direction != null ? direction : "asc";

            Page<?> paged = queryExecutor.executePagedQuery(metadata.getEntityClass(), xQuery, context, pg, sz,
                    sortField, dir);
            List<ObjectNode> results = paged.getContent().stream()
                    .map(entity -> {
                        ObjectNode node = fkTree.isEmpty()
                                ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                                : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
                        injectPermalink(node, entity, resourceName, metadata, baseUrl);
                        return node;
                    }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "data", results,
                    "page", pg,
                    "size", sz,
                    "totalElements", paged.getTotalElements(),
                    "totalPages", paged.getTotalPages()));
        } else {
            List<?> all = queryExecutor.executeQuery(metadata.getEntityClass(), xQuery, context);
            List<ObjectNode> results = all.stream()
                    .map(entity -> {
                        ObjectNode node = fkTree.isEmpty()
                                ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                                : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
                        injectPermalink(node, entity, resourceName, metadata, baseUrl);
                        return node;
                    }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("data", results));
        }
    }

    @GetMapping("/jsonform/{resourceName}")
    public ResponseEntity<?> generateJSONForm(
            @PathVariable String resourceName,
            @RequestParam(required = false) String fieldsCsv,
            HttpServletRequest request) {
        // Extract roles and metadata
        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xAccessManager.checkWriteAccess(roles, metadata, resourceName);

        String requiredFields = metadata.getFields().values().stream()
                .filter(XFieldMetadata::partOfJSONForm)
                .map(XFieldMetadata::getName)
                .collect(Collectors.joining(","));

        if (metadata.hasCompositeKey()) {
            requiredFields += "," + metadata.getPrimaryKey();
        }

        String fields = fieldsCsv != null ? fieldsCsv : requiredFields;
        // Parse fields from CSV
        String[] fieldNames = fields.split(",");

        Map<String, XFieldMetadata> fieldsMeta = metadata.getFields();
        Map<String, XFieldMetadata> embeddedIdClassFieldsMeta = metadata.getEmbeddedIdClassFields();

        // Build form field metadata
        List<Map<String, Object>> formFields = Arrays.stream(fieldNames)
                .filter(fieldName -> {
                    return fieldsMeta.containsKey(fieldName) || embeddedIdClassFieldsMeta.containsKey(fieldName);
                })
                .map(fieldName -> {
                    XFieldMetadata meta = fieldsMeta.get(fieldName);
                    if (meta == null) {
                        meta = embeddedIdClassFieldsMeta.get(fieldName);
                    }
                    Map<String, Object> fieldData = new HashMap<>();
                    fieldData.put("name", meta.getName());
                    fieldData.put("label", meta.getLabel());
                    fieldData.put("type", meta.getType());
                    fieldData.put("required", meta.isRequired());
                    fieldData.put("format", meta.getFormat());
                    fieldData.put("defaultValue", meta.getDefaultValue());
                    fieldData.put("description", meta.getDescription());
                    fieldData.put("enumValues", meta.getEnumValues());
                    fieldData.put("displayseq", meta.getDisplaySeq());
                    if (!metadata.getXJSONFormValidatorsForField(meta.getName()).isEmpty()) {
                        fieldData.put("validators", metadata.getXJSONFormValidatorsForField(meta.getName()).get());
                    }
                    return fieldData;
                })
                .collect(Collectors.toList());

        // Return as JSON
        return ResponseEntity.ok(Map.of("form", formFields));
    }

    @GetMapping("/{resourceName}/{id}")
    public ResponseEntity<?> findById(@PathVariable String resourceName, @PathVariable Object id,
            @RequestParam(required = false) String foreignKeys, HttpServletRequest request) throws URISyntaxException {
        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xAccessManager.checkReadAccess(roles, metadata, resourceName);
        URN urn = XURNEncoder.decode(id.toString());

        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();

        Map<String, String> keyMap = urn.getKeyMap();

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        if (metadata.hasCompositeKey()) {
            // Add the logic here
            Object entity = service.findByCompositeKey(resourceName, keyMap)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Resource " + resourceName + " with key " + keyMap + " not found"));

            ObjectNode filtered = fkTree.isEmpty()
                    ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                    : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
            injectPermalink(filtered, entity, resourceName, metadata, baseUrl);
            return ResponseEntity.ok(filtered);

        } else {
            Object entity = service.findById(resourceName, keyMap.get("id"))
                    .orElseThrow(() -> new ResourceNotFoundException(resourceName + " with ID " + id + " not found"));
            ObjectNode filtered = fkTree.isEmpty()
                    ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                    : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
            injectPermalink(filtered, entity, resourceName, metadata, baseUrl);
            return ResponseEntity.ok(filtered);
        }

    }

    @GetMapping("/{resourceName}/{id}/{field}")
    public ResponseEntity<?> getFieldFromResourceById(@PathVariable String resourceName, @PathVariable String field,
            @PathVariable Object id, @RequestParam(required = false) String foreignKeys, HttpServletRequest request)
            throws URISyntaxException {
        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xAccessManager.checkReadAccess(roles, metadata, resourceName);
        URN urn = XURNEncoder.decode(id.toString());

        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();

        Map<String, String> keyMap = urn.getKeyMap();

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        if (metadata.hasCompositeKey()) {
            // Add the logic here
            Object entity = service.findByCompositeKey(resourceName, keyMap)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Resource " + resourceName + " with key " + keyMap + " not found"));

            Field requestedField = this.findField(entity.getClass(), field);

            if (requestedField == null) {
                throw new ResourceNotFoundException(
                        field + " is not found inside " + resourceName + " with ID " + id + ".");
            }
            requestedField.setAccessible(true);

            Object fieldValue;
            try {
                fieldValue = requestedField.get(entity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String fieldResourceName = null;
            if (fieldValue instanceof HibernateProxy) {
                Hibernate.initialize(fieldValue);
                fieldValue = ((HibernateProxy) fieldValue).getHibernateLazyInitializer().getImplementation();
                Table table = fieldValue.getClass().getAnnotation(Table.class);

                if (table != null && !table.name().isEmpty()) {
                    fieldResourceName = table.name();
                }
            }

            if (fieldValue instanceof List<?>) {
                List<?> list = (List<?>) fieldValue;

                if (!list.isEmpty()) {
                    Object firstElement = list.get(0);
                    Class<?> elementClass = Hibernate.unproxy(firstElement).getClass();

                    Table tableAnnotation = elementClass.getAnnotation(Table.class);
                    if (tableAnnotation != null) {
                        fieldResourceName = tableAnnotation.name();
                    }
                }
            }

            if (fieldValue == null || fieldResourceName == null)
                throw new ResourceNotFoundException(
                        field + " is not found inside " + resourceName + " with ID " + id + ".");

            XResourceMetadata nestedMeta = registry.get(fieldResourceName);

            if (fieldValue instanceof List<?>) {
                List<?> list = (List<?>) fieldValue;
                ArrayNode nestedArray = objectMapper.createArrayNode();
                for (Object element : list) {
                    ObjectNode itemNode = fkTree.isEmpty()
                            ? xAccessManager.filterFieldsByAccess(element, nestedMeta, roles, baseUrl)
                            : xAccessManager.filterFieldsByAccess(element, nestedMeta, roles, fkTree, baseUrl);
                    injectPermalink(itemNode, element, fieldResourceName, nestedMeta, baseUrl);
                    nestedArray.add(itemNode);
                }
                return ResponseEntity.ok(nestedArray);
            } else {
                ObjectNode filtered = fkTree.isEmpty()
                        ? xAccessManager.filterFieldsByAccess(fieldValue, nestedMeta, roles, baseUrl)
                        : xAccessManager.filterFieldsByAccess(fieldValue, nestedMeta, roles, fkTree, baseUrl);
                injectPermalink(filtered, fieldValue, fieldResourceName, nestedMeta, baseUrl);
                return ResponseEntity.ok(filtered);
            }

        } else {
            Object entity = service.findById(resourceName, keyMap.get("id"))
                    .orElseThrow(() -> new ResourceNotFoundException(resourceName + " with ID " + id + " not found"));
            Field requestedField = this.findField(entity.getClass(), field);

            if (requestedField == null) {
                throw new ResourceNotFoundException(
                        field + " is not found inside " + resourceName + " with ID " + id + ".");
            }
            requestedField.setAccessible(true);
            Object fieldValue;
            try {
                fieldValue = requestedField.get(entity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String fieldResourceName = null;
            if (fieldValue instanceof HibernateProxy) {
                Hibernate.initialize(fieldValue);
                fieldValue = ((HibernateProxy) fieldValue).getHibernateLazyInitializer().getImplementation();
                Table table = fieldValue.getClass().getAnnotation(Table.class);

                if (table != null && !table.name().isEmpty()) {
                    fieldResourceName = table.name();
                }
            }

            if (fieldValue instanceof List<?>) {
                List<?> list = (List<?>) fieldValue;

                if (!list.isEmpty()) {
                    Object firstElement = list.get(0);
                    Class<?> elementClass = Hibernate.unproxy(firstElement).getClass();

                    Table tableAnnotation = elementClass.getAnnotation(Table.class);
                    if (tableAnnotation != null) {
                        fieldResourceName = tableAnnotation.name();
                    }
                }
            }

            if (fieldValue == null || fieldResourceName == null)
                throw new ResourceNotFoundException(
                        field + " is not found inside " + resourceName + " with ID " + id + ".");

            XResourceMetadata nestedMeta = registry.get(fieldResourceName);

            if (fieldValue instanceof List<?>) {
                List<?> list = (List<?>) fieldValue;
                ArrayNode nestedArray = objectMapper.createArrayNode();
                for (Object element : list) {
                    ObjectNode itemNode = fkTree.isEmpty()
                            ? xAccessManager.filterFieldsByAccess(element, nestedMeta, roles, baseUrl)
                            : xAccessManager.filterFieldsByAccess(element, nestedMeta, roles, fkTree, baseUrl);
                    injectPermalink(itemNode, element, fieldResourceName, nestedMeta, baseUrl);
                    nestedArray.add(itemNode);
                }
                return ResponseEntity.ok(nestedArray);
            } else {
                ObjectNode filtered = fkTree.isEmpty()
                        ? xAccessManager.filterFieldsByAccess(fieldValue, nestedMeta, roles, baseUrl)
                        : xAccessManager.filterFieldsByAccess(fieldValue, nestedMeta, roles, fkTree, baseUrl);
                injectPermalink(filtered, fieldValue, fieldResourceName, nestedMeta, baseUrl);
                return ResponseEntity.ok(filtered);
            }
        }

    }

    @PostMapping("/{resourceName}")
    public ResponseEntity<?> create(@PathVariable String resourceName, @RequestBody String body,
            HttpServletRequest request) throws Exception {
        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xAccessManager.checkWriteAccess(roles, metadata, resourceName);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        Object entity = service.convertJsonToEntity(resourceName, body);
        xAccessManager.validateEntity(entity, ValidationContext.OperationType.CREATE);

        Object saved = service.create(resourceName, entity, roles);

        ObjectNode filtered = xAccessManager.filterFieldsByAccess(saved, metadata, roles, baseUrl);
        injectPermalink(filtered, saved, resourceName, metadata, baseUrl);

        return ResponseEntity.ok(filtered);
    }

    @PostMapping("/composite")
    public ResponseEntity<?> createComposite(@RequestBody JsonNode requestBody, HttpServletRequest request)
            throws Exception {
        List<String> roles = getCurrentUserRoles();
        String resourceName = requestBody.get("resource").asText();

        xAccessManager.checkWriteAccess(roles, registry.get(resourceName), resourceName);

        Object saved = service.saveWithChildren(resourceName, requestBody, roles);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        ObjectNode filtered = xAccessManager.filterFieldsByAccess(
                saved, registry.get(resourceName), roles, baseUrl);

        injectPermalink(filtered, saved, resourceName, registry.get(resourceName), baseUrl);

        return ResponseEntity.ok(filtered);
    }

    @PutMapping("/{resourceName}/{id}")
    public ResponseEntity<?> update(@PathVariable String resourceName, @PathVariable Object id,
            @RequestBody String body, @RequestParam(required = false) String foreignKeys, HttpServletRequest request)
            throws Exception {
        List<String> roles = getCurrentUserRoles();
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        XResourceMetadata metadata = registry.get(resourceName);
        xAccessManager.checkWriteAccess(roles, metadata, resourceName);
        URN urn = XURNEncoder.decode(id.toString());

        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();
        Map<String, String> keyMap = urn.getKeyMap();

        if (metadata.hasCompositeKey()) {

            Object entity = service.findByCompositeKey(resourceName, keyMap)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Resource " + resourceName + " with key " + keyMap + " not found"));
            // Got the existing entity from db
            Object inputEntity = service.convertJsonToEntity(resourceName, body);
            xAccessManager.validateEntity(inputEntity, ValidationContext.OperationType.UPDATE);

            Object updated = service.update(resourceName, entity, inputEntity, roles);

            ObjectNode filtered = fkTree.isEmpty()
                    ? xAccessManager.filterFieldsByAccess(updated, metadata, roles, baseUrl)
                    : xAccessManager.filterFieldsByAccess(updated, metadata, roles, fkTree, baseUrl);
            injectPermalink(filtered, updated, resourceName, metadata, baseUrl);

            return ResponseEntity.ok(filtered);

        } else {
            Object entity = service.findById(resourceName, keyMap.get("id"))
                    .orElseThrow(() -> new ResourceNotFoundException(resourceName + " with ID " + id + " not found"));
            // Got the existing entity from db
            Object inputEntity = service.convertJsonToEntity(resourceName, body);
            xAccessManager.validateEntity(inputEntity, ValidationContext.OperationType.UPDATE);

            Object updated = service.update(resourceName, entity, inputEntity, roles);

            ObjectNode filtered = fkTree.isEmpty()
                    ? xAccessManager.filterFieldsByAccess(updated, metadata, roles, baseUrl)
                    : xAccessManager.filterFieldsByAccess(updated, metadata, roles, fkTree, baseUrl);
            injectPermalink(filtered, updated, resourceName, metadata, baseUrl);

            return ResponseEntity.ok(filtered);

        }

    }

    @DeleteMapping("/{resourceName}/{id}")
    public ResponseEntity<?> delete(@PathVariable String resourceName, @PathVariable Object id)
            throws URISyntaxException {
        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xAccessManager.checkWriteAccess(roles, metadata, resourceName);

        URN urn = XURNEncoder.decode(id.toString());

        Map<String, String> keyMap = urn.getKeyMap();

        if (metadata.hasCompositeKey()) {

            Object entity = service.findByCompositeKey(resourceName, keyMap)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Resource " + resourceName + " with key " + keyMap + " not found"));

            service.deleteByEntity(resourceName, entity, roles);

            return ResponseEntity.ok().build();

        } else {
            Object entity = service.findById(resourceName, keyMap.get("id"))
                    .orElseThrow(() -> new ResourceNotFoundException(resourceName + " with ID " + id + " not found"));

            service.deleteByEntity(resourceName, entity, roles);

            return ResponseEntity.ok().build();

        }

    }

    // Action Handlers
    @DeleteMapping("/{resourceName}/{id}/actions/{action}")
    public ResponseEntity<?> handleDeleteAction(@PathVariable String resourceName, @PathVariable String action,
            @PathVariable Object id, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {

        return this.processAction(resourceName, action, id, request, response);
    }

    @PutMapping("/{resourceName}/{id}/actions/{action}")
    public ResponseEntity<?> handlePutAction(@PathVariable String resourceName, @PathVariable String action,
            @PathVariable Object id, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {

        return this.processAction(resourceName, action, id, request, response);
    }

    @PostMapping("/{resourceName}/{id}/actions/{action}")
    public ResponseEntity<?> handlePostAction(@PathVariable String resourceName, @PathVariable String action,
            @PathVariable Object id, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {

        return this.processAction(resourceName, action, id, request, response);
    }

    @GetMapping("/{resourceName}/{id}/actions/{action}")
    public ResponseEntity<?> handleGetAction(@PathVariable String resourceName, @PathVariable String action,
            @PathVariable Object id, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {

        return this.processAction(resourceName, action, id, request, response);
    }

    private ResponseEntity<?> processAction(String resourceName, String action, Object id, HttpServletRequest request,
            HttpServletResponse response) throws URISyntaxException {
        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xAccessManager.checkReadAccess(roles, metadata, resourceName);
        URN urn = XURNEncoder.decode(id.toString());

        Map<String, String> keyMap = urn.getKeyMap();

        if (metadata.hasCompositeKey()) {
            // Add the logic here
            Object entity = service.findByCompositeKey(resourceName, keyMap)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Resource " + resourceName + " with key " + keyMap + " not found"));

            Optional<XAction> xAction = metadata.getXAction(action);

            if (xAction.isEmpty()) {
                throw new ResourceNotFoundException(
                        action + " is not found for " + resourceName + " with ID " + id + ".");
            }
            Optional<ResponseEntity<?>> result = xActionExecutor.excute(xAction.get(), entity, request, response);
            if (result.isEmpty()) {
                throw new ResourceNotFoundException("unknow error occured.");
            }
            return result.get();

        } else {
            Object entity = service.findById(resourceName, keyMap.get("id"))
                    .orElseThrow(() -> new ResourceNotFoundException(resourceName + " with ID " + id + " not found"));
            Optional<XAction> xAction = metadata.getXAction(action);

            if (xAction.isEmpty()) {
                throw new ResourceNotFoundException(
                        action + " is not found for " + resourceName + " with ID " + id + ".");
            }
            Optional<ResponseEntity<?>> result = xActionExecutor.excute(xAction.get(), entity, request, response);
            if (result.isEmpty()) {
                throw new ResourceNotFoundException("unknow error occured.");
            }
            return result.get();
        }
    }

    private List<String> getCurrentUserRoles() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void injectPermalink(ObjectNode node, Object entity, String resourceName, XResourceMetadata metadata,
            String baseUrl) {
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

            String permalink = baseUrl + "/api/resource/" + resourceName + "/" + urn;
            node.put("permalink", permalink);
            node.put("urn", urn);

        } catch (Exception ex) {
            // Log and continue — do not block response
            ex.printStackTrace();
            System.err.println("Failed to generate permalink for " + resourceName + ": " + ex.getMessage());
            node.put("permalink", (String) null);
            node.put("urn", (String) null);
        }
    }

    /**
     * Safely finds a declared field from a class or its superclasses.
     */
    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private Object getFieldValue(Object entity, String fieldName) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access field '" + fieldName + "' in entity", e);
        }
    }

    private Map<String, String> parseDynamicFilters(String json) {
        if (json == null || json.isBlank())
            return Collections.emptyMap();
        try {
            return new ObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Invalid dynamicFilters format", e);
        }
    }

    /**
     * Main entry for api/resource
     */
    @GetMapping
    public ResponseEntity<?> getXResourceOverview() {
        Map<String, Object> response = new LinkedHashMap<>();

        // 1. List all registered XResources
        Map<String, Object> resourcesInfo = new LinkedHashMap<>();
        registry.getRegistry().forEach((resourceName, metadata) -> {
            Map<String, Object> resourceMeta = new LinkedHashMap<>();
            resourceMeta.put("entityClass", metadata.getEntityClass().getName());
            resourceMeta.put("fields", metadata.getFields().keySet());
            List<Map<String, Object>> queries = metadata.getXQueriesMap().values().stream()
                    .map(q -> Map.of(
                            "name", q.name(),
                            "contextParams", Arrays.asList(q.contextParams())))
                    .toList();

            List<?> actions = metadata.getXActionsMap().values().stream()
                    .map(a -> Map.of(
                            "name", a.name(),
                            "type", a.type()))
                    .toList();
            resourceMeta.put("queries", queries);
            resourceMeta.put("actions", actions);
            resourcesInfo.put(resourceName, resourceMeta);
        });

        // 2. JVM/System info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemInfo = new LinkedHashMap<>();
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        systemInfo.put("maxMemory", runtime.maxMemory());
        systemInfo.put("totalMemory", runtime.totalMemory());
        systemInfo.put("freeMemory", runtime.freeMemory());
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("jvmName", System.getProperty("java.vm.name"));
        systemInfo.put("uptime", getUptime());

        // 3. Construct full response
        response.put("resources", resourcesInfo);
        response.put("systemInfo", systemInfo);

        return ResponseEntity.ok(response);
    }

    private String getUptime() {
        try {
            long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
            return Duration.ofMillis(uptimeMs).toString();
        } catch (Exception e) {
            return "N/A";
        }
    }
}
