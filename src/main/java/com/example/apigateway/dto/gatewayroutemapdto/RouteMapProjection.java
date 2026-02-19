package com.example.apigateway.dto.gatewayroutemapdto;

public interface RouteMapProjection {

  Long getRouteId();

  String getServiceName();

  String getStatus();

  Long getRateLimitPolicyId();

  Long getCircuitBreakerPolicyId();
}
