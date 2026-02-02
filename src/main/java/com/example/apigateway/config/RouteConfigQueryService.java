package com.example.apigateway.config;

import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import com.example.apigateway.enums.Status;
import com.example.apigateway.modles.*;
import com.example.apigateway.repositories.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RouteConfigQueryService {
  private final GatewayRouteRepository routeRepository;
  private final GatewayRoutePolicyMapRepository routePolicyMapRepository;
  private final GatewayRateLimitPolicyRepository rateLimitRepository;
  private final GatewayCircuityBreakerPolicyRepository circuitBreakerRepository;
  private final GatewayKeyResolverPolicyRepository keyResolverRepository;

  @Transactional(readOnly = true)
  public GatewayRouteConfigResponse getRouteConfig(String routeCode) {

    GatewayRoute route =
        routeRepository
            .findByRouteCode(routeCode)
            .orElseThrow(() -> new NotFoundException("Route not found"));

    GatewayRoutePolicyMap map =
        routePolicyMapRepository
            .findFirstByRouteIdAndStatus(route.getId(), Status.ACTIVE)
            .orElseThrow(() -> new NotFoundException("Active route policy not found"));

    GatewayRateLimitPolicy rateLimit =
        Optional.ofNullable(map.getRateLimitPolicyId())
            .flatMap(rateLimitRepository::findById)
            .orElse(null);

    GatewayKeyResolverPolicy keyResolver =
        Optional.ofNullable(rateLimit)
            .map(GatewayRateLimitPolicy::getKeyResolverPolicyId)
            .flatMap(keyResolverRepository::findById)
            .orElse(null);

    GatewayCircuityBreakerPolicy cb =
        Optional.ofNullable(map.getCircuitBreakerPolicyId())
            .flatMap(circuitBreakerRepository::findById)
            .orElse(null);

    return buildResponse(route, rateLimit, keyResolver, cb);
  }

  private GatewayRouteConfigResponse buildResponse(
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
        .filters(GatewayRouteConfigResponse.Filters.from(route, rateLimit, keyResolver, cb))
        .build();
  }
}
