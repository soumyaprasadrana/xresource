package org.xresource.demo.security;

import org.xresource.demo.entity.User;
import org.xresource.demo.entity.Authorization;
import org.xresource.demo.repository.UserRepository;
import org.xresource.demo.repository.AuthorizationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthorizationRepository authorizationRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        Authorization authorization = authorizationRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Authorization not found for user: " + userId));

        String role = authorization.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUserId())
                .password(user.getUserPass())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .build();
    }
}
