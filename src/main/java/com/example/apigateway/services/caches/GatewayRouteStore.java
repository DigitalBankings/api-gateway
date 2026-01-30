package com.example.apigateway.services.caches;

import com.example.apigateway.config.CacheNames;
import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import com.example.apigateway.services.RoutePolicyMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class GatewayRouteStore {

  private final RoutePolicyMapService routePolicyMapService;

  /**
   * READ - cache per routeCode
   * Cache miss → fallback to DB
   */
  @Cacheable(cacheNames = CacheNames.GATEWAY_ROUTE, key = "#routeCode")
  public GatewayRouteConfigResponse get(String routeCode) {
    return routePolicyMapService.getRouteConfigFromDB(routeCode);
  }

  /**
   * WRITE / UPDATE
   */
  @CachePut(cacheNames = CacheNames.GATEWAY_ROUTE, key = "#route.routeId")
  public GatewayRouteConfigResponse put(GatewayRouteConfigResponse route) {
    return route;
  }

  /**
   * DELETE
   */
  @CacheEvict(cacheNames = CacheNames.GATEWAY_ROUTE, key = "#routeCode")
  public void evict(String routeCode) {}

  /**
   * CLEAR ALL
   */
  @CacheEvict(cacheNames = CacheNames.GATEWAY_ROUTE, allEntries = true)
  public void evictAll() {}

  /**
   * GET ALL ROUTES - fallback to DB
   * Spring Cache cannot iterate keys, so we fetch from DB
   */
  public Collection<GatewayRouteConfigResponse> getAllCachedOrDbRoutes() {
    return routePolicyMapService.getAllRoutesFromDB(); // DB fetch + optional caching
  }


}
