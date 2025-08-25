package com.teamphacode.MerchantManagement.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

    // Định nghĩa security scheme cho Swagger (Bearer JWT)
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    // Tạo một server object (liệt kê server trong Swagger UI)
    private Server createServer(String url, String description) {
        Server server = new Server();
        server.setUrl(url);
        server.setDescription(description);
        return server;
    }

    private Contact createContact() {
        return new Contact()
                .email("tuanne2542004@gmail.com")
                .name("Tuanne")
                .url("https://tuanne.vn");
    }

    private License createLicense() {
        return new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");
    }

    private Info createApiInfo() {
        return new Info()
                .title("Tuanne API")
                .version("1.0")
                .contact(createContact())
                .description("Tuanne hehe")
                .termsOfService("https://tuanne.vn/donate")
                .license(createLicense());
    }

   // Bean chính -> cấu hình OpenAPI cho swagger-ui
    @Bean
    public OpenAPI myOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo()) // set metadata cho spring
                .servers(List.of(
                        createServer("http://localhost:8080", "Server URL in Development environment"),
                        createServer("https://tuanne.vn", "Server URL in Production environment")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication")) // yêu cầu security
                .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }
}