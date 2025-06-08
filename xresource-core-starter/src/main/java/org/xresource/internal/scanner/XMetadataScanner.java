package org.xresource.internal.scanner;

import org.springframework.security.core.context.SecurityContextHolder;
import org.xresource.core.annotations.*;
import org.xresource.internal.intent.core.parser.model.IntentMeta;
import org.xresource.core.logging.XLogger;
import org.xresource.core.validation.XValidator;
import org.xresource.core.validation.XValidatorRegistry;
import org.xresource.internal.exception.XAccessDeniedException;
import org.xresource.internal.models.XFieldMetadata;
import org.xresource.internal.models.XResourceMetadata;
import org.xresource.internal.registry.XResourceMetadataRegistry;
import org.xresource.internal.validation.XValidationUtil;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Scanner responsible for analyzing annotated JPA entity and repository classes
 * to produce an {@link XResourceMetadata} object for generic resource handling.
 * Supports JSON schema enrichment, access control definitions, and field
 * metadata extraction.
 */
@RequiredArgsConstructor
public class XMetadataScanner {

    private final XResourceMetadataRegistry registry;

    private static final XLogger log = XLogger.forClass(XMetadataScanner.class);

    private final XValidatorRegistry xValidatorRegistry;

    /**
     * Scans the provided entity and repository classes to produce metadata for the
     * specified table.
     *
     * @param tableName   name of the table
     * @param entityClass JPA entity class
     * @param repository  corresponding repository class
     */
    public void scan(String tableName, Class<?> entityClass, Class<?> repository) {
        log.info("Scanning entity: %s for table: %s", entityClass.getSimpleName(), tableName);

        XResourceMetadata metadata = new XResourceMetadata();
        metadata.setEntityClass(entityClass);
        metadata.setRepositoryClass(repository);
        metadata.setResourceName(tableName);

        this.scanAndRegisterXQueries(repository, entityClass, metadata);
        this.scanAndRegisterXActions(repository, entityClass, metadata);
        this.scanAndRegisterXJSONFormValidators(repository, entityClass, metadata);

        Map<String, JsonNode> externalFieldSchema = new HashMap<>();
        String rawSchema = null;

        if (repository.isAnnotationPresent(XMetadata.class)) {
            String path = repository.getAnnotation(XMetadata.class).path();
            log.debug("Loading schema from repository annotation at path: %s", path);
            try (InputStream is = repository.getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    rawSchema = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode schemaNode = mapper.readTree(rawSchema);

                    if (schemaNode.has("fields")) {
                        Iterator<String> fieldNames = schemaNode.get("fields").fieldNames();
                        int fieldIndex = 0;
                        while (fieldNames.hasNext()) {
                            String fieldName = fieldNames.next();
                            JsonNode fieldNode = schemaNode.get("fields").get(fieldName);
                            ((ObjectNode) fieldNode).put("displayseq", fieldIndex);
                            externalFieldSchema.put(fieldName, fieldNode);
                            fieldIndex++;
                        }
                    }

                    metadata.setRawSchema(rawSchema);
                    log.info("Loaded external schema from repository for table: %s", tableName);
                }
            } catch (Exception e) {
                log.error("Failed to load schema from repository for %s", entityClass.getName(), e);
                throw new RuntimeException("Failed to load schema for " + entityClass.getName(), e);
            }
        }

        if (entityClass.isAnnotationPresent(XMetadata.class)) {
            String path = entityClass.getAnnotation(XMetadata.class).path();
            log.debug("Loading schema from entity annotation at path: %s", path);
            try (InputStream is = entityClass.getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    rawSchema = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode schemaNode = mapper.readTree(rawSchema);

                    if (schemaNode.has("fields")) {
                        Iterator<String> fieldNames = schemaNode.get("fields").fieldNames();
                        int fieldIndex = 0;
                        while (fieldNames.hasNext()) {
                            String fieldName = fieldNames.next();
                            JsonNode fieldNode = schemaNode.get("fields").get(fieldName);
                            ((ObjectNode) fieldNode).put("displayseq", fieldIndex);
                            externalFieldSchema.put(fieldName, fieldNode);
                            fieldIndex++;
                        }
                    }

                    metadata.setRawSchema(rawSchema);
                    log.info("Loaded external schema from entity for table: %s", tableName);
                }
            } catch (Exception e) {
                log.error("Failed to load schema from entity for %s", entityClass.getName(), e);
                throw new RuntimeException("Failed to load schema for " + entityClass.getName(), e);
            }
        }

        log.debug("Checking access control annotations...");
        applyResourceAccess(entityClass, repository, metadata);

        for (Field field : getAllFields(entityClass)) {
            processField(field, entityClass, metadata, externalFieldSchema, false);
        }

        log.info("Metadata scan complete for table: %s with %s fields.", tableName, metadata.getFields().size());
        if (repository.isAnnotationPresent(XResourceIgnore.class)) {
            if (repository.isAnnotationPresent(XResourceExposeAsEmbeddedResource.class)) {
                log.info("Resource %s is configured for exposing as embedded resource.", tableName);
                metadata.setExposedForEmbbededReferencesOnly(true);
                registry.registerEmbeddedResource(tableName, metadata);
            }

            if (repository.isAnnotationPresent(XCronResource.class)) {
                log.info("Resource %s is configured for exposing as cron job resource.", tableName);
                metadata.setExposedForCron(true);
                registry.registerCronResource(tableName, metadata);
            }

        } else {
            registry.register(tableName, metadata);
        }

    }

    private String toSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    private boolean isFieldRequired(Field field) {
        if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).nullable()) {
            return true;
        }

        if (field.isAnnotationPresent(JoinColumn.class) && !field.getAnnotation(JoinColumn.class).nullable()) {
            return true;
        }

        return field.isAnnotationPresent(NotNull.class);
    }

    /**
     * Resolves the type of the field and returns its string representation.
     * Handles basic types, collections, arrays, and custom objects.
     *
     * @param field the field whose type needs to be resolved
     * @return a string representing the field's type
     */
    private String resolveType(Field field) {
        Class<?> type = field.getType();

        if (type.isArray()) {
            log.debug("Field '%s' is an array of type %s.", field.getName(), type.getComponentType().getSimpleName());
            return "array";
        }

        if (Collection.class.isAssignableFrom(type)) {
            log.debug("Field '%s' is a collection type (%s).", field.getName(), type.getSimpleName());
            return "array";
        }

        if (type == String.class) {
            log.debug("Field '%s' is of type String.", field.getName());
            return "string";
        }

        if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
            if (type == int.class || type == Integer.class) {
                log.debug("Field '%s' is of type Integer.", field.getName());
                return "integer";
            }
            if (type == long.class || type == Long.class) {
                log.debug("Field '%s' is of type Long.", field.getName());
                return "long";
            }
            if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
                log.debug("Field '%s' is of type Number.", field.getName());
                return "number";
            }
        }

        if (type == boolean.class || type == Boolean.class) {
            log.debug("Field '%s' is of type Boolean.", field.getName());
            return "boolean";
        }

        if (Date.class.isAssignableFrom(type)) {
            log.debug("Field '%s' is of type Date.", field.getName());
            return "date";
        }

        log.debug("Field '%s' is of type Object (custom type or unknown).", field.getName());
        return "object";
    }

    public String resolveColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (!column.name().isEmpty()) {
                return column.name();
            }
        }

        if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (!joinColumn.name().isEmpty()) {
                return joinColumn.name();
            }
        }

        return field.getName();
    }

    public boolean resolveInsertable(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return column.insertable();
        }

        if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            return joinColumn.insertable();
        }

        return true; // default JPA behavior
    }

    public boolean resolveUpdatable(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return column.updatable();
        }

        if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            return joinColumn.updatable();
        }

        return true; // default JPA behavior
    }

    /**
     * Resolves the format of the field based on its annotations.
     * 
     * @param field the field whose format needs to be resolved
     * @return a string representing the field's format
     */
    private String resolveFormat(Field field) {
        if (field.isAnnotationPresent(Email.class)) {
            log.debug("Field '%s' is annotated with Email.", field.getName());
            return "email";
        }
        if (field.isAnnotationPresent(Pattern.class)) {
            String pattern = field.getAnnotation(Pattern.class).regexp();
            log.debug("Field '%s' is annotated with Pattern, regexp: %s", field.getName(), pattern);
            return pattern;
        }
        return "";
    }

    /**
     * Retrieves all fields of a class, including fields from its superclasses.
     * 
     * @param type the class whose fields need to be retrieved
     * @return a list of fields from the class and its superclasses
     */
    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        log.debug("Retrieved %d fields from class '%s'.", fields.size(), type.getName());
        return fields;
    }

    /**
     * Scans the repository and entity classes for XQuery annotations and registers
     * them.
     * 
     * @param repoClass   the repository class
     * @param entityClass the entity class
     * @param meta        the XResourceMetadata to register the queries in
     */
    private void scanAndRegisterXQueries(Class<?> repoClass, Class<?> entityClass, XResourceMetadata meta) {
        log.debug("Scanning repository class '%s' and entity class '%s' for XQueries.", repoClass.getName(),
                entityClass.getName());
        if (repoClass.isAnnotationPresent(XQuery.class)) {
            meta.addXQuery(repoClass.getAnnotation(XQuery.class));
        } else if (repoClass.isAnnotationPresent(XQueries.class)) {
            for (XQuery q : repoClass.getAnnotation(XQueries.class).value()) {
                if (q.autoApply()) {
                    meta.setHasAutoApplyQuery(true);
                }
                meta.addXQuery(q);
            }
        }

        if (entityClass.isAnnotationPresent(XQuery.class)) {
            meta.addXQuery(entityClass.getAnnotation(XQuery.class));
        } else if (entityClass.isAnnotationPresent(XQueries.class)) {
            for (XQuery q : entityClass.getAnnotation(XQueries.class).value()) {
                meta.addXQuery(q);
            }
        }
    }

    /**
     * Scans the repository and entity classes for XAction annotations and registers
     * them.
     * 
     * @param repoClass   the repository class
     * @param entityClass the entity class
     * @param meta        the XResourceMetadata to register the actions in
     */
    private void scanAndRegisterXActions(Class<?> repoClass, Class<?> entityClass, XResourceMetadata meta) {
        log.debug("Scanning repository class '%s' and entity class '%s' for XActions.", repoClass.getName(),
                entityClass.getName());
        if (repoClass.isAnnotationPresent(XAction.class)) {
            meta.addXAction(repoClass.getAnnotation(XAction.class));
        } else if (repoClass.isAnnotationPresent(XActions.class)) {
            for (XAction a : repoClass.getAnnotation(XActions.class).value()) {
                meta.addXAction(a);
            }
        }

        if (entityClass.isAnnotationPresent(XAction.class)) {
            meta.addXAction(entityClass.getAnnotation(XAction.class));
        } else if (entityClass.isAnnotationPresent(XActions.class)) {
            for (XAction a : entityClass.getAnnotation(XActions.class).value()) {
                meta.addXAction(a);
            }
        }
    }

    /**
     * Scans the repository and entity classes for XJSONFormValidators annotations
     * and registers them.
     * 
     * @param repoClass   the repository class
     * @param entityClass the entity class
     * @param meta        the XResourceMetadata to register the validators in
     */
    private void scanAndRegisterXJSONFormValidators(Class<?> repoClass, Class<?> entityClass, XResourceMetadata meta) {
        log.debug("Scanning repository class '%s' and entity class '%s' for XJSONFormValidators.", repoClass.getName(),
                entityClass.getName());
        Map<String, List<Map<String, String>>> validatorsMap = extractValidators(repoClass);
        Map<String, List<Map<String, String>>> entityValidatorsMap = extractValidators(entityClass);
        validatorsMap.putAll(entityValidatorsMap);

        meta.setXJSONFormValidatorsMap(validatorsMap);
    }

    /**
     * Extracts the validators from the class annotations.
     * 
     * @param clazz the class whose validators need to be extracted
     * @return a map of field names to lists of validation rules
     */
    private Map<String, List<Map<String, String>>> extractValidators(Class<?> clazz) {
        Map<String, List<Map<String, String>>> validatorsMap = new HashMap<>();

        if (clazz.isAnnotationPresent(XJSONFormValidators.class)) {
            XJSONFormValidators annotation = clazz.getAnnotation(XJSONFormValidators.class);

            for (XJSONFormFieldValidator fieldValidator : annotation.value()) {
                List<Map<String, String>> ruleList = new ArrayList<>();

                for (XJSONFormValidatorRule rule : fieldValidator.rules()) {
                    Map<String, String> ruleMap = new HashMap<>();
                    ruleMap.put("type", rule.type().name());
                    if (!rule.value().isEmpty()) {
                        ruleMap.put("value", rule.value());
                    }
                    ruleList.add(ruleMap);
                }

                validatorsMap.put(fieldValidator.name(), ruleList);
            }
        }

        return validatorsMap;
    }

    private void processField(Field field, Class<?> entityClass, XResourceMetadata metadata,
            Map<String, JsonNode> externalFieldSchema, boolean isEmbeeddedIdField) {
        field.setAccessible(true);
        String fieldName = field.getName();
        boolean required = isFieldRequired(field);
        String type = resolveType(field);
        String format = resolveFormat(field);
        String columnNme = resolveColumnName(field);
        boolean allowInsert = resolveInsertable(field);
        boolean allowUpdate = resolveUpdatable(field);

        if (required) {
            XValidator validator = (entity, context) -> {
                try {
                    Object value = field.get(entity);
                    if (value == null) {
                        context.addViolation(field.getName(), "Field '" + field.getName() + "' must not be null.");
                    }
                } catch (Exception e) {
                    log.warn("NotNull validation skipped for field: " + field.getName());
                }
            };
            xValidatorRegistry.register(entityClass, validator);
            metadata.addXJSONFormValidatorForField(fieldName,
                    Map.of("type", XJSONFormValidatorType.REQUIRED.name()));
        }

        XFieldMetadata fieldMeta = new XFieldMetadata(fieldName, required, type, format, field, columnNme);
        handleForeignKey(field, fieldMeta);
        handleControlledByActions(field, metadata, fieldMeta, allowInsert, allowUpdate);

        if (field.isAnnotationPresent(XReadonly.class)) {
            fieldMeta.setReadonly(true);
        }

        if (fieldMeta.isReadonly()) {
            XValidator validator = (entity, context) -> {
                try {
                    Object value = field.get(entity);
                    if (value != null) {
                        context.addViolation(field.getName(), "Field '" + field.getName() + "' is readonly.");
                    }
                } catch (Exception e) {
                    log.warn("Readonly validation skipped for field: " + field.getName());
                }
            };
            xValidatorRegistry.register(entityClass, validator);
            metadata.addXJSONFormValidatorForField(fieldName,
                    Map.of("type", XJSONFormValidatorType.READONLY.name()));
        }

        if (field.isAnnotationPresent(XJSONFormFieldMetadata.class)) {
            XJSONFormFieldMetadata xJsonFormFieldMetadata = field.getAnnotation(XJSONFormFieldMetadata.class);
            String label = xJsonFormFieldMetadata.label();
            String description = xJsonFormFieldMetadata.description();
            int displayseq = xJsonFormFieldMetadata.displaySeq();
            boolean isIncludedInform = xJsonFormFieldMetadata.includeInJSONForm();

            if (label != null)
                fieldMeta.setLabel(label);
            if (description != null)
                fieldMeta.setDescription(description);
            if (displayseq != -1)
                fieldMeta.setDisplaySeq(displayseq);

            fieldMeta.setIncludeinjsonform(isIncludedInform);
        }

        String snakeCaseFieldName = toSnakeCase(fieldName);
        if (externalFieldSchema.containsKey(snakeCaseFieldName)) {
            JsonNode fieldJson = externalFieldSchema.get(snakeCaseFieldName);
            if (fieldJson.has("label"))
                fieldMeta.setLabel(fieldJson.get("label").asText());
            if (fieldJson.has("description"))
                fieldMeta.setDescription(fieldJson.get("description").asText());
            if (fieldJson.has("default"))
                fieldMeta.setDefaultValue(fieldJson.get("default").asText());
            if (fieldJson.has("required") || isEmbeeddedIdField)
                fieldMeta.setRequired(true);
            if (fieldJson.has("format"))
                fieldMeta.setFormat(fieldJson.get("format").asText());
            if (fieldJson.has("includeinjsonform"))
                fieldMeta.setIncludeinjsonform(fieldJson.get("includeinjsonform").asBoolean());
            if (fieldJson.has("displayseq"))
                fieldMeta.setDisplaySeq(fieldJson.get("displayseq").asInt());
            if (fieldJson.has("enum")) {
                List<String> enumList = new ArrayList<>();
                for (JsonNode option : fieldJson.get("enum")) {
                    enumList.add(option.asText());
                }
                fieldMeta.setEnumValues(enumList);
            }
        }

        log.debug("Cheking for any field acces applied...");
        applyFieldAccess(entityClass, field, fieldMeta);

        if (field.isAnnotationPresent(XHidden.class)) {
            fieldMeta.setHidden(true);
        }

        List<String> primaryKeyFields = new ArrayList<>();
        boolean hasCompositeKey = false;

        if (field.isAnnotationPresent(Id.class)) {
            primaryKeyFields.add(field.getName());
            if (field.isAnnotationPresent(GeneratedValue.class)) {
                log.debug("Field %s is auto-generated.", field.getName());
            }
        } else if (field.isAnnotationPresent(EmbeddedId.class)) {
            hasCompositeKey = true;
            Class<?> embeddedClass = field.getType();
            metadata.setEmbeddedIdClassType(embeddedClass);
            metadata.setEmbeddedKeyFieldName(field.getName());

            for (Field embeddedField : embeddedClass.getDeclaredFields()) {
                if (embeddedField.isAnnotationPresent(Column.class)) {
                    embeddedField.setAccessible(true);
                    primaryKeyFields.add(embeddedField.getName());
                    processField(field, entityClass, metadata, externalFieldSchema, true);
                }
            }
        }

        if (entityClass.isAnnotationPresent(IdClass.class)) {
            hasCompositeKey = true;
            Class<?> idClass = entityClass.getAnnotation(IdClass.class).value();
            for (Field idField : idClass.getDeclaredFields()) {
                if (idField.isAnnotationPresent(Column.class)) {
                    idField.setAccessible(true);
                    primaryKeyFields.add(idField.getName());
                }
            }
        }

        if (hasCompositeKey) {
            metadata.setHasCompositeKey(true);

        }

        if (!primaryKeyFields.isEmpty()) {
            metadata.setPrimaryKey(
                    hasCompositeKey ? String.join(",", primaryKeyFields) : primaryKeyFields.get(0));
        }

        // Register Jakarta Validation API validators
        XValidationUtil.registerFieldValidators(field, entityClass, xValidatorRegistry, log);

        // Register UI Validators
        XValidationUtil.registerUIValidators(field, metadata);
        if (isEmbeeddedIdField)
            metadata.addEmbeddedIdClassField(fieldName, fieldMeta);
        else
            metadata.addField(fieldName, fieldMeta);
    }

    private void handleControlledByActions(Field field, XResourceMetadata resourceMeta, XFieldMetadata metadata,
            boolean allowInsert,
            boolean allowUpdate) {

        if (!allowInsert)
            metadata.setAllowInsert(false);
        if (!allowUpdate)
            metadata.setAllowUpdate(false);

        if (field.isAnnotationPresent(XControlledByAction.class)) {
            XControlledByAction xControlledByAction = field.getAnnotation(XControlledByAction.class);
            boolean allowInsertFromXControlled = xControlledByAction.allowInsert();
            boolean allowUpdateFromXControlled = xControlledByAction.allowUpdate();

            // If in Column or JoinColumn default values are overriden for insertable and
            // updatable; like to marked to false then consider state from JPA classes
            // If it is marked as true which is by default still some one set allowInsertTo
            // false here then through exception field is not allowed for insert or update
            if (!allowInsert || (allowInsert && !allowInsertFromXControlled)) {
                metadata.setAllowInsert(false);
            }
            if (!allowUpdate || (allowUpdate && !allowUpdateFromXControlled)) {
                metadata.setAllowUpdate(false);
            }

            XFieldAction[] xFieldActions = xControlledByAction.actions();
            for (XFieldAction action : xFieldActions) {
                resourceMeta.addXFieldAction(metadata.getName(), action.name(), action);
            }

        }

        if (!metadata.isAllowInsert() && !metadata.isAllowUpdate())
            metadata.setReadonly(true);
    }

    public boolean handleForeignKey(Field field, XFieldMetadata metadata) {
        boolean isManyToOne = field.isAnnotationPresent(ManyToOne.class);
        boolean isOneToOne = field.isAnnotationPresent(OneToOne.class);
        boolean hasJoinColumn = field.isAnnotationPresent(JoinColumn.class);
        boolean hasJoinColumns = field.isAnnotationPresent(JoinColumns.class);

        if ((!hasJoinColumn && !hasJoinColumns) || (!isManyToOne && !isOneToOne)) {
            return false;
        }

        Class<?> targetEntityClass = field.getType();
        metadata.setForeignKey(true);
        metadata.setForeignKeyRefTable(getTableName(targetEntityClass));

        if (hasJoinColumns) {
            JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
            Map<String, String> joinMap = new LinkedHashMap<>();
            for (JoinColumn jc : joinColumns.value()) {
                String localCol = jc.name().isEmpty() ? field.getName() + "_" + jc.referencedColumnName() : jc.name();
                String refCol = jc.referencedColumnName();
                joinMap.put(localCol, refCol);
            }
            metadata.setCompositeForeignKey(true);
            metadata.setCompositeForeignKeyMap(joinMap);
        } else {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            String localCol = joinColumn.name().isEmpty() ? field.getName() + "_id" : joinColumn.name();

            Field idField = findIdField(targetEntityClass);
            if (idField == null) {
                throw new IllegalStateException(
                        "No primary key field found in foreign key entity: " + targetEntityClass.getName());
            }

            metadata.setForeignKeyColumn(localCol);
            metadata.setForeignKeyRefField(idField.getName());
            metadata.setForeignKeyRefColumn(resolveColumnName(idField));
            metadata.setCompositeForeignKey(false);
            metadata.setCompositeForeignKeyMap(null);
        }

        return true;
    }

    public Field findIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        return null;
    }

    public String getTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                return table.name();
            }
        }
        return entityClass.getSimpleName();
    }

    /**
     * Role-based access evaluation for a field.
     */
    private AccessLevel getEffectiveAccess(List<String> roles, XFieldMetadata fieldMeta) {
        for (String role : roles) {
            AccessLevel level = fieldMeta.getAccessLevelForRole(role);
            if (level != AccessLevel.NONE) {
                return level;
            }
        }
        return AccessLevel.NONE;
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
     * Applies {@link XFieldAccess} annotation logic to populate access levels
     * in the given {@link XFieldMetadata}.
     *
     * @param field      the reflection field to scan
     * @param xFieldMeta metadata object to populate access map
     */
    private void applyFieldAccess(Class<?> entityClass, Field field, XFieldMetadata xFieldMeta) {
        if (field.isAnnotationPresent(XFieldAccess.class)) {
            XFieldAccess access = field.getAnnotation(XFieldAccess.class);

            for (String role : access.denyRoles()) {
                xFieldMeta.addFieldAccess(role, AccessLevel.NONE);
            }

            for (String role : access.readRoles()) {
                // Prevent overwriting WRITE if already stronger
                xFieldMeta.addFieldAccess(role, AccessLevel.READ);
            }

            for (String role : access.writeRoles()) {
                xFieldMeta.addFieldAccess(role, AccessLevel.WRITE);
            }

            XValidator fieldAuthResourceWriteValidator = (entity, context) -> {

                log.debug("Checking field level access for field %s", field.getName());
                List<String> roles = getCurrentUserRoles();
                AccessLevel level = getEffectiveAccess(roles, xFieldMeta);

                try {
                    field.setAccessible(true);
                    if (field.get(entity) != null && level == AccessLevel.NONE)
                        throw new XAccessDeniedException("No write access to field: " + xFieldMeta);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                    // Ignore
                }

            };
            xValidatorRegistry.register(entityClass, fieldAuthResourceWriteValidator);

        }
    }

    /**
     * Applies {@link XResourceAccess} annotation logic to populate role-based
     * access levels in the given {@link XResourceMetadata}.
     *
     * @param entityClass     the entity class to scan
     * @param repositoryClass the repository class to scan (takes priority if
     *                        annotated)
     * @param xResourceMeta   the metadata object to populate access map
     */
    private void applyResourceAccess(Class<?> entityClass, Class<?> repositoryClass, XResourceMetadata xResourceMeta) {
        // Start with repository-level annotation if present
        XResourceAccess access = repositoryClass != null && repositoryClass.isAnnotationPresent(XResourceAccess.class)
                ? repositoryClass.getAnnotation(XResourceAccess.class)
                : entityClass.isAnnotationPresent(XResourceAccess.class)
                        ? entityClass.getAnnotation(XResourceAccess.class)
                        : null;

        if (access == null) {
            return; // No access rules to apply
        }

        // Apply denyRoles first — highest precedence
        for (String role : access.denyRoles()) {
            xResourceMeta.addResourceAccess(role, AccessLevel.NONE);
        }

        // Apply readRoles (only if not denied)
        for (String role : access.readRoles()) {
            xResourceMeta.addResourceAccess(role, AccessLevel.READ);
        }

        // Apply writeRoles (only if not denied)
        for (String role : access.writeRoles()) {
            xResourceMeta.addResourceAccess(role, AccessLevel.WRITE);
        }
    }

}
