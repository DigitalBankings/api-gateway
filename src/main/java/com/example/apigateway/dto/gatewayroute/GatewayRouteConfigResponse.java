package com.example.apigateway.dto.gatewayroute;

import com.example.apigateway.modles.GatewayCircuityBreakerPolicy;
import com.example.apigateway.modles.GatewayKeyResolverPolicy;
import com.example.apigateway.modles.GatewayRateLimitPolicy;
import com.example.apigateway.modles.GatewayRoute;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatewayRouteConfigResponse {

  private String routeId;
  private String serviceName;
  private String uri;
  private Predicates predicates;
  private Filters filters;

  // ================= PREDICATES =================
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Predicates {
    private String path;
    private String method;
  }

  // ================= FILTERS =================
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Filters {
    private Integer timeoutMs;
    private Boolean authRequired;
    private RateLimit rateLimit;
    private CircuitBreaker circuitBreaker;

    public static Filters from(
        GatewayRoute route,
        GatewayRateLimitPolicy rateLimit,
        GatewayKeyResolverPolicy keyResolver,
        GatewayCircuityBreakerPolicy cb) {

      return Filters.builder()
          .timeoutMs(route.getTimeOutMs())
          .authRequired(route.getAuthRequired())
          .rateLimit(
              rateLimit != null
                  ? RateLimit.builder()
                      .replenishRate(rateLimit.getReplenishRate())
                      .burstCapacity(rateLimit.getBurstCapacity())
                      .windowSeconds(rateLimit.getWindowSeconds())
                      .keyResolver(
                          keyResolver != null
                              ? KeyResolver.builder()
                                  .strategy(keyResolver.getStrategy().name())
                                  .headerName(keyResolver.getHeaderName())
                                  .fallbackStrategy(
                                      keyResolver.getFallbackStrategy() != null
                                          ? keyResolver.getFallbackStrategy().name()
                                          : null)
                                  .build()
                              : null)
                      .build()
                  : null)
          .circuitBreaker(
              cb != null
                  ? CircuitBreaker.builder()
                      .slidingWindowType(cb.getSlidingWindowType().name())
                      .windowSize(cb.getSlidingWindowSize())
                      .failureRateThreshold(cb.getFailureRateThreshold())
                      .slowCallRateThreshold(cb.getSlowCallRateThreshold())
                      .slowCallDurationMs(cb.getSlowCallDurationMs())
                      .openStateWaitMs(cb.getOpenStateWaitMs())
                      .halfOpenCalls(cb.getHalfOpenCalls())
                      .timeoutMs(cb.getTimeoutMs())
                      .build()
                  : null)
          .build();
    }
  }

  // ================= RATE LIMIT =================
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class RateLimit {
    private Integer replenishRate;
    private Integer burstCapacity;
    private Integer windowSeconds;
    private KeyResolver keyResolver;
  }

  // ================= KEY RESOLVER =================
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class KeyResolver {
    private String strategy;
    private String headerName;
    private String fallbackStrategy;
  }

  // ================= CIRCUIT BREAKER =================
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
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
