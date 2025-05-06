package org.xresource.core.auth;

import org.xresource.core.annotation.AccessLevel;
import java.util.Map;

public class RoleBasedAccessEvaluator {
    public boolean canRead(String role, Map<String, AccessLevel> fieldAccess) {
        return fieldAccess.getOrDefault(role, AccessLevel.NONE).ordinal() >= AccessLevel.READ.ordinal();
    }

    public boolean canWrite(String role, Map<String, AccessLevel> fieldAccess) {
        return fieldAccess.getOrDefault(role, AccessLevel.NONE).ordinal() >= AccessLevel.WRITE.ordinal();
    }
}