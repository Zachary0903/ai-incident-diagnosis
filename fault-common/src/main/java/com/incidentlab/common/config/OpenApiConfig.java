package com.incidentlab.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * OpenAPI docs + Swagger UI for every service that depends on fault-common.
 *
 * <p>UI at {@code /swagger-ui.html}; raw specs at {@code /v3/api-docs/{group}}. The UI opens on
 * "All endpoints" (every path; see {@code urls-primary-name}). The other groups split the same
 * paths into the service's own API, the fault-injection admin endpoints, and actuator.
 * Defaults live in {@code openapi-defaults.properties}; a service's application.yml overrides them.
 */
@Configuration
@PropertySource("classpath:openapi-defaults.properties")
public class OpenApiConfig {

    static final String ADMIN_PATHS = "/admin/**";
    static final String ACTUATOR_PATHS = "/actuator/**";
    /** Must match springdoc.swagger-ui.urls-primary-name so Swagger UI opens on this group. */
    static final String ALL_DISPLAY_NAME = "All endpoints";

    @Bean
    OpenAPI serviceOpenApi(@Value("${spring.application.name}") String serviceName) {
        return new OpenAPI().info(new Info()
                .title(serviceName)
                .version("0.1.0")
                .description("HTTP endpoints of " + serviceName + " (incident-diagnosis lab)"));
    }

    @Bean
    GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName(ALL_DISPLAY_NAME)
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    GroupedOpenApi serviceApi() {
        return GroupedOpenApi.builder()
                .group("service")
                .displayName("Service API")
                .pathsToExclude(ADMIN_PATHS, ACTUATOR_PATHS)
                .build();
    }

    @Bean
    GroupedOpenApi faultAdminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("Fault injection (admin)")
                .pathsToMatch(ADMIN_PATHS)
                .build();
    }

    @Bean
    GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
                .group("actuator")
                .displayName("Actuator")
                .pathsToMatch(ACTUATOR_PATHS)
                .build();
    }
}
