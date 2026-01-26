package com.example.apigateway.services.impl;

import com.example.apigateway.dto.circuitbreaker.CreateCircuitBreaker;
import com.example.apigateway.dto.circuitbreaker.ResponseCircuitBreaker;
import com.example.apigateway.modles.GatewayCircuityBreakerPolicy;
import com.example.apigateway.repositories.GatewayCircuityBreakerPolicyRepository;
import com.example.apigateway.services.CircuitBreakerPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CircuitBreakerPolicyServiceImpl implements CircuitBreakerPolicyService {
  private final GatewayCircuityBreakerPolicyRepository gatewayCircuityBreakerPolicyRepository;

  @Override
  public ResponseCircuitBreaker create(CreateCircuitBreaker createCircuitBreaker) {
    GatewayCircuityBreakerPolicy saved =
        gatewayCircuityBreakerPolicyRepository.save(createCircuitBreaker.toEntity());
    return ResponseCircuitBreaker.fromEntity(saved);
  }
}
