package com.example.apiGateway.dto.gatewayRoute;

import com.example.apiGateway.enums.RouteStatus;
import com.example.apiGateway.modles.GatewayRoute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayRouteResponse {
  private Long id;
  private String serviceName;
  private String routeCode;
  private String paths;
  private String httpMethod;
  private String targetUri;
  private boolean authRequired;
  private int timeoutMs;
  private RouteStatus status;

  public static GatewayRouteResponse fromEntity(GatewayRoute gatewayRoute) {

    GatewayRouteResponse gatewayRouteResponse = new GatewayRouteResponse();
    gatewayRouteResponse.setId(gatewayRoute.getId());
    gatewayRouteResponse.setServiceName(gatewayRoute.getServiceName());
    gatewayRouteResponse.setRouteCode(gatewayRoute.getRouteCode());
    gatewayRouteResponse.setPaths(gatewayRoute.getPath());
    gatewayRouteResponse.setHttpMethod(gatewayRoute.getHttpMethod());
    gatewayRouteResponse.setTargetUri(gatewayRoute.getTargetUri());
    gatewayRouteResponse.setTimeoutMs(gatewayRoute.getTimeOutMs());
    gatewayRouteResponse.setStatus(gatewayRoute.getStatus());
    return gatewayRouteResponse;

  }
}
