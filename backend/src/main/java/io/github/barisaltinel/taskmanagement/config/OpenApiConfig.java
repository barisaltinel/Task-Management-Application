package io.github.barisaltinel.taskmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Management Application API")
                        .version("v1")
                        .description("API documentation for the Task Management Application backend."))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("opaque-token")
                                        .description("Paste the opaque bearer token returned by the login endpoint. This API does not use JWTs.")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}

