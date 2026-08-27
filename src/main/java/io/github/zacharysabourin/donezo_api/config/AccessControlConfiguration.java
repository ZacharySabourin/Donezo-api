package io.github.zacharysabourin.donezo_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration object used to add <code>Access-Control-Allow</code> headers to
 * a response. Configured by updating the
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
                Boolean enabled,
                String origin,
                String methods,
                String headers) {

}
