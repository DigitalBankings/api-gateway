package com.example.api_gateway.services;

import com.example.api_gateway.dto.gatewayRoute.CreateGatewayRouteRequest;
import com.example.api_gateway.dto.gatewayRoute.GatewayRouteResponse;

public interface GatewayRouteService {
  GatewayRouteResponse create(CreateGatewayRouteRequest request);
}
