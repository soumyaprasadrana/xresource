package org.xresource.core.auth;

import org.xresource.core.annotations.AccessLevel;
import org.xresource.core.exception.ResourceNotFoundException;
import org.xresource.core.exception.XAccessDeniedException;
import org.xresource.core.model.XFieldMetadata;
import org.xresource.core.model.XResourceMetadata;

import java.util.List;

public class XRoleBasedAccessEvaluator {

    // Pluggable auth provider interface to enbale developers to implement their
    // custom logic
    private XResourceAuthProvider provider;

    public XRoleBasedAccessEvaluator(XResourceAuthProvider provider) {
        this.provider = provider;
    }

    /**
     * Throws exception if the user does not have read access to the resource.
     */
    public void checkReadAccess(List<String> roles, XResourceMetadata metadata, String resourceName) {
        if (metadata == null) {
            throw new ResourceNotFoundException("Resource not found: " + resourceName);
        }
        AccessLevel effectiveAccessLevel = getEffectiveAccessForResource(roles, metadata);
        AccessLevel authProviderLevel = this.provider.getEffectiveAccess(XResourceTypes.ENITITY,
                effectiveAccessLevel, resourceName, null, roles);
        if (authProviderLevel == AccessLevel.NONE) {
            throw new XAccessDeniedException("No read access to resource: " + resourceName);
        }
    }

    /**
     * Throws exception if the user does not have write access to the resource.
     */
    public void checkWriteAccess(List<String> roles, XResourceMetadata metadata, String resourceName) {
        if (metadata == null) {
            throw new ResourceNotFoundException("Resource not found: " + resourceName);
        }

        if (getEffectiveWriteAccessForResource(roles, metadata) == AccessLevel.NONE) {
            throw new XAccessDeniedException("No write access to resource: " + resourceName);
        }
    }

    /**
     * Role-based access evaluation for a field.
     */
    public AccessLevel getEffectiveAccess(List<String> roles, XFieldMetadata fieldMeta) {
        for (String role : roles) {
            AccessLevel level = fieldMeta.getAccessLevelForRole(role);
            if (level != AccessLevel.NONE) {
                return level;
            }
        }
        return AccessLevel.NONE;
    }

    /**
     * Role-based access evaluation for a resource.
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