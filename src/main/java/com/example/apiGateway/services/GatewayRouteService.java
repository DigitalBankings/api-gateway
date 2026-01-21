package com.example.apiGateway.services;

import com.example.apiGateway.dto.gatewayRoute.CreateGatewayRouteRequest;
import com.example.apiGateway.dto.gatewayRoute.GatewayRouteResponse;

public interface GatewayRouteService {
  GatewayRouteResponse create(CreateGatewayRouteRequest request);
}
