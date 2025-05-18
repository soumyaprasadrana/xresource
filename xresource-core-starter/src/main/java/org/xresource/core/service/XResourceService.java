package org.xresource.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.xresource.core.annotations.AccessLevel;
import org.xresource.core.annotations.XQuery;
import org.xresource.core.hook.XResourceEventContext;
import org.xresource.core.hook.XResourceEventType;
import org.xresource.core.hook.XResourceHookRegistry;
import org.xresource.internal.query.XQueryContextProvider;
import org.xresource.internal.query.XQueryExecutor;
import org.xresource.core.validation.ValidationContext;
import org.xresource.internal.auth.XAccessManager;
import org.xresource.internal.auth.XRoleBasedAccessEvaluator;
import org.xresource.internal.exception.ResourceNotFoundException;
import org.xresource.internal.exception.XResourceAlreadyExistsException;
import org.xresource.internal.exception.XResourceException;
import org.xresource.internal.models.XFieldMetadata;
import org.xresource.internal.models.XResourceMetadata;
import org.xresource.internal.registry.XResourceMetadataRegistry;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class XResourceService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private XResourceMetadataRegistry registry;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private XResourceHookRegistry hooksRegistry;

    @Autowired
    private XAccessManager xAccessManager;

    @Autowired
    private XQueryExecutor xQueryExecutor;

    @Autowired
    XQueryContextProvider contextProvider;

    @Autowired
    XRoleBasedAccessEvaluator xRoleBasedAccessEvaluator;

    @SuppressWarnings("unchecked")
    private JpaRepository<Object, Object> getRepository(String resourceName) {
        XResourceMetadata metadata = registry.get(resourceName);
        Class<?> repoClass = metadata.getRepositoryClass();
        JpaRepository<Object, Object> repo = null;
        try {
            repo = (JpaRepository<Object, Object>) this.applicationContext.getBean(repoClass);
        } catch (NoSuchBeanDefinitionException e) {
        }
        return repo;
    }

    private Class<?> getEntityClass(String resourceName) {
        return registry.get(resourceName).getEntityClass();
    }

    @SuppressWarnings("unchecked")
    public List<Object> findAll(String resourceName) {
        XResourceMetadata metadata = registry.get(resourceName);
        if (metadata.isHasAutoApplyQuery()) {
            Map<String, XQuery> autoApplyQueries = getAutoApplyQueriesBasedOnRoles(metadata, getCurrentUserRoles());
            if (!autoApplyQueries.isEmpty()) {
                Map<String, Object> userContext = getCurrentUserContext();
                Map<String, Object> context = contextProvider.buildContext(userContext, Collections.emptyMap());
                return (List<Object>) xQueryExecutor.executeXQueries(metadata.getEntityClass(), autoApplyQueries,
                        context);
            } else
                return getRepository(resourceName).findAll();
        }
        return getRepository(resourceName).findAll();
    }

    private List<String> getCurrentUserRoles() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());
    }

    public Map<String, Object> getCurrentUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyMap();
        }

        Map<String, Object> context = new HashMap<>();
        context.put("loginid", authentication.getName());

        context.put("roles", authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        Object principal = authentication.getPrincipal();

        // Try to extract properties dynamically (if not just a String username)
        if (principal != null && !(principal instanceof String)) {
            for (var method : principal.getClass().getMethods()) {
                if (method.getParameterCount() == 0 && method.getName().startsWith("get")) {
                    try {
                        String propName = Character.toLowerCase(method.getName().charAt(3))
                                + method.getName().substring(4);
                        Object value = method.invoke(principal);
                        context.put(propName, value);
                    } catch (Exception ignored) {
                        // skip if inaccessible
                    }
                } else if (method.getParameterCount() == 0 && method.getName().startsWith("is")) {
                    try {
                        String propName = Character.toLowerCase(method.getName().charAt(2))
                                + method.getName().substring(3);
                        Object value = method.invoke(principal);
                        context.put(propName, value);
                    } catch (Exception ignored) {
                        // skip if inaccessible
                    }
                }
            }
        }

        return context;
    }

    public Map<String, XQuery> getAutoApplyQueries(XResourceMetadata metadata) {
        return metadata.getXQueriesMap().entrySet().stream()
                .filter(entry -> entry.getValue().autoApply())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, XQuery> getAutoApplyQueriesBasedOnRoles(XResourceMetadata metadata, List<String> userRoles) {
        return metadata.getXQueriesMap().entrySet().stream()
                .filter(entry -> {
                    XQuery query = entry.getValue();

                    if (!query.autoApply())
                        return false;

                    String[] applicableRoles = query.appliesToRoles();
                    if (applicableRoles.length == 1 && "*".equals(applicableRoles[0]))
                        return true;
                    if (Arrays.stream(applicableRoles).anyMatch(r -> "*".equals(r)))
                        return true;

                    // Check if any user role is included
                    return userRoles.stream().anyMatch(role -> Arrays.asList(applicableRoles).contains(role));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @SuppressWarnings("unchecked")
    public Page<Object> findPaged(String resourceName, int page, int size, String sortBy, String direction) {
        XResourceMetadata metadata = registry.get(resourceName);

        if (metadata.isHasAutoApplyQuery()) {
            Map<String, XQuery> autoApplyQueries = getAutoApplyQueriesBasedOnRoles(metadata, getCurrentUserRoles());
            if (!autoApplyQueries.isEmpty()) {
                Map<String, Object> userContext = getCurrentUserContext();
                Map<String, Object> context = contextProvider.buildContext(userContext, Collections.emptyMap());

                return (Page<Object>) xQueryExecutor.executePagedQueries(
                        metadata.getEntityClass(),
                        autoApplyQueries,
                        context,
                        page,
                        size,
                        sortBy,
                        direction);
            }
        }

        // Fallback to repository-based paging
        if (sortBy != null) {
            Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            return getRepository(resourceName).findAll(pageable);
        } else {
            Pageable pageable = PageRequest.of(page, size);
            return getRepository(resourceName).findAll(pageable);
        }

    }

    public Optional<Object> findById(String resourceName, Object id) {
        XResourceMetadata metadata = registry.get(resourceName);

        if (metadata.isHasAutoApplyQuery()) {
            Map<String, XQuery> autoApplyQueries = getAutoApplyQueriesBasedOnRoles(metadata, getCurrentUserRoles());
            if (!autoApplyQueries.isEmpty()) {
                Map<String, Object> userContext = getCurrentUserContext();
                Map<String, Object> context = contextProvider.buildContext(userContext, Collections.emptyMap());
                return xQueryExecutor.findById(metadata, autoApplyQueries, context, id);
            }
        }

        // Fallback to standard repository call
        return getRepository(resourceName).findById(id);
    }

    @Transactional
    public Object create(String resourceName, Object entity, List<String> roles) {
        XResourceMetadata metadata = registry.get(resourceName);
        JpaRepository<Object, Object> repo = getRepository(resourceName);
        Map<String, Object> extra = Map.of(); // Optional context data

        // Extract ID from entity and check existence
        Object id = extractEntityId(entity);
        if (id != null && repo.existsById(id)) {
            throw new XResourceAlreadyExistsException(resourceName + " with ID " + id.toString() + " already exists.");
        }

        // Before create hook
        XResourceEventContext context = new XResourceEventContext(entity, metadata.getResourceName(), roles, extra);
        hooksRegistry.executeHooks(resourceName, XResourceEventType.BEFORE_CREATE, context);

        // Persist entity
        Object saved = repo.save(entity);

        // After create hook
        hooksRegistry.executeHooks(resourceName, XResourceEventType.AFTER_CREATE,
                new XResourceEventContext(saved, metadata.getResourceName(), roles, extra));

        return saved;
    }

    @Transactional
    public Object saveWithChildren(String resourceName, JsonNode rootNode, List<String> roles) throws Exception {
        XResourceMetadata metadata = registry.get(resourceName);
        Object entity = objectMapper.treeToValue(rootNode.get("data"), metadata.getEntityClass());

        // Run validation and hook (optional)
        xAccessManager.validateEntity(entity, ValidationContext.OperationType.CREATE);
        XResourceEventContext ctx = new XResourceEventContext(entity, metadata.getResourceName(), roles, Map.of());
        hooksRegistry.executeHooks(resourceName, XResourceEventType.BEFORE_CREATE, ctx);

        // Save using JPA Repo (cascading works)
        Object saved = getRepository(resourceName).save(entity);

        hooksRegistry.executeHooks(resourceName, XResourceEventType.AFTER_CREATE,
                new XResourceEventContext(saved, metadata.getResourceName(), roles, Map.of()));

        return saved;
    }

    @Transactional
    public Object update(String resourceName, Object existingEntity, Object inputEntity, List<String> roles) {
        XResourceMetadata metadata = registry.get(resourceName);
        JpaRepository<Object, Object> repo = getRepository(resourceName);
        Map<String, Object> extra = Map.of(); // Optional hook data

        // Patch non-null values from inputEntity into existingEntity (excluding primary
        // keys)
        for (Field field : metadata.getEntityClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(inputEntity);
                if (value != null) {
                    if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                        // Commented for future enhancement:
                        // if (!metadata.isPrimaryKeyUpdatable(field)) continue;
                        throw new XResourceException("Can not update primary keys");
                        // For now, skip primary key fields (no updates allowed)
                    }
                    field.set(existingEntity, value);
                }
            } catch (IllegalAccessException e) {
                throw new XResourceException("Failed to patch field: " + field.getName() + "," + e.getMessage());
            }
        }

        // Fire BEFORE_UPDATE hook
        XResourceEventContext context = new XResourceEventContext(existingEntity,
                metadata.getResourceName(), roles, extra);
        hooksRegistry.executeHooks(resourceName, XResourceEventType.BEFORE_UPDATE, context);

        // Save patched entity
        Object updated = repo.save(existingEntity);

        // Fire AFTER_UPDATE hook
        hooksRegistry.executeHooks(resourceName, XResourceEventType.AFTER_UPDATE,
                new XResourceEventContext(updated, metadata.getResourceName(), roles, extra));

        return updated;
    }

    public void deleteById(String resourceName, Object id, List<String> roles) {
        JpaRepository<Object, Object> repo = getRepository(resourceName);
        XResourceMetadata metadata = registry.get(resourceName);
        Map<String, Object> extra = Map.of();

        Object entity = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(resourceName + " with ID " + id + " not found"));

        XResourceEventContext context = new XResourceEventContext(entity, metadata.getResourceName(), roles, extra);
        hooksRegistry.executeHooks(resourceName, XResourceEventType.BEFORE_DELETE, context);

        repo.deleteById(id);

        hooksRegistry.executeHooks(resourceName, XResourceEventType.AFTER_DELETE,
                new XResourceEventContext(entity, metadata.getResourceName(), roles, extra));
    }

    public void deleteByEntity(String resourceName, Object entity, List<String> roles) {
        JpaRepository<Object, Object> repo = getRepository(resourceName);
        XResourceMetadata metadata = registry.get(resourceName);
        Map<String, Object> extra = Map.of();

        XResourceEventContext context = new XResourceEventContext(entity, metadata.getResourceName(), roles, extra);
        hooksRegistry.executeHooks(resourceName, XResourceEventType.BEFORE_DELETE, context);

        repo.delete(entity);

        hooksRegistry.executeHooks(resourceName, XResourceEventType.AFTER_DELETE,
                new XResourceEventContext(entity, metadata.getResourceName(), roles, extra));
    }

    public List<Object> saveAll(String resourceName, List<Object> entities) {
        return getRepository(resourceName).saveAll(entities);
    }

    public void deleteAll(String resourceName) {
        getRepository(resourceName).deleteAll();
    }

    public long count(String resourceName) {
        return getRepository(resourceName).count();
    }

    public Object convertJsonToEntity(String resourceName, String json) throws Exception {
        return objectMapper.readValue(json, getEntityClass(resourceName));
    }

    public Object convertMapToEntity(String resourceName, Map<String, Object> map) {
        return objectMapper.convertValue(map, getEntityClass(resourceName));
    }

    public List<Object> findByExample(String resourceName, Map<String, Object> exampleMap) {
        Class<?> entityClass = getEntityClass(resourceName);
        Object probe = objectMapper.convertValue(exampleMap, entityClass);
        Example<Object> example = Example.of(probe, ExampleMatcher.matchingAll().withIgnoreNullValues());
        return (List<Object>) ((QueryByExampleExecutor<Object>) getRepository(resourceName)).findAll(example);
    }

    public Page<Object> findByExamplePaged(
            String resourceName, Map<String, Object> exampleMap, int page, int size, String sortBy, String direction) {
        Object probe = objectMapper.convertValue(exampleMap, getEntityClass(resourceName));
        Example<Object> example = Example.of(probe, ExampleMatcher.matchingAll().withIgnoreNullValues());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ((QueryByExampleExecutor<Object>) getRepository(resourceName)).findAll(example, pageable);
    }

    @SuppressWarnings("unchecked")
    public Optional<Object> findByCompositeKey(String resourceName, Map<String, String> keyMap) {
        XResourceMetadata metadata = registry.get(resourceName);

        if (metadata.isHasAutoApplyQuery()) {
            Map<String, XQuery> autoApplyQueries = getAutoApplyQueriesBasedOnRoles(metadata, getCurrentUserRoles());
            if (!autoApplyQueries.isEmpty()) {
                Map<String, Object> userContext = getCurrentUserContext();
                Map<String, Object> context = contextProvider.buildContext(userContext, Collections.emptyMap());

                // Build WHERE clause from composite key
                StringBuilder whereClause = new StringBuilder();
                for (Map.Entry<String, String> entry : keyMap.entrySet()) {
                    if (whereClause.length() > 0)
                        whereClause.append(" AND ");
                    whereClause.append("e.id.").append(entry.getKey()).append(" = :").append(entry.getKey());
                }

                // Append XQuery conditions
                for (XQuery query : autoApplyQueries.values()) {
                    whereClause.append(" AND (").append(query.where()).append(")");
                }

                // Construct full JPQL
                String jpql = "SELECT e FROM " + metadata.getEntityClass().getSimpleName() + " e WHERE " + whereClause;
                TypedQuery<Object> jpaQuery = (TypedQuery<Object>) entityManager.createQuery(jpql,
                        metadata.getEntityClass());

                // Set composite key params
                for (Map.Entry<String, String> entry : keyMap.entrySet()) {
                    jpaQuery.setParameter(entry.getKey(), entry.getValue());
                }

                // Set dynamic context params from all XQueries
                for (XQuery query : autoApplyQueries.values()) {
                    for (String ctxKey : query.contextParams()) {
                        String paramName = xQueryExecutor.extractParamName(ctxKey);
                        Object resolvedValue;

                        if (paramName.startsWith("context_loggeduser_")) {
                            String loggedParam = paramName.substring("context_loggeduser_".length());
                            resolvedValue = xQueryExecutor.resolveLoggedUserContextValue(loggedParam, context);
                        } else {
                            resolvedValue = context.get(paramName);
                        }

                        if (resolvedValue != null) {
                            jpaQuery.setParameter(paramName, resolvedValue);
                        }
                    }
                }

                List<Object> results = jpaQuery.getResultList();
                return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            }
        }

        // Fallback to simple specification-based fetch if no XQuery
        JpaRepository<Object, ?> repo = getRepository(resourceName);
        Specification<Object> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Path<?> embeddedRoot = root.get("id");
            for (Map.Entry<String, String> entry : keyMap.entrySet()) {
                predicates.add(cb.equal(embeddedRoot.get(entry.getKey()), entry.getValue()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Object> results = ((JpaSpecificationExecutor<Object>) repo).findAll(spec);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private Object extractEntityId(Object entity) {
        Class<?> clazz = entity.getClass();

        // Case 1: @Id on a field
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                try {
                    return field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot access @Id field", e);
                }
            }
        }

        // Case 2: @EmbeddedId
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(EmbeddedId.class)) {
                field.setAccessible(true);
                try {
                    return field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot access @EmbeddedId field", e);
                }
            }
        }

        return null;
    }

    public List<Map<String, Object>> generateJsonForm(String resourceName, String fieldsCsv) {
        return buildFormFields(resourceName, fieldsCsv, null);
    }

    public List<Map<String, Object>> generateNestedJsonForm(String resourceName, String fieldsCsv,
            Class<?> parentClass) {
        return buildFormFields(resourceName, fieldsCsv, parentClass);
    }

    private List<Map<String, Object>> buildFormFields(String resourceName, String fieldsCsv, Class<?> parentClass) {
        List<String> roles = getCurrentUserRoles();

        XResourceMetadata metadata = Optional.ofNullable(registry.get(resourceName))
                .orElseGet(() -> registry.getEmbeddedResource(resourceName));
        if (metadata == null) {
            return new ArrayList<>();
        }

        xRoleBasedAccessEvaluator.checkWriteAccess(roles, metadata, resourceName);

        // Dynamically compute default fields
        String requiredFields = metadata.getFields().values().stream()
                .filter(XFieldMetadata::partOfJSONForm)
                .filter(field -> xRoleBasedAccessEvaluator.getFieldEffectiveAccess(roles, field) != AccessLevel.NONE)
                .filter(field -> parentClass == null || !isForeignKeyBackReference(field.getField(), parentClass))
                .filter(field -> {
                    if (parentClass != null && field.getField().isAnnotationPresent(Id.class)) {
                        return !isForeignKeyBackReferenceHasMapsId(metadata.getFields().values().toArray(),
                                parentClass);
                    }
                    return true;
                })
                .map(XFieldMetadata::getName)
                .collect(Collectors.joining(","));

        if (metadata.hasCompositeKey()) {
            requiredFields += "," + metadata.getPrimaryKey();
        }

        String fields = fieldsCsv != null ? fieldsCsv : requiredFields;
        String[] fieldNames = fields.split(",");

        Map<String, XFieldMetadata> fieldsMeta = metadata.getFields();
        Map<String, XFieldMetadata> embeddedIdClassFieldsMeta = metadata.getEmbeddedIdClassFields();

        return Arrays.stream(fieldNames)
                .filter(fieldName -> fieldsMeta.containsKey(fieldName)
                        || embeddedIdClassFieldsMeta.containsKey(fieldName))
                .map(fieldName -> {
                    XFieldMetadata meta = fieldsMeta.getOrDefault(fieldName, embeddedIdClassFieldsMeta.get(fieldName));
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

                    if (meta.isForeignKey()) {
                        fieldData.put("isNestedObject", true);
                        fieldData.put("nestedObjectKeyField", meta.getForeignKeyRefField());

                        XResourceMetadata nestedMeta = Optional.ofNullable(registry.get(meta.getForeignKeyRefTable()))
                                .orElseGet(() -> registry.getEmbeddedResource(meta.getForeignKeyRefTable()));

                        boolean nestedObjectKeyFieldIsRequired = false;
                        if (nestedMeta != null && nestedMeta.getFields().containsKey(meta.getForeignKeyRefField())) {
                            nestedObjectKeyFieldIsRequired = nestedMeta.getFields().get(meta.getForeignKeyRefField())
                                    .isRequired();
                        }
                        fieldData.put("nestedObjectKeyFieldIsRequired", nestedObjectKeyFieldIsRequired);
                    }

                    if (!metadata.getXJSONFormValidatorsForField(meta.getName()).isEmpty()) {
                        fieldData.put("validators", metadata.getXJSONFormValidatorsForField(meta.getName()).get());
                    }

                    Field field = meta.getField();
                    if (isInverse(field)) {
                        String nestedFieldForeignKeyTable = resolveInverseOneToOneOrOneToManyForeignTable(field);
                        if (nestedFieldForeignKeyTable != null) {
                            fieldData.put("nestedObjectForm", generateNestedJsonForm(
                                    resolveInverseOneToOneOrOneToManyForeignTable(field), null,
                                    metadata.getEntityClass()));
                        }

                    }

                    return fieldData;
                })
                .collect(Collectors.toList());
    }

    /**
     * Fallback handler for inverse @OneToOne reference where no @JoinColumn is
     * present.
     * Extracts the mapped table name from the field type.
     *
     * @param field The field annotated with @OneToOne but without @JoinColumn
     * @return The table name of the referenced entity class, or null if not
     *         applicable
     */
    public String resolveInverseOneToOneOrOneToManyForeignTable(Field field) {
        if (!field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(OneToMany.class)) {
            return null;
        }

        if (field.isAnnotationPresent(JoinColumn.class)) {
            return null; // Owning side already handled elsewhere
        }

        Class<?> targetEntity = field.getType();

        if (!targetEntity.isAnnotationPresent(Entity.class)) {
            return null; // Not a JPA entity
        }

        Table tableAnnotation = targetEntity.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isBlank()) {
            return tableAnnotation.name();
        }

        // Fallback to class simple name if @Table not present
        return targetEntity.getSimpleName().toLowerCase(); // Or any naming strategy you're using
    }

    /**
     * Determines if a field is a valid inverse-side @OneToOne reference.
     *
     * @param field The field to check
     * @return true if it should be treated as an inverse @OneToOne
     */
    public boolean isInverse(Field field) {
        if (field.isAnnotationPresent(OneToOne.class)) {
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            if (!oneToOne.mappedBy().isEmpty()) {
                return true;
            }
        }

        if (field.isAnnotationPresent(OneToMany.class)) {
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            if (!oneToMany.mappedBy().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public boolean isForeignKeyBackReference(Field field, Class<?> parentClass) {
        if (field.isAnnotationPresent(JoinColumn.class) && field.isAnnotationPresent(OneToOne.class) || field
                .isAnnotationPresent(JoinColumn.class) && field.isAnnotationPresent(ManyToOne.class)) {
            Class<?> type = field.getType();
            return type.equals(parentClass);
        }
        return false;
    }

    public boolean isForeignKeyBackReferenceHasMapsId(Object[] fieldMetas, Class<?> parentClass) {
        for (Object fieldMetaObj : fieldMetas) {
            XFieldMetadata fieldMeta = (XFieldMetadata) fieldMetaObj;
            Field field = fieldMeta.getField();
            if (((field.isAnnotationPresent(JoinColumn.class) && field.isAnnotationPresent(OneToOne.class))
                    || (field.isAnnotationPresent(JoinColumn.class) && field.isAnnotationPresent(ManyToOne.class)))
                    && field.getType().equals(parentClass)
                    && field.isAnnotationPresent(MapsId.class)) {
                return true;
            }
        }
        return false;
    }

}
