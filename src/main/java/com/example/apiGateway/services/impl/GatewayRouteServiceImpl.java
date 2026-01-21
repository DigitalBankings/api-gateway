package com.example.apiGateway.services.impl;

import com.example.apiGateway.dto.gatewayRoute.CreateGatewayRouteRequest;
import com.example.apiGateway.dto.gatewayRoute.GatewayRouteResponse;
import com.example.apiGateway.modles.GatewayRoute;
import com.example.apiGateway.repositories.GatewayRouteRepository;
import com.example.apiGateway.services.GatewayRouteService;
import com.example.apiGateway.services.caches.GatewayRouteStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GatewayRouteServiceImpl implements GatewayRouteService {

  private final GatewayRouteRepository repository;
  private final GatewayRouteStore cacheStore;

  // ---------- CREATE ----------
  @Override
  public GatewayRouteResponse create(CreateGatewayRouteRequest request) {
    GatewayRoute saved = repository.save(request.toEntity());
    GatewayRouteResponse response = GatewayRouteResponse.fromEntity(saved);
    cacheStore.put(response);
    return response;
  }
}
