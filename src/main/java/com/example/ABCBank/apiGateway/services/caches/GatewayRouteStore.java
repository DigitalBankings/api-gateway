package com.example.ABCBank.apiGateway.services.caches;


import com.example.ABCBank.apiGateway.config.CacheNames;
import com.example.ABCBank.apiGateway.dto.gatewayRoute.GatewayRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatewayRouteStore {
    /**
     * READ
     * - Called first
     * - If cache hit → method body NOT executed
     * - If miss → return null → service loads from DB and saves
     */
    @Cacheable(
            cacheNames = CacheNames.GATEWAY_ROUTE,
            key = "#routeCode",
            unless = "#result == null"
    )
    public GatewayRouteResponse get(String routeCode) {
        return null; // cache miss
    }

    /**
     * WRITE / UPDATE
     * - Always put into cache
     */
    @CachePut(
            cacheNames = CacheNames.GATEWAY_ROUTE,
            key = "#route.routeCode"
    )
    public GatewayRouteResponse put(GatewayRouteResponse route) {
        return route;
    }

    /**
     * DELETE
     */
    @CacheEvict(
            cacheNames = CacheNames.GATEWAY_ROUTE,
            key = "#routeCode"
    )
    public void evict(String routeCode) {
        // eviction only
    }

    /**
     * ADMIN: clear all routes
     */
    @CacheEvict(
            cacheNames = CacheNames.GATEWAY_ROUTE,
            allEntries = true
    )
    public void evictAll() {
    }
}
