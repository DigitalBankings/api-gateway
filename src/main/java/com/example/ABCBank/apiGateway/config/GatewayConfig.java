package com.example.ABCBank.apiGateway.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    // Use real client IP for rate limiting
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Forwarded-For"); // if behind proxy
            if (ip == null) {
                ip = exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress();
            }
            return Mono.just(ip);
        };
    }
}
