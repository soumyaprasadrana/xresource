package org.xresource.core.intent.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines a JOIN clause between entities/tables in an Intent query.
 * 
 * The join condition specifies how two tables are related.
 * Optional filters can be applied on the joined table.
 * 
 * Example:
 * 
 * <pre>
 *  @Join(entity = "project", alias = "p", on = "a.project_id = p.project_id")
 * </pre>
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface Join {

    /**
     * Name of the target resource/table to join.
     * 
     * @return the resource name
     */
    String resource();

    /**
     * Alias for the joined entity/table.
     * Used to reference fields in select attributes, filters, etc.
     * If alias is not present then a random alias name will be given
     * 
     * @return the alias string
     */
    String alias() default "";

    /**
     * Determines whether this join should automatically infer its relationship path
     * from the previous join in the list, based on foreign key metadata.
     *
     * <p>
     * When {@code autoChain} is {@code true} (the default), and {@code on} is
     * not specified, the system will try to automatically resolve the join path
     * from the entity used in the previous {@code @Join}, using foreign key
     * relationships defined in metadata.
     *
     * <p>
     * If {@code autoChain} is set to {@code false}, and {@code on} is not
     * provided, the system will attempt to resolve the join relationship
     * from the root entity instead of the previous join.
     *
     * <p>
     * If multiple paths exist or no direct foreign key relationship can be found,
     * it is recommended to define the {@code on} condition explicitly to avoid
     * ambiguity.
     *
     * <p>
     * <b>Example Usage:</b>
     * 
     * <pre>
     * {@code
     * &#64;Join(entity = "project", alias = "p") // autoChain = true by default
     * &#64;Join(entity = "department", alias = "d", autoChain = false) // joins to root
     * }
     * </pre>
     *
     * @return whether the join should automatically chain from the previous entity
     */
    boolean autoChain() default true;

    /**
     * Join condition expressed as a SQL ON clause (without the ON keyword).
     * For example: "a.project_id = p.project_id"
     * 
     * @return the join condition string
     */
    String on() default "";

    /**
     * Optional filters to apply on the joined table as part of the join.
     * These are typically equality or like conditions on fields of the joined
     * entity.
     * 
     * @return array of JoinFilter annotations
     */
    JoinFilter[] filters() default {};
}
