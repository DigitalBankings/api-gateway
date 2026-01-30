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
    log.info("Registering gateway route : {}", request);
    return gatewayRouteService.create(request);
  }

  //  @PostMapping("/getAll")
  //  public PagedResponse<GatewayRouteConfigResponse> getAllRoutes(
  //      @RequestBody GetAllRequest getAllRequest) {
  //    return gatewayRouteService.getAllRoutes(getAllRequest);
  //  }


}
