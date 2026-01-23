package com.example.apigateway.controller;

import com.example.apigateway.dto.gatewayroute.CreateGatewayRouteRequest;
import com.example.apigateway.dto.gatewayroute.GatewayRouteResponse;
import com.example.apigateway.services.GatewayRouteService;
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
