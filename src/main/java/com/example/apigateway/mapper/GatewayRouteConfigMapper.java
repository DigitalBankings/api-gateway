package com.example.apigateway.mapper;

import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import com.example.apigateway.modles.*;
import org.springframework.stereotype.Component;

@Component
public class GatewayRouteConfigMapper {

  public GatewayRouteConfigResponse toResponse(
      GatewayRoute route,
      GatewayRateLimitPolicy rateLimit,
      GatewayKeyResolverPolicy keyResolver,
      GatewayCircuityBreakerPolicy cb) {

    return GatewayRouteConfigResponse.builder()
        .routeId(route.getRouteCode())
        .serviceName(route.getServiceName())
        .uri(route.getTargetUri())
        .predicates(
            GatewayRouteConfigResponse.Predicates.builder()
                .path(route.getPath())
                .method(route.getHttpMethod())
                .build())
        .filters(
            GatewayRouteConfigResponse.Filters.builder()
                .timeoutMs(route.getTimeOutMs())
                .authRequired(route.getAuthRequired())
                .rateLimit(buildRateLimit(rateLimit, keyResolver))
                .circuitBreaker(buildCircuitBreaker(cb))
                .build())
        .build();
  }

  private GatewayRouteConfigResponse.RateLimit buildRateLimit(
      GatewayRateLimitPolicy rateLimit, GatewayKeyResolverPolicy keyResolver) {

    if (rateLimit == null) return null;

    return GatewayRouteConfigResponse.RateLimit.builder()
        .replenishRate(rateLimit.getReplenishRate())
        .burstCapacity(rateLimit.getBurstCapacity())
        .windowSeconds(rateLimit.getWindowSeconds())
        .keyResolver(
            keyResolver == null
                ? null
                : GatewayRouteConfigResponse.KeyResolver.builder()
                    .strategy(keyResolver.getStrategy().name())
                    .headerName(keyResolver.getHeaderName())
                    .fallbackStrategy(
                        keyResolver.getFallbackStrategy() == null
                            ? null
                            : keyResolver.getFallbackStrategy().name())
                    .build())
        .build();
  }

  private GatewayRouteConfigResponse.CircuitBreaker buildCircuitBreaker(
      GatewayCircuityBreakerPolicy cb) {
    if (cb == null) return null;

    return GatewayRouteConfigResponse.CircuitBreaker.builder()
        .slidingWindowType(cb.getSlidingWindowType().name())
        .windowSize(cb.getSlidingWindowSize())
        .failureRateThreshold(cb.getFailureRateThreshold())
        .slowCallRateThreshold(cb.getSlowCallRateThreshold())
        .slowCallDurationMs(cb.getSlowCallDurationMs())
        .openStateWaitMs(cb.getOpenStateWaitMs())
        .halfOpenCalls(cb.getHalfOpenCalls())
        .timeoutMs(cb.getTimeoutMs())
        .build();
  }
}
