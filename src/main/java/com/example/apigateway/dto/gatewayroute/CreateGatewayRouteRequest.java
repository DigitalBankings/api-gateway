package com.example.apigateway.dto.gatewayroute;

import com.example.apigateway.modles.GatewayRoute;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGatewayRouteRequest {

  @NotBlank private String serviceName;
  private String routeCode;
  @NotBlank private String path;
  @NotBlank private String httpMethod;
  @NotBlank private String targetUri;
  private boolean authRequired = true;

  public GatewayRoute toEntity() {
    GatewayRoute gatewayRoute = new GatewayRoute();
    gatewayRoute.setServiceName(serviceName);
    gatewayRoute.setRouteCode(
        serviceName.toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 8));
    gatewayRoute.setPath(path);
    gatewayRoute.setHttpMethod(httpMethod);
    gatewayRoute.setTargetUri(targetUri);
    gatewayRoute.setAuthRequired(authRequired);
    return gatewayRoute;
  }
}
