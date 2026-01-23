package com.example.api_gateway.controller;

import com.example.api_gateway.dto.gatewayRoute.CreateGatewayRouteRequest;
import com.example.api_gateway.dto.gatewayRoute.GatewayRouteResponse;
import com.example.api_gateway.services.GatewayRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/gateway/route")
@RequiredArgsConstructor
public class GatewayRouteController {
  private final GatewayRouteService gatewayRouteService;

  @PostMapping("/register")
  public GatewayRouteResponse createRoute(@RequestBody CreateGatewayRouteRequest request) {
    log.info("gateway route request: {}", request);
    log.info("gateway route response: {}", gatewayRouteService.create(request));
    return gatewayRouteService.create(request);
  }
}
