package org.xresource.core.intent.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;

/**
 * Defines a complex Intent query with metadata-driven structure for
 * resource data retrieval involving joins, filters, parameters, sorting, and
 * pagination.
 * 
 * <p>
 * This annotation allows declarative definition of multi-entity queries,
 * including selects, joins, where clause, and execution options.
 * </p>
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(Intents.class)
public @interface Intent {

    /**
     * Unique name of the Intent.
     * Used to identify and execute this query.
     * 
     * @return the intent name
     */
    String name();

    /**
     * Optional description of the Intent for documentation.
     * 
     * @return the description text
     */
    String description() default "";

    /**
     * Alias to use for the root/main entity/table in the query.
     * Defaults to lowercase first character of the entity if not set.
     * 
     * @return root alias string
     */
    String rootAlias() default "";

    /**
     * List of attributes to select in the query result.
     * 
     * @return array of SelectAttribute
     */
    SelectAttribute[] selectAttributes() default {};

    /**
     * List of join definitions to define additional tables and their relations.
     * 
     * @return array of Join annotations
     */
    Join[] joins() default {};

    /**
     * Parameters that can be bound dynamically for filtering, sorting, or join
     * conditions.
     * 
     * @return array of IntentParameter annotations
     */
    IntentParameter[] parameters() default {};

    /**
     * Additional WHERE clause for the query (excluding join filters).
     * Parameters should be referenced by :paramName syntax.
     * 
     * @return the where clause string
     */
    String where() default "";

    /**
     * Fields to sort the results by.
     * Use alias.field format.
     * 
     * @return array of sort expressions
     */
    String[] sortBy() default {};

    /**
     * Fields to group the results by (reserved for future use).
     * 
     * @return array of group by expressions
     */
    String[] groupBy() default {};

    /**
     * Enable pagination support for the query.
     * 
     * @return true if pagination enabled
     */
    boolean paginated() default false;

    /**
     * Maximum number of results to return.
     * -1 means no limit.
     * 
     * @return max result limit
     */
    int limit() default -1;
}
