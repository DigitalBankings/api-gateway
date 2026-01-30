package com.example.apigateway.services.impl;

import com.example.apigateway.dto.gatewayroute.*;
import com.example.apigateway.modles.GatewayRoute;
import com.example.apigateway.repositories.GatewayCircuityBreakerPolicyRepository;
import com.example.apigateway.repositories.GatewayRateLimitPolicyRepository;
import com.example.apigateway.repositories.GatewayRoutePolicyMapRepository;
import com.example.apigateway.repositories.GatewayRouteRepository;
import com.example.apigateway.services.GatewayRouteService;
import com.example.apigateway.services.caches.GatewayRouteStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
  public GatewayRouteResponse create(CreateGatewayRouteRequest request) {
    GatewayRoute save = gatewayRouteRepository.save(request.toEntity());
    return GatewayRouteResponse.fromEntity(save);
  }

  //  @Override
  //  public PagedResponse<GatewayRouteConfigResponse> getAllRoutes(GetAllRequest getAllRequest) {
  //
  //    log.info("getAllRoutes : {}", getAllRequest);
  //    // Create pageable object (0-based page internally)
  //    PageRequest pageable =
  //        PageRequest.of(
  //            getAllRequest.getPage() - 1,
  //            getAllRequest.getSize(),
  //            Sort.by("createdAt").descending());
  //    var routePage = gatewayRouteRepository.findAll(pageable);
  //
  //    // Map GatewayRoute to GatewayRouteConfigResponse
  //    List<GatewayRouteConfigResponse> routeResponses =
  //        routePage.stream().map(this::buildFullResponse).toList();
  //
  //    // Build modern pagination response
  //    PagedResponse.Pagination pagination =
  //        new PagedResponse.Pagination(
  //            getAllRequest.getSize(),
  //            routePage.getTotalPages(),
  //            routePage.getTotalElements(),
  //            getAllRequest.getPage());
  //
  //    return new PagedResponse<>(routeResponses, pagination);
  //  }
  //
  //  private GatewayRouteConfigResponse buildFullResponse(GatewayRoute route) {
  //    GatewayRoutePolicyMap map =
  //        gatewayRoutePolicyMapRepository.findByRouteId(route.getId()).orElse(null);
  //    GatewayRateLimitPolicy rate = null;
  //    GatewayCircuityBreakerPolicy cb = null;
  //
  //    if (map != null) {
  //      if (map.getRateLimitPolicyId() != null) {
  //        rate =
  // gatewayRateLimitPolicyRepository.findById(map.getRateLimitPolicyId()).orElse(null);
  //      }
  //      if (map.getCircuitBreakerPolicyId() != null) {
  //        cb =
  //            gatewayCircuityBreakerPolicyRepository
  //                .findById(map.getCircuitBreakerPolicyId())
  //                .orElse(null);
  //      }
  //    }
  //
  //    return GatewayRouteConfigResponse.builder()
  //        .routeCode(route.getRouteCode())
  //        .serviceName(route.getServiceName())
  //        .path(route.getPath())
  //        .targetUri(route.getTargetUri())
  //        .method(route.getHttpMethod())
  //        .timeoutMs(route.getTimeOutMs())
  //        .authRequired(true)
  //        .rateLimit(toRateLimitDTO(rate))
  //        .circuitBreaker(toCircuitBreakerDTO(cb))
  //        .build();
  //  }

}
