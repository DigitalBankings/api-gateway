package com.example.apigateway.config;

import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import com.example.apigateway.services.caches.GatewayRouteStore;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DynamicRouteLocator implements RouteDefinitionLocator {

    private final GatewayRouteStore gatewayRouteStore;
    private final RedisRateLimiter redisRateLimiter;
    private final KeyResolver keyResolver;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {

        List<RouteDefinition> routes = new ArrayList<>();

        for (GatewayRouteConfigResponse route : gatewayRouteStore.getAllCachedOrDbRoutes()) {

            RouteDefinition def = new RouteDefinition();
            def.setId(route.getRouteId());

            // ✅ Fix typo
            String fixedUri = route.getUri().replace("lcoalhost", "localhost");
            def.setUri(URI.create(fixedUri));

            // --- Predicates ---
            List<PredicateDefinition> predicates = new ArrayList<>();
            if (route.getPredicates() != null) {
                if (route.getPredicates().getPath() != null) {
                    PredicateDefinition path = new PredicateDefinition();
                    path.setName("Path");
                    path.addArg("pattern", route.getPredicates().getPath());
                    predicates.add(path);
                }
                if (route.getPredicates().getMethod() != null) {
                    PredicateDefinition method = new PredicateDefinition();
                    method.setName("Method");
                    method.addArg("methods", route.getPredicates().getMethod());
                    predicates.add(method);
                }
            }
            def.setPredicates(predicates);

            // --- Filters ---
            List<FilterDefinition> filters = new ArrayList<>();
            if (route.getFilters() != null) {

                // RateLimiter
                if (route.getFilters().getRateLimit() != null) {
                    FilterDefinition rl = new FilterDefinition();
                    rl.setName("RequestRateLimiter");
                    rl.addArg("redis-rate-limiter.replenishRate",
                            String.valueOf(route.getFilters().getRateLimit().getReplenishRate()));
                    rl.addArg("redis-rate-limiter.burstCapacity",
                            String.valueOf(route.getFilters().getRateLimit().getBurstCapacity()));
                    rl.addArg("key-resolver", "#{@keyResolver}");
                    filters.add(rl);
                }

                // CircuitBreaker
                if (route.getFilters().getCircuitBreaker() != null) {
                    FilterDefinition cb = new FilterDefinition();
                    cb.setName("CircuitBreaker");
                    cb.addArg("name", route.getRouteId() + "CB");
                    cb.addArg("fallbackUri", "forward:/fallback");
                    filters.add(cb);
                }
            }

            def.setFilters(filters);
            routes.add(def);
        }

        return Flux.fromIterable(routes);
    }

}
