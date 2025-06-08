package org.xresource.internal.intent.core.parser.model;

import lombok.Data;
import org.xresource.core.intent.core.annotations.BindingType;

/**
 * Metadata for a join filter inside a join clause.
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@Data
public class JoinFilterMeta {
    private String field;
    private String param;
    private BindingType binding;
}
