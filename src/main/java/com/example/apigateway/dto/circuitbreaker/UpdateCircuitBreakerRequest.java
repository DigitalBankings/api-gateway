package com.example.apigateway.dto.circuitbreaker;

import com.example.apigateway.enums.CircuitBreakerStatus;
import com.example.apigateway.enums.SlidingWindowType;
import com.example.apigateway.modles.GatewayCircuityBreakerPolicy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCircuitBreakerRequest {
  private Long id;
  private String cbName;
  private SlidingWindowType slidingWindowType;
  private Integer slidingWindowSize;
  private Integer failureRateThreshold;
  private Integer slowCallRateThreshold;
  private Integer slowCallDurationMs;
  private Integer openStateWaitMs;
  private Integer halfOpenStateWaitMs;
  private Integer halfOpenCalls;
  private Integer timeoutMs;
  private CircuitBreakerStatus status;

  public void updateEntity(GatewayCircuityBreakerPolicy gatewayCircuityBreakerPolicy) {
    gatewayCircuityBreakerPolicy.setCbName(cbName);
    gatewayCircuityBreakerPolicy.setSlidingWindowType(slidingWindowType);
    gatewayCircuityBreakerPolicy.setSlidingWindowSize(slidingWindowSize);
    gatewayCircuityBreakerPolicy.setFailureRateThreshold(failureRateThreshold);
    gatewayCircuityBreakerPolicy.setSlowCallRateThreshold(slowCallRateThreshold);
    gatewayCircuityBreakerPolicy.setSlowCallDurationMs(slowCallDurationMs);
    gatewayCircuityBreakerPolicy.setOpenStateWaitMs(openStateWaitMs);
    gatewayCircuityBreakerPolicy.setTimeoutMs(timeoutMs);
    gatewayCircuityBreakerPolicy.setHalfOpenCalls(halfOpenCalls);
  }
}
