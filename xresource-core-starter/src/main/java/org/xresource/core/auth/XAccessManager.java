package org.xresource.core.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.core.JsonGenerator;

import org.xresource.core.model.ForeignKeyTree;
import org.xresource.core.model.XFieldMetadata;
import org.xresource.core.model.XResourceMetadata;
import org.xresource.core.annotation.AccessLevel;
import org.xresource.core.annotation.XForceAllowResourceRef;
import org.xresource.core.exception.ResourceNotFoundException;
import org.xresource.core.exception.XAccessDeniedException;
import org.xresource.core.exception.XValidationException;
import org.xresource.core.registry.XResourceMetadataRegistry;
import org.xresource.core.util.XResourceLinkResolver;
import org.xresource.core.validation.ValidationContext;
import org.xresource.core.validation.XValidator;
import org.xresource.core.validation.XValidatorRegistry;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.hibernate.Hibernate;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;

@Component
public class XAccessManager {

    private final ObjectMapper objectMapper;

    @Autowired
    private XResourceMetadataRegistry metadataRegistry;

    @Autowired
    private XResourceLinkResolver linkResolver;

    public XAccessManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectNode filterFieldsByAccess(Object entity, XResourceMetadata metadata, List<String> userRoles,String baseUrl) {
        return filterFieldsByAccess(entity, metadata, userRoles, new ForeignKeyTree(), baseUrl);
    }

    /**
     * Performs recursive access filtering on the object for all levels of nesting.
     */
public ObjectNode filterFieldsByAccess(Object entity, XResourceMetadata metadata,
                                       List<String> userRoles,
                                       ForeignKeyTree fkTree,
                                       String baseUrl) {

    ObjectNode result = objectMapper.createObjectNode();
    Map<String, XFieldMetadata> fields = metadata.getFields();

    for (Map.Entry<String, XFieldMetadata> entry : fields.entrySet()) {
        String fieldName = entry.getKey();
        XFieldMetadata fieldMeta = entry.getValue();
        AccessLevel effectiveAccess = getEffectiveAccess(userRoles, fieldMeta);

        if (effectiveAccess == AccessLevel.NONE) continue;

        Field field = findField(entity.getClass(), fieldName);
        if (field == null) continue;

        try {
            field.setAccessible(true);

            // Skip if Jackson tells us to
            if (field.isAnnotationPresent(JsonBackReference.class) || field.isAnnotationPresent(JsonIgnore.class)) {
                if(field.isAnnotationPresent(XForceAllowResourceRef.class))
                {
                    //ignore
                }
                else
                    continue;
            }

            Object fieldValue = field.get(entity);

            // Skip nulls if @JsonInclude(Include.NON_NULL) is present
            JsonInclude include = field.getAnnotation(JsonInclude.class);
            if (include != null && include.value() == JsonInclude.Include.NON_NULL && fieldValue == null) {
                continue;
            }

            // Field rename logic
            String outputFieldName = fieldName;
            JsonProperty jsonProp = field.getAnnotation(JsonProperty.class);
            if (jsonProp != null && !jsonProp.value().isEmpty()) {
                outputFieldName = jsonProp.value();
            }

            // Check if FK expansion is requested
            boolean isNestedFkExplicitlyRequested = fkTree.hasChild(fieldName);
            ForeignKeyTree nestedFkTree = fkTree.getChild(fieldName);

            // Handle Hibernate proxy
            if (fieldValue instanceof HibernateProxy || fieldValue instanceof PersistentCollection) {
                if (!isNestedFkExplicitlyRequested) {
                    if (baseUrl != null) {
                        String refUrl = linkResolver.buildResourceLink(entity, metadata, baseUrl, fieldName);
                        if (refUrl != null) {
                            result.put(outputFieldName + "_resource", refUrl);
                        }
                    }
                    continue;
                }

                Hibernate.initialize(fieldValue);
                fieldValue = ((HibernateProxy) fieldValue).getHibernateLazyInitializer().getImplementation();
            }

            if (fieldValue == null) continue;

            // Foreign key nested expansion
            if (fieldValue.getClass().isAnnotationPresent(Entity.class)) {
                String resourceName = fieldValue.getClass().getSimpleName();
                Table table = fieldValue.getClass().getAnnotation(Table.class);
                if (table != null && !table.name().isEmpty()) {
                    resourceName = table.name();
                }

                XResourceMetadata nestedMeta = metadataRegistry.get(resourceName);
                if (nestedMeta != null) {
                    if (!isNestedFkExplicitlyRequested) {
                        if (baseUrl != null) {
                            String refUrl = linkResolver.buildResourceLink(entity, metadata, baseUrl, fieldName);
                            if (refUrl != null) {
                                result.put(outputFieldName + "_resource", refUrl);
                            }
                        }
                        continue;
                    } else {
                        ObjectNode nestedNode = filterFieldsByAccess(fieldValue, nestedMeta, userRoles, nestedFkTree, baseUrl);
                        result.set(outputFieldName, nestedNode);
                        continue;
                    }
                }
            }

            if (fieldValue instanceof List<?>) {
                List<?> list = (List<?>) fieldValue;
            
                if (!list.isEmpty()) {
                    Object firstElement = list.get(0);
                    Class<?> elementClass = Hibernate.unproxy(firstElement).getClass(); // Unwrap Hibernate proxy if needed
            
                    Table tableAnnotation = elementClass.getAnnotation(Table.class);
                    if (tableAnnotation != null) {
                        String listElementResourceName = tableAnnotation.name(); // this is your resource name
            
                        XResourceMetadata listElementMeta = metadataRegistry.get(listElementResourceName);
                        if (listElementMeta != null) {
                            if (!isNestedFkExplicitlyRequested) {
                                if (baseUrl != null) {
                                    String refUrl = linkResolver.buildResourceLink(entity, metadata, baseUrl, fieldName);
                                    if (refUrl != null) {
                                        result.put(outputFieldName + "_resource", refUrl);
                                    }
                                }
                                continue;
                            } else {
                                ArrayNode nestedArray = objectMapper.createArrayNode();
                                for (Object element : list) {
                                    ObjectNode itemNode = filterFieldsByAccess(element, listElementMeta, userRoles, nestedFkTree, baseUrl);
                                    nestedArray.add(itemNode);
                                }
                                result.set(outputFieldName, nestedArray);
                                continue;
                            }
                        }
                    }
                }
            }
            

            // Support for @JsonFormat and @JsonSerialize
            JsonNode nodeValue;
            if (field.isAnnotationPresent(JsonSerialize.class)) {
                JsonSerializer<?> customSerializer = field.getAnnotation(JsonSerialize.class).using().getDeclaredConstructor().newInstance();
                
                // Explicitly cast to JsonSerializer<Object> to avoid the compilation error
                JsonSerializer<Object> typedSerializer = (JsonSerializer<Object>) customSerializer;
                
                // Create a StringWriter and JsonGenerator to serialize the value
                StringWriter writer = new StringWriter();
                JsonGenerator generator = objectMapper.getFactory().createGenerator(writer);
                
                // Get the provider to pass into the serializer
                SerializerProvider provider = objectMapper.getSerializerProvider();
                
                // Manually serialize the object using the custom serializer
                typedSerializer.serialize(fieldValue, generator, provider);
                generator.close();
                
                // Convert the serialized string to a JsonNode
                nodeValue = objectMapper.readTree(writer.toString());
            } else {
                // Regular serialization
                nodeValue = objectMapper.valueToTree(fieldValue);
            }
            

            result.set(outputFieldName, nodeValue);

        } catch (Exception e) {
            // Optional: Log or debug
        }
    }

    return result;
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
    
    
    

    /**
     * Throws exception if the user does not have read access to the resource.
     */
    public void checkReadAccess(List<String> roles, XResourceMetadata metadata, String resourceName) {
        if (metadata == null) {
            throw new ResourceNotFoundException("Resource not found: " + resourceName);
        }

        if (getEffectiveAccessForResource(roles, metadata) == AccessLevel.NONE) {
            throw new XAccessDeniedException("No read access to resource: " + resourceName);
        }
    }

    /**
     * Throws exception if the user does not have write access to the resource.
     */
    public void checkWriteAccess(List<String> roles, XResourceMetadata metadata, String resourceName) {
        if (metadata == null) {
            throw new ResourceNotFoundException("Resource not found: " + resourceName);
        }

        if (getEffectiveWriteAccessForResource(roles, metadata) == AccessLevel.NONE) {
            throw new XAccessDeniedException("No write access to resource: " + resourceName);
        }
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
     * Role-based access evaluation for a resource.
     */
    private AccessLevel getEffectiveAccessForResource(List<String> roles, XResourceMetadata resourceMeta) {
        for (String role : roles) {
            AccessLevel level = resourceMeta.getAccessLevelForRole(role);
            if (level != AccessLevel.NONE) {
                return level;
            }
        }
        return AccessLevel.NONE;
    }

    private AccessLevel getEffectiveWriteAccessForResource(List<String> roles, XResourceMetadata resourceMeta) {
        for (String role : roles) {
            AccessLevel level = resourceMeta.getAccessLevelForRole(role);
            if (level == AccessLevel.WRITE) {
                return level;
            }
        }
        return AccessLevel.NONE;
    }

    public void validateEntity(Object entity, ValidationContext.OperationType type) {
        ValidationContext ctx = new ValidationContext(type);
        for (XValidator validator : XValidatorRegistry.getValidators(entity.getClass())) {
            validator.validate(entity, ctx);
        }
    
        if (ctx.hasViolations()) {
            throw new XValidationException(ctx);
        }
    }
    
}
