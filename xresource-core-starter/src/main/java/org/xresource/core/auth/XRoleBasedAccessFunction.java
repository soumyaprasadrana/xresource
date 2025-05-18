package org.xresource.core.auth;

import java.util.List;
import org.xresource.core.annotations.AccessLevel;

/**
 * Functional interface for determining the effective access level
 * for a resource or field based on user roles and the current access level.
 * <p>
 * This interface allows developers to provide their own access evaluation logic
 * that is applied after the framework's default access evaluation completes.
 * It acts as a pluggable hook to customize or override the default behavior.
 * </p>
 *
 * @apiNote Implementations of this interface should be registered via
 *          a wrapper Spring bean to integrate with the framework's access
 *          control system.
 *          The wrapper bean serves as an indirection layer and can be
 *          overridden
 *          by developers to customize access evaluation without modifying core
 *          framework code.
 *
 * @see org.xresource.internal.auth.XRoleBasedAccessEvaluator
 * @author soumya
 * @since xresource-core 0.1
 */
@FunctionalInterface
public interface XRoleBasedAccessFunction {
    /**
     * Computes the effective access level for a resource or field.
     *
     * @param type                  The type of resource being accessed (entity,
     *                              field, etc.)
     * @param currentEffectiveLevel The current effective access level before this
     *                              evaluation
     * @param resourceName          The name of the resource or entity
     * @param fieldName             The name of the field (nullable or empty if not
     *                              field-level)
     * @param roles                 The list of roles associated with the current
     *                              user/request
     * @return The resolved {@link AccessLevel} (e.g., NONE, READ, WRITE)
     */
    AccessLevel getEffectiveAccess(
            XResourceTypes type,
            AccessLevel currentEffectiveLevel,
            String resourceName,
            String fieldName,
            List<String> roles);
}
