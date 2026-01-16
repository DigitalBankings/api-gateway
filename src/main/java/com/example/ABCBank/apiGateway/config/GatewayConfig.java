package com.example.ABCBank.apiGateway.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import reactor.core.publisher.Mono;


@Configuration
public class GatewayConfig {

    // This resolves the key for rate limiting (could be client ID, IP, etc.)
    @Bean
    public KeyResolver clientKeyResolver() {
        return exchange -> {
            // Example: resolve by client IP
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
