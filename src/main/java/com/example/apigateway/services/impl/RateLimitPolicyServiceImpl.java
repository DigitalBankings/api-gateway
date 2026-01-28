package com.example.apigateway.services.impl;

import com.example.apigateway.dto.ratelimitpolicy.CreateRateLimitPolicy;
import com.example.apigateway.dto.ratelimitpolicy.RateLimitPolicyResponse;
import com.example.apigateway.modles.GatewayRateLimitPolicy;
import com.example.apigateway.repositories.GatewayRateLimitPolicyRepository;
import com.example.apigateway.services.RateLimitPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitPolicyServiceImpl implements RateLimitPolicyService {

  private final GatewayRateLimitPolicyRepository rateLimitPolicyRepository;

  @Override
  public RateLimitPolicyResponse create(CreateRateLimitPolicy createRateLimitPolicy) {
    GatewayRateLimitPolicy gatewayRateLimitPolicy =
        rateLimitPolicyRepository.save(createRateLimitPolicy.toEntity());
    log.info(
        "RateLimitPolicy created: {}", RateLimitPolicyResponse.fromEntity(gatewayRateLimitPolicy));
    return RateLimitPolicyResponse.fromEntity(gatewayRateLimitPolicy);
  }
}
