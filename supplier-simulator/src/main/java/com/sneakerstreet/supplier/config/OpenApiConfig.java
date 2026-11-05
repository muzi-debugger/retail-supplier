package com.sneakerstreet.supplier.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI supplierOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sneaker Street Supplier API")
                        .version("1.0.0")
                        .description("""
                                Supplier catalogue and inventory
                                management API.

                                Part of the Sneaker Street distributed
                                e-commerce platform.

                                This service manages supplier products,
                                variants and, in future iterations,
                                inventory and retailer orders.
                                """)
                        .contact(new Contact()
                                .name("Sneaker Street Engineering")));
    }
}
