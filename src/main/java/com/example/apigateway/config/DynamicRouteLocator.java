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
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
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
  private final ReactiveResilience4JCircuitBreakerFactory cbFactory;

  @Override
  public Flux<RouteDefinition> getRouteDefinitions() {
    Collection<GatewayRouteConfigResponse> routes = gatewayRouteStore.getAllRoutes();
    return Flux.fromIterable(routes).map(this::buildRouteDefinition);
  }

  private RouteDefinition buildRouteDefinition(GatewayRouteConfigResponse route) {

    RouteDefinition definition = new RouteDefinition();
    definition.setId(route.getRouteId());
    definition.setUri(URI.create(route.getUri()));

    definition.setPredicates(buildPredicates(route));
    definition.setFilters(buildFilters(route));

    log.info("Loaded Route: {} → {}", route.getRouteId(), route.getUri());
    return definition;
  }

  // ===================== PREDICATES =====================

  private List<PredicateDefinition> buildPredicates(GatewayRouteConfigResponse route) {

    List<PredicateDefinition> predicates = new ArrayList<>();
    var predicateConfig = route.getPredicates();

    if (predicateConfig == null) return predicates;

    if (predicateConfig.getPath() != null) {
      predicates.add(createPredicate("Path", "pattern", predicateConfig.getPath()));
    }

    if (predicateConfig.getMethod() != null) {
      predicates.add(
          createPredicate("Method", "methods", predicateConfig.getMethod().toUpperCase()));
    }

    return predicates;
  }

  private PredicateDefinition createPredicate(String name, String key, String value) {
    PredicateDefinition predicate = new PredicateDefinition();
    predicate.setName(name);
    predicate.addArg(key, value);
    return predicate;
  }

  // ===================== FILTERS =====================

  private List<FilterDefinition> buildFilters(GatewayRouteConfigResponse route) {

    List<FilterDefinition> filters = new ArrayList<>();
    var filterConfig = route.getFilters();

    if (filterConfig == null) return filters;

    buildRateLimiter(filterConfig, filters);
    buildCircuitBreaker(route, filterConfig, filters);

    return filters;
  }

  private void buildRateLimiter(Object filterConfigObj, List<FilterDefinition> filters) {

    var filterConfig = (GatewayRouteConfigResponse.Filters) filterConfigObj;

    if (filterConfig.getRateLimit() == null) return;

    FilterDefinition rl = new FilterDefinition();
    rl.setName("RequestRateLimiter");
    rl.addArg(
        "redis-rate-limiter.replenishRate",
        String.valueOf(filterConfig.getRateLimit().getReplenishRate()));
    rl.addArg(
        "redis-rate-limiter.burstCapacity",
        String.valueOf(filterConfig.getRateLimit().getBurstCapacity()));
    rl.addArg("key-resolver", "#{@ipKeyResolver}");

    filters.add(rl);
  }

  private void buildCircuitBreaker(
      GatewayRouteConfigResponse route,
      GatewayRouteConfigResponse.Filters filterConfig,
      List<FilterDefinition> filters) {

    if (filterConfig.getCircuitBreaker() == null && filterConfig.getTimeoutMs() == null) {
      return;
    }

    String cbName = route.getRouteId();

    Duration timeout =
        filterConfig.getTimeoutMs() != null
            ? Duration.ofMillis(filterConfig.getTimeoutMs())
            : Duration.ofSeconds(5);

    TimeLimiterConfig timeLimiterConfig = createTimeLimiterConfig(timeout);
    CircuitBreakerConfig cbConfig = createCircuitBreakerConfig(filterConfig);

    cbFactory.configure(
        builder -> builder.circuitBreakerConfig(cbConfig).timeLimiterConfig(timeLimiterConfig),
        cbName);

    FilterDefinition breaker = new FilterDefinition();
    breaker.setName("CircuitBreaker");
    breaker.addArg("name", cbName);

    filters.add(breaker);
  }

  private TimeLimiterConfig createTimeLimiterConfig(Duration timeout) {
    return TimeLimiterConfig.custom().timeoutDuration(timeout).cancelRunningFuture(true).build();
  }

  private CircuitBreakerConfig createCircuitBreakerConfig(
      GatewayRouteConfigResponse.Filters filterConfig) {

    var cb = filterConfig.getCircuitBreaker();

    return CircuitBreakerConfig.custom()
        .failureRateThreshold(cb != null ? cb.getFailureRateThreshold() : 50)
        .slowCallRateThreshold(cb != null ? cb.getSlowCallRateThreshold() : 50)
        .slowCallDurationThreshold(
            cb != null ? Duration.ofMillis(cb.getSlowCallDurationMs()) : Duration.ofSeconds(5))
        .waitDurationInOpenState(
            cb != null ? Duration.ofMillis(cb.getOpenStateWaitMs()) : Duration.ofSeconds(10))
        .permittedNumberOfCallsInHalfOpenState(cb != null ? cb.getHalfOpenCalls() : 3)
        .build();
  }
}
