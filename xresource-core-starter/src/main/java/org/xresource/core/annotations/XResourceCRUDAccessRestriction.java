package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines CRUD operation restrictions for specific roles on a resource
 * (entity).
 * <p>
 * This is a deny-list based annotation that allows you to block certain roles
 * from executing specific HTTP methods (GET, POST, PUT, DELETE) on a resource,
 * even if those roles have general access rights through other means.
 *
 * <h3>Example Usage:</h3>
 * 
 * <pre>{@code
 * &#64;XResourceCRUDAccessDeny(
 *     delete = {"MODERATOR", "AUDITOR"},
 *     post = {"VIEWER"}
 * )
 * public class UserEntity/Repository { ... }
 * }</pre>
 *
 * <h3>Behavior:</h3>
 * <ul>
 * <li>Any role listed under a specific operation (e.g., `delete`) is denied
 * that operation.</li>
 * <li>If a role is not listed under an operation, access is governed by the
 * global or default access policy.</li>
 * <li>This annotation is useful for selectively restricting operations on a
 * resource per role.</li>
 * <li>HTTP 403 Forbidden is returned if a denied role attempts the restricted
 * operation.</li>
 * </ul>
 *
 * <h3>Use Case:</h3>
 * Suppose all users can access a `Document` resource, but you want to ensure
 * "MODERATOR" users cannot delete documents. Use this:
 * 
 * <pre>{@code
 * &#64;XResourceCRUDAccessDeny(delete = {"MODERATOR"})
 * public class Document { ... }
 * }</pre>
 *
 * @apiNote Use this annotation to explicitly deny CRUD actions to specific
 *          roles at the resource level.
 *          It complements allow-based annotations like {@link XResourceAccess}
 *          and {@link XFieldAccess}.
 *
 * @see XResourceAccess
 * @see XFieldAccess
 * @author soumya
 * @since xresource-core 0.1
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface XResourceCRUDAccessRestriction {

    /**
     * Roles that should be denied GET (read) operations.
     */
    String[] get() default {};

    /**
     * Roles that should be denied POST (create) operations.
     */
    String[] post() default {};

    /**
     * Roles that should be denied PUT (update) operations.
     */
    String[] put() default {};

    /**
     * Roles that should be denied DELETE operations.
     */
    String[] delete() default {};
}
