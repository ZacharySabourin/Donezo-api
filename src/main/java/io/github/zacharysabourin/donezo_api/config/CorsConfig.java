package io.github.zacharysabourin.donezo_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final AccessControlConfiguration config;

    public CorsConfig(AccessControlConfiguration config) {
        this.config = config;
    }

    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(config.origin)
                        .allowedMethods(config.methods)
                        .allowedHeaders(config.headers)
                        .allowCredentials(true);
            }
        };
    }

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
            Boolean enabled,
            String origin,
            String[] methods,
            String headers) {

    }
}