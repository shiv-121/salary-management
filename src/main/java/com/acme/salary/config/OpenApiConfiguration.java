package com.acme.salary.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**  a
 * OpenAPI 3.0 configuration for Swagger/Springdoc.
 * Defines API metadata and documentation structure.
 *
 * Available endpoints:
 * - Swagger UI: GET /swagger-ui/index.html
 * - OpenAPI spec: GET /v3/api-docs
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI acmeSalaryManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ACME Salary Management API")
                        .description("REST API for managing employee compensation and salary history")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ACME Engineering")
                                .url("https://salary-ui.onrender.com")
                                .email("shivsharmaw121@gmail.com")));
    }
}
