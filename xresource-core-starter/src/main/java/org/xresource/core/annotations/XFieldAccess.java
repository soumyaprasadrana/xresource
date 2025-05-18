package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares fine-grained, role-based access control for a specific field within
 * an XResource-managed entity.
 * <p>
 * The XResource framework follows a **deny-override model**, where all fields
 * are by default
 * considered readable and writable for any role that has general access to the
 * resource.
 * This annotation allows **selective overrides** to restrict or fine-tune
 * visibility and mutability
 * at the field level.
 *
 * <h3>Behavior Summary:</h3>
 * <ul>
 * <li>By default, all roles can read/write all fields unless this annotation is
 * applied.</li>
 * <li>If {@code denyRoles} includes a role (or "*"), that role is denied access
 * to this field.</li>
 * <li>{@code readRoles} allows only the listed roles to read the field.</li>
 * <li>{@code writeRoles} allows only the listed roles to write the field.</li>
 * <li>If both {@code denyRoles} and {@code readRoles}/{@code writeRoles}
 * include a role,
 * {@code denyRoles} takes precedence.</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * 
 * <pre>{@code
 * @XFieldAccess(denyRoles = { "*" }, // Deny all by default
 *         readRoles = { "ROLE_USER" }, // Allow only ROLE_USER to read
 *         writeRoles = { "ROLE_ADMIN" } // Allow only ROLE_ADMIN to write
 * )
 * private String sensitiveInfo;
 * }</pre>
 *
 * <h3>Wildcard Support:</h3>
 * <ul>
 * <li>"*" can be used in {@code denyRoles}, {@code readRoles}, {@ writeRoles}
 * to match all
 * roles.</li>
 * <li>This enables constructing field rules such as "hidden from all
 * except..."</li>
 * </ul>
 *
 * @apiNote This annotation is intended to be used on entity fields scanned by
 *          the XResource metadata
 *          engine. The access logic is enforced during
 *          serialization/deserialization and request filtering.
 *
 * @see org.xresource.core.model.XFieldMetadata
 * @see org.xresource.core.annotations.XResourceAccess
 * @author soumya
 * @since xresource-core 0.1
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface XFieldAccess {

    /**
     * List of role names that should be denied any access to this field
     * (read/write).
     * If "*" is present, the field is hidden from all unless explicitly overridden.
     */
    String[] denyRoles() default {};

    /**
     * List of role names allowed to read this field.
     * Ignored for roles in {@code denyRoles}.
     */
    String[] readRoles() default {};

    /**
     * List of role names allowed to write this field.
     * Ignored for roles in {@code denyRoles}.
     */
    String[] writeRoles() default {};
}
