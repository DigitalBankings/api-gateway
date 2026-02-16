package com.example.apigateway.controller;

import com.example.apigateway.dto.gatewayroute.*;
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
  public GatewayRouteResponse registerRoute(@RequestBody CreateGatewayRouteRequest request) {
    return gatewayRouteService.create(request);
  }

  @PostMapping("/getAll")
  public PagedResponse<GatewayRouteResponse> getAll(@RequestBody GetAllRequest request) {
    return gatewayRouteService.getAll(request);
  }

  @GetMapping("getOne/{routeId}")
  public GatewayRouteResponse getOne(@PathVariable Long routeId) {
    return gatewayRouteService.getOneById(routeId);
  }

  @PostMapping("updateByRouteId")
  public GatewayRouteResponse updateByRouteId(@RequestBody UpdateGatewayRouteRequest request) {
    return gatewayRouteService.updateRouteById(request);
  }
}
