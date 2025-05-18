package org.xresource.demo.auth;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.xresource.core.annotations.AccessLevel;
import org.xresource.core.auth.XResourceTypes;
import org.xresource.core.auth.XRoleBasedAccessFunction;
import org.xresource.demo.security.CustomUserDetails;

@Component
public class ResourceAuthProvider implements XRoleBasedAccessFunction {

    String[] restrictedUsers = { "testUser3" };

    @Override
    public AccessLevel getEffectiveAccess(XResourceTypes type, AccessLevel currentEffectiveAlevel, String resourceName,
            String fieldName, List<String> roles) {

        for (String role : roles) {
            if (role == "ROLE_USER") {
                CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();
                boolean restricted = Arrays.stream(restrictedUsers)
                        .anyMatch(user -> userDetails.getUserId().equals(user));
                if (restricted) {
                    return AccessLevel.NONE;
                }
            }
        }
        return currentEffectiveAlevel;
    }

}
