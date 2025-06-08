package org.xresource.internal.intent.core.parser.model;

import lombok.Data;
import org.xresource.core.intent.core.annotations.ParamSource;
import org.xresource.core.intent.core.annotations.BindingType;

/**
 * Metadata for a parameter used in Intent query.
 * 
 * Author: soumya
 * 
 * @since xresource-core 0.2
 */
@Data
public class IntentParameterMeta {
    private String name;
    private Class<?> type;
    private String defaultValue;
    private ParamSource source;
    private BindingType binding;
}
