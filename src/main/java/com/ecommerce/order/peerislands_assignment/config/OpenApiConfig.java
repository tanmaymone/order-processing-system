package com.ecommerce.order.peerislands_assignment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "Peerislands Order API",
            version = "v1",
            description = "REST API for creating, retrieving, listing, and updating e-commerce orders.",
            contact = @Contact(name = "Order Service Team"),
            license = @License(name = "Proprietary")))
public class OpenApiConfig {}
