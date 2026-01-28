package com.example.apigateway.controller;

import com.example.apigateway.dto.gatewayroutemapdto.CreateGatewayRouteMapRequest;
import com.example.apigateway.dto.gatewayroutemapdto.ResponseGatewayRouteMap;
import com.example.apigateway.services.RoutePolicyMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/route/mapping")
@RequiredArgsConstructor
public class RouteMapPolicyController {
  private final RoutePolicyMapService routePolicyMapService;

  @PostMapping("/register")
  public ResponseGatewayRouteMap createGatewayMapPolicy(
      @RequestBody CreateGatewayRouteMapRequest request) {
    log.info("createGatewayMapPolicy: {}", request);
    return routePolicyMapService.createRouteMap(request);
  }
}
