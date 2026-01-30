package com.example.apigateway.config;

import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import com.example.apigateway.services.caches.GatewayRouteStore;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Configuration
@RequiredArgsConstructor
public class DynamicGatewayConfig {

    private final GatewayRouteStore gatewayRouteStore; // your service to fetch routes

    // ---------------------------
    // Single RedisRateLimiter Bean
    // ---------------------------
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20); // 10 requests/sec, burst 20
    }

    // ---------------------------
    // Single KeyResolver Bean
    // ---------------------------
    @Bean
    @Primary
    public KeyResolver keyResolver() {
        return exchange -> {
            String clientId = exchange.getRequest().getHeaders().getFirst("X-Client-Id");
            return Mono.just(clientId != null ? clientId : "anonymous");
        };
    }

    // ---------------------------
    // Dynamic RouteLocator Bean
    // ---------------------------
    @Bean
    public RouteLocator dynamicRoutes(
            RouteLocatorBuilder builder,
            KeyResolver keyResolver,
            RedisRateLimiter redisRateLimiter,
            RequestRateLimiterGatewayFilterFactory rateLimiterFactory, // injected here only
            SpringCloudCircuitBreakerFilterFactory circuitBreakerFactory // injected here only
    ) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        Collection<GatewayRouteConfigResponse> allRoutes = gatewayRouteStore.getAllCachedOrDbRoutes();

        for (GatewayRouteConfigResponse route : allRoutes) {
            String fixedUri = route.getUri().replace("lcoalhost", "localhost");

            routes.route(route.getRouteId(), r -> r
                    // 1️⃣ Predicates
                    .path(route.getPredicates().getPath())
                    .and()
                    .method(route.getPredicates().getMethod())
                    // 2️⃣ Filters
                    .filters(f -> {
                        if (route.getFilters() != null && route.getFilters().getRateLimit() != null) {
                            f.requestRateLimiter(c -> {
                                c.setRateLimiter(redisRateLimiter);
                                c.setKeyResolver(keyResolver);
                            });
                        }
                        if (route.getFilters() != null && route.getFilters().getCircuitBreaker() != null) {
                            f.circuitBreaker(c -> {
                                c.setName(route.getRouteId() + "-CB");
                                c.setFallbackUri("forward:/fallback");
                            });
                        }
                        return f;
                    })
                    // 3️⃣ Target URI
                    .uri(fixedUri)
            );
        }

        return routes.build();
    }
}
