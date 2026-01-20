package com.example.apiGateway.dto.gatewayRoute;


import com.example.apiGateway.modles.GatewayRoute;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGatewayRouteRequest {

    @NotBlank
    private String routeCode;
    @NotBlank
    private String paths;
    @NotBlank
    private String httpMethod;
    @NotBlank
    private String targetUri;
    private boolean authRequired = true;

    public GatewayRoute toEntity() {
        GatewayRoute gatewayRoute = new GatewayRoute();
        gatewayRoute.setId(UUID.randomUUID().toString());
        gatewayRoute.setRouteCode(routeCode);
        gatewayRoute.setPaths(paths);
        gatewayRoute.setHttpMethod(httpMethod);
        gatewayRoute.setTargetUri(targetUri);
        gatewayRoute.setAuthRequired(authRequired);
        return gatewayRoute;
    }

}
