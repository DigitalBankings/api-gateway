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

  @PostMapping("/getAll")
  public PagedResponse<GatewayRouteConfigResponse> getAllRoutes(@RequestBody GetAllRequest getAllRequest)

           {
    return gatewayRouteService.getAllRoutes(getAllRequest);

  }


}
