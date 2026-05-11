package com.riwi.intro.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Test API",
        version = "1.0",
        description = "API de prueba nueva",
        contact = @Contact(name = "Soporte Riwi", email = "info@riwi.io")

))
public class SwaggerConfig {
}
