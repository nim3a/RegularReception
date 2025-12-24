package com.daryaftmanazam.daryaftcore.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration for API documentation.
 * Access at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configure OpenAPI documentation.
     *
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI daryaftCoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("دریافت منظم API")
                        .description("سیستم مدیریت دریافت و اشتراک‌های منظم")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("تیم توسعه دریافت منظم")
                                .email("support@daryaftmanazam.com")
                                .url("https://daryaftmanazam.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("سرور توسعه"),
                        new Server()
                                .url("https://api.daryaftmanazam.com")
                                .description("سرور تولید")))
                .externalDocs(new ExternalDocumentation()
                        .description("مستندات سیستم دریافت منظم")
                        .url("https://docs.daryaftmanazam.com"));
    }
}
