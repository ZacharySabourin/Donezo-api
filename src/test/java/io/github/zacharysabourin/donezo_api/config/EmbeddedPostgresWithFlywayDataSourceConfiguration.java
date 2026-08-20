package io.github.zacharysabourin.donezo_api.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.opentable.db.postgres.embedded.FlywayPreparer;
import com.opentable.db.postgres.embedded.PreparedDbProvider;

/**
 * Datasource configuration for this testing. Embeds a postgresql database that
 * is auto-migrated using flyway.
 */
@TestConfiguration
public class EmbeddedPostgresWithFlywayDataSourceConfiguration {

    @Bean
    DataSource dataSource() throws SQLException {
        return PreparedDbProvider
                .forPreparer(FlywayPreparer.forClasspathLocation("db/migration"))
                .createDataSource();
    }
}
