package org.camunda.community.api;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * OpenAPI 3.0 configuration for Swagger UI documentation.
 * Configures metadata, servers, contact info, and external documentation links
 * for REST API endpoints.
 */
@Configuration
public class OpenApiConfig {

    private static final String OPENAPI_TITLE = "Orchestration API Client Java API";
    private static final String DEFAULT_VERSION = "0.0.1-SNAPSHOT";

    private static String resolveApplicationVersion() {
        String implementationVersion = OpenApiConfig.class.getPackage().getImplementationVersion();
        return implementationVersion != null ? implementationVersion : DEFAULT_VERSION;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(OPENAPI_TITLE)
                        .version(resolveApplicationVersion())
                        .description("REST and SOAP API client for interacting with Camunda 8 Orchestration Cluster. " +
                                "This application provides endpoints to search and evaluate DMN decision definitions " +
                                "using the official Camunda Java Client library.")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local HTTP server")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Camunda 8 Documentation")
                        .url("https://docs.camunda.io/"));
    }
}
