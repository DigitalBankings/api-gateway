package com.example.api_gateway.services.impl;

import com.example.api_gateway.dto.rateLimitPolicy.CreateRateLimitPolicy;
import com.example.api_gateway.dto.rateLimitPolicy.RateLimitPolicyResponse;
import com.example.api_gateway.modles.GatewayRateLimitPolicy;
import com.example.api_gateway.repositories.GatewayRateLimitPolicyRepository;
import com.example.api_gateway.services.RateLimitPolicyService;
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
    log.info("RateLimitPolicy created: {}", RateLimitPolicyResponse.fromEntity(gatewayRateLimitPolicy));
    return RateLimitPolicyResponse.fromEntity(gatewayRateLimitPolicy);
  }
}
