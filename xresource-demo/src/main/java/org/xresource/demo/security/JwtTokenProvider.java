package org.xresource.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.xresource.demo.entity.User;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final long TOKEN_VALIDITY = 1000 * 60 * 30; // 30 minutes
    private final Key jwtSecret;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKeyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
        this.jwtSecret = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user, boolean isAdmin) {
        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("isAdmin", isAdmin)
                .claim("teamId", user.getTeam().getTeamId())
                .claim("email", user.getEmail())
                .claim("fullName", user.getFirstName() + " " + user.getLastName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
                .signWith(jwtSecret)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token).getBody();
    }

    public boolean isAdmin(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("isAdmin", Boolean.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
