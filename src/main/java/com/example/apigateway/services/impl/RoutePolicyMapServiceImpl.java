package com.example.apigateway.services.impl;

import com.example.apigateway.dto.gatewayroutemapdto.CreateGatewayRouteMapRequest;
import com.example.apigateway.dto.gatewayroutemapdto.ResponseGatewayRouteMap;
import com.example.apigateway.modles.GatewayRoutePolicyMap;
import com.example.apigateway.repositories.*;
import com.example.apigateway.services.RoutePolicyMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoutePolicyMapServiceImpl implements RoutePolicyMapService {

  private final GatewayRoutePolicyMapRepository gatewayRoutePolicyMapRepository;
  private final GatewayRouteRepository gatewayRouteRepository;
  private final GatewayRateLimitPolicyRepository gatewayRateLimitPolicyRepository;
  private final GatewayCircuityBreakerPolicyRepository gatewayCircuityBreakerPolicyRepository;
  private final GatewayKeyResolverPolicyRepository gatewayKeyResolverPolicyRepository;

  @Override
  public ResponseGatewayRouteMap createRouteMap(CreateGatewayRouteMapRequest request) {

    // 1️⃣ Save mapping
    GatewayRoutePolicyMap savedMap = gatewayRoutePolicyMapRepository.save(request.toEntity());

    // 2️⃣ Load related route
    var route = gatewayRouteRepository.findById(savedMap.getRouteId())
            .orElseThrow(() -> new RuntimeException("Route not found"));

    // 3️⃣ Load rate limit and key resolver
    var rateLimit = savedMap.getRateLimitPolicyId() != null
            ? gatewayRateLimitPolicyRepository.findById(savedMap.getRateLimitPolicyId())
            .orElse(null)
            : null;

    var keyResolver = rateLimit != null && rateLimit.getKeyResolverPolicyId() != null
            ? gatewayKeyResolverPolicyRepository.findById(rateLimit.getKeyResolverPolicyId()).orElse(null)
            : null;

    // 4️⃣ Load circuit breaker
    var cb = savedMap.getCircuitBreakerPolicyId() != null
            ? gatewayCircuityBreakerPolicyRepository.findById(savedMap.getCircuitBreakerPolicyId())
            .orElse(null)
            : null;

    // 5️⃣ Build full modern config response
    return ResponseGatewayRouteMap.builder()
            .routeCode(route.getRouteCode())
            .serviceName(route.getServiceName())
            .path(route.getPath())
            .targetUri(route.getTargetUri())
            .method(route.getHttpMethod())
            .timeoutMs(route.getTimeOutMs())
            .authRequired(true)
            .rateLimit(rateLimit != null
                    ? ResponseGatewayRouteMap.RateLimitDTO.builder()
                    .replenishRate(rateLimit.getReplenishRate())
                    .burstCapacity(rateLimit.getBurstCapacity())
                    .windowSeconds(rateLimit.getWindowSeconds())
                    .keyResolver(keyResolver != null ? keyResolver.getStrategy() : null)
                    .build()
                    : null)
            .circuitBreaker(cb != null
                    ? ResponseGatewayRouteMap.CircuitBreakerDTO.builder()
                    .slidingWindowType(cb.getSlidingWindowType())
                    .windowSize(cb.getSlidingWindowSize())
                    .failureRateThreshold(cb.getFailureRateThreshold())
                    .slowCallRateThreshold(cb.getSlowCallRateThreshold())
                    .slowCallDurationMs(cb.getSlowCallDurationMs())
                    .openStateWaitMs(cb.getOpenStateWaitMs())
                    .halfOpenCalls(cb.getHalfOpenCalls())
                    .timeoutMs(cb.getTimeoutMs())
                    .build()
                    : null)
            .build();
  }
}

