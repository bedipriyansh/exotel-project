package com.exotel.missedcalls.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI exotelMissedCallOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Exotel Missed Call Tracking System")
                        .description("REST APIs for capturing and retrieving missed call events received from Exotel webhooks.")
                        .version("1.0.0")
                        .contact(new Contact().name("Engineering Team").email("engineering@example.com"))
                        .license(new License().name("Internal Use")));
    }
}
