package com.example.apigateway.config;

import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import com.example.apigateway.services.caches.GatewayRouteStore;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicRouteLocator implements RouteDefinitionLocator {

  private final GatewayRouteStore gatewayRouteStore;
  private final RedisRateLimiter redisRateLimiter;
  private final KeyResolver keyResolver;

  @Override
  public Flux<RouteDefinition> getRouteDefinitions() {
    Collection<GatewayRouteConfigResponse> allRoutes = gatewayRouteStore.getAllRoutes();
    List<RouteDefinition> routes = new ArrayList<>();

    for (GatewayRouteConfigResponse route : allRoutes) {

      if (route.getPredicates() == null || route.getPredicates().getPath() == null) {
        log.warn("Skipping route {} because path predicate is missing", route.getRouteId());
        continue;
      }

      RouteDefinition def = new RouteDefinition();
      def.setId(route.getRouteId());

      // Fix typo
      String fixedUri = route.getUri().replace("lcoalhost", "localhost");
      def.setUri(URI.create(fixedUri));

      // Predicates
      List<PredicateDefinition> predicates = new ArrayList<>();
      PredicateDefinition path = new PredicateDefinition();
      path.setName("Path");
      path.addArg("pattern", route.getPredicates().getPath());
      predicates.add(path);

      if (route.getPredicates().getMethod() != null) {
        PredicateDefinition method = new PredicateDefinition();
        method.setName("Method");
        method.addArg("methods", route.getPredicates().getMethod());
        predicates.add(method);
      }

      def.setPredicates(predicates);

      // Filters
      List<FilterDefinition> filters = new ArrayList<>();
      if (route.getFilters() != null) {
        // RateLimiter
        if (route.getFilters().getRateLimit() != null) {
          FilterDefinition rl = new FilterDefinition();
          rl.setName("RequestRateLimiter");
          rl.addArg(
              "redis-rate-limiter.replenishRate",
              String.valueOf(route.getFilters().getRateLimit().getReplenishRate()));
          rl.addArg(
              "redis-rate-limiter.burstCapacity",
              String.valueOf(route.getFilters().getRateLimit().getBurstCapacity()));
          rl.addArg("key-resolver", "#{@keyResolver}");
          filters.add(rl);
        }

        // CircuitBreaker
        if (route.getFilters().getCircuitBreaker() != null) {
          FilterDefinition cb = new FilterDefinition();
          cb.setName("CircuitBreaker");
          cb.addArg("name", route.getRouteId() + "-CB");
          cb.addArg("fallbackUri", "forward:/fallback");
          filters.add(cb);
        }
      }

      def.setFilters(filters);
      routes.add(def);
    }

    if (routes.isEmpty()) {
      log.warn("No active routes found — all unmatched requests will return 404");
    }

    return Flux.fromIterable(routes);
  }
}
