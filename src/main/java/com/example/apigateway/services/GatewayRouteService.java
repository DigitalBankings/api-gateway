package com.example.apigateway.services;

import com.example.apigateway.dto.gatewayroute.CreateGatewayRouteRequest;
import com.example.apigateway.dto.gatewayroute.GatewayRouteResponse;

public interface GatewayRouteService {
  GatewayRouteResponse create(CreateGatewayRouteRequest request);
}
