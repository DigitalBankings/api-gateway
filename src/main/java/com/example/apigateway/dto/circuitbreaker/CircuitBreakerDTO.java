package com.example.apigateway.dto.circuitbreaker;

import com.example.apigateway.enums.SlidingWindowType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CircuitBreakerDTO {
    private SlidingWindowType slidingWindowType;
    private int windowSize;
    private int failureRateThreshold;
    private int slowCallRateThreshold;
    private int slowCallDurationMs;
    private int openStateWaitMs;
    private int halfOpenCalls;
    private int timeoutMs;
}
