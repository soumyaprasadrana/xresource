package org.xresource.core.scanner;


import org.xresource.core.annotation.*;
import org.xresource.core.model.*;
import org.xresource.core.registry.XResourceMetadataRegistry;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.lang.reflect.Field;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class XMetadataScanner {

    private final XResourceMetadataRegistry registry;

    public XMetadataScanner(XResourceMetadataRegistry registry) {
        this.registry = registry;
    }

    public void scan(String tableName, Class<?> entityClass, Class<?> repository) {
        XResourceMetadata metadata = new XResourceMetadata();
        metadata.setEntityClass(entityClass);
        metadata.setRepositoryClass(repository);
        metadata.setResourceName(tableName);
        
        this.scanAndRegisterXQueries(repository,entityClass,metadata);
        this.scanAndRegisterXActions(repository,entityClass,metadata);
        this.scanAndRegisterXJSONFormValidators(repository,entityClass,metadata);
        
        Map<String, JsonNode> externalFieldSchema = new HashMap<>();
        String rawSchema = null;

        if (repository.isAnnotationPresent(XMetadata.class)) {
            String path = repository.getAnnotation(XMetadata.class).path();
            try (InputStream is = repository.getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    rawSchema = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode schemaNode = mapper.readTree(rawSchema);
    
                    if (schemaNode.has("fields")) {
                        // Iterate over the field names at the root level of the node
                        Iterator<String> fieldNames = schemaNode.get("fields").fieldNames();
                        int fieldIndex = 0;
                        while (fieldNames.hasNext()) {
                            String fieldName = fieldNames.next();  // The name of the node itself
                            JsonNode fieldNode = schemaNode.get("fields").get(fieldName);
                            ((ObjectNode)fieldNode).put("displayseq",fieldIndex);


                            // You can now use the fieldName as the key
                            externalFieldSchema.put(fieldName, fieldNode);
                            fieldIndex++;
                        }
                    }
    
                    metadata.setRawSchema(rawSchema);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load schema for " + entityClass.getName(), e);
            }
        }
    
        if (entityClass.isAnnotationPresent(XMetadata.class)) {
            String path = entityClass.getAnnotation(XMetadata.class).path();
            try (InputStream is = entityClass.getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    rawSchema = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode schemaNode = mapper.readTree(rawSchema);
    
                    if (schemaNode.has("fields")) {
                        // Iterate over the field names at the root level of the node
                        Iterator<String> fieldNames = schemaNode.get("fields").fieldNames();
                        int fieldIndex = 0;
                        while (fieldNames.hasNext()) {
                            String fieldName = fieldNames.next();  // The name of the node itself
                            JsonNode fieldNode = schemaNode.get("fields").get(fieldName);
                           ((ObjectNode)fieldNode).put("displayseq",fieldIndex);

                            // You can now use the fieldName as the key
                            externalFieldSchema.put(fieldName, fieldNode);
                            fieldIndex++;
                        }
                    }
    
                    metadata.setRawSchema(rawSchema);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load schema for " + entityClass.getName(), e);
            }
        }


        if (entityClass.isAnnotationPresent(XResourceAuthGroup.class) || entityClass.isAnnotationPresent(XResourceAuthGroups.class)) {
            XResourceAuthGroup[] authGroups = entityClass.getAnnotationsByType(XResourceAuthGroup.class);
            for (XResourceAuthGroup auth : authGroups) {
                metadata.addResourceAccess( auth.role(), auth.access());
            }

        }

        if (repository.isAnnotationPresent(XResourceAuthGroup.class) || repository.isAnnotationPresent(XResourceAuthGroups.class)) {
            XResourceAuthGroup[] authGroups = repository.getAnnotationsByType(XResourceAuthGroup.class);
            for (XResourceAuthGroup auth : authGroups) {
                metadata.addResourceAccess( auth.role(), auth.access());
            }

        }

        boolean foundCompositeKey = false;
    
        for (Field field : getAllFields(entityClass)) {
            field.setAccessible(true);
            String fieldName = field.getName();
            boolean required = isFieldRequired(field);
            String type = resolveType(field);
            String format = resolveFormat(field);
    
            XFieldMetadata fieldMeta = new XFieldMetadata(fieldName, required, type, format, field);
    
            // Enrich from external JSON schema if available
            String snakeCaseFieldName = toSnakeCase(fieldName);
            if (externalFieldSchema.containsKey(snakeCaseFieldName)) {
                JsonNode fieldJson = externalFieldSchema.get(snakeCaseFieldName);

                if (fieldJson.has("label")) fieldMeta.setLabel(fieldJson.get("label").asText());
                if (fieldJson.has("description")) fieldMeta.setDescription(fieldJson.get("description").asText());
                if (fieldJson.has("default")) fieldMeta.setDefaultValue(fieldJson.get("default").asText());

                //Overriding metadata values as they are more trustworthy than JPA Classes
                if(fieldJson.has("required")) fieldMeta.setRequired(true);
                if(fieldJson.has("format")) fieldMeta.setFormat(fieldJson.get("format").asText());

                //Check if explicitely marked to be included in json form 
                if(fieldJson.has("includeinjsonform")) fieldMeta.setIncludeinjsonform(fieldJson.get("includeinjsonform").asBoolean());
                

                if(fieldJson.has("displayseq")) 
                    fieldMeta.setDisplaySeq(fieldJson.get("displayseq").asInt());
               


                if (fieldJson.has("enum")) {
                    List<String> enumList = new ArrayList<>();
                    for (JsonNode option : fieldJson.get("enum")) {
                        enumList.add(option.asText());
                    }
                    fieldMeta.setEnumValues(enumList);
                }
            }
            if (field.isAnnotationPresent(XResourceAuthGroup.class) || field.isAnnotationPresent(XResourceAuthGroups.class)) {
                XResourceAuthGroup[] authGroups = field.getAnnotationsByType(XResourceAuthGroup.class);
                for (XResourceAuthGroup auth : authGroups) {
                    fieldMeta.addFieldAccess( auth.role(), auth.access());
                }
            }
    
            if (field.isAnnotationPresent(XHidden.class)) {
                fieldMeta.addFieldAccess("*", AccessLevel.NONE);
            }

            List<String> primaryKeyFields = new ArrayList<>();
            boolean hasCompositeKey = false;
            if (field.isAnnotationPresent(Id.class)) {
                primaryKeyFields.add(field.getName());

                if (field.isAnnotationPresent(GeneratedValue.class)) {
                    System.out.println("Field " + field.getName() + " is auto-generated.");
                }
            }

            // Check for @EmbeddedId (composite key)
            else if (field.isAnnotationPresent(EmbeddedId.class)) {
                hasCompositeKey = true;
                Class<?> embeddedClass = field.getType();
                String embeddedKeyFieldName = field.getName();     
                metadata.setEmbeddedIdClassType(embeddedClass);
                metadata.setEmbeddedKeyFieldName(embeddedKeyFieldName);

                for (Field embeddedField : embeddedClass.getDeclaredFields()) {
                    if(embeddedField.isAnnotationPresent(Column.class)){
                        embeddedField.setAccessible(true);
                        primaryKeyFields.add(embeddedField.getName());
                        metadata.addEmbeddedIdClassField(embeddedField.getName(), processEmbbededIdClassField(embeddedField, externalFieldSchema));
                    }
                }
            }
        

        // Optional: check for @IdClass (alternate composite key strategy)
        if (entityClass.isAnnotationPresent(IdClass.class)) {
            hasCompositeKey = true;
            Class<?> idClass = entityClass.getAnnotation(IdClass.class).value();

            for (Field idField : idClass.getDeclaredFields()) {
                if(idField.isAnnotationPresent(Column.class)){
                    idField.setAccessible(true);
                    primaryKeyFields.add(idField.getName());
                }
                
            }
        }
        if(!foundCompositeKey && hasCompositeKey){
            metadata.setHasCompositeKey(hasCompositeKey);
            foundCompositeKey = true;
        }
           

        if (!primaryKeyFields.isEmpty()) {
            metadata.setPrimaryKey(
                hasCompositeKey
                ? String.join(",", primaryKeyFields)
                : primaryKeyFields.get(0)
            );
        }
    
            metadata.addField(fieldName, fieldMeta);
    
           
        }
    
        registry.register(tableName, metadata);
    }
    private String toSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
    private boolean isFieldRequired(Field field) {
        return (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).nullable()) ||
               field.isAnnotationPresent(NotNull.class);
    }

    private String resolveType(Field field) {
        Class<?> type = field.getType();
        if (type == String.class) return "string";
        if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
            if (type == int.class || type == Integer.class) return "integer";
            if (type == long.class || type == Long.class) return "long";
            if (type == double.class || type == Double.class || type == float.class || type == Float.class) return "number";
        }
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (Date.class.isAssignableFrom(type)) return "date";
        return "object";
    }

    private String resolveFormat(Field field) {
        if (field.isAnnotationPresent(Email.class)) return "email";
        if (field.isAnnotationPresent(Pattern.class)) {
            return field.getAnnotation(Pattern.class).regexp();
        }
        return "";
    }

    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
    }

    private void scanAndRegisterXQueries(Class<?> repoClass,Class<?> entityClass, XResourceMetadata meta) {
        
        if (repoClass.isAnnotationPresent(XQuery.class)) {
            meta.addXQuery(repoClass.getAnnotation(XQuery.class));
        } else if (repoClass.isAnnotationPresent(XQueries.class)) {
            for (XQuery q : repoClass.getAnnotation(XQueries.class).value()) {
                if(q.autoApply()){
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

    private void scanAndRegisterXActions(Class<?> repoClass,Class<?> entityClass, XResourceMetadata meta) {
        
        if (repoClass.isAnnotationPresent(XAction.class)) {
            meta.addXAction(repoClass.getAnnotation(XAction.class));
        } else if (repoClass.isAnnotationPresent(XAction.class)) {
            for (XAction a : repoClass.getAnnotation(XActions.class).value()) {
                meta.addXAction(a);
            }
        }

        if (entityClass.isAnnotationPresent(XAction.class)) {
            meta.addXAction(entityClass.getAnnotation(XAction.class));
        } else if (entityClass.isAnnotationPresent(XAction.class)) {
            for (XAction a : entityClass.getAnnotation(XActions.class).value()) {
                meta.addXAction(a);
            }
        }
    }

    private void scanAndRegisterXJSONFormValidators(Class<?> repoClass,Class<?> entityClass, XResourceMetadata meta) {
        
        Map<String, List<Map<String, String>>> validatorsMap = extractValidators(repoClass);
        Map<String, List<Map<String, String>>> entityValidatorsMap = extractValidators(entityClass);
        validatorsMap.putAll(entityValidatorsMap);
        
        meta.setXJSONFormValidatorsMap(validatorsMap);
        
    }

    private  Map<String, List<Map<String, String>>> extractValidators(Class<?> clazz) {
        Map<String, List<Map<String, String>>> validatorsMap = new HashMap<>();

        if (clazz.isAnnotationPresent(XJSONFormValidators.class)) {
            XJSONFormValidators annotation = clazz.getAnnotation(XJSONFormValidators.class);

            for (XJSONFormFieldValidaor fieldValidator : annotation.value()) {
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

    private XFieldMetadata processEmbbededIdClassField(Field field,Map<String, JsonNode> externalFieldSchema){
        String fieldName = field.getName();
        boolean required = isFieldRequired(field);
        String type = resolveType(field);
        String format = resolveFormat(field);

        XFieldMetadata fieldMeta = new XFieldMetadata(fieldName, required, type, format, field);

        // Enrich from external JSON schema if available
        String snakeCaseFieldName = toSnakeCase(fieldName);
        if (externalFieldSchema.containsKey(snakeCaseFieldName)) {
            JsonNode fieldJson = externalFieldSchema.get(snakeCaseFieldName);

            if (fieldJson.has("label")) fieldMeta.setLabel(fieldJson.get("label").asText());
            if (fieldJson.has("description")) fieldMeta.setDescription(fieldJson.get("description").asText());
            if (fieldJson.has("default")) fieldMeta.setDefaultValue(fieldJson.get("default").asText());

            //Overriding metadata values as they are more trustworthy than JPA Classes
            if(fieldJson.has("required")) fieldMeta.setRequired(true);
            if(fieldJson.has("format")) fieldMeta.setFormat(fieldJson.get("format").asText());

            //Check if explicitely marked to be included in json form 
            if(fieldJson.has("includeinjsonform")) fieldMeta.setIncludeinjsonform(fieldJson.get("includeinjsonform").asBoolean());
            
            if(fieldJson.has("displayseq")) 
                fieldMeta.setDisplaySeq(fieldJson.get("displayseq").asInt());
            

            

            if (fieldJson.has("enum")) {
                List<String> enumList = new ArrayList<>();
                for (JsonNode option : fieldJson.get("enum")) {
                    enumList.add(option.asText());
                }
                fieldMeta.setEnumValues(enumList);
            }
        }
        return fieldMeta;   
    }
}
