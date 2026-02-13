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
}
