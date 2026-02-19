package com.example.apigateway.services;

import com.example.apigateway.dto.gatewayroute.PagedResponse;
import com.example.apigateway.dto.ratelimitpolicy.CreateRateLimitPolicy;
import com.example.apigateway.dto.ratelimitpolicy.ListRateLimitPolicyRequest;
import com.example.apigateway.dto.ratelimitpolicy.RateLimitPolicyResponse;
import com.example.apigateway.dto.ratelimitpolicy.UpdateRateLimitRequest;

public interface RateLimitPolicyService {
  RateLimitPolicyResponse create(CreateRateLimitPolicy createRateLimitPolicy);

  RateLimitPolicyResponse getOne(Long id);

  PagedResponse<RateLimitPolicyResponse> getAll(ListRateLimitPolicyRequest request);

  RateLimitPolicyResponse updateById(UpdateRateLimitRequest request);
}
