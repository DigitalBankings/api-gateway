package com.example.apigateway.dto.gatewayroutemapdto;

import com.example.apigateway.enums.Status;
import com.example.apigateway.modles.GatewayRoutePolicyMap;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGatewayRouteMapRequest {

  private Long routeId;
  private Long rateLimitPolicyId;
  private Long circuitBreakerPolicyId;
  private Status status;

  public GatewayRoutePolicyMap toEntity() {
    GatewayRoutePolicyMap gatewayRoutePolicyMap = new GatewayRoutePolicyMap();
    gatewayRoutePolicyMap.setRouteId(routeId);
    gatewayRoutePolicyMap.setRateLimitPolicyId(rateLimitPolicyId);
    gatewayRoutePolicyMap.setCircuitBreakerPolicyId(circuitBreakerPolicyId);
    gatewayRoutePolicyMap.setStatus(status);
    return gatewayRoutePolicyMap;
  }
}
