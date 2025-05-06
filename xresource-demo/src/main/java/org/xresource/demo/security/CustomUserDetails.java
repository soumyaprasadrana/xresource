package org.xresource.demo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final String userId;
    private final String email;
    private final String fullName;
    private final String teamId;
    private final boolean isAdmin;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(
            String userId,
            String email,
            String fullName,
            String teamId,
            boolean isAdmin,
            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.teamId = teamId;
        this.isAdmin = isAdmin;
        this.authorities = authorities;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getTeamId() {
        return teamId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Not needed for stateless JWT
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
