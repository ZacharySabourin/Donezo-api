package io.github.zacharysabourin.donezo_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "donezo.access-control-allow")
public record AccessControlConfiguration(
        Boolean enabled,
        String origin,
        String methods,
        String headers) {

}
