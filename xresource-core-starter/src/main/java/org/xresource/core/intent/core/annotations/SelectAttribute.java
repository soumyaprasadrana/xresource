package org.xresource.core.intent.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines a single attribute/field to select in an Intent query result.
 * Supports aliasing of both source entity and the selected field in output.
 * 
 * Example:
 * 
 * <pre>
 *  @SelectAttribute(alias = "a", field = "assetId", aliasAs = "asset_id")
 * </pre>
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface SelectAttribute {

    /**
     * Alias of the entity/table from which this field is selected.
     * This should correspond to one of the aliases defined in @Intent or @Join.
     * <p>
     * If no alias is provided, and you want to access a field from a related
     * (child) resource,
     * you must specify the fully qualified field name using the resource name and
     * field
     * (e.g., "ResourceName.fieldName").
     *
     * @return the entity alias
     */
    String alias() default "";

    /**
     * Name of the field or column to select.
     * 
     * @return field name
     */
    String field();

    /**
     * Optional alias for the selected field in the query result.
     * If not provided, the original field name is used.
     * 
     * @return alias for output field
     */
    String aliasAs() default "";
}
