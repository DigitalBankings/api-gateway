package com.example.apigateway.services.caches;

import com.example.apigateway.config.CacheNames;
import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import com.example.apigateway.services.RoutePolicyMapService;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayRouteStore {

  private final RoutePolicyMapService routePolicyMapService;
  private final ApplicationEventPublisher publisher;

  /** Get all routes from cache; fallback to DB if missing. Cache key: "ALL" */
  @Cacheable(cacheNames = CacheNames.GATEWAY_ROUTE, key = "'ALL'")
  public Collection<GatewayRouteConfigResponse> getAllRoutes() {
    return routePolicyMapService.getAllRoutesFromDB();
  }

  @CachePut(cacheNames = CacheNames.GATEWAY_ROUTE, key = "'ALL'")
  public Collection<GatewayRouteConfigResponse> refreshAllRoutes() {
    Collection<GatewayRouteConfigResponse> routes = routePolicyMapService.getAllRoutesFromDB();

    // Publish refresh event so Gateway reloads dynamically
    publisher.publishEvent(new RefreshRoutesEvent(this));
    return routes;
  }

  /** Get a single route by routeCode from cache if available */
  @Cacheable(cacheNames = CacheNames.GATEWAY_ROUTE, key = "#routeCode")
  public GatewayRouteConfigResponse get(String routeCode) {
    return routePolicyMapService.getRouteConfigFromDB(routeCode);
  }

  /** Put/update a route into cache */
  @CachePut(cacheNames = CacheNames.GATEWAY_ROUTE, key = "#route.routeId")
  public GatewayRouteConfigResponse put(GatewayRouteConfigResponse route) {
    return route;
  }

  /** Evict a single route from cache */
  @CacheEvict(cacheNames = CacheNames.GATEWAY_ROUTE, key = "#routeCode")
  public void evict(String routeCode) {
    log.info("Evicting route {} from cache", routeCode);
  }

  /** Evict all routes from cache */
  @CacheEvict(cacheNames = CacheNames.GATEWAY_ROUTE, allEntries = true)
  public void evictAll() {
    log.info("Evicting all routes from cache");
  }

  /** Optional helper: get a single route from all routes */
  public Optional<GatewayRouteConfigResponse> getRouteById(String routeId) {
    return getAllRoutes().stream().filter(r -> r.getRouteId().equals(routeId)).findFirst();
  }
}
