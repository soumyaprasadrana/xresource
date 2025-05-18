package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares role-based access control for a resource (entity or repository)
 * managed by XResource.
 * <p>
 * By default, all roles are allowed full access to the resource. This
 * annotation provides
 * the ability to selectively restrict or fine-tune access for specific roles at
 * the resource level.
 *
 * <h3>Behavior Summary:</h3>
 * <ul>
 * <li>If {@code denyRoles} includes a role (or "*"), that role is denied access
 * to this resource entirely.</li>
 * <li>{@code readRoles} allows only the listed roles to perform read (GET)
 * operations.</li>
 * <li>{@code writeRoles} allows only the listed roles to perform write
 * (POST/PUT/DELETE) operations.</li>
 * <li>If both {@code denyRoles} and {@code readRoles}/{@code writeRoles}
 * include a role,
 * {@code denyRoles} takes precedence.</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * 
 * <pre>{@code
 * &#64;XResourceAccess(
 *     denyRoles = {"*"},                   // Deny all roles by default
 *     readRoles = {"ROLE_USER", "AUDITOR"},// Allow only ROLE_USER and AUDITOR to read
 *     writeRoles = {"ROLE_ADMIN"}          // Allow only ROLE_ADMIN to write
 * )
 * public class UserEntity { ... }
 * }</pre>
 *
 * <h3>Supported Targets:</h3>
 * <ul>
 * <li>Entity class (e.g., {@code UserEntity.class})</li>
 * <li>Repository class (e.g., {@code UserRepository.class})</li>
 * </ul>
 *
 * <h3>Wildcard Support:</h3>
 * <ul>
 * <li>"*" can be used in {@code denyRoles}, {@code readRoles}, {@ writeRoles}
 * to match all
 * roles.</li>
 * </ul>
 *
 * @apiNote This annotation should be used when you want to define overall
 *          access control
 *          for an XResource-managed entity or its corresponding repository.
 *
 * @see org.xresource.core.annotations.XFieldAccess
 * @see org.xresource.core.model.XResourceMetadata
 * @author soumya
 * @since xresource-core 0.1
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface XResourceAccess {

    /**
     * List of role names that should be denied any access to this resource
     * (read/write).
     * If "*" is present, the resource is hidden from all unless explicitly
     * overridden.
     */
    String[] denyRoles() default {};

    /**
     * List of role names allowed to read this resource (GET operations).
     * Ignored for roles in {@code denyRoles}.
     */
    String[] readRoles() default {};

    /**
     * List of role names allowed to write to this resource (POST, PUT, DELETE).
     * Ignored for roles in {@code denyRoles}.
     */
    String[] writeRoles() default {};
}
