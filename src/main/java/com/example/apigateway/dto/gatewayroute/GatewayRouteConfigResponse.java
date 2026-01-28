package com.example.apigateway.dto.gatewayroute;

import com.example.apigateway.dto.circuitbreaker.CircuitBreakerDTO;
import com.example.apigateway.dto.ratelimitpolicy.RateLimitDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GatewayRouteConfigResponse {

  private String routeCode;
  private String serviceName;
  private String path;
  private String targetUri;
  private String method;
  private Integer timeoutMs;
  private boolean authRequired;

  private RateLimitDTO rateLimit;
  private CircuitBreakerDTO circuitBreaker;
}
