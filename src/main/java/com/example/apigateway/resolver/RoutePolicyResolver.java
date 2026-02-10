package com.example.apigateway.resolver;

import com.example.apigateway.modles.*;
import com.example.apigateway.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoutePolicyResolver {

  private final GatewayRateLimitPolicyRepository rateLimitRepository;
  private final GatewayCircuityBreakerPolicyRepository circuitBreakerRepository;
  private final GatewayKeyResolverPolicyRepository keyResolverRepository;

  public ResolvedPolicies resolve(GatewayRoutePolicyMap map) {
    GatewayRateLimitPolicy rateLimit =
        map.getRateLimitPolicyId() == null
            ? null
            : rateLimitRepository.findById(map.getRateLimitPolicyId()).orElse(null);

    GatewayKeyResolverPolicy keyResolver =
        (rateLimit != null && rateLimit.getKeyResolverPolicyId() != null)
            ? keyResolverRepository.findById(rateLimit.getKeyResolverPolicyId()).orElse(null)
            : null;

    GatewayCircuityBreakerPolicy cb =
        map.getCircuitBreakerPolicyId() == null
            ? null
            : circuitBreakerRepository.findById(map.getCircuitBreakerPolicyId()).orElse(null);

    return new ResolvedPolicies(rateLimit, keyResolver, cb);
  }

  public record ResolvedPolicies(
      GatewayRateLimitPolicy rateLimit,
      GatewayKeyResolverPolicy keyResolver,
      GatewayCircuityBreakerPolicy circuitBreaker) {}
}
