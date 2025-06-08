package org.xresource.internal.intent.core.parser.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsed metadata holder for @Intent annotation.
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentMeta {
    private String name;
    private Class<?> entityClass;
    private String description;
    private String rootAlias;
    private String rootResource;
    private List<SelectAttributeMeta> selectAttributes;
    private List<JoinMeta> joins;
    private List<IntentParameterMeta> parameters;
    private String where;
    private List<String> sortBy;
    private List<String> groupBy;
    private boolean paginated;
    private int limit;

    public void addSelectAttribute(SelectAttributeMeta meta) {
        if (this.selectAttributes == null)
            this.selectAttributes = new ArrayList<>();
        this.selectAttributes.add(meta);
    }

    public void mergeSelectAttributes(List<SelectAttributeMeta> newAttributes) {
        if (newAttributes == null || newAttributes.isEmpty()) {
            return; // nothing to add
        }

        if (selectAttributes != null) {
            selectAttributes.addAll(newAttributes);
        } else {
            selectAttributes = new ArrayList<>(newAttributes);
        }
    }

}
