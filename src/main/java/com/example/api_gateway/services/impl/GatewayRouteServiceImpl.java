package com.example.api_gateway.services.impl;

import com.example.api_gateway.dto.gatewayRoute.CreateGatewayRouteRequest;
import com.example.api_gateway.dto.gatewayRoute.GatewayRouteResponse;
import com.example.api_gateway.modles.GatewayRoute;
import com.example.api_gateway.repositories.GatewayRouteRepository;
import com.example.api_gateway.services.GatewayRouteService;
import com.example.api_gateway.services.caches.GatewayRouteStore;
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
    try {
      GatewayRoute saved = repository.save(request.toEntity());
      GatewayRouteResponse response = GatewayRouteResponse.fromEntity(saved);
//      cacheStore.put(response);
      return response;
    }catch (Exception e) {
      throw  new RuntimeException(e);
    }

  }
}
