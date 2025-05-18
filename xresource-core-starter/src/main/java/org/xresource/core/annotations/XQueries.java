package org.xresource.core.annotations;

import java.lang.annotation.*;

/**
 * Container annotation for declaring multiple {@link XQuery} annotations on a
 * single entity class.
 *
 * <p>
 * This is used when an entity needs to expose more than one dynamic query in
 * the XResource framework.
 * </p>
 *
 * <p>
 * Each contained {@code @XQuery} defines a separate named JPQL/HQL `WHERE`
 * clause and optional context parameters,
 * which can be invoked via the dynamic endpoint:
 * </p>
 *
 * <pre>
 *   ${xresource.api.base-path:/api/resources}/{resourceName}/query/{queryName}
 * </pre>
 *
 * <h3>Example Usage:</h3>
 * 
 * <pre>{@code
 * &#64;XQueries({
 *   &#64;XQuery(
 *     name = "filterByTeam",
 *     where = "team.teamId = :context_loggeduser_teamId",
 *     contextParams = {"context_loggeduser_teamId"},
 *     autoApply = true,
 *     appliesToRoles = {"ROLE_USER"}
 *   ),
 *   &#64;XQuery(
 *     name = "filterByStatus",
 *     where = "status = :status",
 *     contextParams = {"status"}
 *   )
 * })
 * }</pre>
 *
 * <p>
 * This container is required because Java annotations do not support multiple
 * occurrences of the same annotation
 * type on a single element prior to Java 8 unless explicitly marked with
 * {@link Repeatable}.
 * </p>
 *
 * @apiNote This container annotation is used automatically when applying
 *          multiple {@link XQuery} annotations to the same class.
 *          It is not typically declared directly—just use {@code @XQuery}
 *          multiple times if your framework supports `@Repeatable`.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XQueries {
    /**
     * An array of {@link XQuery} definitions to apply to the annotated class.
     */
    XQuery[] value();
}
