package com.example.apigateway.dto.circuitbreaker;

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
public class ResponseCircuitBreaker {

  private Long id;
  private String cbName;
  private SlidingWindowType slidingWindowType;
  private Integer slidingWindowSize;
  private Integer failureRateThreshold;
  private Integer slowCallRateThreshold;
  private Integer slowCallDurationMs;
  private Integer openStateWaitMs;
  private Integer halfOpenCalls;
  private Integer timeoutMs;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static ResponseCircuitBreaker fromEntity(GatewayCircuityBreakerPolicy responseEntity) {
    ResponseCircuitBreaker responseCircuitBreaker = new ResponseCircuitBreaker();
    responseCircuitBreaker.setId(responseEntity.getId());
    responseCircuitBreaker.setCbName(responseEntity.getCbName());
    responseCircuitBreaker.setSlidingWindowType(responseEntity.getSlidingWindowType());
    responseCircuitBreaker.setSlidingWindowSize(responseEntity.getSlidingWindowSize());
    responseCircuitBreaker.setFailureRateThreshold(responseEntity.getFailureRateThreshold());
    responseCircuitBreaker.setSlowCallRateThreshold(responseEntity.getSlowCallRateThreshold());
    responseCircuitBreaker.setSlowCallDurationMs(responseEntity.getSlowCallDurationMs());
    responseCircuitBreaker.setOpenStateWaitMs(responseEntity.getOpenStateWaitMs());
    responseCircuitBreaker.setHalfOpenCalls(responseEntity.getHalfOpenCalls());
    responseCircuitBreaker.setTimeoutMs(responseEntity.getTimeoutMs());
    responseCircuitBreaker.setCreatedAt(responseEntity.getCreatedAt());
    responseCircuitBreaker.setUpdatedAt(responseEntity.getUpdatedAt());
    return responseCircuitBreaker;
  }
}
