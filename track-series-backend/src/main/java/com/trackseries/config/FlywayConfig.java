package com.trackseries.config;

import java.util.Arrays;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            MigrationInfo[] migrations = flyway.info().all();
            boolean hasFailedMigration = Arrays.stream(migrations)
                    .anyMatch(migration -> migration != null && migration.getState() == MigrationState.FAILED);

            if (hasFailedMigration) {
                flyway.repair();
            }

            flyway.migrate();
        };
    }

}