package com.example.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("API Gateway")
                .version("v1.0")
                .description("Modern API Gateway documentation")
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
        .servers(
            List.of(
                new Server().url("http://localhost:9090").description("Local server"),
                new Server().url("https://api.example.com").description("Production server")));
  }
}
