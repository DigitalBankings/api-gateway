package com.example.ABCBank.apiGateway.services;

import com.example.ABCBank.apiGateway.dto.gatewayRoute.CreateGatewayRouteRequest;
import com.example.ABCBank.apiGateway.dto.gatewayRoute.GatewayRouteResponse;

public interface GatewayRouteService {
    GatewayRouteResponse create(CreateGatewayRouteRequest request);
}
