package org.xresource.core.model;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.xresource.core.annotation.AccessLevel;
import org.xresource.core.annotation.XAction;
import org.xresource.core.annotation.XQuery;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class XResourceMetadata {
    private Class<?> entityClass;
    private Class<?> repositoryClass;
    private Map<String, XFieldMetadata> fields = new HashMap<>();
    private Map<String, XFieldMetadata> embeddedIdClassFields = new HashMap<>();
    private Map<String,AccessLevel> accessMap = new HashMap<String,AccessLevel>();
    private Map<String,XQuery> xQueriesMap = new HashMap<String,XQuery>();
    private Map<String,XAction> xActionsMap = new HashMap<String,XAction>();
    private Map<String, List<Map<String, String>>> xJSONFormValidatorsMap = new HashMap<String, List<Map<String, String>>>();
    private boolean hasAutoApplyQuery;
    private boolean hasCompositeKey;
    private Class<?> embeddedIdClassType;
    private String embeddedKeyFieldName;
    private String primaryKey;
    private String resourceName;

    @JsonIgnore
    private String rawSchema;

    public void addField(String fieldName, XFieldMetadata metadata) {
        this.fields.put(fieldName, metadata);
    }

    public void addEmbeddedIdClassField(String fieldName, XFieldMetadata metadata) {
        this.embeddedIdClassFields.put(fieldName, metadata);
    }

    public void addResourceAccess(String role,AccessLevel lavel){
        this.accessMap.put(role,lavel);
    }

    public AccessLevel getAccessLevelForRole(String role){
        return this.accessMap.containsKey(role) ? this.accessMap.get(role) : AccessLevel.WRITE; 
    }
    
    public void addXQuery(XQuery query){
        this.xQueriesMap.put(query.name(),query);
    }

    public Optional<XQuery> getXQuery(String name){
        return Optional.ofNullable(xQueriesMap.get(name));
    }

    public void addXAction(XAction action){
        this.xActionsMap.put(action.name(), action);
    }
    public Optional<XAction> getXAction(String name){
        return Optional.ofNullable(xActionsMap.get(name));
    }

    public Optional<List<Map<String,String>>> getXJSONFormValidatorsForField(String fieldName){
        return Optional.ofNullable(xJSONFormValidatorsMap.get(fieldName));
    }

    public Map<String,List<Map<String,String>>> getXJSONFormValidators(String fieldName){
        return xJSONFormValidatorsMap;
    }
   
    public boolean hasCompositeKey(){
        return this.hasCompositeKey;
    }
   
}
