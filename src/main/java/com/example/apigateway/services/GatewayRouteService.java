package com.example.apigateway.services;

import com.example.apigateway.dto.gatewayroute.*;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface GatewayRouteService {
//  GatewayRouteResponse create(GatewayRouteRuntimeDTO request);
  Map<String, GatewayRouteRuntimeDTO> createFullRoute(CreateFullGatewayRouteRequest request);
  Page<GatewayRouteConfigResponse> getAllRoutes(int page, int size);

}
