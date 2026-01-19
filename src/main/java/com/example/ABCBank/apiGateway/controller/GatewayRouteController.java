package com.example.ABCBank.apiGateway.controller;


import com.example.ABCBank.apiGateway.dto.gatewayRoute.CreateGatewayRouteRequest;
import com.example.ABCBank.apiGateway.dto.gatewayRoute.GatewayRouteResponse;
import com.example.ABCBank.apiGateway.services.GatewayRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gateway/route")
@RequiredArgsConstructor
public class GatewayRouteController {
    private final GatewayRouteService gatewayRouteService;

    @PostMapping("/register")
    public GatewayRouteResponse createRoute(@RequestBody CreateGatewayRouteRequest request) {
        return gatewayRouteService.create(request);
    }

}
