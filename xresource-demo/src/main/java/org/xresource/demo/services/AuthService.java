package org.xresource.demo.services;

import org.xresource.demo.dto.LoginRequest;
import org.xresource.demo.dto.LoginResponse;
import org.xresource.demo.dto.LogoutRequest;
import org.xresource.demo.entity.Session;
import org.xresource.demo.exception.XException;
import org.xresource.demo.repository.AuthorizationRepository;
import org.xresource.demo.repository.SessionRepository;
import org.xresource.demo.repository.UserRepository;
import org.xresource.demo.security.JwtTokenProvider;
import org.xresource.demo.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthorizationRepository authorizationRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private static final long SESSION_TIMEOUT_MINUTES = 30;

    // --- LOGIN ---
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        var user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new XException("user", "not_found"));

        var authorization = authorizationRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Authorization not found"));

        String encoded = request.getPassword();
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        String decodedPassword = new String(decodedBytes);

        if (!passwordEncoder.matches(decodedPassword, user.getUserPass())) {
            throw new XException("auth", "invalid_cred", HttpStatus.UNAUTHORIZED);
        }

        // Generate JWT
        String jwt = jwtTokenProvider.generateToken(user, authorization.isAdmin());

        // Store session
        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        session.setAuthToken(jwt);
        session.setUser(user);
        session.setUserTeam(user.getTeam());
        session.setStartTimestamp(Instant.now().toString());
        session.setLastActivityTimestamp(Instant.now().toString());

        session.setClientIp(httpRequest.getRemoteAddr());
        session.setClientOs(httpRequest.getHeader("User-Agent"));
        session.setClientBrowser("Unknown");

        sessionRepository.save(session);

        return new LoginResponse(jwt, user.getUserId(), authorization.isAdmin());
    }

    // --- LOGOUT ---
    public void logout(LogoutRequest request) {
        // Optionally: Add the token to a blacklist (not shown here)
        sessionRepository.deleteById(request.getSessionId());
    }

    // --- SESSION VALIDATION ---
    public boolean isSessionValid(String jwt) {
        Session session = sessionRepository.findByAuthToken(jwt).orElse(null);
        if (session == null)
            return false;

        Instant lastActivity = Instant.parse(session.getLastActivityTimestamp());
        long minutesInactive = ChronoUnit.MINUTES.between(lastActivity, Instant.now());
        return minutesInactive <= SESSION_TIMEOUT_MINUTES;
    }

    // --- JWT AUTH HELPERS ---
    public CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        }
        return null;
    }

    public void updateLastActivity(String jwt) {
        sessionRepository.findByAuthToken(jwt).ifPresent(session -> {
            session.setLastActivityTimestamp(Instant.now().toString());
            sessionRepository.save(session);
        });
    }

    // --- SESSION CLEANUP ---
    @Scheduled(fixedRate = 10 * 60 * 1000) // every 10 minutes
    public void cleanupExpiredSessions() {
        Instant now = Instant.now();
        sessionRepository.findAll().forEach(session -> {
            Instant lastActive = Instant.parse(session.getLastActivityTimestamp());
            if (ChronoUnit.MINUTES.between(lastActive, now) > SESSION_TIMEOUT_MINUTES) {
                sessionRepository.deleteById(session.getSessionId());
            }
        });
    }
}
