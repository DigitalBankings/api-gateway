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

    return Flux.fromIterable(allRoutes)
            .filter(route -> route.getPredicates() != null && route.getPredicates().getPath() != null)
            .map(route -> {

              RouteDefinition def = new RouteDefinition();
              def.setId(route.getRouteId());

              // Fix URI typo if exists
              String fixedUri = route.getUri().replace("lcoalhost", "localhost");
              def.setUri(URI.create(fixedUri));

              // -------------------------
              // Predicates
              // -------------------------
              List<PredicateDefinition> predicates = new ArrayList<>();

              PredicateDefinition path = new PredicateDefinition();
              path.setName("Path");
              path.addArg("pattern", route.getPredicates().getPath()); // e.g. /otp/retrieve
              predicates.add(path);

              if (route.getPredicates().getMethod() != null) {
                PredicateDefinition method = new PredicateDefinition();
                method.setName("Method");
                method.addArg("methods", route.getPredicates().getMethod()); // GET / POST
                predicates.add(method);
              }

              def.setPredicates(predicates);

              // -------------------------
              // Filters (optional)
              // -------------------------
              List<FilterDefinition> filters = new ArrayList<>();

              if (route.getFilters() != null && route.getFilters().getRateLimit() != null) {
                FilterDefinition rl = new FilterDefinition();
                rl.setName("RequestRateLimiter");
                rl.addArg("redis-rate-limiter.replenishRate",
                        String.valueOf(route.getFilters().getRateLimit().getReplenishRate()));
                rl.addArg("redis-rate-limiter.burstCapacity",
                        String.valueOf(route.getFilters().getRateLimit().getBurstCapacity()));
                rl.addArg("key-resolver", "#{@keyResolver}");
                filters.add(rl);
              }

              def.setFilters(filters);
              return def;
            });
  }

}
