package org.xresource.core.intent.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines a filter condition to be applied on a joined entity/table
 * as part of the join clause in an Intent query.
 * 
 * Example:
 * 
 * <pre>
 *  @JoinFilter(field = "r.name", param = "regionName", binding = BindingType.EXACT)
 * </pre>
 * 
 * The filter binds a field to a parameter value with specified binding type.
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface JoinFilter {

    /**
     * Qualified field name in the joined table to filter on.
     * Use alias.field format (e.g. "r.name").
     * 
     * @return field name
     */
    String field();

    /**
     * Name of the IntentParameter whose value will be bound to this filter.
     * 
     * @return parameter name
     */
    String param();

    /**
     * Defines how the parameter value should be bound to the filter field.
     * Options include EXACT, LIKE, IN, etc.
     * 
     * @return binding type
     */
    BindingType binding() default BindingType.EXACT;
}
