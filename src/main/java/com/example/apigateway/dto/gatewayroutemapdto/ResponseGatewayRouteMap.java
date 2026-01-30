package com.example.apigateway.dto.gatewayroutemapdto;

import com.example.apigateway.enums.KeyResolverStrategy;
import com.example.apigateway.enums.SlidingWindowType;
import com.example.apigateway.modles.GatewayCircuityBreakerPolicy;
import com.example.apigateway.modles.GatewayKeyResolverPolicy;
import com.example.apigateway.modles.GatewayRateLimitPolicy;
import com.example.apigateway.modles.GatewayRoute;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseGatewayRouteMap {

  private String routeCode;
  private String serviceName;
  private String path;
  private String targetUri;
  private String method;
  private Integer timeoutMs;
  private Boolean authRequired;
  private RateLimitDTO rateLimit;
  private CircuitBreakerDTO circuitBreaker;

  @Data
  @Builder
  public static class RateLimitDTO {
    private Integer replenishRate;
    private Integer burstCapacity;
    private Integer windowSeconds;
    private KeyResolverStrategy keyResolver;
  }

  @Data
  @Builder
  public static class CircuitBreakerDTO {
    private SlidingWindowType slidingWindowType;
    private Integer windowSize;
    private Integer failureRateThreshold;
    private Integer slowCallRateThreshold;
    private Integer slowCallDurationMs;
    private Integer openStateWaitMs;
    private Integer halfOpenCalls;
    private Integer timeoutMs;
  }

  // Builder from Entities
  public static ResponseGatewayRouteMap fromEntities(
      GatewayRoute route,
      GatewayRateLimitPolicy rateLimit,
      GatewayCircuityBreakerPolicy cb,
      GatewayKeyResolverPolicy keyResolver) {
    return ResponseGatewayRouteMap.builder()
        .routeCode(route.getRouteCode())
        .serviceName(route.getServiceName())
        .path(route.getPath())
        .targetUri(route.getTargetUri())
        .method(route.getHttpMethod())
        .timeoutMs(route.getTimeOutMs())
        .authRequired(route.getAuthRequired())
        .rateLimit(
            rateLimit != null
                ? RateLimitDTO.builder()
                    .replenishRate(rateLimit.getReplenishRate())
                    .burstCapacity(rateLimit.getBurstCapacity())
                    .windowSeconds(rateLimit.getWindowSeconds())
                    .keyResolver(keyResolver != null ? keyResolver.getStrategy() : null)
                    .build()
                : null)
        .circuitBreaker(
            cb != null
                ? CircuitBreakerDTO.builder()
                    .slidingWindowType(cb.getSlidingWindowType())
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
