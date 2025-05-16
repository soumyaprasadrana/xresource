package org.xresource.core.model;

import java.util.Map;
import java.util.Optional;

import org.xresource.core.annotations.AccessLevel;
import org.xresource.core.annotations.XAction;
import org.xresource.core.annotations.XFieldAction;
import org.xresource.core.annotations.XQuery;
import org.xresource.core.exception.XInvalidConfigurationException;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class XResourceMetadata {
    private Class<?> entityClass;
    private Class<?> repositoryClass;
    private Map<String, XFieldMetadata> fields = new HashMap<>();
    private Map<String, XFieldMetadata> embeddedIdClassFields = new HashMap<>();
    private Map<String, AccessLevel> accessMap = new HashMap<String, AccessLevel>();
    private Map<String, XQuery> xQueriesMap = new HashMap<String, XQuery>();
    private Map<String, XAction> xActionsMap = new HashMap<String, XAction>();
    private Map<String, Map<String, XFieldAction>> xFieldActions = new HashMap<String, Map<String, XFieldAction>>();
    private Map<String, List<Map<String, String>>> xJSONFormValidatorsMap = new HashMap<String, List<Map<String, String>>>();
    private boolean hasAutoApplyQuery;
    private boolean hasCompositeKey;
    private Class<?> embeddedIdClassType;
    private String embeddedKeyFieldName;
    private String primaryKey;
    private String resourceName;
    private boolean isExposedForEmbbededReferencesOnly = false;
    private boolean isExposedForCron = false;

    @JsonIgnore
    private String rawSchema;

    public void addField(String fieldName, XFieldMetadata metadata) {
        this.fields.put(fieldName, metadata);
    }

    public XFieldMetadata getField(String fieldName) {
        return this.fields.get(fieldName);
    }

    public void addEmbeddedIdClassField(String fieldName, XFieldMetadata metadata) {
        this.embeddedIdClassFields.put(fieldName, metadata);
    }

    public void addResourceAccess(String role, AccessLevel lavel) {
        this.accessMap.put(role, lavel);
    }

    public AccessLevel getAccessLevelForRole(String role) {
        return this.accessMap.containsKey(role) ? this.accessMap.get(role)
                : this.accessMap.containsKey("*")
                        ? this.accessMap.get("*")
                        : AccessLevel.WRITE;
    }

    public void addXQuery(XQuery query) {
        this.xQueriesMap.put(query.name(), query);
    }

    public Optional<XQuery> getXQuery(String name) {
        return Optional.ofNullable(xQueriesMap.get(name));
    }

    public void addXAction(XAction action) {
        this.xActionsMap.put(action.name(), action);
    }

    public Optional<XAction> getXAction(String name) {
        return Optional.ofNullable(xActionsMap.get(name));
    }

    public void addXFieldAction(String fieldName, String actionName, XFieldAction action) {

        for (Map.Entry<String, Map<String, XFieldAction>> entry : xFieldActions.entrySet()) {
            if (entry.getValue().containsKey(actionName)) {
                throw new XInvalidConfigurationException(
                        String.format(
                                "Duplicate action name '%s' detected in field '%s'. Action names must be unique across all fields and repository level.",
                                actionName, entry.getKey()));
            }
        }

        Optional<XAction> checkXAction = getXAction(actionName);

        if (checkXAction.isPresent()) {
            throw new XInvalidConfigurationException(
                    String.format(
                            "Duplicate action name '%s' detected in repository. Action names must be unique across all fields and repository level.",
                            actionName));
        }

        xFieldActions
                .computeIfAbsent(fieldName, k -> new HashMap<>())
                .put(actionName, action);
    }

    public Optional<XFieldAction> getXFieldAction(String fieldName, String actionName) {
        return Optional.ofNullable(
                xFieldActions.getOrDefault(fieldName, Collections.emptyMap()).get(actionName));
    }

    public Optional<Map.Entry<String, XFieldAction>> findFieldAndActionByActionName(String actionName) {
        for (Map.Entry<String, Map<String, XFieldAction>> fieldEntry : xFieldActions.entrySet()) {
            Map<String, XFieldAction> actions = fieldEntry.getValue();
            if (actions.containsKey(actionName)) {
                return Optional.of(new AbstractMap.SimpleEntry<>(fieldEntry.getKey(), actions.get(actionName)));
            }
        }
        return Optional.empty();
    }

    public Map<String, XFieldAction> getFieldActions(String fieldName) {
        return xFieldActions.getOrDefault(fieldName, Collections.emptyMap());
    }

    public Map<String, Map<String, XFieldAction>> getAllXFieldActions() {
        return xFieldActions;
    }

    public Optional<List<Map<String, String>>> getXJSONFormValidatorsForField(String fieldName) {
        return Optional.ofNullable(xJSONFormValidatorsMap.get(fieldName));
    }

    public void addXJSONFormValidatorForField(String fieldName, Map<String, String> validatorEntry) {
        Optional<List<Map<String, String>>> ruleListOptional = Optional
                .ofNullable(xJSONFormValidatorsMap.get(fieldName));
        List<Map<String, String>> rules;
        if (!ruleListOptional.isEmpty()) {
            rules = ruleListOptional.get();
        } else {
            rules = new ArrayList<Map<String, String>>();
            xJSONFormValidatorsMap.put(fieldName, rules);
        }
        rules.add(validatorEntry);
    }

    public Map<String, List<Map<String, String>>> getXJSONFormValidators(String fieldName) {
        return xJSONFormValidatorsMap;
    }

    public boolean hasCompositeKey() {
        return this.hasCompositeKey;
    }

}
