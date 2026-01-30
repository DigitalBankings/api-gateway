package com.example.apigateway.dto.gatewayroute;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GatewayRouteConfigResponse {

  private String routeId;
  private String serviceName;
  private String uri;
  private Predicates predicates;
  private Filters filters;

  // ================= PREDICATES =================
  @Data
  @Builder
  public static class Predicates {
    private String path;
    private String method;
  }

  // ================= FILTERS =================
  @Data
  @Builder
  public static class Filters {
    private Integer timeoutMs;
    private Boolean authRequired;
    private RateLimit rateLimit;
    private CircuitBreaker circuitBreaker;
  }

  // ================= RATE LIMIT =================
  @Data
  @Builder
  public static class RateLimit {
    private Integer replenishRate;
    private Integer burstCapacity;
    private Integer windowSeconds;
    private KeyResolver keyResolver;
  }

  // ================= KEY RESOLVER =================
  @Data
  @Builder
  public static class KeyResolver {
    private String strategy;
    private String headerName;
    private String fallbackStrategy;
  }

  // ================= CIRCUIT BREAKER =================
  @Data
  @Builder
  public static class CircuitBreaker {
    private String slidingWindowType;
    private Integer windowSize;
    private Integer failureRateThreshold;
    private Integer slowCallRateThreshold;
    private Integer slowCallDurationMs;
    private Integer openStateWaitMs;
    private Integer halfOpenCalls;
    private Integer timeoutMs;
  }
}
