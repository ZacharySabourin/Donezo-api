package io.github.zacharysabourin.donezo_api.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration object used to configure <code>Access-Control-Allow-*</code>
 * for the application. Configured by updating the
 * <code>donezo.access-control-allow.*</code> values in the
 * <code>applicaton.properties</code> file.
 * 
 * @param enabled
 * @param origin
 * @param methods
 * @param headers
 */
@ConfigurationProperties(prefix = "donezo.access-control-allow")
public record AccessControlConfiguration(
        List<String> origin,
        List<String> methods,
        List<String> headers,
        Boolean credentials,
        Long maxAge) {
}