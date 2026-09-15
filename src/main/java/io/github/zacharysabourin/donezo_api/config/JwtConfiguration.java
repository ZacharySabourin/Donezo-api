package io.github.zacharysabourin.donezo_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration object used to configure JWT secret, expiration(ms), and cookie
 * name for the application. Configured by updating the
 * <code>donezo.jwt.*</code> values in the <code>applicaton.properties</code>
 * file.
 * 
 * @param secret
 * @param expiration
 * @param cookie
 */
@ConfigurationProperties(prefix = "donezo.jwt")
public record JwtConfiguration(String secret, Integer expiration, String cookie) {

}
