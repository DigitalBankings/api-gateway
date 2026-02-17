package com.example.apigateway.services;

import com.example.apigateway.dto.circuitbreaker.CreateCircuitBreaker;
import com.example.apigateway.dto.circuitbreaker.ListAllCircuitBreakerPolicy;
import com.example.apigateway.dto.circuitbreaker.ResponseCircuitBreaker;
import com.example.apigateway.dto.circuitbreaker.UpdateCircuitBreakerRequest;
import com.example.apigateway.dto.gatewayroute.PagedResponse;

public interface CircuitBreakerPolicyService {
  ResponseCircuitBreaker create(CreateCircuitBreaker createCircuitBreaker);

  ResponseCircuitBreaker getOne(Long id);

  PagedResponse<ResponseCircuitBreaker> getAll(ListAllCircuitBreakerPolicy request);

  ResponseCircuitBreaker updateById(UpdateCircuitBreakerRequest request);
}
