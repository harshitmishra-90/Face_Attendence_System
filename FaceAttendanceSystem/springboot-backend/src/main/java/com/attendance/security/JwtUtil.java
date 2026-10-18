package com.attendance.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Issues and validates the JWTs used for authentication.
 *
 * Signing key comes from {@link JwtKeyProvider}, which derives it from the
 * jwt.secret property - see that class for the bug this fixes (a random,
 * never-persisted key was previously generated on every restart).
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Autowired
    private JwtKeyProvider jwtKeyProvider;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    /**
     * Builds a signed JWT for the given user.
     *
     * @param email the user's email, stored as the token subject
     * @param role  the user's role (e.g. "ADMIN", "TEACHER", "STUDENT"),
     *              stored as a custom claim
     */
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(jwtKeyProvider.getKey())
                .compact();
    }

    /**
     * @return the email (subject) embedded in the token.
     * @throws JwtException if the token is missing, malformed, expired, or
     *                       has an invalid signature. Callers that only want
     *                       a boolean should use {@link #isTokenValid} first.
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * @return the role claim embedded in the token.
     * @throws JwtException under the same conditions as {@link #extractEmail}.
     */
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * @return true if the token has a valid signature and is not expired;
     * false for any other problem (malformed, expired, unsupported, or
     * null/blank token). Never throws.
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("Rejected expired JWT: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Rejected invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtKeyProvider.getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
