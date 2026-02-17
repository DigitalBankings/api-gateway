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

  public void updateEntity(GatewayRoute route) {
    if (serviceName != null) route.setServiceName(serviceName);
    if (routeCode != null) route.setRouteCode(route.getRouteCode());
    if (path != null) route.setPath(path);
    if (httpMethod != null) route.setHttpMethod(httpMethod);
    if (targetUri != null) route.setTargetUri(targetUri);

    route.setTimeOutMs(timeoutMs);
    route.setAuthRequired(authRequired);
  }
}
