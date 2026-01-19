package com.example.apiGateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Configuration
public class GatewayConfig {

    // ------------------------
    // Key resolver for Redis rate limiter
    // ------------------------
    @Bean
    public KeyResolver clientKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (ip == null) {
                ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
            }
            return Mono.just(ip);
        };
    }

    // ------------------------
    // Circuit breaker configuration
    // -----------------------
    @Bean
    @ConditionalOnMissingBean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> {

            // CircuitBreaker configuration
            CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                    .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                    .slidingWindowSize(20)
                    .failureRateThreshold(50)
                    .slowCallRateThreshold(50)
                    .slowCallDurationThreshold(Duration.ofSeconds(2)) // 3s -> slow call
                    .waitDurationInOpenState(Duration.ofSeconds(30))
                    .permittedNumberOfCallsInHalfOpenState(5)
                    .recordExceptions(
                            java.net.ConnectException.class,
                            java.util.concurrent.TimeoutException.class
                    )
                    .build();

            // TimeLimiter configuration
            TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                    .timeoutDuration(Duration.ofSeconds(2)) // Gateway timeout 2s
                    .cancelRunningFuture(true)
                    .build();

            // Apply CB + TL to factory
            factory.configure(builder -> builder
                            .circuitBreakerConfig(circuitBreakerConfig)
                            .timeLimiterConfig(timeLimiterConfig)
                    , "authServiceCB");
        };
    }
}

