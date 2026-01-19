package com.example.ABCBank.apiGateway.services.impl;

import com.example.ABCBank.apiGateway.dto.gatewayRoute.CreateGatewayRouteRequest;
import com.example.ABCBank.apiGateway.dto.gatewayRoute.GatewayRouteResponse;
import com.example.ABCBank.apiGateway.modles.GatewayRoute;
import com.example.ABCBank.apiGateway.repositories.GatewayRouteRepository;
import com.example.ABCBank.apiGateway.services.GatewayRouteService;
import com.example.ABCBank.apiGateway.services.caches.GatewayRouteStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GatewayRouteServiceImpl implements GatewayRouteService {

    private final GatewayRouteRepository repository;
    private final GatewayRouteStore cacheStore;

    // ---------- CREATE ----------
    @Override
    public GatewayRouteResponse create(CreateGatewayRouteRequest request) {

        GatewayRoute saved = repository.save(request.toEntity());
        GatewayRouteResponse response = GatewayRouteResponse.fromEntity(saved);
        cacheStore.put(response);
        return response;
    }



}
