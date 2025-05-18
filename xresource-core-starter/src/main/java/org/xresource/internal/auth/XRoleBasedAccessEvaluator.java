package org.xresource.internal.auth;

import org.xresource.core.annotations.AccessLevel;
import org.xresource.core.auth.XAccessEvaluatorDelegate;
import org.xresource.core.auth.XResourceTypes;
import org.xresource.core.auth.XRoleBasedAccessFunction;
import org.xresource.internal.exception.ResourceNotFoundException;
import org.xresource.internal.exception.XAccessDeniedException;
import org.xresource.internal.models.XFieldMetadata;
import org.xresource.internal.models.XResourceMetadata;

import java.util.List;

/**
 * Core engine responsible for evaluating role-based access permissions on
 * resources and their fields within the XResource framework.
 *
 * <p>
 * This class is used internally by the framework to determine whether the
 * currently authenticated user (based on assigned roles) has the necessary
 * {@link AccessLevel} permissions to read or write to a resource or its
 * individual fields.
 * </p>
 *
 * <p>
 * The evaluator integrates a pluggable authorization strategy via
 * {@link XRoleBasedAccessFunction}, which allows developers to override or
 * extend default access evaluation logic after static metadata-based
 * resolution completes.
 * </p>
 *
 * @apiNote
 *          This class is invoked automatically by the XResource runtime engine
 *          when
 *          enforcing access to API resources. It should not be used directly by
 *          consumers unless performing custom access control logic.
 *
 *          Developers can customize the behavior by providing their own
 *          implementation
 *          of {@link XRoleBasedAccessFunction}, and overriding the wrapper bean
 *          {@link XAccessEvaluatorDelegate} with a Spring-managed replacement.
 * @author soumya
 * @since xresource-core 0.1
 */
public class XRoleBasedAccessEvaluator {

    private final XRoleBasedAccessFunction xRoleBasedAccessFunction;

    /**
     * Constructs the access evaluator using the provided delegate.
     * The delegate allows decoupling the evaluator from a specific function
     * implementation, enabling developer overrides.
     *
     * @param xAccessEvaluatorDelegate the delegate providing the access function
     */
    public XRoleBasedAccessEvaluator(XAccessEvaluatorDelegate xAccessEvaluatorDelegate) {
        this.xRoleBasedAccessFunction = xAccessEvaluatorDelegate.getXRoleBasedAccessFunction();
    }

    /**
     * Validates whether the current roles have read access to a given resource.
     *
     * @param roles        the list of roles associated with the user
     * @param metadata     the resource metadata (including static access rules)
     * @param resourceName the name of the resource being accessed
     * @throws ResourceNotFoundException if the resource metadata is missing
     * @throws XAccessDeniedException    if access is denied
     */
    public void checkReadAccess(List<String> roles, XResourceMetadata metadata, String resourceName) {
        if (metadata == null) {
            throw new ResourceNotFoundException("Resource not found: " + resourceName);
        }
        AccessLevel effectiveAccessLevel = getEffectiveAccessForResource(roles, metadata);
        AccessLevel evaluatedAccess = this.xRoleBasedAccessFunction.getEffectiveAccess(
                XResourceTypes.ENTITY, effectiveAccessLevel, resourceName, null, roles);
        if (evaluatedAccess == AccessLevel.NONE) {
            throw new XAccessDeniedException("No read access to resource: " + resourceName);
        }
    }

    /**
     * Validates whether the current roles have write access to a given resource.
     *
     * @param roles        the list of roles associated with the user
     * @param metadata     the resource metadata (including static access rules)
     * @param resourceName the name of the resource being accessed
     * @throws ResourceNotFoundException if the resource metadata is missing
     * @throws XAccessDeniedException    if access is denied
     */
    public void checkWriteAccess(List<String> roles, XResourceMetadata metadata, String resourceName) {
        if (metadata == null) {
            throw new ResourceNotFoundException("Resource not found: " + resourceName);
        }
        AccessLevel effectiveAccessLevel = getEffectiveWriteAccessForResource(roles, metadata);
        AccessLevel evaluatedAccess = this.xRoleBasedAccessFunction.getEffectiveAccess(
                XResourceTypes.ENTITY, effectiveAccessLevel, resourceName, null, roles);
        if (evaluatedAccess == AccessLevel.NONE) {
            throw new XAccessDeniedException("No write access to resource: " + resourceName);
        }
    }

    /**
     * Computes the effective access level for a given field based on assigned
     * roles.
     * This considers both static metadata and post-evaluation via the access
     * function.
     *
     * @param roles     the user roles
     * @param fieldMeta the field metadata
     * @return the resolved {@link AccessLevel} for the field
     */
    public AccessLevel getFieldEffectiveAccess(List<String> roles, XFieldMetadata fieldMeta) {
        if (fieldMeta == null) {
            return AccessLevel.NONE;
        }
        AccessLevel effectiveAccessLevel = getEffectiveAccessInternal(roles, fieldMeta);
        return this.xRoleBasedAccessFunction.getEffectiveAccess(
                XResourceTypes.FIELD, effectiveAccessLevel, null, fieldMeta.getName(), roles);
    }

    /**
     * Computes the access level for a specific field based only on static metadata.
     *
     * @param roles     the user roles
     * @param fieldMeta the field metadata
     * @return the statically resolved {@link AccessLevel}
     */
    public AccessLevel getEffectiveAccessInternal(List<String> roles, XFieldMetadata fieldMeta) {
        for (String role : roles) {
            AccessLevel level = fieldMeta.getAccessLevelForRole(role);
            if (level != AccessLevel.NONE) {
                return level;
            }
        }
        return AccessLevel.NONE;
    }

    /**
     * Computes the access level for a resource based only on static metadata.
     *
     * @param roles        the user roles
     * @param resourceMeta the resource metadata
     * @return the statically resolved {@link AccessLevel}
     */
    public AccessLevel getEffectiveAccessForResource(List<String> roles, XResourceMetadata resourceMeta) {
        for (String role : roles) {
            AccessLevel level = resourceMeta.getAccessLevelForRole(role);
            if (level != AccessLevel.NONE) {
                return level;
            }
        }
        return AccessLevel.NONE;
    }

    /**
     * Computes the write-level access to a resource based on metadata.
     * Only returns {@link AccessLevel#WRITE} if explicitly allowed.
     *
     * @param roles        the user roles
     * @param resourceMeta the resource metadata
     * @return WRITE if at least one role grants it, otherwise NONE
     */
    public AccessLevel getEffectiveWriteAccessForResource(List<String> roles, XResourceMetadata resourceMeta) {
        for (String role : roles) {
            AccessLevel level = resourceMeta.getAccessLevelForRole(role);
            if (level == AccessLevel.WRITE) {
                return level;
            }
        }
        return AccessLevel.NONE;
    }
}
