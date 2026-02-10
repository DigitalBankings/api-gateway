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

  @Override
  public Flux<RouteDefinition> getRouteDefinitions() {

    Collection<GatewayRouteConfigResponse> routes = gatewayRouteStore.getAllRoutes();

    return Flux.fromIterable(routes)
        .filter(r -> r.getPredicates() != null && r.getPredicates().getPath() != null)
        .map(
            route -> {
              RouteDefinition def = new RouteDefinition();
              def.setId(route.getRouteId());

              // FIX URI typo
              String fixedUri = route.getUri().replace("lcoalhost", "localhost");
              def.setUri(URI.create(fixedUri));

              /* ------------------ Predicates ------------------ */
              List<PredicateDefinition> predicates = new ArrayList<>();

              PredicateDefinition path = new PredicateDefinition();
              path.setName("Path");
              path.addArg("pattern", route.getPredicates().getPath());
              predicates.add(path);

              if (route.getPredicates().getMethod() != null) {
                PredicateDefinition method = new PredicateDefinition();
                method.setName("Method");
                method.addArg("methods", route.getPredicates().getMethod().toUpperCase());
                predicates.add(method);
              }

              def.setPredicates(predicates);

              /* ------------------ Filters ------------------ */
              List<FilterDefinition> filters = new ArrayList<>();
              var filterCfg = route.getFilters();
              var rateLimit = filterCfg != null ? filterCfg.getRateLimit() : null;

              if (filterCfg != null) {
                // ---------------- RATE LIMIT ----------------
                if (rateLimit != null) {
                  FilterDefinition rl = new FilterDefinition();
                  rl.setName("RequestRateLimiter");
                  rl.addArg(
                      "redis-rate-limiter.replenishRate",
                      String.valueOf(rateLimit.getReplenishRate()));
                  rl.addArg(
                      "redis-rate-limiter.burstCapacity",
                      String.valueOf(rateLimit.getBurstCapacity()));

                  String strategy = rateLimit.getKeyResolver().getStrategy();
                  if ("JWT_CLAIM".equalsIgnoreCase(strategy)) {
                    rl.addArg("key-resolver", "#{@clientKeyResolver}");
                  } else {
                    rl.addArg("key-resolver", "#{@ipKeyResolver}");
                  }

                  filters.add(rl);
                }

                // ---------------- CIRCUIT BREAKER ----------------
                var cb = filterCfg.getCircuitBreaker();
                if (cb != null) {
                  FilterDefinition breaker = new FilterDefinition();
                  breaker.setName("CircuitBreaker");
                  breaker.addArg("name", route.getRouteId() + "-cb");
                  filters.add(breaker);
                }

                // ---------------- TIMEOUT ----------------
                if (filterCfg.getTimeoutMs() != null && filterCfg.getTimeoutMs() > 0) {
                  FilterDefinition timeout = new FilterDefinition();
                  timeout.setName("CustomTimeout");
                  timeout.addArg("timeoutMs", String.valueOf(filterCfg.getTimeoutMs()));
                  filters.add(timeout);
                }
              }

              def.setFilters(filters);
              return def;
            });
  }
}
