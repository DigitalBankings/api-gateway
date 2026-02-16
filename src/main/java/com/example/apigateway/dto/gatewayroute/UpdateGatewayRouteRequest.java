package com.example.apigateway.dto.gatewayroute;

import com.example.apigateway.modles.GatewayRoute;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateGatewayRouteRequest {

  private Long routeId;
  private String serviceName;
  private String routeCode;
  private String path;
  private String httpMethod;
  private String targetUri;
  private int timeoutMs;
  private boolean authRequired;

  public GatewayRoute toEntity() {
    GatewayRoute gatewayRoute = new GatewayRoute();
    gatewayRoute.setServiceName(serviceName);
    gatewayRoute.setRouteCode(routeCode);
    gatewayRoute.setPath(path);
    gatewayRoute.setHttpMethod(httpMethod);
    gatewayRoute.setTargetUri(targetUri);
    gatewayRoute.setAuthRequired(authRequired);
    return gatewayRoute;
  }
}
