package com.example.apigateway.controller;

import com.example.apigateway.dto.circuitbreaker.CreateCircuitBreaker;
import com.example.apigateway.dto.circuitbreaker.ResponseCircuitBreaker;
import com.example.apigateway.services.CircuitBreakerPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/circuit-breaker-policy")
@RequiredArgsConstructor
public class CircuitBreakerPolicyController {

  private final CircuitBreakerPolicyService circuitBreakerPolicyService;

  @PostMapping("create")
  public ResponseCircuitBreaker create(@RequestBody CreateCircuitBreaker createCircuitBreaker) {
    log.info(" Create circuit breaker : {} ", createCircuitBreaker);
    return circuitBreakerPolicyService.create(createCircuitBreaker);
  }
}
