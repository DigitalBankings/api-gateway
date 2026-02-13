package com.example.apigateway.config;

import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import com.example.apigateway.services.caches.GatewayRouteStore;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicRouteLocator implements RouteDefinitionLocator {

    private final GatewayRouteStore gatewayRouteStore;
    private final ReactiveResilience4JCircuitBreakerFactory cbFactory;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {

        Collection<GatewayRouteConfigResponse> routes = gatewayRouteStore.getAllRoutes();

        return Flux.fromIterable(routes)
                .map(this::buildRouteDefinition);
    }

    private RouteDefinition buildRouteDefinition(GatewayRouteConfigResponse route) {

        RouteDefinition def = new RouteDefinition();
        def.setId(route.getRouteId());
        def.setUri(URI.create(route.getUri()));

        // ------------------ Predicates ------------------
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
                method.addArg("methods", route.getPredicates().getMethod().toUpperCase());
                predicates.add(method);
            }
        }
        def.setPredicates(predicates);

        // ------------------ Filters ------------------
        List<FilterDefinition> filters = new ArrayList<>();
        if (route.getFilters() != null) {
            var filterCfg = route.getFilters();

            // ================= RATE LIMIT =================
            if (filterCfg.getRateLimit() != null) {
                FilterDefinition rl = new FilterDefinition();
                rl.setName("RequestRateLimiter");
                rl.addArg("redis-rate-limiter.replenishRate",
                        String.valueOf(filterCfg.getRateLimit().getReplenishRate()));
                rl.addArg("redis-rate-limiter.burstCapacity",
                        String.valueOf(filterCfg.getRateLimit().getBurstCapacity()));
                rl.addArg("key-resolver", "#{@ipKeyResolver}");
                filters.add(rl);
            }

            // ================= CIRCUIT BREAKER + TIMELIMITER =================
            if (filterCfg.getCircuitBreaker() != null || filterCfg.getTimeoutMs() != null) {

                FilterDefinition breaker = new FilterDefinition();
                breaker.setName("CircuitBreaker");

                // Use routeId as the dynamic CircuitBreaker name
                String cbName = route.getRouteId();
                breaker.addArg("name", cbName);

                // Dynamically create Resilience4j configs
                Duration timeoutDuration = filterCfg.getTimeoutMs() != null
                        ? Duration.ofMillis(filterCfg.getTimeoutMs())
                        : Duration.ofSeconds(5); // fallback default

                TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                        .timeoutDuration(timeoutDuration)
                        .cancelRunningFuture(true)
                        .build();

                CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                        .failureRateThreshold(
                                filterCfg.getCircuitBreaker() != null
                                        ? filterCfg.getCircuitBreaker().getFailureRateThreshold()
                                        : 50)
                        .slowCallRateThreshold(
                                filterCfg.getCircuitBreaker() != null
                                        ? filterCfg.getCircuitBreaker().getSlowCallRateThreshold()
                                        : 50)
                        .slowCallDurationThreshold(
                                filterCfg.getCircuitBreaker() != null
                                        ? Duration.ofMillis(filterCfg.getCircuitBreaker().getSlowCallDurationMs())
                                        : Duration.ofSeconds(5))
                        .waitDurationInOpenState(
                                filterCfg.getCircuitBreaker() != null
                                        ? Duration.ofMillis(filterCfg.getCircuitBreaker().getOpenStateWaitMs())
                                        : Duration.ofSeconds(10))
                        .permittedNumberOfCallsInHalfOpenState(
                                filterCfg.getCircuitBreaker() != null
                                        ? filterCfg.getCircuitBreaker().getHalfOpenCalls()
                                        : 3)
                        .build();

                // Register dynamically per route
                cbFactory.configure(builder ->
                                builder.circuitBreakerConfig(cbConfig)
                                        .timeLimiterConfig(timeLimiterConfig),
                        cbName);

                filters.add(breaker);
            }
        }

        def.setFilters(filters);

        log.info("Loaded Route: {} → {}", route.getRouteId(), route.getUri());

        return def;
    }
}
