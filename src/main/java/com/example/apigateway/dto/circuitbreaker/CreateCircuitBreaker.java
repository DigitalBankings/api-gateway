package com.example.apigateway.dto.circuitbreaker;

import com.example.apigateway.enums.CircuitBreakerStatus;
import com.example.apigateway.enums.SlidingWindowType;
import com.example.apigateway.modles.GatewayCircuityBreakerPolicy;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCircuitBreaker {

  private Long id;
  private String cbName;
  private SlidingWindowType slidingWindowType;
  private Integer slidingWindowSize;
  private Integer failureRateThreshold;
  private Integer slowCallRateThreshold;
  private Integer slowCallDurationMs;
  private Integer openStateWaitMs;
  private Integer halfOpenStateWaitMs;
  private Integer timeoutMs;
  private CircuitBreakerStatus status;

  public GatewayCircuityBreakerPolicy toEntity() {
    GatewayCircuityBreakerPolicy gatewayCircuityBreakerPolicy = new GatewayCircuityBreakerPolicy();
    gatewayCircuityBreakerPolicy.setId(id);
    gatewayCircuityBreakerPolicy.setCbName(cbName);
    gatewayCircuityBreakerPolicy.setSlidingWindowType(slidingWindowType);
    gatewayCircuityBreakerPolicy.setSlidingWindowSize(slidingWindowSize);
    gatewayCircuityBreakerPolicy.setFailureRateThreshold(failureRateThreshold);
    gatewayCircuityBreakerPolicy.setSlowCallRateThreshold(slowCallRateThreshold);
    gatewayCircuityBreakerPolicy.setSlowCallDurationMs(slowCallDurationMs);
    gatewayCircuityBreakerPolicy.setOpenStateWaitMs(openStateWaitMs);
    gatewayCircuityBreakerPolicy.setHalfOpenCalls(halfOpenStateWaitMs);
    gatewayCircuityBreakerPolicy.setTimeoutMs(timeoutMs);
    gatewayCircuityBreakerPolicy.setCreatedAt(LocalDateTime.now());
    gatewayCircuityBreakerPolicy.setUpdatedAt(LocalDateTime.now());
    return gatewayCircuityBreakerPolicy;
  }
}
