package com.example.apiGateway.services.impl;

import com.example.apiGateway.dto.rateLimitPolicy.CreateRateLimitPolicy;
import com.example.apiGateway.dto.rateLimitPolicy.RateLimitPolicyResponse;
import com.example.apiGateway.modles.GatewayRateLimitPolicy;
import com.example.apiGateway.repositories.GatewayRateLimitPolicyRepository;
import com.example.apiGateway.services.RateLimitPolicyService;
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
