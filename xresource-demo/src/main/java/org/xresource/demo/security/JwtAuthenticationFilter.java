package org.xresource.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.xresource.demo.repository.SessionRepository;

import io.jsonwebtoken.Claims;

import org.xresource.demo.entity.Session;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SessionRepository sessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            if (jwtTokenProvider.validateToken(jwtToken)) {
                Claims claims = jwtTokenProvider.parseClaims(jwtToken);

                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                String fullName = claims.get("fullName", String.class);
                String teamId = claims.get("teamId", String.class);
                boolean isAdmin = claims.get("isAdmin", Boolean.class);

                String role = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";

                CustomUserDetails userDetails = new CustomUserDetails(
                        userId,
                        email,
                        fullName,
                        teamId,
                        isAdmin,
                        List.of(new SimpleGrantedAuthority(role)));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                Session session = sessionRepository.findByAuthToken(jwtToken).orElse(null);
                if (session != null) {
                    // Invalid Session
                    sessionRepository.delete(session);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

}
