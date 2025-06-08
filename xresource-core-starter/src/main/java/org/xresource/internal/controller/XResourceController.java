package org.xresource.internal.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.v3.oas.annotations.Hidden;

import org.xresource.core.annotations.XAction;
import org.xresource.core.annotations.XFieldAction;
import org.xresource.core.annotations.XQuery;
import org.xresource.core.logging.XLogger;
import org.xresource.internal.query.XQueryContextProvider;
import org.xresource.internal.query.XQueryExecutor;
import org.xresource.core.response.XResponseTranformersRegistry;
import org.xresource.core.response.XResponseTransformer;
import org.xresource.core.service.XResourceService;
import org.xresource.core.util.XUtils;
import org.xresource.core.validation.ValidationContext;
import org.xresource.internal.actions.XActionExecutor;
import org.xresource.internal.auth.XAccessManager;
import org.xresource.internal.auth.XRoleBasedAccessEvaluator;
import org.xresource.internal.exception.ResourceNotFoundException;
import org.xresource.internal.exception.XResourceException;
import org.xresource.internal.intent.core.dsl.IntentDslCompiler;
import org.xresource.internal.intent.core.parser.model.IntentMeta;
import org.xresource.internal.intent.core.xml.XmlIntentParser;
import org.xresource.internal.models.ForeignKeyTree;
import org.xresource.internal.models.XFieldMetadata;
import org.xresource.internal.models.XResourceMetadata;
import org.xresource.internal.registry.XResourceMetadataRegistry;
import org.xresource.internal.util.ForeignKeyParser;
import org.xresource.internal.util.XResourceLinkResolver;
import org.xresource.internal.util.XURNEncoder;
import org.xresource.internal.util.XURNEncoder.URN;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import static org.xresource.internal.config.XResourceConfigProperties.API_BASE_PATH;

import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * REST controller responsible for handling dynamic XResource-based CRUD
 * operations and metadata-driven queries.
 *
 * <p>
 * This controller supports:
 * </p>
 * <ul>
 * <li>Paginated and non-paginated data retrieval</li>
 * <li>Execution of metadata-defined custom queries</li>
 * <li>Dynamic generation of JSON-based forms derived from metadata</li>
 * </ul>
 *
 * <p>
 * All operations enforce field-level access control and support foreign key
 * tree resolution.
 * </p>
 */
@RestController
@RequestMapping(API_BASE_PATH)
@Hidden
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
    private XQueryExecutor queryExecutor;

    @Autowired
    private XQueryContextProvider contextProvider;

    @Autowired
    private XActionExecutor xActionExecutor;

    @Autowired
    private XRoleBasedAccessEvaluator xRoleBasedAccessEvaluator;

    @Autowired
    private XResponseTranformersRegistry xResponseTranformersRegistry;

    @Autowired
    private XResourceLinkResolver linkResolver;

    private static final XLogger log = XLogger.forClass(XResourceController.class);

    /**
     * Retrieves all records for the specified resource.
     *
     * <p>
     * This endpoint supports optional pagination, sorting, and foreign key
     * traversal.
     * Access is restricted based on user roles and field-level permissions.
     * </p>
     *
     * @param resourceName the name of the resource to query
     * @param page         optional page number for pagination
     * @param size         optional page size for pagination
     * @param sortBy       optional field name to sort by
     * @param direction    optional sort direction ("asc" or "desc")
     * @param foreignKeys  optional foreign key traversal instructions
     * @param request      the current HTTP servlet request
     * @return a paginated or complete list of records for the resource, filtered by
     *         access
     */
    @GetMapping("/{resourceName}")
    public ResponseEntity<?> findAll(
            @PathVariable String resourceName,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String foreignKeys,
            HttpServletRequest request) {

        log.info(
                "Fetching resource: %s with pagination params page=%s, size=%s, sortBy=%s, direction=%s and foreignKeys=%s",
                resourceName, page, size, sortBy, direction, foreignKeys);

        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        log.debug("Loaded metadata for resource: %s", resourceName);

        xRoleBasedAccessEvaluator.checkReadAccess(roles, metadata, resourceName);
        log.trace("Access granted to roles: %s for read on resource: %s", roles, resourceName);

        boolean isPaged = page != null || size != null || sortBy != null || direction != null;
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();
        log.debug("Parsed foreign key tree: %s", fkTree);

        if (isPaged) {
            int pg = page != null ? page : 0;
            int sz = size != null ? size : 10;
            String sortField = sortBy != null ? sortBy : null;
            String dir = direction != null ? direction : "asc";

            log.debug("Executing paged query for resource: %s page=%s, size=%s, sort=%s, direction=%s...", resourceName,
                    pg, sz, sortField, dir);

            Page<Object> paged = service.findPaged(resourceName, pg, sz, sortField, dir);
            List<ObjectNode> results = paged.getContent().stream()
                    .map(entity -> {
                        ObjectNode node = fkTree.isEmpty()
                                ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                                : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
                        linkResolver.injectPermalink(node, entity, resourceName, metadata, baseUrl);
                        return node;
                    }).collect(Collectors.toList());

            log.info("Paged result: %s records fetched from resource: %s", results.size(), resourceName);

            // Handle response transformer
            if (xResponseTranformersRegistry.getFindAllTransformer() != null) {
                results = applyFindAllTransformer(results, resourceName,
                        xResponseTranformersRegistry.getFindAllTransformer());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", results);
            response.put("page", pg);
            response.put("size", sz);
            response.put("totalElements", paged.getTotalElements());
            response.put("totalPages", paged.getTotalPages());

            return ResponseEntity.ok(response);
        } else {
            log.debug("Executing unpaged query for resource: %s", resourceName);
            List<Object> all = service.findAll(resourceName);
            List<ObjectNode> results = all.stream()
                    .map(entity -> {
                        ObjectNode node = fkTree.isEmpty()
                                ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                                : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
                        linkResolver.injectPermalink(node, entity, resourceName, metadata, baseUrl);
                        return node;
                    }).collect(Collectors.toList());

            log.info("Unpaged result: %s records fetched from resource: %s", results.size(), resourceName);

            // Handle response transformer
            if (xResponseTranformersRegistry.getFindAllTransformer() != null) {
                results = applyFindAllTransformer(results, resourceName,
                        xResponseTranformersRegistry.getFindAllTransformer());
            }

            return ResponseEntity.ok(Map.of("data", results));
        }
    }

    /**
     * Executes a named query defined in the metadata for the specified resource.
     *
     * <p>
     * Supports dynamic filter parameters, pagination, sorting, and foreign key
     * resolution.
     * The query is executed in a secure context respecting the user's roles and
     * field-level visibility.
     * </p>
     *
     * @param resourceName     the name of the resource
     * @param queryName        the name of the query to execute
     * @param page             optional page number for pagination
     * @param size             optional page size for pagination
     * @param sortBy           optional field to sort by
     * @param direction        optional sort direction ("asc" or "desc")
     * @param foreignKeys      optional foreign key traversal instructions
     * @param queryFiltersJson optional JSON string containing dynamic query filters
     * @param request          the current HTTP servlet request
     * @return the result set of the executed query, formatted as a list of JSON
     *         objects
     */
    @GetMapping("/{resourceName}/query/{queryName}")
    public ResponseEntity<?> executeXQuery(
            @PathVariable String resourceName,
            @PathVariable String queryName,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String foreignKeys,
            @RequestParam(required = false, name = "xQueryParams") String xQueryParamsJson,
            HttpServletRequest request) {

        log.info("Executing XQuery '%s' on resource: %s", queryName, resourceName);

        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xRoleBasedAccessEvaluator.checkReadAccess(roles, metadata, resourceName);
        log.trace("Access verified for query '%s', roles: %s", queryName, roles);

        boolean isPaged = page != null || size != null || sortBy != null || direction != null;
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();
        Map<String, String> xQueryParams = parseXQueryParams(xQueryParamsJson);
        Map<String, Object> userContext = service.getCurrentUserContext();
        Map<String, Object> context = contextProvider.buildContext(userContext, xQueryParams);

        XQuery xQuery = metadata.getXQuery(queryName)
                .orElseThrow(() -> new RuntimeException("Query not found: " + queryName));

        if (isPaged) {
            int pg = page != null ? page : 0;
            int sz = size != null ? size : 10;
            String sortField = sortBy != null ? sortBy : null;
            String dir = direction != null ? direction : "asc";

            log.debug("Running paged query '%s' with page=%s, size=%s, sort=%s, dir=%s", queryName, pg, sz, sortField,
                    dir);

            Page<?> paged = queryExecutor.executePagedQuery(metadata.getEntityClass(), xQuery, context, pg, sz,
                    sortField, dir);
            List<ObjectNode> results = paged.getContent().stream()
                    .map(entity -> {
                        ObjectNode node = fkTree.isEmpty()
                                ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                                : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
                        linkResolver.injectPermalink(node, entity, resourceName, metadata, baseUrl);
                        return node;
                    }).collect(Collectors.toList());

            // Handle response transformer
            if (xResponseTranformersRegistry.getNamedQueryTransformer() != null) {
                results = applyFindAllTransformer(results, resourceName,
                        xResponseTranformersRegistry.getNamedQueryTransformer());
            }
            return ResponseEntity.ok(Map.of(
                    "data", results,
                    "page", pg,
                    "size", sz,
                    "totalElements", paged.getTotalElements(),
                    "totalPages", paged.getTotalPages()));
        } else {
            log.debug("Running unpaged query '%s'", queryName);
            List<?> all = queryExecutor.executeQuery(metadata.getEntityClass(), xQuery, context);
            List<ObjectNode> results = all.stream()
                    .map(entity -> {
                        ObjectNode node = fkTree.isEmpty()
                                ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                                : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
                        linkResolver.injectPermalink(node, entity, resourceName, metadata, baseUrl);
                        return node;
                    }).collect(Collectors.toList());
            // Handle response transformer
            if (xResponseTranformersRegistry.getNamedQueryTransformer() != null) {
                results = applyFindAllTransformer(results, resourceName,
                        xResponseTranformersRegistry.getNamedQueryTransformer());
            }
            return ResponseEntity.ok(Map.of("data", results));
        }
    }

    /**
     * Executes a named query defined in the metadata for the specified resource.
     *
     * <p>
     * Supports dynamic filter parameters, pagination, sorting, and foreign key
     * resolution.
     * The query is executed in a secure context respecting the user's roles and
     * field-level visibility.
     * </p>
     *
     * @param resourceName     the name of the resource
     * @param queryName        the name of the query to execute
     * @param page             optional page number for pagination
     * @param size             optional page size for pagination
     * @param sortBy           optional field to sort by
     * @param direction        optional sort direction ("asc" or "desc")
     * @param foreignKeys      optional foreign key traversal instructions
     * @param queryFiltersJson optional JSON string containing dynamic query filters
     * @param request          the current HTTP servlet request
     * @return the result set of the executed query, formatted as a list of JSON
     *         objects
     */
    @GetMapping("/{resourceName}/intents/{intentName}")
    public ResponseEntity<?> executeXIntent(
            @PathVariable String resourceName,
            @PathVariable String intentName,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String foreignKeys,
            @RequestParam(required = false, name = "xIntentParams") String xQueryParamsJson,
            HttpServletRequest request) {

        log.info("Executing Intent '%s' on resource: %s", intentName, resourceName);

        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xRoleBasedAccessEvaluator.checkReadAccess(roles, metadata, resourceName);
        log.trace("Access verified for query '%s', roles: %s", intentName, roles);

        boolean isPaged = page != null || size != null || sortBy != null || direction != null;
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();
        Map<String, String> xQueryParams = parseXQueryParams(xQueryParamsJson);
        Map<String, Object> userContext = service.getCurrentUserContext();
        Map<String, Object> context = contextProvider.buildContext(userContext, xQueryParams);

        IntentMeta xIntent = metadata.getXIntent(intentName)
                .orElseThrow(() -> new RuntimeException("Intent not found: " + intentName));

        if (isPaged) {
            int pg = page != null ? page : 0;
            int sz = size != null ? size : 10;
            String sortField = sortBy != null ? sortBy : null;
            String dir = direction != null ? direction : "asc";

            log.debug("Running paged query '%s' with page=%s, size=%s, sort=%s, dir=%s", intentName, pg, sz, sortField,
                    dir);

            Page<?> paged = queryExecutor.executePagedIntent(xIntent, context, pg, sz);
            /*
             * List<ObjectNode> results = paged.getContent().stream()
             * .map(entity -> {
             * ObjectNode node = fkTree.isEmpty()
             * ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
             * : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree,
             * baseUrl);
             * linkResolver.injectPermalink(node, entity, resourceName, metadata, baseUrl);
             * return node;
             * }).collect(Collectors.toList());
             */

            // Handle response transformer

            return ResponseEntity.ok(Map.of(
                    "data", paged.getContent(),
                    "page", pg,
                    "size", sz,
                    "totalElements", paged.getTotalElements(),
                    "totalPages", paged.getTotalPages()));
        } else {
            log.debug("Running unpaged query  for intent '%s'", intentName);
            List<?> all = queryExecutor.executeIntent(metadata.getEntityClass(), xIntent, context);
            /*
             * List<ObjectNode> results = all.stream()
             * .map(entity -> {
             * ObjectNode node = fkTree.isEmpty()
             * ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
             * : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree,
             * baseUrl);
             * linkResolver.injectPermalink(node, entity, resourceName, metadata, baseUrl);
             * return node;
             * }).collect(Collectors.toList());
             */
            // Handle response transformer

            return ResponseEntity.ok(Map.of("data", all));
        }
    }

    /**
     * Executes a named query defined in the metadata for the specified resource.
     *
     * <p>
     * Supports dynamic filter parameters, pagination, sorting, and foreign key
     * resolution.
     * The query is executed in a secure context respecting the user's roles and
     * field-level visibility.
     * </p>
     *
     * @param resourceName     the name of the resource
     * @param queryName        the name of the query to execute
     * @param page             optional page number for pagination
     * @param size             optional page size for pagination
     * @param sortBy           optional field to sort by
     * @param direction        optional sort direction ("asc" or "desc")
     * @param foreignKeys      optional foreign key traversal instructions
     * @param queryFiltersJson optional JSON string containing dynamic query filters
     * @param request          the current HTTP servlet request
     * @return the result set of the executed query, formatted as a list of JSON
     *         objects
     */
    @PostMapping("/{resourceName}/intents")
    public ResponseEntity<?> executeDynamicXIntent(
            @PathVariable String resourceName,
            @RequestBody String reqBody,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String foreignKeys,
            @RequestParam(required = false, name = "xIntentParams") String xQueryParamsJson,
            HttpServletRequest request) {

        if (reqBody == null || reqBody.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body missing"));
        }

        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xRoleBasedAccessEvaluator.checkReadAccess(roles, metadata, resourceName);
        log.trace("Access verified for resource '%s', roles: %s", resourceName, roles);

        boolean isPaged = page != null || size != null || sortBy != null || direction != null;
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();
        Map<String, String> xQueryParams = parseXQueryParams(xQueryParamsJson);
        Map<String, Object> userContext = service.getCurrentUserContext();
        Map<String, Object> context = contextProvider.buildContext(userContext, xQueryParams);

        try {
            IntentDslCompiler compiler = new IntentDslCompiler();
            Element xml;
            try {
                xml = compiler.compile(reqBody);
                log.debug("Intent compiled XML=" + XmlIntentParser.printXmlElement(xml));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error",
                        "unable to compile the requested intent, please make sure you are following proper syntax.",
                        "exception", e.getMessage()));
            }
            IntentMeta imeta;
            try {
                imeta = XmlIntentParser.compile(xml, resourceName, metadata);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error",
                        "unable to compile the requested intent, please make sure you are following proper syntax.",
                        "exception", e.getMessage()));

            }
            if (isPaged) {
                int pg = page != null ? page : 0;
                int sz = size != null ? size : 10;
                String sortField = sortBy != null ? sortBy : null;
                String dir = direction != null ? direction : "asc";

                log.debug("Running paged query with page=%s, size=%s, sort=%s, dir=%s", pg, sz, sortField,
                        dir);

                Page<?> paged = queryExecutor.executePagedIntent(imeta, context, pg, sz);
                /*
                 * List<ObjectNode> results = paged.getContent().stream()
                 * .map(entity -> {
                 * ObjectNode node = fkTree.isEmpty()
                 * ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                 * : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree,
                 * baseUrl);
                 * linkResolver.injectPermalink(node, entity, resourceName, metadata, baseUrl);
                 * return node;
                 * }).collect(Collectors.toList());
                 */

                // Handle response transformer

                return ResponseEntity.ok(Map.of(
                        "data", paged.getContent(),
                        "page", pg,
                        "size", sz,
                        "totalElements", paged.getTotalElements(),
                        "totalPages", paged.getTotalPages()));
            } else {
                log.debug("Running unpaged query  for intent");
                List<?> all = queryExecutor.executeIntent(metadata.getEntityClass(), imeta, context);
                /*
                 * List<ObjectNode> results = all.stream()
                 * .map(entity -> {
                 * ObjectNode node = fkTree.isEmpty()
                 * ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                 * : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree,
                 * baseUrl);
                 * linkResolver.injectPermalink(node, entity, resourceName, metadata, baseUrl);
                 * return node;
                 * }).collect(Collectors.toList());
                 */
                // Handle response transformer

                return ResponseEntity.ok(Map.of("data", all));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "unable to process the requested intent, please make sure you are following proper syntax.",
                    "exception", e.getMessage()));
        }

    }

    @PostMapping("/{resourceName}/intents/xml")
    public ResponseEntity<?> executeDynamicXmlIntent(
            @PathVariable String resourceName,
            @RequestBody String xmlBody,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String foreignKeys,
            @RequestParam(required = false, name = "xIntentParams") String xQueryParamsJson,
            HttpServletRequest request) {

        if (xmlBody == null || xmlBody.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body (XML) is missing"));
        }

        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xRoleBasedAccessEvaluator.checkReadAccess(roles, metadata, resourceName);
        log.trace("Access verified for resource '%s', roles: %s", resourceName, roles);

        boolean isPaged = page != null || size != null || sortBy != null || direction != null;
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();
        Map<String, String> xQueryParams = parseXQueryParams(xQueryParamsJson);
        Map<String, Object> userContext = service.getCurrentUserContext();
        Map<String, Object> context = contextProvider.buildContext(userContext, xQueryParams);

        try {
            // Parse raw XML string to org.w3c.dom.Element
            Element xmlElement;
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(xmlBody)));
                xmlElement = doc.getDocumentElement();
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error",
                        "Failed to parse XML body",
                        "exception", e.getMessage()));
            }

            // Generate IntentMeta from XML
            IntentMeta imeta;
            try {
                imeta = XmlIntentParser.compile(xmlElement, resourceName, metadata);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error",
                        "Unable to compile the XML intent. Check XML structure and schema.",
                        "exception", e.getMessage()));
            }

            // Paged vs Unpaged Execution (same as before)
            if (isPaged) {
                int pg = page != null ? page : 0;
                int sz = size != null ? size : 10;
                Page<?> paged = queryExecutor.executePagedIntent(imeta, context, pg, sz);
                return ResponseEntity.ok(Map.of(
                        "data", paged.getContent(),
                        "page", pg,
                        "size", sz,
                        "totalElements", paged.getTotalElements(),
                        "totalPages", paged.getTotalPages()));
            } else {
                List<?> all = queryExecutor.executeIntent(metadata.getEntityClass(), imeta, context);
                return ResponseEntity.ok(Map.of("data", all));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "Unable to process the requested XML intent.",
                    "exception", e.getMessage()));
        }
    }

    /**
     * Generates a dynamic JSON form schema for a specified resource based on its
     * metadata configuration.
     *
     * <p>
     * This endpoint is useful for front-end form rendering and UI generation.
     * Field inclusion is determined by metadata flags and optional user-specified
     * field lists.
     * </p>
     *
     * @param resourceName the name of the resource
     * @param fieldsCsv    optional comma-separated list of field names to include
     *                     in the form;
     *                     if not provided, all fields marked for form inclusion are
     *                     returned
     * @param request      the current HTTP servlet request
     * @return a structured JSON form schema containing metadata-enriched field
     *         definitions
     */
    @GetMapping("/jsonform/{resourceName}")
    public ResponseEntity<?> generateJSONForm(
            @PathVariable String resourceName,
            @RequestParam(required = false) String fieldsCsv,
            HttpServletRequest request) {

        log.info("Generating JSON form for resource: %s with fieldsCsv: %s", resourceName, fieldsCsv);

        List<Map<String, Object>> formFields = service.generateJsonForm(resourceName, fieldsCsv);

        log.debug("Generated %s form fields for resource: %s", formFields.size(), resourceName);

        // Handle response transformer
        if (xResponseTranformersRegistry.getGetJsonFormTransformer() != null) {
            formFields = applyJsonFormTransformer(
                    formFields, resourceName,
                    xResponseTranformersRegistry.getGetJsonFormTransformer());
        }

        return ResponseEntity.ok(Map.of("form", formFields));
    }

    /**
     * Handles HTTP GET requests to retrieve a specific resource instance by ID.
     * <p>
     * This method supports both simple and composite primary keys by decoding the
     * provided URN-formatted ID. It optionally parses foreign key trees for nested
     * fetches and applies field-level access control based on the current user's
     * roles.
     *
     * @param resourceName The name of the resource (table/entity).
     * @param id           The URN-formatted primary key or simple ID.
     * @param foreignKeys  Optional string describing foreign key relationships to
     *                     be resolved.
     * @param request      The HttpServletRequest, used to determine base URL.
     * @return A filtered JSON object representing the requested entity with links
     *         and
     *         access-controlled fields.
     * @throws URISyntaxException if the given ID cannot be parsed into a valid URN.
     */
    @GetMapping("/{resourceName}/{id}")
    public ResponseEntity<?> findById(
            @PathVariable String resourceName,
            @PathVariable Object id,
            @RequestParam(required = false) String foreignKeys,
            HttpServletRequest request) throws URISyntaxException {

        log.info("Received request to fetch resource '%s' with ID '%s'", resourceName, id);

        List<String> roles = getCurrentUserRoles();
        log.debug("Current user roles: %s", roles);

        XResourceMetadata metadata = registry.get(resourceName);
        log.debug("Fetched metadata for '%s': %s", resourceName, metadata);

        xRoleBasedAccessEvaluator.checkReadAccess(roles, metadata, resourceName);
        log.info("Read access granted for resource '%s' with roles: %s", resourceName, roles);

        URN urn = XURNEncoder.decode(id.toString());
        log.debug("Decoded URN: %s", urn);

        ForeignKeyTree fkTree = (foreignKeys != null)
                ? ForeignKeyParser.parse(foreignKeys)
                : new ForeignKeyTree();
        log.debug("Parsed ForeignKeyTree: %s", fkTree);

        Map<String, String> keyMap = urn.getKeyMap();
        log.debug("URN key map: %s", keyMap);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();
        log.debug("Base URL determined from request: %s", baseUrl);

        Object entity;
        if (metadata.hasCompositeKey()) {
            log.info("Resource '%s' uses composite key: %s", resourceName, keyMap);

            entity = service.findByCompositeKey(resourceName, keyMap)
                    .orElseThrow(() -> {
                        String errorMsg = "Resource " + resourceName + " with key " + keyMap + " not found";
                        log.warn(errorMsg);
                        return new ResourceNotFoundException(errorMsg);
                    });
        } else {
            String simpleKey = keyMap.get("id");
            log.info("Resource '%s' uses simple key: %s", resourceName, simpleKey);

            entity = service.findById(resourceName, simpleKey)
                    .orElseThrow(() -> {
                        String errorMsg = resourceName + " with ID " + id + " not found";
                        log.warn(errorMsg);
                        return new ResourceNotFoundException(errorMsg);
                    });
        }

        ObjectNode filtered = fkTree.isEmpty()
                ? xAccessManager.filterFieldsByAccess(entity, metadata, roles, baseUrl)
                : xAccessManager.filterFieldsByAccess(entity, metadata, roles, fkTree, baseUrl);
        log.debug("Filtered result: %s", filtered);

        linkResolver.injectPermalink(filtered, entity, resourceName, metadata, baseUrl);
        log.info("Permalink injected into result");

        log.info("Returning final response for resource '%s' with ID '%s'", resourceName, id);

        // Handle Response transformer
        if (xResponseTranformersRegistry.getFindOneTransformer() != null) {
            filtered = applyFindOneTransformer(filtered, resourceName,
                    xResponseTranformersRegistry.getFindOneTransformer());
        }
        return ResponseEntity.ok(filtered);
    }

    /**
     * Retrieves the value of a specific field from a resource entity by its ID.
     * Supports nested foreign key fetching and field-level access control.
     * 
     * @param resourceName the name of the resource (i.e., table/entity name)
     * @param field        the name of the field to fetch from the resource
     * @param id           the encoded URN or ID of the resource (supports composite
     *                     keys)
     * @param foreignKeys  optional query parameter to specify which nested foreign
     *                     keys to expand
     * @param request      the HTTP servlet request for building context-specific
     *                     base URL
     * @return a JSON response containing either the filtered object or list from
     *         the field
     * @throws URISyntaxException        if the ID cannot be parsed into a valid URN
     * @throws ResourceNotFoundException if the resource, field, or nested metadata
     *                                   cannot be resolved
     */
    @GetMapping("/{resourceName}/{id}/{field}")
    public ResponseEntity<?> getFieldFromResourceById(@PathVariable String resourceName, @PathVariable String field,
            @PathVariable Object id, @RequestParam(required = false) String foreignKeys, HttpServletRequest request)
            throws URISyntaxException {

        log.debug("Fetching field '%s' from resource '%s' with ID '%s'", field, resourceName, id);

        List<String> roles = getCurrentUserRoles();
        XResourceMetadata metadata = registry.get(resourceName);
        xRoleBasedAccessEvaluator.checkReadAccess(roles, metadata, resourceName);
        URN urn = XURNEncoder.decode(id.toString());

        ForeignKeyTree fkTree = (foreignKeys != null) ? ForeignKeyParser.parse(foreignKeys) : new ForeignKeyTree();
        Map<String, String> keyMap = urn.getKeyMap();

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();

        Object entity;
        if (metadata.hasCompositeKey()) {
            log.debug("Resource '%s' uses composite key: %s", resourceName, keyMap);
            entity = service.findByCompositeKey(resourceName, keyMap)
                    .orElseThrow(() -> {
                        log.warn("Composite resource not found: %s with keys %s", resourceName, keyMap);
                        return new ResourceNotFoundException(
                                "Resource " + resourceName + " with key " + keyMap + " not found");
                    });
        } else {
            log.debug("Resource '%s' uses simple key: %s", resourceName, keyMap.get("id"));
            entity = service.findById(resourceName, keyMap.get("id"))
                    .orElseThrow(() -> {
                        log.warn("Simple resource not found: %s with ID %s", resourceName, id);
                        return new ResourceNotFoundException(resourceName + " with ID " + id + " not found");
                    });
        }

        Field requestedField = XUtils.findField(entity.getClass(), field);
        if (requestedField == null) {
            log.warn("Field '%s' not found in resource '%s'", field, resourceName);
            throw new ResourceNotFoundException(
                    field + " is not found inside " + resourceName + " with ID " + id + ".");
        }

        requestedField.setAccessible(true);
        Object fieldValue;
        try {
            fieldValue = requestedField.get(entity);
        } catch (Exception e) {
            log.error("Error accessing field '%s' in resource '%s': %s", field, resourceName, e.getMessage());
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
                } else {
                    fieldResourceName = elementClass.getSimpleName();
                }
            }
        }

        if (fieldValue == null || fieldResourceName == null) {
            if (fieldValue != null) {
                // Field value is truthy but the field resource name is null which means it's
                // not an foreign key or entity type field
                return ResponseEntity.ok(Map.of(field, fieldValue));
            }
            log.warn("Field '%s' in '%s' resolved to null or unknown resource", field, resourceName);
            throw new ResourceNotFoundException(
                    field + " is not found inside " + resourceName + " with ID " + id + ".");
        }

        XResourceMetadata nestedMeta = registry.get(fieldResourceName);
        if (nestedMeta == null) {
            nestedMeta = registry.getEmbeddedResource(fieldResourceName);
            if (nestedMeta == null) {
                log.warn("Metadata for nested field '%s' not found for '%s'", field, resourceName);
                throw new ResourceNotFoundException(
                        field + " is not found inside " + resourceName + " with ID " + id + ".");
            }
        }

        if (fieldValue instanceof List<?>) {
            ArrayNode nestedArray = objectMapper.createArrayNode();
            for (Object element : (List<?>) fieldValue) {
                ObjectNode itemNode = fkTree.isEmpty()
                        ? xAccessManager.filterFieldsByAccess(element, nestedMeta, roles, baseUrl)
                        : xAccessManager.filterFieldsByAccess(element, nestedMeta, roles, fkTree, baseUrl);
                linkResolver.injectPermalink(itemNode, element, fieldResourceName, nestedMeta, baseUrl);
                nestedArray.add(itemNode);
            }
            // Handle response transformer
            if (xResponseTranformersRegistry.getFieldAccessTransformerForList() != null) {
                nestedArray = applyFieldAccessTransformer(
                        nestedArray, resourceName,
                        xResponseTranformersRegistry.getFieldAccessTransformerForList());
            }

            return ResponseEntity.ok(nestedArray);
        } else {
            ObjectNode filtered = fkTree.isEmpty()
                    ? xAccessManager.filterFieldsByAccess(fieldValue, nestedMeta, roles, baseUrl)
                    : xAccessManager.filterFieldsByAccess(fieldValue, nestedMeta, roles, fkTree, baseUrl);
            linkResolver.injectPermalink(filtered, fieldValue, fieldResourceName, nestedMeta, baseUrl);
            // Handle response transformer
            if (xResponseTranformersRegistry.getFieldAccessTransformerForOne() != null) {
                filtered = applyFieldAccessTransformerForOne(
                        filtered, resourceName,
                        xResponseTranformersRegistry.getFieldAccessTransformerForOne());
            }

            return ResponseEntity.ok(filtered);
        }
    }

    /**
     * Creates a new resource entity from the provided request body and returns the
     * created entity.
     *
     * @param resourceName the name of the resource to be created
     * @param body         the JSON representation of the entity to be created
     * @param request      the HTTP request used to build the base URL
     * @return a ResponseEntity containing the created entity with filtered fields
     *         based on the user's access roles
     * @throws Exception if an error occurs during entity conversion or creation
     */
    @PostMapping("/{resourceName}")
    public ResponseEntity<?> create(@PathVariable String resourceName, @RequestBody String body,
            HttpServletRequest request) throws Exception {

        log.enter("create %s", resourceName);

        List<String> roles = getCurrentUserRoles();
        log.debug("User roles: %s", roles);

        XResourceMetadata metadata = registry.get(resourceName);
        log.debug("Fetched metadata for resource: %s", resourceName);

        xRoleBasedAccessEvaluator.checkWriteAccess(roles, metadata, resourceName);
        log.debug("Write access granted for resource: %s", resourceName);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();
        log.debug("Base URL resolved as: %s", baseUrl);

        Object entity = service.convertJsonToEntity(resourceName, body);
        log.debug("Converted JSON to entity: %s", entity);

        xAccessManager.validateEntity(entity, ValidationContext.OperationType.CREATE);
        log.debug("Entity passed validation.");
        try {
            Object saved = service.create(resourceName, entity, roles);
            log.info("Entity created for resource '%s'", resourceName);
            ObjectNode filtered = xAccessManager.filterFieldsByAccess(saved, metadata, roles, baseUrl);
            log.debug("Filtered entity: %s", filtered);

            linkResolver.injectPermalink(filtered, saved, resourceName, metadata, baseUrl);
            log.debug("Permalink injected for entity.");

            // Handle Response transformer
            if (xResponseTranformersRegistry.getCreateOneTransformer() != null) {
                filtered = applyFindOneTransformer(filtered, resourceName,
                        xResponseTranformersRegistry.getCreateOneTransformer());
            }

            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to create resource:" + resourceName + ".", e);
            throw new XResourceException("Failed to create resource:" + resourceName + ".", e);
        }

    }

    /**
     * Creates a composite resource entity (with associated children) from the
     * provided request body
     * and returns the created entity.
     *
     * @param requestBody the JSON representation of the composite resource entity
     * @param request     the HTTP request used to build the base URL
     * @return a ResponseEntity containing the created composite entity with
     *         filtered fields based on the user's access roles
     * @throws Exception if an error occurs during the creation process
     */
    @PostMapping("/composite")
    public ResponseEntity<?> createComposite(@RequestBody JsonNode requestBody, HttpServletRequest request)
            throws Exception {

        log.enter("createComposite", requestBody);

        List<String> roles = getCurrentUserRoles();
        log.debug("User roles: %s", roles);

        String resourceName = requestBody.get("resource").asText();
        log.debug("Creating resource: %s", resourceName);

        xRoleBasedAccessEvaluator.checkWriteAccess(roles, registry.get(resourceName), resourceName);
        log.debug("Write access granted for resource: %s", resourceName);

        Object saved = service.saveWithChildren(resourceName, requestBody, roles);
        log.info("Composite entity created for resource '%s'", resourceName);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .build()
                .toUriString();
        log.debug("Base URL resolved as: %s", baseUrl);

        ObjectNode filtered = xAccessManager.filterFieldsByAccess(saved, registry.get(resourceName), roles, baseUrl);
        log.debug("Filtered composite entity: %s", filtered);

        linkResolver.injectPermalink(filtered, saved, resourceName, registry.get(resourceName), baseUrl);
        log.debug("Permalink injected for composite entity.");

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
        xRoleBasedAccessEvaluator.checkWriteAccess(roles, metadata, resourceName);
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
            linkResolver.injectPermalink(filtered, updated, resourceName, metadata, baseUrl);

            // Handle Response transformer
            if (xResponseTranformersRegistry.getUpdateOneTransformer() != null) {
                filtered = applyFindOneTransformer(filtered, resourceName,
                        xResponseTranformersRegistry.getUpdateOneTransformer());
            }

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
            linkResolver.injectPermalink(filtered, updated, resourceName, metadata, baseUrl);

            // Handle Response transformer
            if (xResponseTranformersRegistry.getUpdateOneTransformer() != null) {
                filtered = applyFindOneTransformer(filtered, resourceName,
                        xResponseTranformersRegistry.getUpdateOneTransformer());
            }

            return ResponseEntity.ok(filtered);

        }

    }

    /**
     * Deletes a resource by its ID. Supports both single and composite keys.
     *
     * @param resourceName the name of the resource to delete
     * @param id           the encoded URN ID of the resource
     * @return ResponseEntity indicating success or failure
     * @throws URISyntaxException if the ID is not a valid URN
     */
    @DeleteMapping("/{resourceName}/{id}")
    public ResponseEntity<?> delete(@PathVariable String resourceName, @PathVariable Object id)
            throws URISyntaxException {
        log.enter("delete resourceName=%s id=%s", resourceName, id);

        List<String> roles = getCurrentUserRoles();
        log.debug("User roles: %s", roles);

        XResourceMetadata metadata = registry.get(resourceName);
        log.debug("Fetched metadata for resource: %s", resourceName);

        xRoleBasedAccessEvaluator.checkWriteAccess(roles, metadata, resourceName);
        log.debug("Write access validated for: %s", resourceName);

        URN urn = XURNEncoder.decode(id.toString());
        log.debug("Decoded URN: %s", urn);

        Map<String, String> keyMap = urn.getKeyMap();
        log.debug("Parsed keyMap: %s", keyMap);

        if (metadata.hasCompositeKey()) {
            log.debug("Handling composite key deletion");

            Object entity = service.findByCompositeKey(resourceName, keyMap)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Resource " + resourceName + " with key " + keyMap + " not found"));

            log.debug("Entity found for deletion: %s", entity);

            service.deleteByEntity(resourceName, entity, roles);
            log.info("Deleted composite-key entity for resource: %s", resourceName);

        } else {
            log.debug("Handling single key deletion");

            Object entity = service.findById(resourceName, keyMap.get("id"))
                    .orElseThrow(() -> new ResourceNotFoundException(resourceName + " with ID " + id + " not found"));

            log.debug("Entity found for deletion: %s", entity);

            service.deleteByEntity(resourceName, entity, roles);
            log.info("Deleted entity with ID %s from resource: %s", id, resourceName);
        }
        log.exit("delete", id);
        return ResponseEntity.ok().build();
    }

    /**
     * Handles DELETE action on a resource via custom action handler.
     *
     * @param resourceName name of the resource
     * @param action       the action to be executed
     * @param id           encoded URN ID
     * @param request      HttpServletRequest object
     * @param response     HttpServletResponse object
     * @return the action execution result
     * @throws URISyntaxException if the ID is not a valid URN
     */
    @DeleteMapping("/{resourceName}/{id}/actions/{action}")
    public ResponseEntity<?> handleDeleteAction(@PathVariable String resourceName, @PathVariable String action,
            @PathVariable Object id, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {

        log.info("DELETE action '%s' on resource '%s' with ID %s", action, resourceName, id);
        return this.processAction(resourceName, action, id, request, response);
    }

    /**
     * Handles PUT action on a resource via custom action handler.
     */
    @PutMapping("/{resourceName}/{id}/actions/{action}")
    public ResponseEntity<?> handlePutAction(@PathVariable String resourceName, @PathVariable String action,
            @PathVariable Object id, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {

        log.info("PUT action '%s' on resource '%s' with ID %s", action, resourceName, id);
        return this.processAction(resourceName, action, id, request, response);
    }

    /**
     * Handles POST action on a resource via custom action handler.
     */
    @PostMapping("/{resourceName}/{id}/actions/{action}")
    public ResponseEntity<?> handlePostAction(@PathVariable String resourceName, @PathVariable String action,
            @PathVariable Object id, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {

        log.info("POST action '%s' on resource '%s' with ID %s", action, resourceName, id);
        return this.processAction(resourceName, action, id, request, response);
    }

    /**
     * Handles GET action on a resource via custom action handler.
     */
    @GetMapping("/{resourceName}/{id}/actions/{action}")
    public ResponseEntity<?> handleGetAction(@PathVariable String resourceName, @PathVariable String action,
            @PathVariable Object id, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {

        log.info("GET action '%s' on resource '%s' with ID %s", action, resourceName, id);
        return this.processAction(resourceName, action, id, request, response);
    }

    /**
     * Internal method to process dynamic actions on a given resource entity.
     *
     * @param resourceName the name of the resource
     * @param action       the action to execute
     * @param id           the encoded ID (URN)
     * @param request      HttpServletRequest
     * @param response     HttpServletResponse
     * @return ResponseEntity with action result
     * @throws URISyntaxException if the ID is not a valid URN
     */
    private ResponseEntity<?> processAction(String resourceName, String action, Object id, HttpServletRequest request,
            HttpServletResponse response) throws URISyntaxException {

        log.enter("processAction resourceName=%s action=%s id=%s", resourceName, action, id);

        List<String> roles = getCurrentUserRoles();
        log.debug("User roles: %s", roles);

        XResourceMetadata metadata = registry.get(resourceName);
        log.debug("Fetched metadata for resource: %s", resourceName);

        xRoleBasedAccessEvaluator.checkReadAccess(roles, metadata, resourceName);
        log.debug("Read access validated for: %s", resourceName);

        URN urn = XURNEncoder.decode(id.toString());
        log.debug("Decoded URN: %s", urn);

        Map<String, String> keyMap = urn.getKeyMap();
        log.debug("KeyMap: %s", keyMap);

        Object entity;

        if (metadata.hasCompositeKey()) {
            log.debug("Composite key detected");

            entity = service.findByCompositeKey(resourceName, keyMap)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Resource " + resourceName + " with key " + keyMap + " not found"));
        } else {
            log.debug("Single key detected");

            entity = service.findById(resourceName, keyMap.get("id"))
                    .orElseThrow(() -> new ResourceNotFoundException(resourceName + " with ID " + id + " not found"));
        }

        log.debug("Entity resolved for action '%s': %s", action, entity);

        Optional<XAction> xAction = metadata.getXAction(action);

        if (xAction.isEmpty()) {
            // As there is no XAction let's check forif there is any field control action is
            // present
            Optional<Map.Entry<String, XFieldAction>> xFieldActionMeta = metadata
                    .findFieldAndActionByActionName(action);
            if (xFieldActionMeta.isPresent() && request.getMethod().toUpperCase().equals("POST")) {
                Map.Entry<String, XFieldAction> xFieldActionEntry = xFieldActionMeta.get();
                String fieldName = xFieldActionEntry.getKey();
                XFieldAction xFieldAction = xFieldActionEntry.getValue();
                XFieldMetadata meta = metadata.getField(fieldName);

                Field field = meta.getField();

                Optional<ResponseEntity<?>> result = xActionExecutor.excuteXFieldAction(
                        xFieldAction, entity, request, response, fieldName, field, resourceName, roles, service);

                if (result.isEmpty()) {
                    log.error("Action '%s' execution returned empty result", action);
                    throw new ResourceNotFoundException("Unknown error occurred while executing action: " + action);
                }

                log.info("Action '%s' executed successfully for resource: %s", action, resourceName);
                log.exit("ptocessAction", result.get());
                return result.get();

            } else {
                log.warn("Action '%s' not found for resource: %s", action, resourceName);
                throw new ResourceNotFoundException(
                        action + " is not found for " + resourceName + " with ID " + id + ".");

            }
        }

        Optional<ResponseEntity<?>> result = xActionExecutor.excute(xAction.get(), entity, request, response);
        if (result.isEmpty()) {
            log.error("Action '%s' execution returned empty result", action);
            throw new ResourceNotFoundException("Unknown error occurred while executing action: " + action);
        }

        log.info("Action '%s' executed successfully for resource: %s", action, resourceName);
        log.exit("ptocessAction", result.get());
        return result.get();
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

    /**
     * Parses a JSON string into a map of xquery params.
     *
     * @param json The JSON string representing filters.
     * @return A map of parsed filter key-value pairs.
     */
    private Map<String, String> parseXQueryParams(String json) {
        log.enter("parseXQueryParams(json=%s)", json);
        if (json == null || json.isBlank()) {
            log.debug("Empty or null filter JSON provided.");
            log.exit("parseXQueryParams ", Collections.emptyMap());
            return Collections.emptyMap();
        }
        try {
            Map<String, String> result = new ObjectMapper().readValue(json, new TypeReference<>() {
            });
            log.exit("parseXQueryParams -> %s", result);
            return result;
        } catch (Exception e) {
            log.error("Invalid xQueryParams format: %s", e.getMessage(), e);
            throw new RuntimeException("Invalid xQueryParams format", e);
        }
    }

    /**
     * Main entry point for /api/resource endpoint.
     * Returns an overview of all registered XResources and JVM/system info.
     *
     * @return A ResponseEntity containing resource and system metadata.
     */
    @GetMapping
    public ResponseEntity<?> getXResourceOverview() {
        log.enter("getXResourceOverview");

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

        log.exit("getXResourceOverview -> ", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets JVM uptime as ISO-8601 duration string.
     *
     * @return A string representing JVM uptime or "N/A" on failure.
     */
    private String getUptime() {
        log.enter("getUptime");
        try {
            long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
            String result = Duration.ofMillis(uptimeMs).toString();
            log.exit("getUptime -> %s", result);
            return result;
        } catch (Exception e) {
            log.warn("Unable to fetch uptime: %s", e.getMessage());
            log.exit("getUptime -> ", "N/A");
            return "N/A";
        }
    }

    private ArrayNode applyFieldAccessTransformer(
            ArrayNode response, String resourceName,
            XResponseTransformer<ArrayNode> transformer) {

        if (transformer == null || response == null)
            return response;

        // Create a deep copy of the ArrayNode
        ArrayNode deepCopiedResponse = objectMapper.createArrayNode();
        response.forEach(jsonNode -> deepCopiedResponse.add(jsonNode.deepCopy()));

        return transformer.tranform(deepCopiedResponse, resourceName);
    }

    private List<Map<String, Object>> applyJsonFormTransformer(
            List<Map<String, Object>> response,
            String resourceName,
            XResponseTransformer<List<Map<String, Object>>> transformer) {

        if (transformer == null || response == null)
            return response;

        List<Map<String, Object>> deepCopiedResponse = new ArrayList<>();
        for (Map<String, Object> node : response) {
            // Deep copy using object mapper
            Map<String, Object> copiedNode = objectMapper.convertValue(
                    objectMapper.valueToTree(node), new TypeReference<Map<String, Object>>() {
                    });
            deepCopiedResponse.add(copiedNode);
        }

        return transformer.tranform(deepCopiedResponse, resourceName);
    }

    private List<ObjectNode> applyFindAllTransformer(List<ObjectNode> response, String resourceName,
            XResponseTransformer<List<ObjectNode>> transformer) {
        if (transformer == null || response == null)
            return response;
        List<ObjectNode> deepCopiedResponse = new ArrayList<>();
        for (ObjectNode node : response) {
            deepCopiedResponse.add(objectMapper.valueToTree(node));
        }
        return transformer.tranform(deepCopiedResponse, resourceName);
    }

    private ObjectNode applyFindOneTransformer(ObjectNode response, String resourceName,
            XResponseTransformer<ObjectNode> transformer) {
        if (transformer == null || response == null)
            return response;
        ObjectNode deepCopied = objectMapper.valueToTree(response);
        return transformer.tranform(deepCopied, resourceName);
    }

    private ObjectNode applyFieldAccessTransformerForOne(ObjectNode response, String resourceName,
            XResponseTransformer<ObjectNode> transformer) {
        if (transformer == null || response == null)
            return response;
        ObjectNode deepCopied = objectMapper.valueToTree(response);
        return transformer.tranform(deepCopied, resourceName);
    }

}
