package com.example.apigateway.services.impl;
import com.example.apigateway.dto.circuitbreaker.CircuitBreakerDTO;
import com.example.apigateway.dto.gatewayroute.*;
import com.example.apigateway.dto.ratelimitpolicy.RateLimitDTO;
import com.example.apigateway.enums.SlidingWindowType;
import com.example.apigateway.modles.GatewayCircuityBreakerPolicy;
import com.example.apigateway.modles.GatewayRateLimitPolicy;
import com.example.apigateway.modles.GatewayRoute;
import com.example.apigateway.modles.GatewayRoutePolicyMap;
import com.example.apigateway.repositories.GatewayCircuityBreakerPolicyRepository;
import com.example.apigateway.repositories.GatewayRateLimitPolicyRepository;
import com.example.apigateway.repositories.GatewayRoutePolicyMapRepository;
import com.example.apigateway.repositories.GatewayRouteRepository;
import com.example.apigateway.services.GatewayRouteService;
import com.example.apigateway.services.caches.GatewayRouteStore;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayRouteServiceImpl implements GatewayRouteService {

  private final GatewayRouteRepository gatewayRouteRepository;
  private final GatewayRateLimitPolicyRepository gatewayRateLimitPolicyRepository;
  private final GatewayRoutePolicyMapRepository gatewayRoutePolicyMapRepository;
  private final GatewayCircuityBreakerPolicyRepository gatewayCircuityBreakerPolicyRepository;
  private final GatewayRouteStore cacheStore;


  @Override
  @Transactional
  public Map<String, GatewayRouteRuntimeDTO> createFullRoute(CreateFullGatewayRouteRequest request) {

    // 1. Save Route
    GatewayRoute route = new GatewayRoute();
    route.setServiceName(request.getServiceName());
    route.setPath(request.getPath());
    route.setTargetUri(request.getTargetUri());
    route.setHttpMethod(request.getHttpMethod());
    route.setAuthRequired(request.isAuthRequired());
    route.setTimeOutMs(request.getTimeoutMs());
    route.setRouteCode( request.getServiceName().toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 8));

    route = gatewayRouteRepository.save(route);

    // 2. Save Mapping
    GatewayRoutePolicyMap map = new GatewayRoutePolicyMap();
    map.setRouteId(route.getId());
    map.setRateLimitPolicyId(request.getRateLimitPolicyId());
    map.setCircuitBreakerPolicyId(request.getCircuitBreakerPolicyId());
    gatewayRoutePolicyMapRepository.save(map);

    // 3. Fetch Policies
    GatewayRateLimitPolicy rate = gatewayRateLimitPolicyRepository.findById(request.getRateLimitPolicyId()).orElse(null);
    GatewayCircuityBreakerPolicy cb = gatewayCircuityBreakerPolicyRepository.findById(request.getCircuitBreakerPolicyId()).orElse(null);

    // 4. Build Runtime DTO
    GatewayRouteRuntimeDTO dto = GatewayRouteRuntimeDTO.builder()
            .serviceName(route.getServiceName())
            .path(route.getPath())
            .targetUri(route.getTargetUri())
            .method(route.getHttpMethod())
            .timeoutMs(route.getTimeOutMs())
            .authRequired(true)
            .rateLimit(toRateLimitDTO(rate))
            .circuitBreaker(toCircuitBreakerDTO(cb))
            .build();

    // 5. Return as Map<routeCode, dto>
    return Map.of(route.getRouteCode(), dto);
  }

  private RateLimitDTO toRateLimitDTO(GatewayRateLimitPolicy rate) {
    if (rate == null) return null;

    return RateLimitDTO.builder()
            .replenishRate(rate.getReplenishRate())
            .burstCapacity(rate.getBurstCapacity())
            .windowSeconds(rate.getWindowSeconds())
//            .keyResolver(rate.getKeyResolverPolicy().getHeaderName())
            .build();
  }

  private CircuitBreakerDTO toCircuitBreakerDTO(GatewayCircuityBreakerPolicy cb) {
    if (cb == null) return null;

    return CircuitBreakerDTO.builder()
            .slidingWindowType(SlidingWindowType.valueOf(cb.getSlidingWindowType().name()))
            .windowSize(cb.getSlidingWindowSize())
            .failureRateThreshold(cb.getFailureRateThreshold())
            .slowCallRateThreshold(cb.getSlowCallRateThreshold())
            .slowCallDurationMs(cb.getSlowCallDurationMs())
            .openStateWaitMs(cb.getOpenStateWaitMs())
            .halfOpenCalls(cb.getHalfOpenCalls())
            .timeoutMs(cb.getTimeoutMs())
            .build();
  }

  @Override
  public PagedResponse<GatewayRouteConfigResponse> getAllRoutes(GetAllRequest getAllRequest){

    log.info("getAllRoutes : {}", getAllRequest);
    // Create pageable object (0-based page internally)
    PageRequest pageable = PageRequest.of(getAllRequest.getPage() - 1, getAllRequest.getSize(), Sort.by("createdAt").descending());
    var routePage = gatewayRouteRepository.findAll(pageable);

    // Map GatewayRoute to GatewayRouteConfigResponse
    List<GatewayRouteConfigResponse> routeResponses = routePage
            .stream()
            .map(this::buildFullResponse)
            .toList();

    // Build modern pagination response
    PagedResponse.Pagination pagination = new PagedResponse.Pagination(
            getAllRequest.getSize(),
            routePage.getTotalPages(),
            routePage.getTotalElements(),
            getAllRequest.getPage()
    );

    return new PagedResponse<>(routeResponses, pagination);
  }

  private GatewayRouteConfigResponse buildFullResponse(GatewayRoute route) {
    GatewayRoutePolicyMap map = gatewayRoutePolicyMapRepository.findByRouteId(route.getId()).orElse(null);
    GatewayRateLimitPolicy rate = null;
    GatewayCircuityBreakerPolicy cb = null;

    if (map != null) {
      if (map.getRateLimitPolicyId() != null) {
        rate = gatewayRateLimitPolicyRepository.findById(map.getRateLimitPolicyId()).orElse(null);
      }
      if (map.getCircuitBreakerPolicyId() != null) {
        cb = gatewayCircuityBreakerPolicyRepository.findById(map.getCircuitBreakerPolicyId()).orElse(null);
      }
    }

    return GatewayRouteConfigResponse.builder()
            .routeCode(route.getRouteCode())
            .serviceName(route.getServiceName())
            .path(route.getPath())
            .targetUri(route.getTargetUri())
            .method(route.getHttpMethod())
            .timeoutMs(route.getTimeOutMs())
            .authRequired(true)
            .rateLimit(toRateLimitDTO(rate))
            .circuitBreaker(toCircuitBreakerDTO(cb))
            .build();
  }

}
