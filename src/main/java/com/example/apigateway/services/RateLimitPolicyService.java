package com.example.apigateway.services;

import com.example.apigateway.dto.ratelimitpolicy.CreateRateLimitPolicy;
import com.example.apigateway.dto.ratelimitpolicy.RateLimitPolicyResponse;

public interface RateLimitPolicyService {
  RateLimitPolicyResponse create(CreateRateLimitPolicy createRateLimitPolicy);
}
