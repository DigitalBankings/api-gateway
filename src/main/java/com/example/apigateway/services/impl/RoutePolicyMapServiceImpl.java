package com.example.apigateway.services.impl;

import com.example.apigateway.dto.gatewayroutemapdto.CreateGatewayRouteMapRequest;
import com.example.apigateway.dto.gatewayroutemapdto.ResponseGatewayRouteMap;
import com.example.apigateway.modles.GatewayRoutePolicyMap;
import com.example.apigateway.repositories.GatewayRoutePolicyMapRepository;
import com.example.apigateway.services.RoutePolicyMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoutePolicyMapServiceImpl implements RoutePolicyMapService {

    private final GatewayRoutePolicyMapRepository gatewayRoutePolicyMapRepository;

    @Override
    public ResponseGatewayRouteMap createRouteMap(CreateGatewayRouteMapRequest request) {
        GatewayRoutePolicyMap gatewayRoutePolicyMap = gatewayRoutePolicyMapRepository.save(request.toEntity());
        return ResponseGatewayRouteMap.fromEntity(gatewayRoutePolicyMap);
    }


}
