package io.github.zacharysabourin.donezo_api.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import io.github.zacharysabourin.donezo_api.config.JwtConfiguration;
import io.github.zacharysabourin.donezo_api.models.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility component for generating, parsing, and validating JSON Web Tokens
 * (JWT) and managing HTTP-only security cookies.
 */
@Component
public class JwtUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);

    private final JwtConfiguration config;

    /**
     * Constructs a new {@link JwtUtils} instance with the specified configuration
     * parameters.
     *
     * @param config the JWT configuration settings providing secret key, cookie
     *               name, and expiration properties
     */
    public JwtUtils(JwtConfiguration config) {
        this.config = config;
    }

    /**
     * Derives a cryptographic {@link SecretKey} using HMAC-SHA from the configured
     * secret string.
     *
     * @return the derived secret key for signing and verifying JWTs
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(config.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a secure, HTTP-only response cookie containing a newly issued JWT
     * for an authenticated user.
     *
     * @param userPrincipal the authenticated user principal
     * @return a configured {@link ResponseCookie} containing the generated JWT
     *         token
     */
    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        return ResponseCookie.from(config.cookie(), jwt)
                .path("/api")
                .maxAge(config.expiration() / 1000)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .build();
    }

    /**
     * Generates a response cookie configured to clear/invalidate the active JWT
     * session cookie on the client.
     *
     * @return an empty {@link ResponseCookie} with a null value for logout or
     *         cleanup operations
     */
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(config.cookie(), null)
                .path("/api")
                .build();
    }

    /**
     * Extracts the JWT value from the request's cookies if present.
     *
     * @param request the incoming {@link HttpServletRequest}
     * @return the string value of the JWT cookie, or {@code null} if the cookie is
     *         not present
     */
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, config.cookie());
        return cookie != null ? cookie.getValue() : null;
    }

    /**
     * Builds and signs a signed JWT token for the given username subject.
     *
     * @param username the subject/username to encode in the token payload
     * @return a compact, signed JWT string
     */
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + config.expiration()))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Parses a signed JWT token and extracts the subject (username).
     *
     * @param token the signed JWT string to parse
     * @return the username embedded in the token's subject claim
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    /**
     * Validates the integrity and expiration of a signed JWT token string.
     *
     * @param authToken the JWT token string to validate
     * @return {@code true} if the token is valid and unexpired; {@code false} if
     *         signature verification fails or the token is expired
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            LOGGER.error("Error validating JWT Token: ", e);
        }
        return false;
    }
}