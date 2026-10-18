package com.attendance.security;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Builds the HMAC signing key used for JWTs from the configured
 * {@code jwt.secret} property.
 *
 * BUG FIX: this previously called Keys.secretKeyFor(SignatureAlgorithm.HS256),
 * which generates a brand-new random key every time the class was
 * instantiated (i.e. on every application restart), completely ignoring
 * jwt.secret in application.properties. The practical effect:
 *   - Every restart of the backend silently invalidated every previously
 *     issued token, logging every user out with no warning.
 *   - Running more than one backend instance (or a rolling deploy) meant
 *     each instance had its own key, so a token issued by one instance
 *     would be rejected by another.
 *   - The jwt.secret property looked like it configured signing, but did
 *     nothing at all.
 *
 * Now the key is derived deterministically from jwt.secret, so it is
 * stable across restarts and identical across instances as long as they
 * share the same configured secret.
 */
@Component
public class JwtKeyProvider {

    private static final int MIN_SECRET_BYTES = 32; // 256 bits, required for HS256

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    private void init() {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret must be at least " + MIN_SECRET_BYTES +
                    " bytes (256 bits) for HS256. Current value is only " +
                    secretBytes.length + " bytes. Generate one with: " +
                    "openssl rand -base64 32");
        }

        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public SecretKey getKey() {
        return secretKey;
    }
}
