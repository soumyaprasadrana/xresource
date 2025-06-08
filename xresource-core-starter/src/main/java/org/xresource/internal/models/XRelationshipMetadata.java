package org.xresource.internal.models;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class XRelationshipMetadata {
    private String sourceTable;
    private String targetTable;
    private String foreignKeyField;
    private String foreignKeyColumn;
    private String targetField;
    private String targetColumn;
    private boolean isComposite;
    private Map<String, String> compositeMap;

    public XRelationshipMetadata(
            String sourceTable,
            String targetTable,
            String foreignKeyField,
            String foreignKeyColumn,
            String targetField,
            String targetColumn,
            boolean isComposite,
            Map<String, String> compositeMap) {
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        this.foreignKeyField = foreignKeyField;
        this.foreignKeyColumn = foreignKeyColumn;
        this.targetField = targetField;
        this.targetColumn = targetColumn;
        this.isComposite = isComposite;
        this.compositeMap = compositeMap;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public String getForeignKeyField() {
        return foreignKeyField;
    }

    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }

    public String getTargetField() {
        return targetField;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public boolean isComposite() {
        return isComposite;
    }

    public Map<String, String> getCompositeMap() {
        return compositeMap;
    }
}
