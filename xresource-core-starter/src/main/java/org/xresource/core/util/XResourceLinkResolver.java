package org.xresource.core.util;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import org.springframework.stereotype.Component;

import org.xresource.core.model.XResourceMetadata;

import java.lang.reflect.Field;
@Component
public class XResourceLinkResolver {

    
    public String buildResourceLink(Object entity,XResourceMetadata metadata,String baseUrl, String fieldName){
        String urn = XURNEncoder.getURN(entity, metadata.getResourceName(), metadata);
        String resourceURI= baseUrl + "/api/resource/" + metadata.getResourceName() + "/" + urn + "/" +fieldName;

        return resourceURI;
    }
    
    public String buildResourceLink(Object entity, String baseUrl) {
        try {
            Object idValue = extractEntityId(entity.getClass(), entity);
            if (idValue == null) return null;

            String resourceName = entity.getClass().getSimpleName().toLowerCase();
            return baseUrl + "/api/resource/" + resourceName + "/" + idValue;

        } catch (Exception e) {
            return null;
        }
    }

    public String buildResourceLinkFromJoinColumn(Object parent, Field field, String baseUrl) {
        try {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (joinColumn == null || joinColumn.name().isEmpty()) return null;

            // Get the referenced column from DB via join column value on parent
            String joinColumnName = joinColumn.name();
            //Ref column of the joined table
            String refColumn = joinColumn.referencedColumnName();
            String refTable = field.getType().getAnnotation(jakarta.persistence.Table.class).name();

            
            return baseUrl + "/api/resource/" + refTable + "/" + "";

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

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
