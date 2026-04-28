package com.learn.restapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce REST API — Learning Project")
                        .version("1.0.0")
                        .description("""
                                A hands-on REST API for learning core and enterprise concepts.

                                **Public endpoints** (no auth needed):
                                - GET /api/products
                                - GET /api/products/{id}

                                **Authenticated endpoints** (use Basic Auth):
                                - All /api/orders/** — any logged-in user
                                - POST/PUT/DELETE /api/products/** — ADMIN only

                                **Credentials for testing:**
                                | Username | Password     | Role  |
                                |----------|-------------|-------|
                                | admin    | password123 | ADMIN |
                                | user     | user123     | USER  |
                                """)
                        .contact(new Contact().name("Learning Project")))
                .components(new Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("Use admin/password123 or user/user123")));
    }
}
