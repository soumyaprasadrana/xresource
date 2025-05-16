package org.xresource.core.model;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xresource.core.annotations.AccessLevel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XFieldMetadata {
    private String name;
    private boolean required;
    private boolean readonly;
    private boolean hidden;
    private boolean includeinjsonform;
    private int displaySeq;
    private String type;
    private String format;
    @JsonIgnore
    private Field field;
    private String label;
    private String dbColumnName;
    private String description;
    private String defaultValue;
    private List<String> enumValues;
    private Map<String, AccessLevel> accessMap = new HashMap<String, AccessLevel>();
    private boolean isForeignKey = false;
    private String foreignKeyRefTable;
    private String foreignKeyColumn;
    private String foreignKeyRefField;
    private String foreignKeyRefColumn;
    private boolean controlledByAction;
    private boolean allowInsert = true;
    private boolean allowUpdate = true;

    public XFieldMetadata(String name, boolean required, String type, String format, Field field, String columnName) {
        this.name = name;
        this.required = required;
        this.type = type;
        this.format = format;
        this.field = field;
        this.dbColumnName = columnName;
    }

    public void addFieldAccess(String role, AccessLevel lavel) {
        this.accessMap.put(role, lavel);
    }

    public AccessLevel getAccessLevelForRole(String role) {
        return this.accessMap.containsKey(role) ? this.accessMap.get(role)
                : this.accessMap.containsKey("*") ? this.accessMap.get("*") : AccessLevel.WRITE;
    }

    public boolean partOfJSONForm() {
        return !this.readonly && (this.required || this.includeinjsonform);
    }

    @Override
    public String toString() {
        return "XFieldMetadata{" +
                "name='" + name + '\'' +
                ", required=" + required +
                ", type='" + type + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}
