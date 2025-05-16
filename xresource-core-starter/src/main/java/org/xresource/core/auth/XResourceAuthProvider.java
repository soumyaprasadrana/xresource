package org.xresource.core.auth;

import java.util.List;

import org.xresource.core.annotations.AccessLevel;

@FunctionalInterface
public interface XResourceAuthProvider {
    AccessLevel getEffectiveAccess(XResourceTypes type, AccessLevel currentEffectiveAlevel, String resourceName,
            String fieldName, List<String> roles);
}
