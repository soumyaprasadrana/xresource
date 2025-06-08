package org.xresource.core.intent.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines a parameter that can be dynamically bound in an Intent query.
 * 
 * Parameters can be used in filters, join filters, where clauses, and sorting.
 * Supports static values or dynamic sources like user context or security
 * profile.
 * 
 * Example:
 * 
 * <pre>
 *  @IntentParameter(name = "cityName", type = String.class, source = ParamSource.USER_CONTEXT)
 * </pre>
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface IntentParameter {

    /**
     * Name of the parameter.
     * This is referenced by join filters, where clauses, or sorting.
     * 
     * @return parameter name
     */
    String name();

    /**
     * Java type of the parameter.
     * 
     * @return parameter data type class
     */
    Class<?> type();

    /**
     * Optional default static value for the parameter.
     * If not set, the parameter must be provided dynamically.
     * 
     * @return default value as string
     */
    String defaultValue() default "";

    /**
     * Source of the parameter value.
     * Defaults to STATIC if not specified.
     * 
     * @return source enum
     */
    ParamSource source() default ParamSource.STATIC;

    /**
     * Defines how the parameter value will be bound in filters or joins.
     * 
     * @return binding type enum
     */
    BindingType binding() default BindingType.EXACT;
}
