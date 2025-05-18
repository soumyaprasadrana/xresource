package org.xresource.core.annotations;

import java.lang.annotation.*;

/**
 * Annotation to define a dynamic query (`XQuery`) for an entity in the
 * XResource framework.
 * 
 * <p>
 * This annotation enables metadata-driven querying using dynamically resolved
 * context parameters.
 * It can be used to expose named JPQL/HQL `WHERE` conditions at runtime,
 * and can support both context-based parameter injection and user-provided
 * filters.
 * </p>
 * 
 * <p>
 * Each {@code @XQuery} is accessible through the dynamic query endpoint:
 * </p>
 * 
 * <pre>
 *   ${xresource.api.base-path:/api/resources}/{resourceName}/query/{queryName}
 * </pre>
 * 
 * <h3>Fields:</h3>
 * 
 * <ul>
 * <li><strong>name</strong>: Unique name for the query (used in the endpoint
 * path).</li>
 * <li><strong>where</strong>: JPQL/HQL `WHERE` clause with named parameters
 * (e.g., <code>team.teamId = :context_loggeduser_teamId</code>).</li>
 * <li><strong>contextParams</strong>: Names of parameters to be resolved. These
 * can come from:
 * <ul>
 * <li><code>context_loggeduser_*</code>: Injected from the currently logged-in
 * user's context.</li>
 * <li>Other param names: Expected to be provided by the client in a
 * <code>xQueryParams</code> JSON object.</li>
 * </ul>
 * </li>
 * <li><strong>autoApply</strong>: If {@code true}, this query is automatically
 * applied to "getAll" and "getById" operations.</li>
 * <li><strong>appliesToRoles</strong>: List of roles for which auto-apply is
 * enabled. Defaults to {@code "*"} (all roles).</li>
 * </ul>
 * 
 * <h3>Dynamic Parameter Resolution Logic:</h3>
 * 
 * <pre>{@code
 * for each paramName in contextParams:
 *   if paramName.startsWith("context_loggeduser_"):
 *       String field = paramName.substring("context_loggeduser_".length());
 *       value = resolveLoggedUserContextValue(field, context);
 *   else:
 *       value = resolveContextValue(paramName, context);
 * }</pre>
 * 
 * <p>
 * If a custom {@code UserDetails} implementation is used, additional fields
 * from the user's session
 * can be accessed via {@code context_loggeduser_*} parameters.
 * </p>
 * 
 * <h3>Example Usage:</h3>
 * 
 * <pre>
 * {@code
 * &#64;XQueries({
 *   &#64;XQuery(
 *     name = "filterByTeam",
 *     where = "team.teamId = :context_loggeduser_teamId",
 *     contextParams = {"context_loggeduser_teamId"},
 *     autoApply = true,
 *     appliesToRoles = {"ROLE_USER"}
 *   ),
 *   &#64;XQuery(
 *     name = "filterByTeamInputFromUser",
 *     where = "team.teamId = :teamId",
 *     contextParams = {"teamId"}
 *   )
 * })
 * }
 * </pre>
 * 
 * <p>
 * In the example above:
 * </p>
 * <ul>
 * <li><code>filterByTeam</code> auto-applies to users with role
 * <code>ROLE_USER</code> using the logged-in user's team ID.</li>
 * <li><code>filterByTeamInputFromUser</code> expects the user to pass the team
 * ID manually via the <code>xQueryParams</code> JSON body:
 * 
 * <pre>{@code
 *     {
 *       "teamId": "abc123"
 *     }
 *     }</pre>
 * 
 * </li>
 * </ul>
 *
 * @apiNote This annotation is designed to work with the XResource dynamic
 *          controller system.
 *          It supports contextual query rewriting based on user session
 *          metadata and allows both implicit (auto-applied)
 *          and explicit query execution via HTTP endpoints.
 * @author soumya
 * @since xresource-core 0.1
 */
@Repeatable(XQueries.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XQuery {

    /**
     * Unique name of the query, used in the query endpoint URL.
     */
    String name();

    /**
     * JPQL or HQL WHERE clause with named parameters (e.g., "status = :status").
     */
    String where();

    /**
     * List of context parameter names used in the WHERE clause.
     * Can include values resolved from:
     * - User context (e.g., "context_loggeduser_email")
     * - User-provided query input (via `xQueryParams`)
     */
    String[] contextParams() default {};

    /**
     * Whether this query should be auto-applied when retrieving all or single
     * records.
     */
    boolean autoApply() default false;

    /**
     * Role names for which this query should be automatically applied (works only
     * if autoApply is true).
     * Defaults to "*" (applies to all roles).
     */
    String[] appliesToRoles() default { "*" };
}
