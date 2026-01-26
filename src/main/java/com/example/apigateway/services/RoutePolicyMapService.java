package com.example.apigateway.services;

import com.example.apigateway.dto.gatewayroutemapdto.CreateGatewayRouteMapRequest;
import com.example.apigateway.dto.gatewayroutemapdto.ResponseGatewayRouteMap;

public interface RoutePolicyMapService {
    ResponseGatewayRouteMap createRouteMap(CreateGatewayRouteMapRequest request);
}
