package com.example.apigateway.dto.gatewayroutemapdto;

import com.example.apigateway.enums.Status;
import com.example.apigateway.modles.GatewayRoutePolicyMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ResponseGatewayRouteMap {

    private Long id;
    private Long routeId;
    private Long rateLimitPolicyId;
    private Long circuitBreakerPolicyId;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ResponseGatewayRouteMap fromEntity(GatewayRoutePolicyMap gatewayRoutePolicyMap) {
        ResponseGatewayRouteMap responseGatewayRouteMap = new ResponseGatewayRouteMap();
        responseGatewayRouteMap.setId(gatewayRoutePolicyMap.getId());
        responseGatewayRouteMap.setRouteId(gatewayRoutePolicyMap.getRouteId());
        responseGatewayRouteMap.setRateLimitPolicyId(gatewayRoutePolicyMap.getRateLimitPolicyId());
        responseGatewayRouteMap.setCircuitBreakerPolicyId(gatewayRoutePolicyMap.getCircuitBreakerPolicyId());
        responseGatewayRouteMap.setStatus(gatewayRoutePolicyMap.getStatus());
        responseGatewayRouteMap.setCreatedAt(gatewayRoutePolicyMap.getCreatedAt());
        responseGatewayRouteMap.setUpdatedAt(gatewayRoutePolicyMap.getUpdatedAt());
        return responseGatewayRouteMap;
    }

}