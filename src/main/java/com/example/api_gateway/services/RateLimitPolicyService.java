package com.example.api_gateway.services;

import com.example.api_gateway.dto.rateLimitPolicy.CreateRateLimitPolicy;
import com.example.api_gateway.dto.rateLimitPolicy.RateLimitPolicyResponse;

public interface RateLimitPolicyService {
  RateLimitPolicyResponse create(CreateRateLimitPolicy createRateLimitPolicy);
}
