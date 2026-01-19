package com.example.ABCBank.apiGateway.dto.gatewayRoute;


import com.example.ABCBank.apiGateway.modles.GatewayRoute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayRouteResponse {
    private String id;
    private String routeCode;
    private String paths;
    private String httpMethod;
    private String targetUri;
    private boolean authRequired;
    private int timeoutMs;
    private int status;

    public static GatewayRouteResponse fromEntity(GatewayRoute gatewayRoute) {
        GatewayRouteResponse gatewayRouteResponse = new GatewayRouteResponse();
        gatewayRouteResponse.setId(gatewayRoute.getId());
        gatewayRouteResponse.setRouteCode(gatewayRoute.getRouteCode());
        gatewayRouteResponse.setPaths(gatewayRoute.getPaths());
        gatewayRouteResponse.setHttpMethod(gatewayRoute.getHttpMethod());
        gatewayRouteResponse.setTargetUri(gatewayRoute.getTargetUri());
        gatewayRouteResponse.setTimeoutMs(gatewayRoute.getTimeOutMs());
        gatewayRouteResponse.setStatus(gatewayRouteResponse.getStatus());
        return gatewayRouteResponse;
    }
}
