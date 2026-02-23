package com.example.apigateway.services;

import com.example.apigateway.dto.gatewayroute.*;
import reactor.core.publisher.Mono;

public interface GatewayRouteService {
  GatewayRouteResponse create(CreateGatewayRouteRequest request);

  Mono<PagedResponse<GatewayRouteResponse>> getAll(GetAllRequest request);

  GatewayRouteResponse getOneById(Long routeId);

  GatewayRouteResponse updateRouteById(UpdateGatewayRouteRequest request);
}
