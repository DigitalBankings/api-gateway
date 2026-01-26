package com.example.apigateway.dto.gatewayroute;

import lombok.Data;

@Data
public class CreateFullGatewayRouteRequest {

    private String serviceName;
    private String path;
    private String targetUri;
    private String httpMethod;
    private boolean authRequired;
    private Integer timeoutMs;

    private Long rateLimitPolicyId;
    private Long circuitBreakerPolicyId;
}
