package io.github.zacharysabourin.donezo_api.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Datasource configuration for this application. Connection settings can be
 * updated by modifying the <code>application.properties</code> file and
 * changing the <code>donezo.datasource.*</code> values. Disabled during testing
 * to prevent collision with the
 * {@link EmbeddedPostgresWithFlywayDataSourceConfiguration} class.
 */
@Configuration(proxyBeanMethods = false)
@Profile("!test")
public class DonezoDataSourceConfiguration {

    @Bean
    @ConfigurationProperties("donezo.datasource")
    DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
}
