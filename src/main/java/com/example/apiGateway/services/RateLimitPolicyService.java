package com.example.apiGateway.services;

import com.example.apiGateway.dto.rateLimitPolicy.CreateRateLimitPolicy;
import com.example.apiGateway.dto.rateLimitPolicy.RateLimitPolicyResponse;

public interface RateLimitPolicyService {
  RateLimitPolicyResponse create(CreateRateLimitPolicy createRateLimitPolicy);
}
