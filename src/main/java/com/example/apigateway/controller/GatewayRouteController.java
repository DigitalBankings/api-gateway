package com.example.apigateway.controller;

import com.example.apigateway.dto.gatewayroute.*;
import com.example.apigateway.services.GatewayRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/gateway/route")
@RequiredArgsConstructor
public class GatewayRouteController {
  private final GatewayRouteService gatewayRouteService;

  @PostMapping("/register")
  public Map<String, GatewayRouteRuntimeDTO> registerRoute(
          @RequestBody CreateFullGatewayRouteRequest request) {
    return gatewayRouteService.createFullRoute(request);
  }

  @GetMapping
  public Page<GatewayRouteConfigResponse> getAllRoutes(

          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {
    return gatewayRouteService.getAllRoutes(page, size);

  }


}
