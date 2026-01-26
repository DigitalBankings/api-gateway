package com.example.apigateway.services;

import com.example.apigateway.dto.circuitbreaker.CreateCircuitBreaker;
import com.example.apigateway.dto.circuitbreaker.ResponseCircuitBreaker;

public interface CircuitBreakerPolicyService {
  ResponseCircuitBreaker create(CreateCircuitBreaker createCircuitBreaker);
}
