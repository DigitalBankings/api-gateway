package com.example.apigateway.services;

import com.example.apigateway.dto.gatewayroute.*;

public interface GatewayRouteService {
  GatewayRouteResponse create(CreateGatewayRouteRequest request);

  //  Map<String, GatewayRouteRuntimeDTO> createFullRoute(CreateFullGatewayRouteRequest request);

  //  PagedResponse<GatewayRouteConfigResponse> getAllRoutes(GetAllRequest getAllRequest);
}
