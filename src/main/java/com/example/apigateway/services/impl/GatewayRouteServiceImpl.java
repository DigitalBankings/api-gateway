package com.example.apigateway.services.impl;

import com.example.apigateway.dto.gatewayroute.CreateGatewayRouteRequest;
import com.example.apigateway.dto.gatewayroute.GatewayRouteResponse;
import com.example.apigateway.modles.GatewayRoute;
import com.example.apigateway.repositories.GatewayRouteRepository;
import com.example.apigateway.services.GatewayRouteService;
import com.example.apigateway.services.caches.GatewayRouteStore;
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
