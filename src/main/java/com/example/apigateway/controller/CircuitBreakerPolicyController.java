package com.example.apigateway.controller;

import com.example.apigateway.dto.circuitbreaker.CreateCircuitBreaker;
import com.example.apigateway.dto.circuitbreaker.ListAllCircuitBreakerPolicy;
import com.example.apigateway.dto.circuitbreaker.ResponseCircuitBreaker;
import com.example.apigateway.dto.circuitbreaker.UpdateCircuitBreakerRequest;
import com.example.apigateway.dto.gatewayroute.PagedResponse;
import com.example.apigateway.services.CircuitBreakerPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/circuit-breaker-policy")
@RequiredArgsConstructor
public class CircuitBreakerPolicyController {

  private final CircuitBreakerPolicyService circuitBreakerPolicyService;

  @PostMapping("create")
  public ResponseCircuitBreaker create(@RequestBody CreateCircuitBreaker createCircuitBreaker) {
    return circuitBreakerPolicyService.create(createCircuitBreaker);
  }

  @PostMapping("getAll")
  public PagedResponse<ResponseCircuitBreaker> getAll(
      @RequestBody ListAllCircuitBreakerPolicy request) {
    return circuitBreakerPolicyService.getAll(request);
  }

  @GetMapping("getOne/{id}")
  public ResponseCircuitBreaker getOne(@PathVariable Long id) {
    return circuitBreakerPolicyService.getOne(id);
  }

  @PostMapping("updateById")
  public ResponseCircuitBreaker updateById(@RequestBody UpdateCircuitBreakerRequest request) {
    return circuitBreakerPolicyService.updateById(request);
  }
}
