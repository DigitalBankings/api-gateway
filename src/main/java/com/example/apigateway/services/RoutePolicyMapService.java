package com.example.apigateway.services;

import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import com.example.apigateway.dto.gatewayroutemapdto.CreateGatewayRouteMapRequest;
import com.example.apigateway.dto.gatewayroutemapdto.GetRouteMapRequest;
import com.example.apigateway.dto.gatewayroutemapdto.GetRouteMapResponse;
import java.util.Collection;

public interface RoutePolicyMapService {
  GatewayRouteConfigResponse createRouteMap(CreateGatewayRouteMapRequest request);

  GetRouteMapResponse getAllRouteMaps(GetRouteMapRequest request);

  GatewayRouteConfigResponse getRouteConfigFromDB(String routeCode);

  // NEW: get all routes fully loaded
  Collection<GatewayRouteConfigResponse> getAllRoutesFromDB();
}
