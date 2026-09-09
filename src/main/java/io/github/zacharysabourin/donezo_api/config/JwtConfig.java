package io.github.zacharysabourin.donezo_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "donezo.jwt")
public record JwtConfig(String secret, Integer expiration, String cookie) {

}
